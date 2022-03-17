package SqlDump;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class SqlDump {
    public void mainGenerator() throws IOException {
        initialiseDumpFolder();
        System.out.println("\n Enter database name to generate SQL dump:");
        Scanner sc=new Scanner(System.in);
        final String dbname = sc.nextLine().trim();
        if (!Files.exists(Paths.get(dbname))) {
            System.out.println("Database does not exists!");
            mainGenerator();
            return;
        }
        if (dbname==null||dbname.isEmpty()) {
            System.out.println("Please enter a valid database name! ");
            mainGenerator();
            return;
        }
        else
        {
            boolean dumpCheck = saveDump(dbname);
            if (dumpCheck) {
                System.out.println("Sql Dump of " + dbname + " created successfully !!!");
            }
            else {
                System.out.println("Sql Dump creation of " + dbname + " failed !!");
            }
        }
    }


    public static void initialiseDumpFolder()
    {
         File outputFolder = new File("./src/main/java");
         File sqlDumpsFolder = new File(outputFolder + "/OutputDumps");
        if (!sqlDumpsFolder.exists()) {
            if (sqlDumpsFolder.mkdirs()) {
                System.out.println(sqlDumpsFolder.getName() + " directory created!");
            }
        }

    }


    private boolean saveDump( String databaseName) throws IOException
    {
        DumpQueryProcessor queryProcessor=new DumpQueryProcessor();
        File allTables = new File(databaseName);
        File[] dbTables = allTables.listFiles();

         String dumpFileName = "./src/main/java/OutputDumps/" + databaseName + "-" + System.currentTimeMillis() + ".sql";
        for ( File tableFileName : dbTables)
        {
             String tableName = tableFileName.getName().split("\\.")[0];
            if (!tableName.equalsIgnoreCase("meta"))
            {
                try ( FileWriter fileWriter = new FileWriter(dumpFileName, true);
                      FileReader fileReader = new FileReader(tableFileName);
                      BufferedReader tableReader = new BufferedReader(fileReader))
                {
                    String currentTable;
                    boolean tableData = false;
                    String[] currentTableColNames = new String[0];
                    String[] currentTableColTypes = new String[0];

                    while ((currentTable = tableReader.readLine()) != null)
                    {
                        if (!tableData)
                        {
                            tableData = true;
                            String createTableQuery = queryProcessor.createTableQueryForDump(currentTable, tableName,databaseName);
                            currentTableColNames = getTableColumnNamesList(currentTable);
                            currentTableColTypes = getColumnDataTypesList(tableName,databaseName,currentTable);
                            fileWriter.append(createTableQuery);
                            fileWriter.append("\n");
                        }
                        else
                        {
                            String insertIntoTableQuery = queryProcessor.insertIntoQueryForDump(currentTable, tableName, currentTableColNames, currentTableColTypes);
                            fileWriter.append(insertIntoTableQuery);
                            fileWriter.append("\n");
                        }
                    }
                }
                catch (final IOException e) {
                    final String message = "Error: {" + e.getMessage() + "}!";
                    throw new IOException(message);
                }
            }
        }
        return true;
    }

    private String[] getTableColumnNamesList(String tableHeading) {
        final String[] tableHeadingColumns = tableHeading.split("\\|");
        final String[] columnNames = new String[tableHeadingColumns.length];
        for (int i = 0; i < tableHeadingColumns.length; ++i) {
            final String[] rowColTokens = tableHeadingColumns[i].split("\\(");
            columnNames[i] = rowColTokens[0];
        }
        return columnNames;
    }

    private String[] getColumnDataTypesList(String tableName, String databaseName, String tableEntry) {
        final String[] TableHeadingColumns = tableEntry.split("\\|");
        String[] ColName=new String[TableHeadingColumns.length];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Path.of(databaseName, "meta.txt").toString()));
            String line = reader.readLine();
            int i=0;
            while (line != null) {
                String[] metadata = line.split("\\|");
                if(!metadata[0].equalsIgnoreCase(tableName))
                {
                }
                else
                {
                    ColName[i]=metadata[2];
                    i++;
                }
                // read next line
                line = reader.readLine();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return ColName;
    }


}
