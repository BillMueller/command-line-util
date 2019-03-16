package de.billmueller.certutil;

import java.io.*;
import java.util.Date;

public class Note {
    private BufferedWriter bw = null;
    private String userNoteFile;
    private Main main;

    public boolean createNote(Main main, String userNoteFile) {
        this.main = main;
        this.userNoteFile = userNoteFile;
        main.printDebug("note directory: " + userNoteFile);
        try {
            bw = new BufferedWriter(new FileWriter(new File(userNoteFile), true));
        } catch (IOException ioe) {
            main.printError("Error with setting up the note option");
            main.printDebug("IOExcpetion: " + ioe.toString());
            return false;
        }
        return true;
    }

    public boolean makeNote(String note) {
        if (bw != null) {
            try {
                bw.write("0;" + new Date() + ";" + note);
                bw.newLine();
                bw.flush();
            } catch (IOException ioe) {
                main.printError("Error with creating the note");
                main.printDebug("IOExcpetion: " + ioe.toString());
                return false;
            }
        } else {
            main.printError("Error with user note class creation. Contact system administrator for help.");
            return false;
        }
        return true;
    }

    public boolean makeRegistrationNote(String name, String userName, String eMail, String password) {
        if (bw != null) {
            try {
                bw.write("1;" + new Date() + ";" + name + ";" + userName + ";" + eMail + ";" + password);
                bw.newLine();
                bw.flush();
            } catch (IOException ioe) {
                main.printError("Error with creating the note");
                main.printDebug("IOExcpetion: " + ioe.toString());
                return false;
            }
        } else {
            main.printError("Error with user note class creation. Contact system administrator for help.");
            return false;
        }
        return true;
    }

    public void searchNote(String note) {
        String s;
        String[] sArray;
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(userNoteFile))) {
            while ((s = lnr.readLine()) != null) {
                sArray = s.split(";");
                main.printDebug("searching: " + sArray[2]);
                if (sArray[2].toLowerCase().contains(note.toLowerCase())) {
                    if (sArray[0].equals("0")) {
                        main.printWithDate(sArray[1], sArray[2]);
                    } else if (sArray[0].equals("1")) {
                        main.printWithDate(sArray[1], "name: " + sArray[2]);
                        main.printWithDate(sArray[1], "user name: " + sArray[3]);
                        main.printWithDate(sArray[1], "e-mail: " + sArray[4]);
                        main.printWithDate(sArray[1], "password: " + sArray[5]);
                    } else
                        main.printError("error in the note file. The note file may be broken.");
                }
            }
        } catch (IOException ioe) {
            main.printError("Your permission file is broken. Please contact an administrator");
            main.printDebug("IOExcpetion: " + ioe.toString());
        }
    }
}
