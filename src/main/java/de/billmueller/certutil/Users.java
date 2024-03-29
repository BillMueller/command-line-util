package de.billmueller.certutil;

import java.io.*;

public class Users {
    private String name, password, defaultDirectory;
    private int defaultStyle, permissionLevel;

    public boolean setUser(Main main, String userName) {
        Reader r;
        try {
            r = new LineNumberReader(new FileReader(main.appDataDirectory + "/" + main.usersFileName));
            try (LineNumberReader rdr = new LineNumberReader(r)) {
                String currentLine;
                String[] currentLineArray;
                while ((currentLine = rdr.readLine()) != null) {
                    currentLineArray = currentLine.split(";");
                    if (currentLineArray[0].equals(userName)) {
                        name = userName;
                        password = currentLineArray[1];
                        defaultDirectory = currentLineArray[2];
                        defaultStyle = Integer.parseInt(currentLineArray[3]);
                        permissionLevel = Integer.parseInt(currentLineArray[4]);
                    }
                }
                return name == null;
            } catch (Exception e) {
                main.printError("users.txt file is missing");
                main.printDebug(e.getMessage());
                return true;
            }
        } catch (Exception e) {
            main.printError("the users.txt file you've set couldn't be found");
            main.printDebug(e.getMessage());
            return true;
        }
    }

    //TODO add user creation
//    public boolean createUser(Main main, String userName, String password, String defaultDirectory, int defaultStyle, String permissionFile) {
//        if (main.usersFile.equals("default")) {
//            main.printError("you need to change the users File to add a new user to it");
//        } else if (!setUser(main, userName)) {
//            main.printError("the Username you've entered already exists");
//            main.printInfo("you can for example use following names:");
//            int c = 0;
//            int extra = 0;
//            while (c < 5) {
//                extra = (int) Math.random() * 100;
//                if (setUser(main, "\t" + userName + extra)) {
//                    main.printInfo("\t" + userName + extra);
//                    c++;
//                }
//            }
//        } else {
//            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(main.usersFile), true))) {
//                bw.write(userName);
//                bw.newLine();
//                bw.write(password);
//                bw.newLine();
//                bw.write(userName);
//                bw.newLine();
//                bw.write(defaultDirectory);
//                bw.newLine();
//                bw.write(String.valueOf(defaultStyle));
//                bw.newLine();
//                bw.write(permissionFile);
//                bw.flush();
//            } catch (Exception e) {
//                main.printError("the users file you've set couldn't be found");
//            }
//        }
//        return false;
//    }
//
//    public void setUsersFile(String File) {
//    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public int getDefaultStyle() {
        return defaultStyle;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }
}