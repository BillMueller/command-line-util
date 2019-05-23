package de.billmueller.certutil;

public class Calculator {
    private Main main;

    public Calculator(Main main) {
        this.main = main;
    }

    public double calculate(String problem) {
        problem = problem.replace(" ", "");
        if (!problem.contains("(")) {
            return findOperator(problem);
        } else {

        }
        main.printError("The calculation contains not allowed Characters");
        return 0;
    }

    private double findOperator(String problem) {
        main.printDebug(problem);
        double returnValue = 0;
        if (problem.contains("+")) {
            return divideIntoSimpleCalculations(problem, 0);
        } else if (problem.contains("-")) {
            return divideIntoSimpleCalculations(problem, 1);
        } else if (problem.contains("*")&& !problem.contains("**")) {
            return divideIntoSimpleCalculations(problem, 2);
        } else if (problem.contains("/")) {
            return divideIntoSimpleCalculations(problem, 3);
        } else if (problem.contains("**")) {
            return divideIntoSimpleCalculations(problem, 4);
        } else {
            try {
                returnValue = Double.parseDouble(problem);
            } catch (Exception e) {
                main.printError("The entered calculation contains not allowed characters");
                main.printDebug(e.toString());
            }
        }
        return returnValue;
    }

    private double divideIntoSimpleCalculations(String problem, int operatorID) {
        String[] splitProblem = {};
        switch (operatorID) {
            case 0:
                splitProblem = problem.split("\\+");
                break;
            case 1:
                splitProblem = problem.split("-");
                break;
            case 2:
                splitProblem = problem.split("\\*");
                break;
            case 3:
                splitProblem = problem.split("/");
                break;
            case 4:
                splitProblem = problem.split("\\*\\*");
                break;
        }
        double returnValue = 0.0;
        boolean firstRun = true;
        for (String i : splitProblem) {
            main.printDebug(returnValue + ";" + findOperator(i) + ";" + operatorID + ";" + i);
            if(!firstRun) {
                returnValue = simpleCalculation(returnValue, findOperator(i), operatorID);
            }else{
                returnValue = findOperator(i);
                firstRun = false;
            }
        }
        return returnValue;
    }

    private double simpleCalculation(double firstNumber, double secondNumber, int operatorID) {
        switch (operatorID) {
            case 0:
                main.printDebug("calculating " + firstNumber + " + " + secondNumber);
                return firstNumber + secondNumber;
            case 1:
                main.printDebug("calculating " + firstNumber + " - " + secondNumber);
                return firstNumber - secondNumber;
            case 2:
                main.printDebug("calculating " + firstNumber + " * " + secondNumber);
                return firstNumber * secondNumber;
            case 3:
                main.printDebug("calculating " + firstNumber + " / " + secondNumber);
                return firstNumber / secondNumber;
            case 4:
                main.printDebug("calculating " + firstNumber + " ** " + secondNumber);
                return Math.pow(firstNumber, secondNumber);
        }
        main.printError("OperatorID overflow");
        main.printDebug("OperatorID is: " + operatorID + "expected number between 0 and 4");
        return 0;
    }

