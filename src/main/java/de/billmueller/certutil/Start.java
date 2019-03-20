package de.billmueller.certutil;

import java.io.IOException;

public class Start {
    public static void main(String[] args) {
        Main main = new Main();
//        main.main();
//		main.debug = true;
//		new Setup().start(main, true);
        main.printEditor("TEST", false);
        try {
            new Thread(new Alarm(new Main(), new Main().stringToIntArray("00:01", null, true), "placeholder message")).start();
        } catch (IOException ioe) {
            new Main().printError(ioe.getMessage());
        }
    }
}