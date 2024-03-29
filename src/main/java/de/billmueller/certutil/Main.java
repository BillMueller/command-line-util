package de.billmueller.certutil;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.util.*;

public class Main {
    private String ANSI_RESET = "\u001B[0m";
    private String ANSI_ERROR = "\u001B[91m";
    private String ANSI_HELP = "\u001B[92m";
    private String ANSI_INPUT = "\u001B[93m";
    private String ANSI_OUTPUT = "\u001B[94m";
    private String ANSI_DEBUG = "\u001B[36m";

    private int style = 0;
    public boolean debug = false;
    private String pFile;
    private int permissionLevel = 0;
    public String username = null;
    private Note noteDocument = null;

    private String operatingSystem = System.getProperty("os.name");
    private String setupDir = "jCommander";
    public File appDataDirectory;
    public String configFileName = "config.properties", usersFileName = "users.txt";

    private Thread[] alarmArray = new Thread[5];

    @Parameter(names = {"writeCertificate", "wc"}, description = "generate a new certificate")
    private boolean writeC;
    @Parameter(names = {"writeDocument", "wd"}, description = "changes to J-Console")
    private boolean writeD;
    @Parameter(names = {"readCertificate", "rc"}, description = "read a certificate")
    private boolean readC;
    @Parameter(names = {"readDocument", "rd"}, description = "reads a file")
    private boolean readD;
    @Parameter(names = {"changeDirectory", "cd"}, description = "changes working directory")
    private boolean cd;
    @Parameter(names = "help", description = "prints out a general help")
    private boolean gHelp;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor")
    private boolean exit;
    @Parameter(names = {"encodeDocument", "ed"}, description = "encodes a text file with a certificate")
    private boolean et;
    @Parameter(names = {"decodeDocument", "dd"}, description = "decodes a text file with a private key")
    private boolean dt;
    @Parameter(names = {"changeStyle", "cs"}, description = "changes between colored and non-colored mode")
    private boolean cs;
    @Parameter(names = "note", description = "makes a note (if you have permission)")
    private boolean note;
    @Parameter(names = {"searchNote", "sn"}, description = "makes a note (if you have permission)")
    private boolean searchNote;
    @Parameter(names = "alarm", description = "creates an alarm for the time entered")
    private boolean alarm;
    @Parameter(names = {"toggleDebug", "debug"}, description = "changes between debug and normal mode")
    private boolean toggleDebug;
    @Parameter(names = "logout", description = "logs the user out")
    private boolean logout;

    @Parameter(names = {"--issuerName", "--iName"}, description = "eneter the ca name")
    private String iName;
    @Parameter(names = {"--subjectName", "--sName"}, description = "enter the owner name")
    private String sName;
    @Parameter(names = {"--startDate", "--sDate"}, description = "startdate for the certificate to be valid")
    private String sDate;
    @Parameter(names = {"--expiryDate", "--eDate"}, description = "expirydate for the certificate to be valid")
    private String eDate;
    @Parameter(names = "--keySize", description = "keySize of the public key (in bits)")
    private int keys;
    @Parameter(names = {"--serialNumber", "--serNumb"}, description = "set a serial number")
    private long serNumber;
    @Parameter(names = "--file", description = "set the certificate name")
    private String fileName;
    @Parameter(names = {"--certificateFile", "--certFile"}, description = "set the certificate name")
    private String certFileName;
    @Parameter(names = {"--signatureAlgorithm", "--signAlg"}, description = "set signature algorithm")
    private String signAlg;
    @Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
    private boolean bRead;
    @Parameter(names = {"--help", "-h"}, description = "prints out a help for the command entered before")
    private boolean help;
    @Parameter(names = "--copyConfig", description = "if the program should copy the config file to the directory")
    private boolean copyConfig;
    @Parameter(names = "--directory", description = "set the directory name")
    private String directoryName;
    @Parameter(names = "--certTargetDir", description = "set the custom certificate target folder")
    private String certTargetDirectory;
    @Parameter(names = "--certDirectory", description = "set the custom certificate folder")
    private String certDirectory;
    @Parameter(names = "--docDirectory", description = "set the custom document folder")
    private String docDirectory;
    @Parameter(names = "--style", description = "set a style")
    private String cStyle;
    @Parameter(names = "--toggle", description = "toggle the style")
    private boolean styleToggle;
    @Parameter(names = "--replace")
    private boolean replace;
    @Parameter(names = {"-r", "--registration"})
    private boolean registration;
    @Parameter(names = {"--date", "-d"})
    private String alarmDate;
    @Parameter(names = {"--time", "-t"})
    private String alarmTime;
    @Parameter(names = {"--message", "-m"})
    private String alarmMessage;
    @Parameter(names = {"--relativeTime", "-rT"})
    private String relativeTime;

