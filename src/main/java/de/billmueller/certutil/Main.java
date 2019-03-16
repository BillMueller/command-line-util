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

	public int style = 0;
	public boolean debug = false;
	private String pFile;
	private String configFile = "default";
	private String permissionFile;
	private String username;
	public String usersFile = "default";

	@Parameter(names = { "setConfig", "sc" }, description = "sets the used config file to a new directory")
	private boolean setConfig;
	@Parameter(names = { "writeCertificate", "wc" }, description = "generate a new certificate")
	private boolean writeC;
	@Parameter(names = { "writeDocument", "wd" }, description = "changes to J-Console")
	private boolean writeD;
	@Parameter(names = { "readCertificate", "rc" }, description = "read a certificate")
	private boolean readC;
	@Parameter(names = { "readDocument", "rd" }, description = "reads a file")
	private boolean readD;
	@Parameter(names = { "changeDirectory", "cd" }, description = "changes working directory")
	private boolean cd;
	@Parameter(names = "help", description = "prints out a general help")
	private boolean gHelp;
	@Parameter(names = "exit", description = "exits J-Console/J-Editor")
	private boolean exit;
	@Parameter(names = { "encodeDocument", "ed" }, description = "encodes a text file with a certificate")
	private boolean et;
	@Parameter(names = { "decodeDocument", "dd" }, description = "decodes a text file with a private key")
	private boolean dt;
	@Parameter(names = { "changeStyle", "cs" }, description = "changes between colored and non-colored mode")
	private boolean cs;
	@Parameter(names = "note", description = "makes a note (if you have permission)")
	private boolean note;
	@Parameter(names = { "searchNote", "sn" }, description = "makes a note (if you have permission)")
	private boolean searchNote;
	@Parameter(names = "alarm", description = "creates an alarm for the time entered")
	private boolean alarm;
	@Parameter(names = { "toggleDebug", "debug", "tD" }, description = "changes between debug and normal mode")
	private boolean toggleDebug;

	@Parameter(names = { "--issuerName", "--iName" }, description = "eneter the ca name")
	private String iName;
	@Parameter(names = { "--subjectName", "--sName" }, description = "enter the owner name")
	private String sName;
	@Parameter(names = { "--startDate", "--sDate" }, description = "startdate for the certificate to be valid")
	private String sDate;
	@Parameter(names = { "--expiryDate", "--eDate" }, description = "expirydate for the certificate to be valid")
	private String eDate;
	@Parameter(names = "--keySize", description = "keySize of the public key (in bits)")
	private int keys;
	@Parameter(names = { "--serialNumber", "--serNumb" }, description = "set a serial number")
	private long serNumber;
	@Parameter(names = "--file", description = "set the certificate name")
	private String fileName;
	@Parameter(names = { "--certificateFile", "--certFile" }, description = "set the certificate name")
	private String certFileName;
	@Parameter(names = { "--signatureAlgorithm", "signAlg" }, description = "set signature algorithm")
	private String signAlg;
	@Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
	private boolean bRead;
	@Parameter(names = { "--help", "-h" }, description = "prints out a help for the command entered before")
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
	@Parameter(names = "--time")
	private int time;

	/**
	 * Main function with the J-console input functionality <br>
	 * When called first it shows [J-CONSOLE> Enter directory> <br>
	 * After entering the working directory it will show [J-CONSOLE>
	 * C:/.../exampleFolder> when it's read for an input <br>
	 * After getting the input it will use JController to handle the input and it
	 * will call run() function to call the needed functions
	 */
	public void main() {
		Main main = new Main();
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
					main.printDebug("Starting change Directoy loop");
					main.printEditor("Enter directory");
					in = sc.nextLine();
					if (in.split("/")[0].length() == 2) {
						main.printDebug("The directory passed the easy validity test...");
						main.pFile = in;
						start = true;
					} else if (!in.equals("exit") && !in.equals("login")) {
						main.printDebug("The length of the first thing before the / is not 2 (it is "
								+ in.split("/")[0].length() + ")");
						main.printError("the path file you entered is not valid.");
						main.printInfo("Use '/' for example C:/Users");
					} else if (in.equals("exit")) {
						exitConsole(main);
					} else {
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
						main.permissionFile = user.getPermissionFile();
						start = true;
						main.pFile = user.getDefaultDirectory();
						main.style = user.getDefaultStyle();
						main.printInfo("Welcome " + (main.username = user.getName()));
						main.printDebug("Permissions File " + main.permissionFile);
						main.printDebug("Default Directory " + main.pFile);
						main.printDebug("Default Style: " + main.style);
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
					String defaultConfigFileName = "config.properties";
					Properties dProps = callGetPropertiesFile(main, defaultConfigFileName);

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
	 *         can get the value for the property
	 */
	public Properties getPropertiesFile(Main main, String configFileName, boolean printMsg, boolean defaultPath)
			throws IOException {
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
				main.printDebug("IOExeption:");
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
		setConfig = false;
		note = false;
		searchNote = false;
		alarm = false;
		toggleDebug = false;
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
		time = 0;
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
	private void printEditor(String msg) {
		if (style == 0 || style == 2)
			System.out.print("[" + ANSI_INPUT + "J-CONSOLE" + ANSI_RESET + "> " + ANSI_INPUT + msg + ANSI_RESET + "> ");
		else if (style == 1)
			System.out.print("[J-CONSOLE> " + msg + "> ");
		else {
			String[] msgList = msg.split("/");
			if (msgList.length < 3) {
				System.out.print("[" + ANSI_INPUT + msg + ANSI_RESET + "> ");
			} else {
				System.out.print("[" + ANSI_INPUT + ".../" + msgList[msgList.length - 1] + ANSI_RESET + "> ");
			}
		}
	}

	// -----------+

	/**
	 * calls the functions needed when the "setConfig" command is executed
	 */
	private void callSetConfig(Main main) {
		if (directoryName == null)
			printError(
					"you have to enter a directory path where your want the new config file to be with the argument --directory <filename>");
		else {
			setConfig(main, directoryName + "/config.properties");
			if (copyConfig) {
				copyConfigFile(main, directoryName + "/config.properties");
			}
		}
	}

	/**
	 * Sets the default config file to the target directory
	 *
	 * @param targetDirectory target directory to set the config File to
	 */
	private void setConfig(Main main, String targetDirectory) {
		main.printDebug("setting new config file");
		main.printDebug("old config file: " + configFile);
		configFile = targetDirectory;
		main.printDebug("new config file: " + configFile);
	}

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
			// TODO bugfix pls
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
			} catch (CertificateException e) {
				printError("Couldn't read the certificate");
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
				} catch (SignatureException e) {
					printError("Couldn't sign the certificate");
				} catch (InvalidKeyException e) {
					printError("The generated key isn't valid");
				} catch (IOException e) {
					printError("Couldn't write the certificate");
				} catch (NoSuchAlgorithmException e) {
					printError("The entered algorithm is wrong");
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	 *         public variables)
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
			printHelp("setConfig | sc \t\t\t[changes the position of the config file]");
			printHelp("changeDirectory | cd\t\t[gives you the option to select another directory]");
			printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
			printHelp("exit\t\t\t\t[exits the console]");
			printHelp("");
			printHelp(
					"Use <command> -h | --help to get further information about the command and the parameters you can apply to it");
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
			printHelp("setConfig | sc\t\t\t[changes the position of the config file]");
			printHelp("\t\t--directory\t\t<name of the new directory of the config.properties file>");
			printHelp("\t\t--copyConfig\t\t[copies the default config file to your selected location]");
		} else if (x == 10) {
			printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
			printHelp("\t\t--toggle\t\t[toggles between the different styles");
			printHelp("\t\t--style\t\t\t<the style you want to select");
			printHelp("\t\tavailable styles:");
			printHelp("\t\t\tdefault | d");
			printHelp("\t\t\tnon-colored | nc");
			printHelp("\t\t\tone-colored | oc");
			printHelp("\t\t\tone-lettered | ol");
			printHelp("\t\t\tsimple | s");
		}
	}

	/**
	 * Tests if the input is "default"
	 *
	 * @param propertiesFileInput input
	 * @param milSecValid         time (in mil secs) the Date should be behind the
	 *                            time right now
	 * @return if input = "default" -> the date now + milSecValid (in ms) <br>
	 *         if input != "default" -> the input
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
	 *         functions
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
	 * @param defaultConfigFileName the name of the config file (by default
	 *                              config.properties)
	 * @return properties of the properties file (new Properties() if the file
	 *         couldn't be found)
	 */
	private Properties callGetPropertiesFile(Main main, String defaultConfigFileName) {
		if (!configFile.equals("default")) {
			try {
				return getPropertiesFile(main, configFile, true, false);
			} catch (IOException ioe) {
				main.printError("no file could be found at " + defaultConfigFileName);
				main.printInfo("the name of the config file must be config.properties");
				main.printInfo("using default config.properties file");
				return new Properties();
			}
		} else {
			try {
				return getPropertiesFile(main, defaultConfigFileName, true, true);
			} catch (IOException e) {
				main.printError("default config file couldn't be found");
				main.printInfo("using default configs");
				return new Properties();
			}
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
		if (setConfig) {
			printHelpToConsole(9);
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
		else if (setConfig)
			callSetConfig(main);
		else if (note)
			callMakeNote(main);
		else if (searchNote)
			callSearchNote(main);
		else if (alarm)
			callAlarm(main);
		else if (toggleDebug)
			toggleDebug();
	}

	public void exitConsole(Main main) {
		main.printInfo("exiting");
		System.exit(1);
	}

	private void callMakeNote(Main main) {
		if (main.permissionFile == null)
			printError("You don't have permissions to execute that command");
		else {
			new EditDocument().write(main.username + "s_notes", main.permissionFile, main, false);
		}
	}

	private void callSearchNote(Main main) {
		if (main.permissionFile == null)
			printError("You don't have permissions to execute that command");
		else {
			String input = "something";
			while (!input.equals("")) {
				Scanner scanner = new Scanner(System.in);
				printEditor("Enter the keyword");
				input = scanner.nextLine();
				if (!input.equals("")) {
					main.printInfo("searching in file");
					try (LineNumberReader br = new LineNumberReader(
							new FileReader(main.permissionFile + "/" + main.username + "s_notes.txt"))) {
						String s;
						while ((s = br.readLine()) != null) {
							if (s.toLowerCase().contains(input.toLowerCase()))
								printDocumentData(s, br.getLineNumber(), 1);
						}

					} catch (FileNotFoundException fE) {
						main.printError("could not find notes file");
					} catch (IOException ioe) {
						main.printError("could not read file");
					}
				}
			}
		}
	}

	private void callAlarm(Main main) {
		new Thread(new Alarm(main)).start();
		;
	}

	private void toggleDebug() {
		debug = !debug;
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
}