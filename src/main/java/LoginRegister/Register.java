package LoginRegister;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Register {
    String userID;
    String password;
    final String SQ1= "What is your favourite animal?";
    String SA1;
    final String SQ2= "What is your middle name?";
    String SA2;

    public Register(){
    }

    String getUserIDFromUser() throws NoSuchAlgorithmException {
        System.out.print("Enter a User ID:");
        Scanner sc = new Scanner(System.in);
        userID=sc.nextLine();
        return userID;
    }

    //https://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
    String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder encryptedInput = new StringBuilder();

        for (byte b:bytes){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1){
                encryptedInput.append('0').append(hex);
            } else {
                encryptedInput.append(hex);
            }
        }
        return encryptedInput.toString();
    }

    void getSecurityAnswers(){
        System.out.println(SQ1);
        Scanner sc = new Scanner(System.in);
        SA1=sc.nextLine();
        System.out.println(SQ2);
        SA2=sc.nextLine();
    }

    String getPasswordFromUser() throws NoSuchAlgorithmException {
        System.out.print("Enter a password:");
        Scanner sc = new Scanner(System.in);
        password=sc.nextLine();
        return password;
    }

    boolean checkValidPassword(String password) {
        if(password.length()<8){
            System.out.println("Password should be atleast 8 charaters long");
            return false;
        }
        return true;
    }

    boolean checkUserIdExists() throws IOException, NoSuchAlgorithmException {
        BufferedReader reader = new BufferedReader(new FileReader(Path.of("UserProfile.txt").toString()));
        String line = reader.readLine();
        while (line != null) {
            String[] credentials=line.split("[|]");
            if((credentials[0]).equals(generateHash(userID))){
                System.out.println("User already exists.");
                return true;
            }
            line=reader.readLine();
        }
        return false;
    }

    void writeToFile(String content) throws IOException {
        try {
            File UserProfileLog = new File("UserProfile.txt");
            FileWriter fileWriter=new FileWriter(UserProfileLog,true);
            PrintWriter printWriter=new PrintWriter(fileWriter);
            printWriter.println(content);
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void register() throws IOException, NoSuchAlgorithmException {
        System.out.println("=======REGISTRATION====");
        userID = getUserIDFromUser();
        while (checkUserIdExists()){
            System.out.println("Try a different username.");
            getUserIDFromUser();
        }
        password=getPasswordFromUser();
        while (!checkValidPassword(password)){
            getPasswordFromUser();
        }
        getSecurityAnswers();
        String hashedUserId=generateHash(userID);
        String hashedPassword=generateHash(password);
        //Add hashed userID, hashed password and security answers to file
        writeToFile(hashedUserId+"|"+hashedPassword+"|"+SA1+"|"+SA2);
        System.out.println("Registration Successful.");
        System.out.println("Please log in.");
        Login login=new Login();
        login.login();
        if(login.isAuthenticated){
            Main main=new Main();
            main.accessCentDb();
        }
    }

}

