package ERD;

import QueryValidator.QueryValidator;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ERD {


    public void main() throws IOException {
        int columnCount=0;
        int numberOfColumns=0;
        String table=null;
        System.out.println("Enter database name to generate ERD:");
        Scanner sc = new Scanner(System.in);
        String currentDatabase=sc.nextLine();
        if(!QueryValidator.checkIfDBExists(currentDatabase)){
            System.out.println("No such database exists.");
            return;
        }
        String path = Path.of(currentDatabase, "meta.txt").toString();
        PrintWriter pw = new PrintWriter("ERD.txt");
        pw.close();
        try {
            FileReader fileReader=new FileReader(path);
            BufferedReader bufferedReader=new BufferedReader(fileReader);
            String data=bufferedReader.readLine();

            while(data!=null) {

                String line[]=data.split("[|]");
                table=line[0];

                if(Objects.equals(line[0], table)) {
                    columnCount += 1;
                    data = bufferedReader.readLine();

                    if (data != null) {
                        String nextline[] = data.split("[|]");
                        if(!Objects.equals(nextline[0], table)) {
                            numberOfColumns = columnCount;
                            display(currentDatabase, table, numberOfColumns);
                            columnCount = 0;
                        }
                        line[0] = nextline[0];
                    }

                }
            }
            numberOfColumns=columnCount;
            display(currentDatabase,table,numberOfColumns);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getRelationships(currentDatabase);
        System.out.println("ERD successfully generated.");
    }

    private void display(String currentDatabase,String table, int numberOfColumns) throws IOException {
        String path = Path.of(currentDatabase, "meta.txt").toString();
        try {
            FileReader fileReader=new FileReader(path);
            BufferedReader bufferedReader=new BufferedReader(fileReader);
            String data=bufferedReader.readLine();

            File file=new File("ERD.txt");
            FileWriter fileWriter=new FileWriter(file,true);

            fileWriter.write("\n\nTABLE:  \t"+table);
            fileWriter.write("\nCOLUMNS:");
            List<String> columns=new ArrayList<>();
            while(data!=null) {

                String line[]=data.split("[|]");
                if(Objects.equals(line[0], table)) {
                    if((line.length>3) && Objects.equals(line[3], "pk")){
                        columns.add(line[1]+"(pk)");
                    }
                    else {

                        columns.add(line[1]);
                    }
                }
                data=bufferedReader.readLine();

            }
            for(String column:columns){
                fileWriter.write("\t"+column+",");
            }

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRelationships(String currentDatabase) throws IOException {
        String path = Path.of(currentDatabase, "meta.txt").toString();
        FileReader fileReader=new FileReader(path);
        BufferedReader bufferedReader=new BufferedReader(fileReader);
        String data=bufferedReader.readLine();

        File file=new File("ERD.txt");
        FileWriter fileWriter=new FileWriter(file,true);

        fileWriter.write("\n\n=======Relationships=======");
        while(data!=null) {
            String line[]=data.split("[|]");
            if(line.length>3 && line[3].equals("fk")){
                fileWriter.write("\nTable '"+line[4]+"' has one to many relationship with '"+line[0]+"'.");
            }
            data=bufferedReader.readLine();
        }
        fileWriter.flush();
        fileWriter.close();
    }
}