    /**
     * Main function with the J-console input functionality <br>
     * When called first it shows [J-CONSOLE> Enter directory> <br>
     * After entering the working directory it will show [J-CONSOLE>
     * C:/.../exampleFolder> when it's read for an input <br>
     * After getting the input it will use JController to handle the input and it
     * will call run() function to call the needed functions
     */
    public void main() {
        if (operatingSystem.split(" ")[0].equals("Windows")) {
            setupDir = System.getenv().get("APPDATA") + "/jCommander";
        } else if (operatingSystem.equals("Linux")) {
            setupDir = System.getenv().get("PWD") + "/.jCommander";
        }
        appDataDirectory = new File(setupDir);
        printDebug("setup directory set to: " + setupDir);
        //---
        Main main = new Main();
        main.operatingSystem = operatingSystem;
        main.appDataDirectory = appDataDirectory;
        main.setupDir = setupDir;
        main.printDebug("Operating system found: " + operatingSystem);
        main.printDebug(appDataDirectory.exists() ? "Setup directory: " + appDataDirectory : "Setup directory: No Directory Existing so far");
        if (!new File(setupDir).exists()) {
            main.printInfo("Setup still missing");
            main.printInfo("Running full setup...");
            Setup setup = new Setup();
            setup.start(main, true, setupDir);
        }

        Scanner sc = new Scanner(System.in);
        String in;
        JCommander jc;
        String[] sin; // args.clone();
        main.exit = false;
        boolean start;
        main.cd = true;

        while (!main.exit) {
            main.printDebug("Starting input function");
            if (main.cd) {
                main.printDebug("Starting change Directory process");
                start = false;
                while (!start) {
                    main.printDebug("Starting change Directory loop");
                    main.printEditor("Enter directory");
                    in = sc.nextLine();
                    if (in.equals("exit")) {
                        exitConsole(main);
                    } else if (in.equals(("login"))) {
                        main.printDebug("Starting the login process");
                        Users user = new Users();
                        main.printEditor("Enter Username");
                        boolean login = user.setUser(main, in = sc.nextLine());
                        main.printDebug("Checking username validity...");
                        while (login) {
                            if (in.equals("exit")) {
                                main.printInfo("exiting ...");
                                System.exit(1);
                            }
                            main.printError("The Username you've entered isn't valid");
                            main.printEditor("Enter Username");
                            login = user.setUser(main, in = sc.nextLine());
                        }
                        main.printDebug("Starting password input process");
                        Console console = System.console();
                        while (!user.getPassword()
                                .equals(in = main.style > 2
                                        ? new String(console
                                        .readPassword("[" + ANSI_INPUT + "Enter Password" + ANSI_RESET + "> "))
                                        : new String(console.readPassword("[" + ANSI_INPUT + "J-CONSOLE" + ANSI_RESET
                                        + "> " + ANSI_INPUT + "Enter Password" + ANSI_RESET + "> ")))) {
                            if (in.equals("exit")) {
                                exitConsole(main);
                            }
                            main.printError("The password you've entered isn't valid");
                        }
                        main.printDebug("Setting the default values for the user");
                        main.cd = false;
                        main.permissionLevel = user.getPermissionLevel();
                        start = true;
                        main.pFile = user.getDefaultDirectory();
                        main.style = user.getDefaultStyle();
                        if (main.permissionLevel > 1) {
                            main.noteDocument = new Note();
                            if (!main.noteDocument.createNote(main, appDataDirectory + "/" + user.getName() + "sNotes.txt"))
                                main.printError("Your permissions file couldn't be found. Please contact a system administrator");
                        } else {
                            main.noteDocument = null;
                            debug = false;
                        }
                        main.printInfo("Welcome " + (main.username = user.getName()));
                        main.printDebug("Permissions File " + permissionLevel);
                        main.printDebug("Default Directory " + main.pFile);
                        main.printDebug("Default Style: " + main.style);
                    } else {
                        main.printDebug("The operating System is: " + operatingSystem);
                        if (operatingSystem.equals("Linux")) {
                            if (in.startsWith("/")) {
                                if (in.endsWith("/")) {
                                    in.substring(0, in.length() - 1);
                                }
                                main.printDebug("The directory passed the Linux validity test...");
                                main.pFile = in;
                                start = true;
                            } else {
                                main.printDebug("The directory has to start with /home for Linux");
                                main.printError("the path file you entered is not valid.");
                                main.printInfo("Start the directory with /home");
                            }
                        } else if (operatingSystem.split(" ")[0].equals("Windows")) {
                            if (in.split("/")[0].length() == 2 && in.contains(":")) {
                                if (in.endsWith("/")) {
                                    in.substring(0, in.length() - 1);
                                }
                                main.printDebug("The directory passed the Windows validity test...");
                                main.pFile = in;
                                start = true;
                            } else {
                                main.printDebug("The length of the first thing before the / is not 2 (it is " + in.split("/")[0].length() + ")");
                                main.printError("the path file you entered is not valid.");
                                main.printInfo("Use '/' for example C:/Users");
                            }
                        } else {
                            main.printDebug("The operating system wasn't recognized (it is not Windows or Linux)");
                            main.printDebug("There is no validity test for the operating System (setting the entered file to new directory path): " + in);
                            main.pFile = in;
                            start = true;
                        }
                    }
                }
            }
            main.cd = false;
            main.setToDefault();
            main.printEditor(main.pFile);
            in = sc.nextLine();
            sin = in.split(" ");
            if (sin.length != 0 && !in.equals("")) {
                try {
                    main.printDebug("Starting parse with JCommander");
                    jc = JCommander.newBuilder().addObject(main).build();
                    jc.parse(sin);
                    main.run(main);
                } catch (com.beust.jcommander.ParameterException pe) {
                    main.printDebug("Error: " + pe.getMessage());
                    main.printError("unknown command or parameters");
                    main.exit = false;
                    main.cd = false;
                }
            }
        }
        sc.close();
        main.printDebug("Scanner closed");

        exitConsole(main);

    }

