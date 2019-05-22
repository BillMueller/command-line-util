package de.billmueller.certutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Start {
    public static void main(String[] args) {
//        Main main = new Main();
//        main.main();
//		main.debug = true;
//		new Setup().start(main, true);

        if (args.length == 0) {
            Main main = new Main();
            main.main(false);
        } else if (args[0].equals("calculator")) {
            System.out.println("opening calculator");
            Main main = new Main();
            main.main(true);
        } else {
            new Main().printError("Argument " + args[0] + "isn't valid");
        }

//        Main main = new Main();
//        main.debug = true;
//        Calculator calc = new Calculator(main);
//        main.printInfo("Solution: " + calc.calculate("6*8+3/7"));

//        for(int i = 0; i <= 10000; i++){
//            System.out.println((char) i);
//        }

//        String x = "1+1+1";
//        Main main = new Main();
//        int position = 0;
//        while ((position = x.indexOf("+", position)) != -1){
//            if(Character.toString(x.charAt(position)).equals("+")){
//
//            }else if(Character.toString(x.charAt(position)).equals("-")){
//
//            }else{
//                main.printError("Character at position " + position + " is " + x.charAt(position) + " (expected: + or -)");
//            }
//        }
//        String str = "44";
//        if(str.matches("[0-9]+")) {
//            System.out.println("String contains only digits!");
//        }
    }
}