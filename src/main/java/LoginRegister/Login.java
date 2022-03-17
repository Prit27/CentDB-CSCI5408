package LoginRegister;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Login {

	static String userID;
    String password;
    String hashedUserId;
    String hashedPassword;
    Boolean isAuthenticated=false;
    final String SQ1= "What is your favourite animal?";
    String SA;

    String getSecurityAnswer(){
        System.out.println(SQ1);
        Scanner sc = new Scanner(System.in);
        SA=sc.nextLine();
        return SA;
    }

    void login() throws NoSuchAlgorithmException, IOException {
        System.out.println("======= LOGIN ========");
        Register register=new Register();
        userID=register.getUserIDFromUser();
        password=register.getPasswordFromUser();
        SA=getSecurityAnswer();
        hashedUserId=register.generateHash(userID);
        hashedPassword=register.generateHash(password);
        if(checkIfUserExists()){
            System.out.println("\nWelcome to CentDB!");
            isAuthenticated=true;
            Main.accessCentDb();
        }
        else{
            System.out.println("Incorrect credentials.\nTry again.");
            login();
        }
    }
    
    public static String getCurrentUser() {
    	return userID;
    }

    private boolean checkIfUserExists() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Path.of("UserProfile.txt").toString()));
        String line = reader.readLine();
        while (line != null) {
            String[] credentials=line.split("[|]");
            if(credentials[0].equals(hashedUserId) && credentials[1].equals(hashedPassword)){
                return true;
            }
            line=reader.readLine();
        }
        return false;
    }

}