    /* >>>Second attempt<<<

    private List<Integer> getOperators(String problem, String operator) {
        List<Integer> operatorPositions = new ArrayList<>();
        int currentPosition = 0;
        while ((currentPosition = problem.indexOf(operator, currentPosition)) != -1) {
            operatorPositions.add(currentPosition);
            main.printDebug(Integer.toString(currentPosition));
            currentPosition++;
        }
        return operatorPositions;
    }

    private int simpleCalculation(String problem, List<Integer> firstOperatorPosition, List<Integer> secondOperatorPosition, int operatorId) {
        int firstOperatorAmount = firstOperatorPosition.size(), secondOperatorAmount = secondOperatorPosition.size();
        operatorId = operatorId * 2;
        int firstPos, secondPos;
        int totalOperatorAmount = firstOperatorAmount + secondOperatorAmount, firstOperatorCounter = 0, secondOperatorCounter = 0, returnValue = Integer.parseInt(problem.split("\\+|-", 2)[0]);
        for (int i = 0; i < totalOperatorAmount; i++) {
            firstPos = findPosition(firstOperatorCounter, firstOperatorAmount);
            secondPos = findPosition(secondOperatorCounter, secondOperatorAmount);
            main.printDebug((i + 1) + ". time! Values: firstOperatorCounter: " + firstOperatorCounter + ", firstOperatorAmount: " + firstOperatorAmount + ", secondOperatorCounter: " + secondOperatorCounter + ", secondOperatorAmount: " + secondOperatorAmount + ", firstPos: " + firstPos + ", secondPos: " + secondPos + ", storedValue: " + returnValue);
            if (firstPos == 3) {
                returnValue = calculateWithSecondOperator(returnValue, problem, secondPos, secondOperatorPosition, secondOperatorCounter, operatorId + SECOND_OPERATOR);
                secondOperatorCounter++;
            } else if (secondPos == 3) {
                returnValue = calculateWithSecondOperator(returnValue, problem, firstPos, firstOperatorPosition, firstOperatorCounter, operatorId + FIRST_OPERATOR);
                firstOperatorCounter++;
            } else if (secondOperatorPosition.get(secondOperatorCounter) < firstOperatorPosition.get(firstOperatorCounter)) {
                returnValue = calculateWithSecondOperator(returnValue, problem, secondPos, secondOperatorPosition, secondOperatorCounter, operatorId + SECOND_OPERATOR);
                secondOperatorCounter++;
            } else {
                returnValue = calculateWithSecondOperator(returnValue, problem, firstPos, firstOperatorPosition, firstOperatorCounter, operatorId + FIRST_OPERATOR);
                firstOperatorCounter++;
            }
        }
        return returnValue;
    }

    private int calculateWithSecondOperator(int returnValue, String problem, int pos, List<Integer> operatorPosition, int operatorCounter, int operatorId) {
        switch (pos) {
            case 1:
                System.out.println(operatorPosition.get(operatorCounter));
                System.out.println(operatorPosition.get(operatorCounter+1));
                System.out.println(problem.substring(operatorPosition.get(operatorCounter), operatorPosition.get(operatorCounter+1)));
                return calc(returnValue, Integer.parseInt(problem.substring(operatorPosition.get(operatorCounter), operatorPosition.get(operatorCounter+1))), operatorId);
            case 2:
                System.out.println(operatorPosition.get(operatorCounter));
                System.out.println(problem.substring(operatorPosition.get(operatorCounter - 1)));
                return calc(returnValue, Integer.parseInt(problem.substring(operatorPosition.get(operatorCounter))), operatorId);
        }
        main.printError("Counter out of range!!");
        return returnValue;
    }

    private int findPosition(int counter, int totalAmount) {
        if (counter == totalAmount) {
            return 3;
        } else if (counter == 0) {
            return 1;
        } else if (counter < totalAmount - 1) {
            return 1;
        } else if (counter == totalAmount - 1) {
            return 2;
        }
        main.printError("Counter out of range!!");
        main.printDebug("Actual value: " + counter + ", should be between 0 and " + totalAmount);
        return -1;
    }

    private int calc(int firstNumber, int secondNumber, int operatorId) {
        switch (operatorId) {
            case 0:
                return firstNumber + secondNumber;
            case 1:
                return firstNumber - secondNumber;
            case 2:
                return firstNumber * secondNumber;
            case 3:
                return firstNumber / secondNumber;
            case 4:
                return (int) Math.pow((double) firstNumber, (double) secondNumber);
        }
        return 0;
    }
//    private int simpleCalculation(int[] calculatedParts, String originalProblem, int type){
//        String firstOperator = "placeholder", secondOperator = "placeholder";
//        switch (type){
//            case 1: firstOperator = "+";
//            secondOperator = "-";
//            break;
//            case 2: firstOperator = "*";
//                secondOperator = "/";
//                break;
//            case 3: firstOperator = "**";
//                break;
//        }
//        int position = 0;
//        int c = 0;
//        int returnValue = calculatedParts[0];
//        int currentPart;
//        main.printDebug("amount of values: " + calculatedParts.length);
//        main.printDebug("First values " + calculatedParts[0]);
//        List<Integer> plusPositions = new ArrayList<>(), minusPositions = new ArrayList<>();
//        while ((position = originalProblem.indexOf(firstOperator, position)) != -1){
//            plusPositions.add(position);
//            position ++;
//        }
//        position = 0;
//        while ((position = originalProblem.indexOf("-", position)) != -1){
//            minusPositions.add(position);
//            position ++;
//        }
//        plusPositions.add(originalProblem.length()+1);
//        minusPositions.add(originalProblem.length()+1);
//        int plusCounter = 0, minusCounter = 0, totalLength = plusPositions.size() + minusPositions.size(), currentPosition = 0;
//        for (int totalCounter = 0; totalCounter <= totalLength; totalCounter++){
//            c++;
//            currentPart = calculatedParts[c];
//             if (plusPositions.get(plusCounter) < minusPositions.get(minusCounter)) {
//                 currentPosition = plusPositions.get(plusCounter);
//                 plusCounter ++;
//             }else{
//                 currentPosition = minusPositions.get(minusCounter);
//                 minusCounter ++;
//             }
//             totalCounter++;
//            main.printDebug("position of the next calculation sign is: " + currentPosition);
//            if(Character.toString(originalProblem.charAt(currentPosition)).equals(firstOperator)){
//                returnValue = returnValue + currentPart;
//                main.printDebug("adding " + currentPart);
//            }else if(Character.toString(originalProblem.charAt(currentPosition)).equals("-")){
//                returnValue = returnValue - currentPart;
//                main.printDebug("subtracting " + currentPart);
//            }else{
//                main.printError("Character at position " + currentPosition + " is " + originalProblem.charAt(currentPosition) + " (expected: " + firstOperator + " or " + secondOperator + ")");
//            }
//            main.printDebug("value stored: " + returnValue);
//        }
//        if(calculatedParts.length != c+1){
//            main.printError("Error with the calculation");
//        }
//        return returnValue;
//    }
*/
}