package de.billmueller.certutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VisualCalculator {
    private Main main;
    private Calculator calc;

    public VisualCalculator(Main main) {
        this.main = main;
        calc = new Calculator(main);
    }

    public void showCalculator() {
        boolean exitCalculator = false;
        int lineCounter = 0;
        List<Double> result = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        String input;
        main.printDebug("Starting calculator");
        while (!exitCalculator) {
            main.printCalculator(lineCounter);
            input = sc.nextLine();
            main.printDebug(input);
            if (input != null && !input.equals("")) {
                if (input.toLowerCase().equals("exit")) {
                    exitCalculator = true;
                } else {
                    main.printDebug(input);
                    result.add(lineCounter, calc.calculate(input));
                    main.printSolution(result.get(lineCounter));
                    lineCounter++;
                }
            }
        }
        main.printInfo("Exiting calculator...");
    }
}
