package TransactionProcessor;

import Logger.LogType;
import Logger.Logger;
import Logger.MainLogger;
import QueryProcessor.SelectOptions;
import QueryProcessor.UpdateOptions;
import QueryValidator.QueryValidator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionProcessor {

    public static Date currenttime = new Date(System.currentTimeMillis());

    FileWriter fWriter = new FileWriter("Transactionlog.txt",true);

    public static int varcharMaxLength = 8000;

    public static ArrayList<String> str = new ArrayList<String>();

    public static HashMap<String, List<HashMap<String, String>>> tablesMetaData = new HashMap<>();

    public static HashMap<String, List<HashMap<String, String>>> tableRows = new HashMap<>();

    public static HashMap<String, String> tableColumnsOrder = new HashMap<>();
    private final MainLogger logger;

    public String currentDatabase = "";

    public TransactionProcessor(MainLogger logger) throws IOException {
        this.logger = logger;
    }


    public void resetDatabaseState () {
        currentDatabase = "";
        tablesMetaData = new HashMap<>();
        tableRows = new HashMap<>();
        tableColumnsOrder = new HashMap<>();
    }

    public void createDatabase (String dbName) throws IOException {
        Files.createDirectory(Path.of(dbName));

        if (QueryValidator.checkIfDBExists(dbName)) {
            Files.createFile(Path.of(dbName, "meta.txt"));

                        if (QueryValidator.checkIfDbHasMeta(dbName)) {
                logger.setChangeMessage(Logger.databaseCreationChangeMessage.formatted(dbName));
//                System.out.printf("Database %s created successfully.\n", dbName);
            }
            else {
                logger.setChangeMessage(Logger.databaseMetaErrorChangeMessage.formatted(dbName));
//                System.out.printf("Database %s created successfully. Meta file creation failed.\n", dbName);
            }

        } else {
            logger.setChangeMessage(Logger.databaseMetaErrorChangeMessage.formatted(dbName));
//            System.out.println("Database creation failed.");
        }

    }

    // get data from db's meta file
    public void parseMetaDataOfTable () {
        tablesMetaData = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Path.of(currentDatabase, "meta.txt").toString()));
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split("\\|");
                List<String> lineData = new ArrayList<>(Arrays.asList(splitLine));

                HashMap<String, String> lineDataMap = new HashMap<>();

                // get the table name
                String table = lineData.get(0);
                lineData.remove(0);

                // get the column name and save it in a map
                String column = lineData.get(0);
                lineDataMap.put("name", column);
                lineData.remove(0);


                // get the column data type and save it in a map
                String type = lineData.get(0);
                lineDataMap.put("type", type);
                lineData.remove(0);


                // if the type is varchar, get and save its length in a map
                String length;
                if (type.equals("varchar")) {
                    length = lineData.get(0);
                    lineDataMap.put("size", length);
                    lineData.remove(0);
                }

                String fkTableName = "";
                String fkFieldName = "";

                // check if the column is a primary key
                if (lineData.size() > 0 && lineData.get(0).equals("pk")) {
                    lineDataMap.put("pk", "true");
                    lineData.remove(0);
                }

                if (lineData.size() >= 3 && lineData.get(0).equals("fk")) {
                    fkTableName = lineData.get(1);
                    fkFieldName = lineData.get(2);

                    if (fkTableName.length() > 0 && fkFieldName.length() > 0) {
                        lineDataMap.put("fk", "true");
                        lineDataMap.put("fkTableName", fkTableName);
                        lineDataMap.put("fkFieldName", fkFieldName);
                        lineData.remove(0);
                        lineData.remove(0);
                        lineData.remove(0);
                    }
                }

                if (tablesMetaData.get(table) != null && tablesMetaData.get(table).size() > 0) {
                    // if a list of meta already exists for this table
                    List<HashMap<String, String>> data = new ArrayList<>(tablesMetaData.get(table));
                    data.add(lineDataMap);
                    tablesMetaData.put(table, data);
                } else {
                    // create and insert new list as it doesn't exist already
                    tablesMetaData.put(table, List.of(lineDataMap));
                }

                // read next line
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // save a table's info to its meta.txt
    public void saveTableDataToMetaFile () {
        try {
            // write to file
            String path = Path.of(currentDatabase, "meta.txt").toString();
            FileWriter fw = new FileWriter(path);
            PrintWriter writer = new PrintWriter(fw);

            for (Map.Entry<String, List<HashMap<String, String>>> table: tablesMetaData.entrySet()) {
                String tableName = table.getKey();
                List<HashMap<String, String>> tableColumns = table.getValue();
                String output = tableName;

                for (HashMap<String, String> column : tableColumns) {
                    String colName = column.get("name");
                    String colType = column.get("type");
                    String colSize = column.get("size");
                    String colPk = column.get("pk");
                    String colFk = column.get("fk");
                    String colFkTable = column.get("fkTableName");
                    String colFkField = column.get("fkFieldName");

                    output = output.concat("|").concat(colName);
                    output = output.concat("|").concat(colType);
                    if (colType.equals("varchar")) output = output.concat("|").concat(colSize);
                    if (colPk != null && colPk.equals("true")) output = output.concat("|pk");

                    if (colFk != null && colFk.equals("true")) {
                        output = output.concat("|fk|").concat(colFkTable).concat("|").concat(colFkField);
                    }

                    writer.println(output);
                    output = tableName;
                }
            }
            writer.close();
            logger.setChangeMessage("Table Created Successfully");
            System.out.println("Table created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseCreateTableQuery (List<String> queryChunks) throws IOException {
        // check if a database is selected
        if (currentDatabase.length() == 0) {
            System.out.println("No database selected.");
            return;
        }

        List<String> queryList = new ArrayList<>(queryChunks);
        // removes the 'create' token
        queryList.remove(0);
        // removes the 'table' token
        queryList.remove(0);

        // stores and removes the 'table name' token
        String tableName = queryList.get(0);
        queryList.remove(0);

        // verifies if the table already exists
        if (tablesMetaData.containsKey(tableName)) {
            logger.setChangeMessage("Invalid query. Table already exists.");
            Logger.log(logger);
            System.out.println("Invalid query. Table already exists.");
            return;
        }

        if (queryList.get(0).equals("(") && queryList.get(queryList.size() - 1).equals(")")) {
            // remove '(' from query
            queryList.remove(0);
            // remove last ')' from  query
            queryList.remove(queryList.size() - 1);

            List<String> columns = List.of(String.join(" ", queryList).split(","));

            List<HashMap<String, String>> parsedColumnsMap = new ArrayList<>();
            List<String> columnNameList = new ArrayList<>();

            for (String column : columns) {
                List<String> columnData = new ArrayList<>(List.of(column.trim().split(" ")));
                if (columnData.size() < 2) {
                    logger.setChangeMessage("Invalid query.");
                    System.out.println("Invalid query.");
                    return;
                }

                HashMap<String, String> columnMap = new HashMap<>();

                String columnName = columnData.get(0);
                columnMap.put("name", columnName);
                columnNameList.add(columnName);
                columnData.remove(0);

                String columnType = columnData.get(0);
                columnMap.put("type", columnType);
                columnData.remove(0);

                int columnLength;
                String foreignKeyReferenceTable;
                String foreignKeyReferenceField;

                // get varchar field max size if the type is varchar
                if (columnType.equals("varchar")) {
                    if (columnData.size() >= 3 && columnData.get(0).equals("(") && columnData.get(1).matches("\\d+") && columnData.get(2).equals(")")) {
                        columnLength = Integer.parseInt(columnData.get(1));
                    } else {
                        columnLength = varcharMaxLength;
                    }

                    columnMap.put("size", Integer.toString(columnLength));
                } else if (!columnType.equals("int")) {
                    logger.setChangeMessage("Invalid query.");
                    System.out.println("Invalid query.");
                }

                if (columnData.size() == 0) {
                    // no more column data other than field name and type.
                    parsedColumnsMap.add(columnMap);
                    continue;
                }

                for (int i = 0; i < columnData.size(); i++) {
                    String currentToken = columnData.get(i).toLowerCase();
                    String nextToken = null;

                    if (i + 1 < columnData.size()) nextToken = columnData.get(i + 1).toLowerCase();

                    if (currentToken.equals("primary") && nextToken != null && nextToken.equals("key")) {
                        columnMap.put("pk", "true");
                        i++;
                        continue;
                    }

                    if (currentToken.equals("foreign") && nextToken != null && nextToken.equals("key")) {
                        i++;
                        // <table name> ( <column name> )
                        if (i + 4 < columnData.size()) {
                            String tableNameTemp = columnData.get(i+1);
                            String tableFieldTemp = columnData.get(i+3);
                            if (QueryValidator.validateForeignKeyReference(currentDatabase, columnType, tableNameTemp, tableFieldTemp, tablesMetaData)) {
                                columnMap.put("fk", "true");
                                columnMap.put("fkTableName", tableNameTemp);
                                columnMap.put("fkFieldName", tableFieldTemp);
                                continue;
                            } else {
                                return;
                            }
                        } else {
                            System.out.println("Invalid query. No reference given for foreign key.");
                            return;
                        }
                    }
                }
                parsedColumnsMap.add(columnMap);
            }

            tablesMetaData.put(tableName, parsedColumnsMap);
            saveTableDataToMetaFile();
            Files.createFile(Path.of(currentDatabase, tableName+ ".txt"));

            String columnNamesAsString = String.join("|", columnNameList);
            Files.write(Path.of(currentDatabase, tableName+ ".txt"), columnNamesAsString.getBytes());
            tableColumnsOrder.put(tableName, columnNamesAsString);
        } else {
            logger.setChangeMessage("Invalid query");
            System.out.println("Invalid query.");
        }
    }

    // read and store all tables data
    public void readAllTablesData () {
        for (Map.Entry<String, List<HashMap<String, String>>> entry: tablesMetaData.entrySet()) {
            String tableName = entry.getKey();
            List<HashMap<String, String>> columnsInMetaData = entry.getValue();

            List<String> columnNamesInMetaData = columnsInMetaData.stream().map(v -> v.get("name")).collect(Collectors.toList());

            Path tableFilePath = Path.of(currentDatabase, tableName + ".txt");

            if (!Files.exists(tableFilePath)) {
                System.out.println("Table " + tableName + " does not exist on disk. Meta file must be corrupt.");
                return;
            }

            try {
                BufferedReader reader = new BufferedReader(new FileReader(tableFilePath.toString()));
                String line = reader.readLine();

                if (line.length() == 0) {
                    System.out.println("Table " + tableName + " does not contain header row.");
                    return;
                }

                // check if columns in header match metadata
                List<String> columns = List.of(line.split("\\|"));
                for (String column: columns) {
                    if (!columnNamesInMetaData.contains(column)) {
                        System.out.println("Table " + tableName + " has a column not matching in metadata.");
                        resetDatabaseState();
                        return;
                    }
                }

                // save the column order for this table
                tableColumnsOrder.put(tableName, line);

                // read next line
                line = reader.readLine();

                // list of all rows
                List<HashMap<String, String>> rows = new ArrayList<>();

                // map column name to column meta data for ease of access
                HashMap<String, HashMap<String, String>> columnMeta = new HashMap<>();
                columnsInMetaData.forEach(column -> columnMeta.put(column.get("name"), column));

                // loop till the end of file
                while (line != null) {
                    List<String> rowData = Arrays.asList(line.split("\\|", -1));

                    if (rowData.size() != columns.size()) {
                        System.out.println("Table " + tableName + " has a corrupt row.");
                        resetDatabaseState();
                        return;
                    }

                    // data of current row
                    HashMap<String, String> rowMap = new HashMap<>();

                    for (int index  = 0; index < columns.size(); index++) {
                        String data = rowData.get(index);
                        String column = columns.get(index);

                        HashMap<String, String> currentColumn = columnMeta.get(column);
                        boolean valid = true;

                        // verify data type
                        if (data.length() != 0 && currentColumn.get("type").equals("varchar")) {
                            String size = currentColumn.get("size");
                            if (data.length() > Integer.parseInt(size)) {
                                valid = false;
                            }
                        } else if (data.length() != 0 && currentColumn.get("type").equals("int")) {
                            if (!data.matches("\\d+")) {
                                valid = false;
                            }
                        }

                        // verify uniqueness if it is primary key
                        if (valid && currentColumn.get("pk") != null && currentColumn.get("pk").equals("true")) {
                            valid = rows.stream().noneMatch(row -> row.get(column).equals(data));
                        }

                        if (!valid) {
                            System.out.println("Table " + tableName + " has a corrupt row.");
                            resetDatabaseState();
                            return;
                        } else {
                            // store in list of maps
                            rowMap.put(column, data);
                        }
                    }

                    rows.add(rowMap);

                    // read next line
                    line = reader.readLine();
                }

                tableRows.put(tableName, rows);
            } catch (IOException e) {
                e.printStackTrace();
                resetDatabaseState();
            }
        }
    }

    // returns a column meta info from a table
    public HashMap<String, String> getColumnMetaInfo (String tableName, String columnName) {
        for(HashMap<String, String> column: tablesMetaData.get(tableName)) {
            if (column.get("name").equals(columnName)) {
                return column;
            }
        }

        System.out.println("No column named " + columnName + " found in table " + tableName);
        return new HashMap<>();
    }

    // sample query: insert into tName (id, name) values (1, myname)
    public void parseInsertValueInTableQuery (List<String> queryChunks) {
        // check if no database is selected
        if (currentDatabase.length() == 0) {
            logger.setChangeMessage("No Database Selected");
            System.out.println("No database selected.");
            return;
        }

        List<String> queryList = new ArrayList<>(queryChunks);

        // remove the 'insert' token
        queryList.remove(0);
        // remove the 'into' token
        queryList.remove(0);
        // save the table name and remove token
        String tableName = queryList.remove(0);

        String queryWithoutPrecedingTokens = String.join(" ", queryList);
        String[] columnsAndValuesFromQuery = queryWithoutPrecedingTokens.split("[Vv][Aa][Ll][Uu][Ee][Ss]");

        if (columnsAndValuesFromQuery.length != 2) {
            logger.setChangeMessage("Invalid query. Length");
            System.out.println("Invalid query. Length");
            return;
        }

        List<String> columns = QueryValidator.validateAndCreateListFromRoundBracketValues(columnsAndValuesFromQuery[0]);
        List<String> values = QueryValidator.validateAndCreateListFromRoundBracketValues(columnsAndValuesFromQuery[1]);

        if (values.size() == 0) {
            return;
        }

        List<String> tableColumnsInOrder =  new ArrayList<>(List.of(tableColumnsOrder.get(tableName).split("\\|")));

        // if no columns are given in the query, use all columns
        boolean useAllColumns = columns.size() == 0;

        // store the valid data to this hashmap to insert into table rows
        HashMap<String, String> newRowData = new HashMap<>();

        // index to keep track of values while looping through columns
        int idx = 0;
        for (String column: tableColumnsInOrder) {
            if (!useAllColumns && !columns.contains(column)) {
                newRowData.put(column, "");
                continue;
            }

            String data = useAllColumns ? values.get(idx) : values.get(columns.indexOf(column));
            HashMap<String, String> columnMetaData = getColumnMetaInfo(tableName, column);
            if (QueryValidator.validateDataAsPerColumnMeta(tableName, columnMetaData, data, tableRows)) {
                newRowData.put(column, data);
                idx++;
            } else {
                return;
            }
        }

        // add the validated data to the list of rows for this table
        Integer previousSize = tableRows.get(tableName).size();
        List<HashMap<String, String>> newTableData = tableRows.get(tableName) != null ? new ArrayList<>(tableRows.get(tableName)) : new ArrayList<>();
        newTableData.add(newRowData);
        tableRows.put(tableName, newTableData);

        persistTableDataToDisk(tableName);
        System.out.println("Record added successfully.");
        logger.setTableName(tableName);
        logger.setChangeMessage("%s row(s) added ".formatted(tableRows.get(tableName).size()-previousSize));
    }

    // write the contents of tableRows to table files
    public void persistTableDataToDisk (String tableName) {
        String currentTableColumnsOrder = tableColumnsOrder.get(tableName);
        List<String> tableColumnsInOrder =  new ArrayList<>(List.of(currentTableColumnsOrder.split("\\|")));

        try {
            // write to file
            String path = Path.of(currentDatabase, tableName + ".txt").toString();
            FileWriter fw = new FileWriter(path);
            PrintWriter writer = new PrintWriter(fw);
            List<HashMap<String, String>> currentTableRows = tableRows.get(tableName);

            // output the header line
            writer.println(currentTableColumnsOrder);

            for (HashMap<String, String> row: currentTableRows) {
                String output = "";
                int index = 0;
                for(String column: tableColumnsInOrder) {
                    if (row.containsKey(column)) {
                        output = output.concat(row.get(column));
                    }

                    // if it is not the last column, append a '|' delimeter
                    if (index != tableColumnsInOrder.size() - 1) {
                        output = output.concat("|");
                    }

                    index++;
                }

                writer.println(output);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Process(String query) throws IOException {
       String currentInput = query.substring(0, query.length() - 1);
       String input = "";
        logger.setActiveDatabase(currentDatabase);
        logger.setCurrentTimeMillis(System.currentTimeMillis());
        logger.setUserName("test");


        input = currentInput.replaceAll("[(]", " ( ");
        String input1 = input.replaceAll("[)]", " ) ");

        List<String> inputChunks = new ArrayList<>(Arrays.asList(input1.trim().split(" ")));
        String queryType = inputChunks.get(0).toLowerCase();
            switch (queryType) {
                case "create":
                    String createQueryType = inputChunks.get(1).toLowerCase();
                    long startTime = System.currentTimeMillis();
                    if (createQueryType.equals("database") && QueryValidator.validateCreateDatabaseQuery(inputChunks)) {
                        // query is for creating database and is validated
                        createDatabase(inputChunks.get(2));
                        logger.setExecutionTimeMillis(System.currentTimeMillis() - logger.getCurrentTimeMillis());
                        logger.setLogType(LogType.CREATE);
                        Logger.log(logger);
                    } else if (createQueryType.equals("table")) {
                        parseCreateTableQuery(inputChunks);
                        logger.setExecutionTimeMillis(System.currentTimeMillis() - logger.getCurrentTimeMillis());
                        logger.setLogType(LogType.CREATE);
                        Logger.log(logger);
                    }
                    long endTime = System.currentTimeMillis();
                    logger.setExecutionTimeMillis(endTime - startTime);
                    break;
                case "use":
                    if (QueryValidator.validateUseDatabaseQuery(inputChunks)) {
                        startTime = System.currentTimeMillis();
                        currentDatabase = inputChunks.get(1);
                        parseMetaDataOfTable();
                        readAllTablesData();
                        endTime = System.currentTimeMillis();
                        logger.setActiveDatabase(currentDatabase);
                        logger.setExecutionTimeMillis(endTime - startTime);
                        logger.setChangeMessage("Database Selected " + currentDatabase);
                        logger.setLogType(LogType.USE);
                        logger.setTotalTables(tableRows.size());
                        int totalRecords = tableRows.values().stream().map(value -> value.size()).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
                        logger.setTotalRecords(totalRecords);
                        Logger.log(logger);
                    }
                    break;
                case "insert":
                    startTime = System.currentTimeMillis();
                    String secondToken = inputChunks.get(1).toLowerCase();
                    if (secondToken.equals("into")) {
                        parseInsertValueInTableQuery(inputChunks);
                        logger.setLogType(LogType.INSERT);
                        Logger.log(logger);
                    }
                    endTime = System.currentTimeMillis();
                    logger.setExecutionTimeMillis(endTime - startTime);
                    break;
                case "select":
                    startTime = System.currentTimeMillis();
                    SelectOptions selectOptions = new SelectOptions();
                    List<String> columns = new ArrayList<>();
                    secondToken = inputChunks.get(1).toLowerCase();
                    inputChunks.remove(0);
                    int columnIndex = inputChunks.indexOf("from");
                    String tableName = inputChunks.get(columnIndex + 1);
                    selectOptions.setTableName(tableName);
                    if (secondToken.equals("*")) {
                        selectOptions.setAll(true);
                    } else {
                        for (int i = 0; i < columnIndex; i++) {
                            if (inputChunks.get(i).equals(",")) {
                                continue;
                            }
                            columns.add(inputChunks.get(i));
                        }
                        selectOptions.setColumns(columns);
                    }
                    int whereIndex = inputChunks.indexOf("where");
                    if (whereIndex != -1) {
                        String columnString = inputChunks.get(whereIndex + 1);
                        String columnValue = inputChunks.get(whereIndex + 3);
                        selectOptions.setColumnName(columnString);
                        selectOptions.setColumnValue(columnValue);
                        selectOptions.setWhere(true);
                    }
                    parseSelectQuery(selectOptions);
                    endTime = System.currentTimeMillis();
                    logger.setExecutionTimeMillis(endTime - startTime);
                    logger.setTableName(tableName);
                    logger.setLogType(LogType.SELECT);
                    Logger.log(logger);
                    break;
                case "update":
                    startTime = System.currentTimeMillis();
                    boolean update = parseUpdateQuery(inputChunks);
                    endTime = System.currentTimeMillis();
                    logger.setExecutionTimeMillis(endTime - startTime);
                    logger.setTotalTables(tableRows.size());
                    int totalRecords = tableRows.values().stream().map(value -> value.size()).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
                    logger.setTotalRecords(totalRecords);
                    logger.setLogType(LogType.UPDATE);
                    if (!update) {
                        logger.setLogType(LogType.ERROR);
                    }
                    Logger.log(logger);

                    break;
                    case "tend":
                    String str1 = inputChunks.get(inputChunks.size() - 1);
                    fWriter.write("|Transaction: "+str1+"|status: Committed|execution time:"+currenttime+"\n");
                    fWriter.close();
                    System.out.println("Transaction " + str1 + " committed successfully");
                    break;
                default:
                    startTime = System.currentTimeMillis();
                    endTime = System.currentTimeMillis();
                    logger.setLogType(LogType.INVALID);
                    logger.setChangeMessage("Invalid login");
                    Logger.log(logger);
                    System.out.println("");
                    break;
            }
        input = "";
        }

    private boolean parseUpdateQuery(List<String> inputChunks) {
        String tableName = inputChunks.get(1);
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setTableName(tableName);
        int setIndex = inputChunks.indexOf("set");
        updateOptions.setTargetColumnName(inputChunks.get(setIndex+1));
        updateOptions.setTargetColumnValue(inputChunks.get(setIndex+3));
        int whereIndex = inputChunks.indexOf("where");
        updateOptions.setColumnName(inputChunks.get(whereIndex+1));
        updateOptions.setColumnValue(inputChunks.get(whereIndex+3));

        /* checking if the column to be updated uses reference */

        List<HashMap<String, String>> metaData = tablesMetaData.get(tableName);
        for(HashMap<String, String> meta : metaData) {
            if(meta.containsKey("fk")) {
                String columnName = meta.get("name");
                if(columnName.equals(updateOptions.getTargetColumnName())) {
                    boolean exist = QueryValidator.validateDataInForeignKeyTable(updateOptions.getTargetColumnValue(), meta.get("fkTableName"), meta.get("fkFieldName"), tableRows);
                    if(!exist) {
                        System.out.println("value "+updateOptions.getTargetColumnValue()+" does not exist in referenced table "+meta.get("fkTableName"));
                        logger.setChangeMessage("value "+updateOptions.getTargetColumnValue()+" does not exist in referenced table "+meta.get("fkTableName"));
                        return false;
                    }
                }
            }
        }

        List<HashMap<String, String>> rows = tableRows.get(tableName);
        for(HashMap<String, String> row:rows) {
            if(row.containsKey(updateOptions.getColumnName())) {
                if(row.get(updateOptions.getColumnName()).equals(updateOptions.getColumnValue())) {
                    if(row.containsKey(updateOptions.getTargetColumnName())) {
                        String value = updateOptions.getTargetColumnValue();
                        row.replace(updateOptions.getTargetColumnName(), value);
                    }
                }
            }
        }
        persistTableDataToDisk(tableName);
        logger.setTableName(tableName);
        logger.setChangeMessage("updated 1 row(s)");
        return true;
    }

    private void parseSelectQuery(SelectOptions selectOptions) throws IOException {
        String path = Path.of(currentDatabase, selectOptions.getTableName() + ".txt").toString();
        FileReader fr = new FileReader(new File(path));
        BufferedReader reader = new BufferedReader(fr);
        String nextLine = "";
        String columnString = reader.readLine();
        List<Integer> requiredIndexes = new ArrayList<>();
        List<String> columns =  Arrays.asList(columnString.split("\\|"));
        for(int i = 0 ; i<columns.size() ; i++) {
            if(selectOptions.isAll()) {
                System.out.format("%20s",columns.get(i));
            }else if(selectOptions.getColumns().contains(columns.get(i))) {
                System.out.format("%20s",columns.get(i));
                requiredIndexes.add(i);
            }
        }
        System.out.println();
        while((nextLine=reader.readLine())!=null) {
            List<String> columnValues = Arrays.asList(nextLine.split("\\|"));
            for(int i=0;i<columnValues.size();i++) {
                if(selectOptions.isAll()) {
                    if(selectOptions.isWhere()) {
                        int index = columns.indexOf(selectOptions.getColumnName());
                        if(columnValues.get(index).equals(selectOptions.getColumnValue())) {
                            System.out.format("%20s", columnValues.get(i));
                        }
                    }
                    else {
                        System.out.format("%20s", columnValues.get(i));
                    }
                }
                else {
                    if(requiredIndexes.contains(i)) {
                        if(selectOptions.isWhere()) {
                            int index = columns.indexOf(selectOptions.getColumnName());
                            if(columnValues.get(index).equals(selectOptions.getColumnValue())) {
                                System.out.format("%20s", columnValues.get(i));
                            }
                        }
                        else {
                            System.out.format("%20s", columnValues.get(i));
                        }

                    }
                }

            }
            System.out.println();
        }
        System.out.println("Got %s rows ".formatted(tableRows.get(selectOptions.getTableName()).size()));
        logger.setChangeMessage("Got %s rows ".formatted(tableRows.get(selectOptions.getTableName()).size()));
    }
}