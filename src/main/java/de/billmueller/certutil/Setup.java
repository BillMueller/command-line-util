package de.billmueller.certutil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;

public class Setup {
    public static void main(String[] args) {
//		System.getenv().forEach((k, v) -> {System.out.println(k + ":" + v);});
//        Setup setup = new Setup();
//        Main main = new Main();
//        main.debug = true;
//        setup.start(main, true, System.getenv().get("USERPROFILE") + "/Desktop/jCommander");
    }

    public void start(Main main, boolean fullSetup, String setupDir) {
        String configFileName = "config.properties", usersFileName = "users.txt";
        File index = new File(setupDir);
        main.printInfo(index.toString());
        if (index.exists() && fullSetup) {
            main.printDebug("resetting the installation folder");
            resetDirectory(main, index);
        }
        if (!index.exists() && !fullSetup) {
            main.printDebug("the folder doesn't exist and it wasn't a full setup selected");
            main.printError("The installation folder couldn't be found (you have to do a full setup first)");
        } else if (fullSetup && !index.exists() && !index.mkdirs())
            main.printError("The creation of the directory wasn't successful");
        else {
            main.printDebug("starting the main setup");
            new Setup().setup(main, index.toString(), fullSetup, configFileName, usersFileName);
        }
    }

    private void setup(Main main, String index, boolean fullSetup, String configFileName, String usersFileName) {
        main.printDebug("The setup directory is: " + index);
        //---
        configSetup(main, index, fullSetup, configFileName);
        usersFileSetup(main, index, fullSetup, usersFileName);
        //---
        main.printInfo("Setup successful!");
    }

    private void configSetup(Main main, String index, boolean fullSetup, String configFileName) {
        if (fullSetup || !new File(index + "/" + configFileName).exists())
            copyConfigFile(main, index + "/" + configFileName);
    }

    private void usersFileSetup(Main main, String index, boolean fullSetup, String usersFileName) {
        if (fullSetup || !new File(index + "/" + usersFileName).exists())
            copy(main, index + "/" + usersFileName, usersFileName);
    }

    public void copy(Main main, String destination, String fileName) {
        try (InputStream source = getClass().getClassLoader().getResourceAsStream(fileName)){
            main.printDebug("Copying " + source + " to " + destination);
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException ioe){
            main.printError("Failed to set up users.txt file");
            main.printDebug(ioe.getMessage());
        }
    }

    private boolean copyConfigFile(Main main, String targetDirectory) {
        Properties prop;
        main.printDebug("copying the config file");
        try (FileOutputStream os = new FileOutputStream(targetDirectory)) {
            prop = getPropertiesFile(main);
            prop.store(os, "'default' can be used for: defaultExDate, defaultSerialNumber, defaultValidity, defaultStDate");
            main.printInfo("successfully copied the config.properties file");
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            main.printError("default config file couldn't be found");
            main.printError("copy failed");
            main.printDebug(ioe.getMessage());
            return false;
        }
    }

    private Properties getPropertiesFile(Main main) {
        main.printDebug("loading config.properties");
        Properties prop = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null)
                prop.load(input);
            else
                throw new IOException();
            main.printDebug("successfully loaded settings from " + "config.properties");
        } catch (IOException ex) {
            main.printDebug("IOException:");
            main.printDebug(ex.getMessage());
        }
        return prop;
    }

    private void resetDirectory(Main main, File index) {
        try {
            String[] entries = index.list();
            for (String s : entries) {
                File currentFile = new File(index.getPath(), s);
                currentFile.delete();
            }
        } catch (NullPointerException npe) {
            main.printError("Error with deleting the old setup folder");
            main.printDebug(npe.getMessage());
        }
    }
}