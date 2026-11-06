package classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Stores the login details in a plain text file (no hashing on purpose).
// Each line of the file is:  username,password,ROLE   (ROLE is HOD or LECTURER)
// To add or change a user just edit src/data/users.txt
public class PasswordSystem {

    private static final String FILE_NAME = "src/data/users.txt";

    // each account is stored as {username, password, role}
    private ArrayList<String[]> accounts;

    public PasswordSystem() {
        accounts = new ArrayList<>();
        loadUsers();
    }

    // reads the users file, creates it with two default users if it doesn't exist yet
    private void loadUsers() {
        accounts.clear();
        Path path = Paths.get(FILE_NAME);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.write(path, List.of("hod,hod123,HOD", "lecturer,lecturer123,LECTURER"));
            }

            for (String line : Files.readAllLines(path)) {
                String[] fields = line.split(",");
                // skip blank lines and anything that isn't username,password,role
                if (fields.length == 3) {
                    accounts.add(new String[]{fields[0].trim(), fields[1], fields[2].trim().toUpperCase()});
                }
            }
        } catch (IOException ex) {
            System.getLogger(PasswordSystem.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    // returns "HOD" or "LECTURER" if the login is correct, otherwise null
    public String checkLogin(String username, String password) {
        loadUsers(); // read the file again so changes to it are picked up
        for (String[] account : accounts) {
            if (account[0].equalsIgnoreCase(username.trim()) && account[1].equals(password)) {
                return account[2];
            }
        }
        return null;
    }
}
