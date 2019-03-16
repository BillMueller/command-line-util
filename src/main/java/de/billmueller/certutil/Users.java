package de.billmueller.certutil;

import java.io.*;

public class Users {
	private String name, password, defaultDirectory, permissionFile;
	private int defaultStyle;

	public boolean setUser(Main main, String userName) {
		Reader r;
		try {
			if (main.usersFile.equals("default")) {
				r = new LineNumberReader(
						new InputStreamReader(getClass().getClassLoader().getResourceAsStream("users.txt")));
			} else {
				r = new LineNumberReader(new FileReader(new File(main.usersFile)));
			}
			try (LineNumberReader rdr = new LineNumberReader(r)) {
				String currentLine;
				while ((currentLine = rdr.readLine()) != null) {
					if ((rdr.getLineNumber() % 5) == 1) {
						if (currentLine.equals(userName)) {
							name = userName;
							password = rdr.readLine();
							defaultDirectory = rdr.readLine();
							defaultStyle = Integer.parseInt(rdr.readLine());
							permissionFile = (currentLine = rdr.readLine()).equals("default") ? null : currentLine;
						}
					}
				}
				return name == null;
			} catch (Exception e) {
				main.printError("users.txt file is missing");
				e.printStackTrace();
				return true;
			}
		} catch (Exception e) {
			main.printError("the users.txt file you've set couldn't be found");
			e.printStackTrace();
			return true;
		}
	}

	public boolean createUser(Main main, String userName, String password, String defaultDirectory, int defaultStyle,
			String permissionFile) {
		if (main.usersFile.equals("default")) {
			main.printError("you need to change the users File to add a new user to it");
		} else if (!setUser(main, userName)) {
			main.printError("the Username you've entered already exists");
			main.printInfo("you can for example use following names:");
			int c = 0;
			int extra = 0;
			while (c < 5) {
				extra = (int) Math.random() * 100;
				if (setUser(main, "\t" + userName + extra)) {
					main.printInfo("\t" + userName + extra);
					c++;
				}
			}
		} else {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(main.usersFile), true))) {
				bw.write(userName);
				bw.newLine();
				bw.write(password);
				bw.newLine();
				bw.write(userName);
				bw.newLine();
				bw.write(defaultDirectory);
				bw.newLine();
				bw.write(String.valueOf(defaultStyle));
				bw.newLine();
				bw.write(permissionFile);
				bw.flush();
			} catch (Exception e) {
				main.printError("the users file you've set couldn't be found");
			}
		}
		return false;
	}

	public void setUsersFile(String File) {
	}

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

	public String getPermissionFile() {
		return permissionFile;
	}
}