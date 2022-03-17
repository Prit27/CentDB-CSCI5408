package LoginRegister;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Recovery {
    String UserId;
    String hashedUserId;
    String SA1;
    String SA2;
    String oldUserInfo;

    public void forgotPassword() throws NoSuchAlgorithmException, IOException {
        Register register=new Register();
        UserId=register.getUserIDFromUser();
        hashedUserId=register.generateHash(UserId);
        Scanner sc=new Scanner(System.in);
        System.out.println("What is your favourite animal?");
        SA1 = sc.nextLine();
        System.out.println("What is your middle name?");
        SA2 = sc.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(Path.of("UserProfile.txt").toString()));
        String line = reader.readLine();
        Boolean userFound=false;
        while (line != null) {
            oldUserInfo=line;
            String[] credentials=line.split("[|]");
            if(credentials[0].equals(hashedUserId) && credentials[2].equals(SA1) && credentials[3].equals(SA2)){
                userFound=true;
                String newPassword=getNewPassword();
                setNewPassword(newPassword);
                Login login=new Login();
                login.login();
            }
            line=reader.readLine();
        }
        if (!userFound){
            System.out.println("Incorrect User ID or security answers.\nPlease try again!");
            forgotPassword();
        }
    }

    private void setNewPassword(String newPassword) throws IOException, NoSuchAlgorithmException {
        String newUserInfo= hashedUserId+"|"+newPassword+"|"+SA1+"|"+SA2;
        BufferedReader reader = new BufferedReader(new FileReader(Path.of("UserProfile.txt").toString()));
        String line = reader.readLine();
        StringBuffer buffer = new StringBuffer();
        while (line != null) {
            buffer.append(line+"\n");
            line=reader.readLine();
        }
        String userProfile=buffer.toString();
        userProfile=userProfile.replace(oldUserInfo,newUserInfo);
        File file=new File("UserProfile.txt");
        FileWriter fileWriter=new FileWriter(file);
        fileWriter.append(userProfile);
        fileWriter.flush();
        fileWriter.close();
        System.out.println("Password successfully updated.");
    }

    private String getNewPassword() throws NoSuchAlgorithmException {
        Register register=new Register();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a new password:");
        String newPassword= sc.nextLine();
        System.out.print("Confirm password:");
        String confirmPassword=sc.nextLine();
        if(!newPassword.equals(confirmPassword)){
            System.out.println("Passwords do not match. Try again!");
            getNewPassword();
        }
        return register.generateHash(newPassword);
    }
}
