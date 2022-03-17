package SqlDump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class DumpQueryProcessor {
    public String createTableQueryForDump(String tableHeading, String tableName, String databaseName) throws IOException {
        final StringBuilder createQuery = new StringBuilder();
        final String[] tableHeadingColumns = tableHeading.split("\\|");
        createQuery.append("CREATE").append(" ").append("TABLE").append(" ").append(tableName).append(" ").append("(");
        for (final String column : tableHeadingColumns) {
            createQuery.append(" ").append(column);
            String colType= getColType(column,databaseName,tableName);
            if(colType==null)
            {
                throw new IOException("No Mapping found with meta.txt table");
            }
            createQuery.append(" ").append(colType);
        }
        createQuery.replace(createQuery.length() - 1, createQuery.length(), "");
        createQuery.append(");");
        return createQuery.toString();
    }
    public String insertIntoQueryForDump(String tableData, final String tableName, final String[] tableColumnNames, final String[] tableColumnDataTypes) {
        final StringBuilder insertQuery = new StringBuilder();
        insertQuery.append("INSERT").append(" ").append("INTO").append(" ").append(tableName).append(" ").append("(");

        for (final String tableColumn : tableColumnNames) {
            insertQuery.append(tableColumn).append(", ");

        }
        insertQuery.replace(insertQuery.length() - 2, insertQuery.length(), "");
        insertQuery.append(")").append(" ").append("VALUES").append(" ").append("(");

        final String[] tableDataColumns = tableData.split("\\|");
        if(tableDataColumns.length==tableColumnNames.length)
        {
            for (int i = 0; i < tableDataColumns.length; ++i) {
                if (tableColumnDataTypes[i].equalsIgnoreCase("INT")||tableColumnDataTypes[i].equalsIgnoreCase("FLOAT")||tableColumnDataTypes[i].equalsIgnoreCase("BOOLEAN")) {
                    insertQuery.append(tableDataColumns[i]).append(", ");
                }
                if (tableColumnDataTypes[i].equalsIgnoreCase("TEXT")||tableColumnDataTypes[i].equalsIgnoreCase("VARCHAR")) {
                    insertQuery.append("\"").append(tableDataColumns[i]).append("\"").append(", ");
                }
            }
            insertQuery.replace(insertQuery.length() - 2, insertQuery.length(), "");
            insertQuery.append(");");
            return insertQuery.toString();
        }
        else{
            String tableDataArr[]=new String[tableColumnNames.length];
            int count=tableColumnNames.length;
            int x=0;
            while(x<count-1)
            {
                int index1 = 0;
                int index2 = tableData.indexOf("|");
                String data = tableData.substring(index1, index2);
                tableDataArr[x]=data;
                tableData = tableData.substring(tableData.indexOf(data) + data.length()+1, tableData.length());
                x++;
            }
            for (int i = 0; i < tableDataArr.length; ++i) {
                if (tableColumnDataTypes[i].equalsIgnoreCase("INT")||tableColumnDataTypes[i].equalsIgnoreCase("FLOAT")||tableColumnDataTypes[i].equalsIgnoreCase("BOOLEAN")) {
                    insertQuery.append(tableDataArr[i]).append(", ");
                }
                if (tableColumnDataTypes[i].equalsIgnoreCase("TEXT")||tableColumnDataTypes[i].equalsIgnoreCase("VARCHAR")) {
                    insertQuery.append("\"").append(tableDataArr[i]).append("\"").append(", ");
                }
            }
            insertQuery.replace(insertQuery.length() - 2, insertQuery.length(), "");
            insertQuery.append(");");
            return insertQuery.toString();

        }
    }
    private String getColType(String column, String databaseName,String tableName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Path.of(databaseName, "meta.txt").toString()));
            String line = reader.readLine();
            while (line != null) {
                String[] metadata = line.split("\\|");

                if ((metadata.length == 4||metadata.length==6) && metadata[0].equalsIgnoreCase(tableName)) {
                    if (metadata[1].equalsIgnoreCase(column)) {
                        if(metadata[2].equalsIgnoreCase("varchar"))
                        {
                            String col= metadata[2]+"("+metadata[3]+")"+",";
                            return col;
                        }
                        else if(metadata[3].equalsIgnoreCase("pk"))
                        {
                            String col= metadata[2]+" PRIMARY KEY"+",";
                            return col;
                        }
                        else if(metadata[3].equalsIgnoreCase("fk"))
                        {
                                String col = metadata[2]+"," + " FOREIGN KEY ("+metadata[1]+")"+" REFERENCES " + metadata[4] + "(" + metadata[5] + "),"; //FOREIGN KEY REFERENCES
                                return col;
                        }
                    }
                }
                else if (metadata.length == 3 && metadata[0].equalsIgnoreCase(tableName)) {
                    if (metadata[1].equalsIgnoreCase(column)) {
                        return metadata[2]+",";
                    }
                }
                // read next line
                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