    /**
     * Gets called if a command is entered and calls the needed functions
     *
     * @param main main class object (needed for the following functions)
     */
    private void run(Main main) {
        if (gHelp || help) {
            main.printDebug("calling help");
            callHelp(main);
        } else {
            if (!main.exit) {
                if (cd) {
                    main.printDebug("calling change Direcotry");
                    main.cd = true;
                } else if (cs) {
                    main.printDebug("calling change Style");
                    callChangeStyle(main);
                } else {
                    Properties dProps = callGetPropertiesFile(main);

                    List<String> props = getPropertiesData(dProps);

                    String dIssuerName = props.get(0);
                    String dSubjectName = props.get(1);
                    int dKeys = Integer.parseInt(props.get(2));
                    String dPropsSerNumber = props.get(3);
                    String dPropsStDate = props.get(4);
                    String dPropsExDate = props.get(5);
                    String dPropsValidity = props.get(6);
                    String dSignAlg = props.get(7);

                    long milSecValid = !dPropsValidity.equals("default") ? Long.valueOf(dPropsValidity) : 31536000000L;

                    long dSerNumber = dPropsSerNumber.equals("default") ? new Date().getTime()
                            : Long.valueOf(dPropsSerNumber);

                    Date dStDate = setDefaultPropertiesDates(dPropsStDate, 0);
                    Date dExDate = setDefaultPropertiesDates(dPropsExDate, milSecValid);

                    main.printDebug("starting command calling function");
                    callCommands(main, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dSignAlg);
                }

            }
        }
    }

    /**
     * Reads the config.properties file in the main project folder or a custom
     * config.properties file given with the --config parameter
     *
     * @param configFileName name of the config file (with directory unless its the
     *                       the config.properties file in hte resources folder.
     * @param printMsg       should be true unless it gets called in the test run
     * @param defaultPath    if the name is just config.properties -> true <br>
     *                       if the name is with directory (for example
     *                       C:/.../config.properties -> false
     * @return object of the type Properties (with object.getProperty(property) you
     * can get the value for the property
     */
    public Properties getPropertiesFile(Main main, String configFileName, boolean printMsg, boolean defaultPath) throws IOException {
        if (printMsg)
            if (defaultPath)
                main.printDebug("loading config.properties");
            else
                main.printDebug("loading " + configFileName);
        Properties prop = new Properties();

        if (defaultPath) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                else
                    throw new IOException();
                if (printMsg)
                    main.printDebug("successfully loaded settings from " + configFileName);
            } catch (IOException ex) {
                main.printDebug("IOException:");
                main.printDebug(ex.getMessage());
            }
        } else {
            try (InputStream input = new FileInputStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                main.printDebug("successfully loaded settings from " + configFileName);
            } catch (IOException ex) {
                if (printMsg) {
                    throw new IOException("");
                }
            }
        }
        return prop;
    }

    /**
     * Resets all the Mains global variables needed for JCommander
     */
    private void setToDefault() {
        writeC = false;
        readC = false;
        readD = false;
        cd = false;
        writeD = false;
        gHelp = false;
        et = false;
        dt = false;
        note = false;
        searchNote = false;
        alarm = false;
        toggleDebug = false;
        logout = false;
        // -------+
        iName = null;
        sName = null;
        sDate = null;
        eDate = null;
        keys = 0;
        serNumber = 0L;
        fileName = null;
        signAlg = null;
        bRead = false;
        help = false;
        certFileName = null;
        copyConfig = false;
        certTargetDirectory = null;
        cs = false;
        certDirectory = null;
        docDirectory = null;
        cStyle = null;
        styleToggle = false;
        replace = false;
        alarmTime = null;
        alarmDate = null;
        alarmMessage = null;
        relativeTime = null;
        registration = false;
    }

    /**
     * Prints the message with a blue [INFO] in front of it
     *
     * @param msg the message after the [INFO]
     */
    public void printInfo(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_OUTPUT + "INFO" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[INFO] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_OUTPUT + "I" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_OUTPUT + "+" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a red [ERROR] in front of it
     *
     * @param msg the message after the [ERROR]
     */
    public void printError(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_ERROR + "ERROR" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[ERROR] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_ERROR + "E" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_ERROR + "-" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a red [DEBUG] in front of it if debug = true
     *
     * @param msg the message after the [DEBUG]
     */
    public void printDebug(String msg) {
        if (debug) {
            if (style == 0 || style == 2)
                System.out.println("[" + ANSI_DEBUG + "DEBUG" + ANSI_RESET + "] " + msg);
            else if (style == 1)
                System.out.println("[ERROR] " + msg);
            else if (style == 3)
                System.out.println("[" + ANSI_DEBUG + "D" + ANSI_RESET + "] " + msg);
            else
                System.out.println("[" + ANSI_DEBUG + "#" + ANSI_RESET + "] " + msg);
        }
    }

    /**
     * Prints the message with a green [HELP] in front of it
     *
     * @param msg the message after the [HELP]
     */
    private void printHelp(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_HELP + "HELP" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[HELP] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_HELP + "H" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_HELP + "*" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a blue [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printCertData(String msg) {
        if (style != 1)
            System.out.println("[" + ANSI_OUTPUT + "-" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[-] " + msg);
    }

    /**
     * Prints the message with a red [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printRedCertData(String msg) {
        if (style != 1)
            System.out.println("[" + ANSI_ERROR + "-" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[-] " + msg);
    }

    /**
     * Prints the message with a blue [number of the lines] in front of it for
     * example [25]
     *
     * @param msg the message after the [number of the lines]
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in
     *            front of the number)
     */
    public void printDocumentData(String msg, int i, int max) {
        if (style != 1) {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.println("[" + ANSI_OUTPUT + "   " + i + ANSI_RESET + "] " + msg);
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.println("[" + ANSI_OUTPUT + "  " + i + ANSI_RESET + "] " + msg);
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1)
                    || (max / 10. > 1 && i / 10. < 1)) {
                System.out.println("[" + ANSI_OUTPUT + " " + i + ANSI_RESET + "] " + msg);
            } else {
                System.out.println("[" + ANSI_OUTPUT + i + ANSI_RESET + "] " + msg);
            }
        } else {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.println("[   " + i + "] " + msg);
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.println("[  " + i + "] " + msg);
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1)
                    || (max / 10. > 1 && i / 10. < 1)) {
                System.out.println("[ " + i + "] " + msg);
            } else {
                System.out.println("[" + i + "] " + msg);
            }
        }
    }

    public void printWithDate(String customText, String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_OUTPUT + customText + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[" + customText + "] " + msg);
        else {
            String[] splittedCustomText = customText.split(" ");
            System.out.println("[" + ANSI_OUTPUT + splittedCustomText[0] + " " + splittedCustomText[1] + " " + splittedCustomText[2] + ANSI_RESET + "] " + msg);
        }
    }

    /**
     * Prints a blue [number of the lines] for example [25]
     *
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in
     *            front of the number)
     */
    public void printEditorInput(int i, int max) {
        if (style != 1) {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.print("[" + ANSI_INPUT + "   " + i + ANSI_RESET + "] ");
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.print("[" + ANSI_INPUT + "  " + i + ANSI_RESET + "] ");
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1)
                    || (max / 10. > 1 && i / 10. < 1)) {
                System.out.print("[" + ANSI_INPUT + " " + i + ANSI_RESET + "] ");
            } else {
                System.out.print("[" + ANSI_INPUT + i + ANSI_RESET + "] ");
            }
        } else {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.print("[   " + i + "] ");
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.print("[  " + i + "] ");
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1)
                    || (max / 10. > 1 && i / 10. < 1)) {
                System.out.print("[ " + i + "] ");
            } else {
                System.out.print("[" + i + "] ");
            }
        }
    }

    /**
     * Prints a yellow [J-CONSOLE> in front of the message and the msg after it also
     * in yellow
     *
     * @param msg the message in yellow after the [J-CONSOLE>
     */
    public void printEditor(String msg) {
        if (style == 0 || style == 2)
            System.out.print("[" + ANSI_INPUT + "J-CONSOLE" + ANSI_RESET + "> " + ANSI_INPUT + msg + ANSI_RESET + "> ");
        else if (style == 1)
            System.out.print("[J-CONSOLE> " + msg + "> ");
        else {
            String[] msgList = msg.split("/");
            if (msgList.length < (operatingSystem.contains("Windows") ? 4 : 3)) {
                System.out.print("[" + ANSI_INPUT + msg + ANSI_RESET + "> ");
            } else {
                System.out.print("[" + ANSI_INPUT + ".../" + msgList[msgList.length - 2] + "/" + msgList[msgList.length - 1] + ANSI_RESET + "> ");
            }
        }
    }

    /**
     * Prints a yellow [J-CONSOLE> in front of the message and the msg after it also
     * in yellow
     *
     * @param msg the message in yellow after the [Alarm>
     */
    public void printAlarm(String msg) {
        if (style == 0 || style == 2)
            System.out.print(ANSI_INPUT + "Alarm: " + ANSI_RESET + "> " + ANSI_INPUT + msg + ANSI_RESET + "> ");
        else if (style == 1)
            System.out.print("Alarm> " + msg + "> ");
        else {
            System.out.print(ANSI_INPUT + msg + ANSI_RESET + "> ");
        }
    }

    // -----------+

    /**
     * Copies the Config file to the target directory
     *
     * @param targetDirectory directory to copy the current config file to
     */
    public void copyConfigFile(Main main, String targetDirectory) {
        Properties prop;
        main.printDebug("copying the config file");
        try (FileOutputStream outputStream = new FileOutputStream(targetDirectory)) {
            prop = getPropertiesFile(main, "config.properties", false, true);
            prop.store(new FileOutputStream(targetDirectory),
                    "#'default' can be used for: defaultExDate, defaultSerialNumber, defaultValidity, defaultStDate");
            main.printInfo("successfully copied the config.properties file");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            main.printError("default config file couldn't be found");
            main.printError("copy failed");
            main.printDebug(ioe.getMessage());
        }
    }

    /**
     * Calls all functions needed when the "changeStyle" command is executed
     */
    private void callChangeStyle(Main main) {
        main.printDebug("changing the style");
        main.printDebug("Old style: " + style);
        if (styleToggle) {
            toggleStyle();
        } else if (cStyle == null || cStyle.equals("default") || cStyle.equals("d")) {
            main.printDebug("new style: " + cStyle + "/" + (style = 0));
        } else if (cStyle.equals("non-colored") || cStyle.equals("nc")) {
            main.printDebug("new style: " + cStyle + "/" + (style = 1));
        } else if (cStyle.equals("one-colored") || cStyle.equals("oc")) {
            main.printDebug("new style: " + cStyle + "/" + (style = 2));
        } else if (cStyle.equals("one-lettered") || cStyle.equals("ol")) {
            main.printDebug("new style: " + cStyle + "/" + (style = 3));
        } else if (cStyle.equals("simple") || cStyle.equals("s")) {
            main.printDebug("new style: " + cStyle + "/" + (style = 4));
        } else {
            printError("the style " + cStyle + " doesn't exist");
            printDebug("the style entered was " + cStyle);
        }

        setMessageColor();
    }

    /**
     * Toggles the style by adding the style variable by one and resetting it to 0
     * if it reches 5
     */
    private void toggleStyle() {
        style = (style + 1) % 5;
    }

    /**
     * Sets the message color of all messages to yellow if the style equals 2
     * Otherwise it sets it back to the default colors
     */
    private void setMessageColor() {
        if (style == 2) {
            ANSI_ERROR = ANSI_INPUT;
            ANSI_HELP = ANSI_INPUT;
            ANSI_OUTPUT = ANSI_INPUT;
            ANSI_DEBUG = ANSI_INPUT;
        } else {
            ANSI_ERROR = "\u001B[91m";
            ANSI_HELP = "\u001B[92m";
            ANSI_OUTPUT = "\u001B[94m";
            ANSI_DEBUG = "\u001B[36m";
        }
    }

    /**
     * Calls all functions needed when the "decodeDocument" command is executed
     *
     * @param main main class object (needed for the called functions and for
     *             printing to the console)
     */
    private void callDecodeDocument(Main main) {
        docDirectory = docDirectory != null ? docDirectory : main.pFile;
        if (fileName == null || certFileName == null)
            printError(
                    "you have to enter a file name with the argument --file <filename> and a certificate file name with the argument --certFile <file of the certificate>");
        else
            new TextEncodingDecoding().main(main.pFile, fileName, certFileName, docDirectory, 1, main);
    }

    /**
     * Calls all functions needed when the "encodeDocument" command is executed
     *
     * @param main main class object (needed for the called functions and for
     *             printing to the console)
     */
    private void callEncodeDocument(Main main) {
        certDirectory = certDirectory != null ? certDirectory : main.pFile;
        docDirectory = docDirectory != null ? docDirectory : main.pFile;
        if (fileName == null || certFileName == null)
            main.printError(
                    "you have to enter a file name with the argument --file <filename> and a certificate file name with the argument --certFile <file of the certificate>");
        else
            new TextEncodingDecoding().main(certDirectory, fileName, certFileName, docDirectory, 0, main);
    }

    /**
     * Calls all functions needed when the "readCertificate" command is executed
     *
     * @param main main class object (needed for the called functions and for
     *             printing to the console)
     */
    private void callReadCertificate(Main main) {
        if (fileName == null)
            main.printError("you have to enter a file name with the argument --file <filename>");
        else {
            try {
                new EditCertificate()
                        .printCertDataToConsole(new EditCertificate().read(main.pFile + "/" + fileName, main), main);
            } catch (IOException e) {
                printError("Couldn't find the certificate to read");
                main.printDebug(e.getMessage());
            } catch (CertificateException e) {
                printError("Couldn't read the certificate");
                main.printDebug(e.getMessage());
            }
        }
    }

    /**
     * /** Calls all functions needed when the "decodeDocument" command is executed
     *
     * @param main         main class object (needed for the called functions and
     *                     for printing to the console)
     * @param dIssuerName  default value (needed to call functions)
     * @param dSubjectName default value (needed to call functions)
     * @param dStDate      default value (needed to call functions)
     * @param dExDate      default value (needed to call functions)
     * @param dKeyS        default value (needed to call functions)
     * @param dSerNumber   default value (needed to call functions)
     * @param dSignAlg     default value (needed to call functions)
     */
    private void callWriteCertificate(Main main, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate,
                                      int dKeyS, long dSerNumber, String dSignAlg) {
        if (fileName == null)
            main.printError("you have to enter a file name with the argument --file <filename>");
        else {
            List<Date> dates = prepareCertificateWriterVariable(dIssuerName, dSubjectName, dStDate, dExDate, dKeyS,
                    dSerNumber, dSignAlg);
            Date stDate = dates.get(0);
            Date exDate = dates.get(1);

            printInfo("generating key pair");
            KeyPair keyPair;

            try {
                keyPair = generateKeyPair(keys);

                String pathFile = certTargetDirectory != null ? certTargetDirectory : main.pFile;

                try {
                    new EditCertificate().write(pathFile + "/" + fileName, main.pFile + "/" + fileName, "CN = " + iName,
                            "CN = " + sName, keyPair, serNumber, stDate, exDate, signAlg, false, main);
                } catch (CertificateEncodingException e) {
                    printError("Couldn't encode the certificate");
                    main.printDebug(e.getMessage());
                } catch (SignatureException e) {
                    printError("Couldn't sign the certificate");
                    main.printDebug(e.getMessage());
                } catch (InvalidKeyException e) {
                    printError("The generated key isn't valid");
                    main.printDebug(e.getMessage());
                } catch (IOException e) {
                    printError("Couldn't write the certificate");
                    main.printDebug(e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    printError("The entered algorithm is wrong");
                    main.printDebug(e.getMessage());
                }
            } catch (Exception e) {
                main.printDebug(e.getMessage());
            }
            if (bRead) {
                callReadCertificate(main);
            }
        }
    }

    /**
     * Prepares all values needed to start the EditDocument.write() function
     *
     * @param dIssuerName  default value if the parameter wasn't set via command
     *                     parameter
     * @param dSubjectName default value if the parameter wasn't set via command
     *                     parameter
     * @param dStDate      default value if the parameter wasn't set via command
     *                     parameter
     * @param dExDate      default value if the parameter wasn't set via command
     *                     parameter
     * @param dKeySize     default value if the parameter wasn't set via command
     *                     parameter
     * @param dSerNumber   default value if the parameter wasn't set via command
     *                     parameter
     * @param dSignAlg     default value if the parameter wasn't set via command
     *                     parameter
     * @return A list of that contains the start and expiry date (since these aren't
     * public variables)
     */
    private List<Date> prepareCertificateWriterVariable(String dIssuerName, String dSubjectName, Date dStDate,
                                                        Date dExDate, int dKeySize, long dSerNumber, String dSignAlg) {
        List<Date> outputList = new ArrayList<>();

        printInfo("checking inputs");
        iName = iName != null ? iName : dIssuerName;
        sName = sName != null ? sName : dSubjectName;
        outputList.add(0, sDate != null ? stringToDate(sDate) : dStDate);
        outputList.add(1, eDate != null ? stringToDate(eDate) : dExDate);
        keys = keys > 512 ? keys : dKeySize;
        serNumber = serNumber > 1 ? serNumber : dSerNumber;
        signAlg = signAlg != null ? signAlg : dSignAlg;

        return outputList;
    }

    /**
     * Generates a key pair of the key size given
     *
     * @param KeySize key size that the keys in the key pair will have (in bits)
     * @return the generated key pair
     * @throws Exception if the key pair couldn't be generated (probably because of
     *                   a key that's smaller than 512)
     */
    private KeyPair generateKeyPair(int KeySize) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(KeySize);
        return keyGen.generateKeyPair();
    }

    /**
     * Calls all functions needed when the "readDocument" command is executed
     *
     * @param main main class object (needed for the called functions and for
     *             printing to the console)
     */
    private void callReadDocument(Main main) {
        if (fileName == null)
            main.printError("you have to enter a file name with the argument --file <filename>");
        else
            new EditDocument().read(fileName, main.pFile, main);
    }

    /**
     * Calls all functions needed when the "writeDocument" command is executed
     *
     * @param main main class object (needed for the called functions and for
     *             printing to the console)
     */
    private void callWriteDocument(Main main) {
        if (fileName == null)
            main.printError("you have to enter a file name with the argument --file <filename>");
        else {
            new EditDocument().write(fileName, main.pFile, main, replace);
        }
    }

    /**
     * The function stringToDate() converts a String (i) into a date value.
     * Important for that is, that the String got the format DD-MM-YYYY. If that
     * isn't the case it will close the program with System.exit(). If it works it
     * will return the date in "Date" format.
     *
     * @param i string in DD-MM-YYYY format
     * @return date in Date format as output
     */
    public Date stringToDate(String i) {
        int d = 0, y = 0, m = 0;
        try {
            String[] sa = i.split("");
            d = (Integer.parseInt(sa[0])) * 10 + (Integer.parseInt(sa[1]));
            m = (Integer.parseInt(sa[3])) * 10 + (Integer.parseInt(sa[4]));
            y = (Integer.parseInt(sa[6])) * 1000 + (Integer.parseInt(sa[7])) * 100 + (Integer.parseInt(sa[8])) * 10
                    + (Integer.parseInt(sa[9]));
        } catch (Exception e) {
            // out.println(e);
            System.out.println("No valid date");
            System.exit(1);
        }
        return new GregorianCalendar(y, m - 1, d).getTime();
    }

    /**
     * Prints out the help for the command.
     *
     * @param x tells the function the help of what command it should print<br>
     *          (0 = writeCertificate, 1 = readCertificate, 2 = exit, 3 =
     *          readDocument, 4 = cd, 5 = writeDocument, 6 = general help, 7 =
     *          encodeDocument, 8 = decodeDocument, 9 = setConfig)
     */
    private void printHelpToConsole(int x) {
        if (x == 0) {
            printHelp("writeCertificate | wc\t\t[generates a certificate]");
            printHelp("\t\t--issuerName\t\t<CA-name>");
            printHelp("\t\t--subjectName\t\t<owner-name>");
            printHelp("\t\t--startDate\t\t<start date of the certificate>");
            printHelp("\t\t--expiryDate\t\t<expiry date of the certificate>");
            printHelp("\t\t--keySize\t\t<size of the public key in bits>");
            printHelp("\t\t--serialNumber\t\t<serial number of the certificate>");
            printHelp("\t\t--signatureAlgorithm\t<signature algorithm>");
            printHelp("\t\t--file\t\t\t<name of the generated certificate>");
            printHelp("\t\t--read\t\t\t[enables read]");
            printHelp("\t\t--certTargetDir\t\t<the target directory of the certificate>");
        } else if (x == 1) {
            printHelp("readCertificate | rc \t\t[reads a certificate]");
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 2) {
            printHelp("exit\t\t\t\t[exits the console]");
        } else if (x == 3) {
            if (style < 3) {
                printHelp("readDocument | rd\t\t[reads a *.txt file with your file name]");
            } else {
                printHelp("readDocument | rd\t\t\t[reads a *.txt file with your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 4) {
            printHelp("changeDirectory | cd\t\t[gives you the option to select another directory]");
        } else if (x == 5) {
            if (style < 3) {
                printHelp("writeDocument | wd\t\t[writes a *.txt file to you file name]");
            } else {
                printHelp("writeDocument | wd\t\t\t[writes a *.txt file to you file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to write>");
            printHelp("\t\t--replace\t\t[replace the existing text]");
        } else if (x == 6) {
            printHelp("writeCertificate | wc\t\t[generates a certificate]");
            printHelp("readCertificate | rc\t\t[reads a certificate]");
            if (style < 3) {
                printHelp("writeDocument | wd\t\t[writes a *.txt file to you file name]");
                printHelp("readDocument | rd\t\t[reads a *.txt file with your file name]");
                printHelp("encodeDocument | ed\t\t[encodes a *.txt file at your file name]");
                printHelp("decodeDocument | dd\t\t[decodes a *.txt file at your file name]");
            } else {
                printHelp("writeDocument | wd\t\t\t[writes a *.txt file to you file name]");
                printHelp("readDocument | rd\t\t\t[reads a *.txt file with your file name]");
                printHelp("encodeDocument | ed\t\t\t[encodes a *.txt file at your file name]");
                printHelp("decodeDocument | dd\t\t\t[decodes a *.txt file at your file name]");
            }
            printHelp("changeDirectory | cd\t\t[gives you the option to select another directory]");
            printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
            if (permissionLevel > 0) {
                printHelp("note\t\t\t\t\t\t[makes a note in your users note file]");
                printHelp("searchNote\t\t\t\t[searches in the notes]");
            }
            if (permissionLevel > 1) {

            }
            printHelp("exit\t\t\t\t[exits the console]");
            printHelp("");
            printHelp("Use <command> -h | --help to get further information about the command and the parameters you can apply to it");
        } else if (x == 7) {
            if (style < 3) {
                printHelp("encodeDocument | ed\t\t[encodes a *.txt file at your file name]");
            } else {
                printHelp("encodeDocument | ed\t\t\t[encodes a *.txt file at your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to encode>");
            printHelp(
                    "\t\t--certFile\t\t<name of the certificate file (The name you entered for the writeCertificate command)>");
            printHelp("\t\t--certDirectory\t\t<directory of the certificate>");
            printHelp("\t\t--docDirectory\t\t<directory of the document>");
        } else if (x == 8) {
            if (style < 3) {
                printHelp("decodeDocument | dd\t\t[decodes a *.txt file at your file name]");
            } else {
                printHelp("decodeDocument | dd\t\t\t[decodes a *.txt file at your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to decode>");
            printHelp(
                    "\t\t--certFile\t\t<name of the certificate file (The name you entered for the writeCertificate command)>");
            printHelp("\t\t--docDirectory\t\t<directory of the document>");
        } else if (x == 9) {
            if (permissionLevel == 0) {
                printError("You need to be logged in to execute this command");
            } else {
                printHelp("note\t\t\t\t\t\t[makes a note in your users note file]");
                printHelp("\t\t-r | --registration\t[creates a registration note]");
            }
            printHelp("note\t\t\t\t\t[makes a note in your user note file]");
        } else if (x == 10) {
            printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
            printHelp("\t\t--toggle\t\t[toggles between the different styles]");
            printHelp("\t\t--style\t\t\t<the style you want to select>");
            printHelp("\t\tavailable styles:");
            printHelp("\t\t\tdefault | d");
            printHelp("\t\t\tnon-colored | nc");
            printHelp("\t\t\tone-colored | oc");
            printHelp("\t\t\tone-lettered | ol");
            printHelp("\t\t\tsimple | s");
        } else if (x == 11) {
            printHelp("searchNote\t\t\t\t[searches in the notes]");
        }
    }

    /**
     * Tests if the input is "default"
     *
     * @param propertiesFileInput input
     * @param milSecValid         time (in mil secs) the Date should be behind the
     *                            time right now
     * @return if input = "default" -> the date now + milSecValid (in ms) <br>
     * if input != "default" -> the input
     */
    private Date setDefaultPropertiesDates(String propertiesFileInput, long milSecValid) {
        Date timeNow = new Date();
        if (propertiesFileInput.equals("default")) {
            timeNow.setTime(timeNow.getTime() + milSecValid);
            return timeNow;
        } else
            return stringToDate(propertiesFileInput);
    }

    /**
     * Gets all properties from the properties file and stores and returns them in a
     * List
     *
     * @param properties the properties file to take the properties from
     * @return a List with all properties file data needed in the later called
     * functions
     */
    private List<String> getPropertiesData(Properties properties) {
        List<String> output = new ArrayList<>();

        output.add(0, properties.getProperty("defaultIssuerName", "ca_name"));
        output.add(1, properties.getProperty("defaultSubjectName", "owner_name"));
        output.add(2, properties.getProperty("defaultKeySize", "4096"));
        output.add(3, properties.getProperty("defaultSerialNumber", "default"));
        output.add(4, properties.getProperty("defaultStDate", "default"));
        output.add(5, properties.getProperty("defaultExDate", "default"));
        output.add(6, properties.getProperty("defaultValidity", "default"));
        output.add(7, properties.getProperty("defaultSignatureAlgorithm", "SHA256withRSA"));

        return output;
    }

    /**
     * Calls the current config file set
     *
     * @param main the name of the main class
     * @return properties of the properties file (new Properties() if the file
     * couldn't be found)
     */
    private Properties callGetPropertiesFile(Main main) {
        try {
            return getPropertiesFile(main, main.appDataDirectory + "/" + main.configFileName, true, false);
        } catch (IOException e) {
            main.printError("default config file couldn't be founddefault config file couldn't be found");
            main.printInfo("using default configs");
            return new Properties();
        }
    }

    /**
     * Calls the printHelpToConsole function with the right parameter if the "help"
     * command or the -h / --help parameters gets executed
     *
     * @param main main function (needed to get the exit variable
     */
    private void callHelp(Main main) {
        if (gHelp) {
            printHelpToConsole(6);
        }
        if (readD) {
            printHelpToConsole(3);
        }
        if (writeD) {
            printHelpToConsole(5);
        }
        if (readC) {
            printHelpToConsole(1);
        }
        if (writeC) {
            printHelpToConsole(0);
        }
        if (et) {
            printHelpToConsole(7);
        }
        if (dt) {
            printHelpToConsole(8);
        }
        if (cd) {
            printHelpToConsole(4);
            main.cd = false;
        }
        if (cs) {
            printHelpToConsole(10);
        }
        if (main.exit) {
            printHelpToConsole(2);
            main.exit = false;
        }
    }

    /**
     * Calls the needed call* functions if a command gets executed
     *
     * @param main         main function (needed for the following functions)
     * @param dIssuerName  default value (needed for the following functions)
     * @param dSubjectName default value (needed for the following functions)
     * @param dStDate      default value (needed for the following functions)
     * @param dExDate      default value (needed for the following functions)
     * @param dKeys        default value (needed for the following functions)
     * @param dSerNumber   default value (needed for the following functions)
     * @param dSignAlg     default value (needed for the following functions)
     */
    private void callCommands(Main main, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate, int dKeys,
                              long dSerNumber, String dSignAlg) {
        if (readD)
            callReadDocument(main);
        else if (writeD)
            callWriteDocument(main);
        else if (writeC)
            callWriteCertificate(main, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dSignAlg);
        else if (readC)
            callReadCertificate(main);
        else if (et)
            callEncodeDocument(main);
        else if (dt)
            callDecodeDocument(main);
        else if (note)
            callMakeNote(main);
        else if (searchNote)
            callSearchNote(main);
        else if (alarm)
            callAlarm(main);
        else if (toggleDebug)
            toggleDebug(main);
        else if (logout)
            logout(main);
    }

    private void logout(Main main) {
        main.printInfo("logging out...");
        main.printInfo("bye " + username);
        main.username = null;
        main.noteDocument = null;
        main.permissionLevel = 0;
    }

    private void exitConsole(Main main) {
        main.printInfo("exiting");
        System.exit(1);
    }

    private void callMakeNote(Main main) {
        if (main.permissionLevel == 0)
            printError("You need to be logged in to execute this command");
        else {
            Scanner sc = new Scanner(System.in);
            if (registration) {
                String[] registrationData = new String[4];
                main.printEditor("Enter the platform name");
                registrationData[0] = sc.nextLine();
                main.printEditor("Enter your nickname");
                registrationData[1] = sc.nextLine();
                main.printEditor("Enter the used e-mail address");
                registrationData[2] = sc.nextLine();
                main.printEditor("Enter the password");
                registrationData[3] = sc.nextLine();
                if (!main.noteDocument.makeRegistrationNote(registrationData[0], registrationData[1], registrationData[2], registrationData[3]))
                    main.printError("couldn't create your note");
            } else {
                main.printEditor("Enter your note");
                String input;
                while (!(input = sc.nextLine()).equals("")) {
                    if (!main.noteDocument.makeNote(input)) {
                        main.printError("couldn't search your note");
                        break;
                    } else
                        printEditor("Enter your note");
                }
            }
        }
    }

    private void callSearchNote(Main main) {
        if (main.permissionLevel == 0)
            printError("You need to be logged in to execute this command");
        else {
            printEditor("Enter your keyword");
            Scanner sc = new Scanner(System.in);
            String input;
            while (!(input = sc.nextLine()).equals("")) {
                main.noteDocument.searchNote(input);
                printEditor("Enter your keyword");
            }
        }
    }

    private void callAlarm(Main main) {
        if (permissionLevel == 0) {
            printError("You need to be logged in to execute this command");
        } else {
            if (alarmTime == null && relativeTime == null) {
                main.printError("You have to enter an alarm ");
            } else {
                try {
                    int firstFreeSpot;
                    main.printDebug("The first free spot found in the Array was spot " + (firstFreeSpot = findFirstEmptySpotOfArray(alarmArray)));
                    if (relativeTime != null)
                        alarmArray[firstFreeSpot] = new Thread(new Alarm(main, stringToTimeArray(relativeTime, null, true), alarmMessage));
                    else
                        alarmArray[firstFreeSpot] = new Thread(new Alarm(main, stringToTimeArray(alarmTime, alarmDate, false), alarmMessage));
                    alarmArray[firstFreeSpot].start();
                } catch (IOException ioe) {
                    main.printError(ioe.getMessage());
                }
            }
        }
    }

    private void toggleDebug(Main main) {
        if (permissionLevel > 1)
            debug = !debug;
        else
            main.printError("You don't have permissions for that");
    }

    public int[] stringToTimeArray(String time, String date, boolean relativeTime) throws IOException {
        int[] timeOutput = new int[6];
        String[] timeArray = time.contains("-") ? time.split("-") : time.split(":");
        if (!relativeTime) {
            if (date == null) {
                timeOutput[0] = 0;
                timeOutput[1] = 0;
                timeOutput[2] = 0;
            } else {
                String[] dateArray = date.contains("-") ? date.split("-") : date.split(":");
                if (dateArray[0].length() == 4) {
                    timeOutput[0] = Integer.parseInt(dateArray[0]);
                    timeOutput[1] = Integer.parseInt(dateArray[1]);
                    timeOutput[2] = Integer.parseInt(dateArray[2]);
                } else if (dateArray[2].length() == 4) {
                    timeOutput[0] = Integer.parseInt(dateArray[2]);
                    timeOutput[1] = Integer.parseInt(dateArray[1]);
                    timeOutput[2] = Integer.parseInt(dateArray[0]);
                } else {
                    throw new IOException("Invalid date format. Use: [DD-MM-YYYY] or [YYYY-MM-DD]");
                }
            }
        } else
            timeOutput[0] = -1;
        if (timeArray.length == 1) {
            timeOutput[4] = Integer.parseInt(timeArray[0]);
        } else {
            timeOutput[3] = Integer.parseInt(timeArray[0]);
            timeOutput[4] = Integer.parseInt(timeArray[1]);
        }
        return timeOutput;
    }

    public int findFirstEmptySpotOfArray(Object[] inputArray) throws IOException {
        for (int i = 0; i < inputArray.length; i++) {
            if (inputArray[i] == null) {
                return i;
            }
        }
        throw new IOException("You have already have 5 alarms open (use alarm --show to show them)");
    }
}

//	TODO - do this stuff dude
//	
//	public List<String> getSubInput(String message, Main main, int mode, boolean freeLineallowed) {
//		int c = 1, exit = 0;
//		List<String> output = new ArrayList<>();
//		Scanner sc = new Scanner(System.in);
//        String in;
//		while ((freeLineallowed ? 2 : 1) > exit) {
//            main.printEditorInput(c + 1, 1);
//            in = sc.nextLine();
//            if (in.equals("") || in.split(" ").length == 0)
//                exit++;
//            else {
//                if (exit == 1) {
//                    exit = 0;
//                    output.add("");
//                }
//                bw.write(in);
//                bw.newLine();
//            }
//            c++;
//        }
//	}