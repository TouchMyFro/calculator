/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculator;

//ECE 309  March 26th, 2018  Team Project: Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ExpressionCalculator implements Calculator, ActionListener {
    // GUI Globals

    JFrame calculatorWindow = new JFrame();
    JPanel topPanel = new JPanel();
    JPanel midPanel = new JPanel();
    JPanel botPanel = new JPanel();

    JLabel enterExpressionLabel = new JLabel();
    JLabel enterXLabel = new JLabel();
    JTextField enterExpressionTextField = new JTextField();
    JTextField enterXTextField = new JTextField();
    JTextArea displayTextArea = new JTextArea();
    JTextField errorTextField = new JTextField();
    JButton clearButton = new JButton("Clear Text Fields");
    JButton recallButton = new JButton("Get Last Entered Values");

    JScrollPane displayScrollPane = new JScrollPane(displayTextArea);
    // Global Variables
    String previousExpression;
    String previousXString;
    ArrayList<Integer> operatorIndexList = new ArrayList<Integer>();
    Vector<String> operators = new Vector<String>();

    public static void main(String[] args) {
        System.out.println("Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell");

        try {
            new ExpressionCalculator();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ExpressionCalculator() throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException {
        // Create the GUI
        // ===================================================================================
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        calculatorWindow.setTitle("Expression Calculator");
        calculatorWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        calculatorWindow.getContentPane().add(topPanel, "North");
        calculatorWindow.getContentPane().add(botPanel, "South");
        calculatorWindow.add(midPanel);
        // Top Panel Items
        enterExpressionLabel = new JLabel("Enter Expression:");
        enterExpressionLabel.setHorizontalAlignment(JLabel.CENTER);
        enterExpressionTextField.setText("");
        enterExpressionTextField.addActionListener(this); // activate!
        enterXLabel = new JLabel("For X = ");
        enterXLabel.setHorizontalAlignment(JLabel.CENTER);
        enterXTextField.setText("");
        enterXTextField.addActionListener(this); // activate!
        // Add to Top Panel
        topPanel.setLayout(new GridLayout(1, 4));
        topPanel.add(enterExpressionLabel);
        topPanel.add(enterExpressionTextField);
        topPanel.add(enterXLabel);
        topPanel.add(enterXTextField);
        // Center Panel
        displayTextArea.setFont(new Font("default", Font.BOLD, 14));
        displayTextArea.setEditable(false); // keep cursor out
        displayTextArea.setLineWrap(true);
        displayTextArea.setWrapStyleWord(true);
        // Add to Center Panel
        midPanel.setLayout(new GridLayout(1, 1));
        midPanel.add(displayScrollPane);
        // Bottom Panel
        errorTextField.setText("Errors Will Appear Here");
        errorTextField.setEditable(false); // keep cursor out
        errorTextField.setBackground(Color.pink);
        errorTextField.setForeground(Color.black);
        clearButton.addActionListener(this); // activate!
        clearButton.setBackground(Color.green);
        recallButton.addActionListener(this); // activate!
        recallButton.setBackground(Color.cyan);
        // Add to Bottom Panel
        botPanel.setLayout(new GridLayout(1, 3));
        botPanel.add(clearButton);
        botPanel.add(recallButton);
        botPanel.add(errorTextField);
        // Enable the GUI
        calculatorWindow.setSize(1000, 600); // width, height (in "pixels"!)
        calculatorWindow.setVisible(true); // show it!

        // ===================================================================================
        operators.add("^");
        operators.add("r");
        operators.add("*");
        operators.add("/");
        operators.add("+");
        operators.add("-");
    }

    private int getOperator(String expression) {
        char operator = ' ';
        int i;
        for (i = 1; i < expression.length(); i++) // (1st char shouldn't be an operator)
        { // and starting at 1 allows a unary!
            if ((expression.charAt(i) == '+') || (expression.charAt(i) == '-') || (expression.charAt(i) == '*')
                    || (expression.charAt(i) == '/') || (expression.charAt(i) == '^')
                    || (expression.charAt(i) == 'r')) {
                operator = expression.charAt(i);
                break;
            }
        }
        return i;
    }

    @Override
    public double calculate(String expression, String x) throws Exception {
        ArrayList<Integer> rightParanLocations = new ArrayList<Integer>();
        ArrayList<Integer> leftParanLocations = new ArrayList<Integer>();
        // do operand substitution for pi and e
        int leftParan = 0;
        for (int i = 0; i <= expression.length(); i++) {
            leftParan = 0;
            int rightParan = expression.indexOf(")");
            if (!rightParanLocations.contains(rightParan)) {
                rightParanLocations.add(rightParan);
                // find the matching left by going back from rights location and stopping on
                // first

                for (int j = rightParan; j >= 0; j--) {
                    if (expression.charAt(j) == '(') {
                        leftParan = j;
                        break;
                    }
                }
                if (!leftParanLocations.contains(leftParan)) {
                    leftParanLocations.add(leftParan);
                }

            }
            // After this point we now have each nested expression that is ready to be
            // evaluated

            String smallExpression = null;

            if (rightParan == -1) {
                // all expressions have been evaluated
                smallExpression = expression;
            } else {
                smallExpression = expression.substring(leftParan + 1, rightParan);
            }

            expressionMapCreate(smallExpression);
            // determine the order of operations
            boolean expressionHasOperators = true;
            while (expressionHasOperators) {
                String newExpression = smallExpression;
                boolean expressionHasLevel3Operators = true;
                boolean expressionHasLevel2Operators = true;
                boolean expressionHasLevel1Operators = true;
                while (expressionHasLevel3Operators) {
                    for (Integer location : operatorIndexList) {
                        expressionHasLevel3Operators = false;
                        char operator = smallExpression.charAt(location);
                        if (operator == '^' || operator == 'r') {
                            // solve smallExpression and update the big one with new data
                            newExpression = evaluateExpression(smallExpression, location, operator, rightParan,
                                    leftParan);
                            expressionHasLevel3Operators = true;
                            break;
                        }
                    }
                    if (operatorIndexList.size() == 0) {
                        expressionHasLevel3Operators = false;
                        expressionHasLevel2Operators = false;
                        expressionHasLevel1Operators = false;
                    }
                    smallExpression = newExpression;
                    expressionMapCreate(smallExpression);
                }
                while (expressionHasLevel2Operators) {
                    for (Integer location : operatorIndexList) {
                        expressionHasLevel2Operators = false;
                        char operator = smallExpression.charAt(location);
                        if (operator == '*' || operator == '/') {
                            // solve smallExpression and update the big one with new data
                            newExpression = evaluateExpression(smallExpression, location, operator, rightParan,
                                    leftParan);
                            expressionHasLevel2Operators = true;
                            break;
                        }
                    }
                    if (operatorIndexList.size() == 0) {
                        expressionHasLevel3Operators = false;
                        expressionHasLevel2Operators = false;
                        expressionHasLevel1Operators = false;
                    }
                    smallExpression = newExpression;
                    expressionMapCreate(smallExpression);
                }
                while (expressionHasLevel1Operators) {
                    for (Integer location : operatorIndexList) {
                        expressionHasLevel1Operators = false;
                        char operator = smallExpression.charAt(location);
                        if (operator == '+' || operator == '-') {
                            // solve smallExpression and update the big one with new data
                            newExpression = evaluateExpression(smallExpression, location, operator, rightParan,
                                    leftParan);
                            expressionHasLevel1Operators = true;
                            break;
                        }
                    }
                    if (operatorIndexList.size() == 0) {
                        expressionHasLevel3Operators = false;
                        expressionHasLevel2Operators = false;
                        expressionHasLevel1Operators = false;
                    }
                    smallExpression = newExpression;
                    expressionMapCreate(smallExpression);
                }

                smallExpression = newExpression;
                expressionMapCreate(smallExpression);
                if (operatorIndexList.size() == 0) {
                    // there are no more operators to solve
                    expressionHasOperators = false;
                }
            }
            if (rightParan != -1) {
                // remove expression and continue
                String answer = smallExpression;

                expression = expression.substring(0, leftParan) + smallExpression
                        + expression.substring(rightParan + 1);

            } else {
                expression = smallExpression;
            }

        }
        if (expression.contains("#")) {
            expression = expression.replace("#", "-");
        }
        return Double.parseDouble(expression);
    }

    // this method handles all the code for evaluating an expression given the
    // location of the operand and the expression
    private String evaluateExpression(String smallExpression, int location, char operator, int rightParan,
            int leftParan) {
        // find operands
        ArrayList<String> operands = findOperands(smallExpression, location);
        String leftOperand = operands.get(0);
        String rightOperand = operands.get(1);
        // now we have the operands and operator so send to simple expression calc to
        // solve
        double result = executeSimpleExpresssion(leftOperand, rightOperand, operator);

        // expression has no paran use operators to replace
        int smallExpressionSize = (leftOperand + operator + rightOperand).length();
        int LocationOfOperationInExpression = smallExpression.indexOf(leftOperand + operator + rightOperand);
        if (LocationOfOperationInExpression - 1 == 0) {
            smallExpression = getResult(result)
                    + smallExpression.substring(smallExpression.indexOf(rightOperand), rightOperand.length());
        } else if ((LocationOfOperationInExpression + smallExpressionSize) == smallExpression.length()) {
            smallExpression = smallExpression.substring(0, LocationOfOperationInExpression) + getResult(result);
        } else {
            smallExpression = smallExpression.substring(0, LocationOfOperationInExpression) + getResult(result)
                    + smallExpression.substring(
                            LocationOfOperationInExpression + (leftOperand + operator + rightOperand).length());
        }
        return smallExpression;

    }

    private String getResult(double result) {
        if (result < 0) {
            return "#" + Double.toString(Math.abs(result));
        }
        return Double.toString(result);
    }

    // This method determines where the operands are given the location of the
    // operator
    private ArrayList<String> findOperands(String smallExpression, int location) {
        String rightOperand = "";
        String leftOperand = "";
        ArrayList<String> operands = new ArrayList<String>();
        // Right operand code
        for (int j = location; j < smallExpression.length(); j++) {
            if (operators.contains(String.valueOf(smallExpression.charAt(j))) && j != location) {
                // if the character we are looking at is a operator then everything inbetween
                // that operator and the previous operator is the right operand
                rightOperand = smallExpression.substring(location + 1, j);
                break;
            }
        }
        if (rightOperand.equals("")) {
            // the operand must be located at the end of the string
            rightOperand = smallExpression.substring(location + 1);
        }
        // Left operand code
        for (int j = location; j >= 0; j--) {
            if (operators.contains(String.valueOf(smallExpression.charAt(j))) && j != location) {
                // if the character we are looking at is a operator then everything inbetween
                // the previous operator and the operator is the left operand
                leftOperand = smallExpression.substring(j + 1, location);
                break;
            }
        }
        if (leftOperand.equals("")) {
            // the operand must be located at the start of the string
            leftOperand = smallExpression.substring(0, location);
        }

        operands.add(leftOperand);
        operands.add(rightOperand);
        return operands;
    }

    // executes a simple expression given the expression the left operand right
    // operand ,and operator
    private double executeSimpleExpresssion(String leftOperand, String rightOperand, char operator) {
        leftOperand = leftOperand.trim();
        rightOperand = rightOperand.trim();
        // convert operands from String to double
        // Note that parseDouble() will allow a unary operator!
        if (leftOperand.contains("#")) {
            leftOperand = leftOperand.replaceAll("#", "-");
        }
        if (rightOperand.contains("#")) {
            rightOperand = rightOperand.replaceAll("#", "-");
        }
        double leftNumber = 0;
        try {
            leftNumber = Double.parseDouble(leftOperand);
        } catch (NumberFormatException nfe) {
            System.out.println("Left operand is not numeric.");
        }
        double rightNumber = 0;
        try {
            rightNumber = Double.parseDouble(rightOperand);
        } catch (NumberFormatException nfe) {
            System.out.println("Right operand is not numeric.");
        }
        double result = 0;
        switch (operator) {
            case '+':
                result = leftNumber + rightNumber;
                break;
            case '-':
                result = leftNumber - rightNumber;
                break;
            case '*':
                result = leftNumber * rightNumber;
                break;
            case '/':
                result = leftNumber / rightNumber;
                break;
            case '^':
                result = Math.pow(leftNumber, rightNumber);
                break;
            case 'r':
                result = Math.pow(leftNumber, 1 / rightNumber);
                break;
        }
        return result;
    }

    // --------------------------------------------------------------------------------
    // adjacentOperand: checks for adjacent operand errors in the entered
    // expression.
    // Returned value = boolean true when an error is found
    // Errors: when any of the following are adjacent to each other: [ '^' , 'r' ,
    // '*' , '/' , '+' , '-' ]
    // and/or if the following strings are found in the expression: [ "()" , ")(" ]
    // --------------------------------------------------------------------------------
    private boolean adjacentOperand(String expression, JTextField errorTextField2) {
        int i = 0;
        expression = expression.trim();

        for (i = 0; i < (expression.length() - 2); i++) {
            if ((operators.contains(expression.substring(i, i + 1))) && (operators.contains(expression.substring(i + 1, i + 2)))) {
                errorTextField2.setText("Error: Adjacent Operands at " + expression.substring(i, i + 1) + expression.substring(i + 1, i + 2));
                return (true);
            }
            if (((expression.charAt(i) == '(') && (expression.charAt(i + 1) == ')')) || ((expression.charAt(i) == ')') && (expression.charAt(i + 1) == '('))) {
                errorTextField2.setText("Invalid Order of Parantheses");
                return (true);
            }
            ArrayList<Integer> rightParanLocations = new ArrayList<Integer>();
            ArrayList<Integer> leftParanLocations = new ArrayList<Integer>();
            // do operand substitution for pi and e
            int leftParan = 0;
            for (int j = 0; j <= expression.length(); j++) {
                leftParan = 0;
                int rightParan = expression.indexOf(")");
                if (!rightParanLocations.contains(rightParan) && rightParan != -1) {
                    rightParanLocations.add(rightParan);
                    // find the matching left by going back from rights location and stopping on
                    // first

                    for (int k = rightParan; k >= 0; k--) {
                        if (expression.charAt(k) == '(') {
                            leftParan = k;
                            if (!leftParanLocations.contains(leftParan)) {
                                leftParanLocations.add(leftParan);
                            }
                        }
                    }
                }
            }
            if (rightParanLocations.size() != leftParanLocations.size()) {
                errorTextField2.setText("Invalid number of Parantheses");
                return (true);
            }
        }
        return (false);
    }

    // ------------------------------------------------------------------------------
    // Replaces symbols (passed expression with passed XString, E, pi, r,) returns
    // corrected expression
    // ------------------------------------------------------------------------------
    private String replaceSymbols(String expression, String XString) {
        expression = expression.replaceAll("x", XString);
        expression = expression.replaceAll("X", XString);
        expression = expression.replaceAll("E", Double.toString(Math.E));
        expression = expression.replaceAll("PI", Double.toString(Math.PI));
        expression = expression.replaceAll("Pi", "pi");
        expression = expression.replaceAll("pI", "pi");
        expression = expression.replaceAll("e", Double.toString(Math.E));
        expression = expression.replaceAll("pi", Double.toString(Math.PI));
        // # is minus
        expression = expression.replaceAll("\\(-", "(#");
        expression = expression.replaceAll("--", "-#");
        expression = expression.replaceAll("\\+-", "\\+#");
        expression = expression.replaceAll("\\*-", "\\*#");
        expression = expression.replaceAll("/-", "/#");
        expression = expression.replaceAll("r-", "r#");
        expression = expression.replaceAll("\\^-", "\\^#");
        if (expression.startsWith("-")) {
            expression = expression.replaceFirst("-", "#");
        }
        return (expression);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        errorTextField.setText("");
        errorTextField.setBackground(Color.pink);
        if (ae.getSource() == enterExpressionTextField || ae.getSource() == enterXTextField) {
            try {
                String expression = enterExpressionTextField.getText().trim();
                String expressionUserEntered = expression;
                String XString = enterXTextField.getText().trim();
                previousExpression = expression;
                previousXString = XString;

                expression = replaceSymbols(expression, XString);
                if (!errorCheckingExpression(expression, expressionUserEntered, XString)) {
                    return;
                }
                enterExpressionTextField.setText("");
                enterXTextField.setText("");
                double result = calculate(expression, XString);
                Double temp = new Double(result);
                String resultString;

                if (temp.isInfinite() || temp.isNaN()) {
                    resultString = temp.toString();
                } else {
                    resultString = temp.toString();
                }

                displayTextArea.append("\n" + expression + " = " + resultString);
                if (previousExpression.contains("x") || previousExpression.contains("X")) {
                    displayTextArea.append(" for X = " + XString);
                }
                displayTextArea.setCaretPosition(displayTextArea.getDocument().getLength());

            } catch (Exception e) {
                //e.printStackTrace();
                errorTextField.setText(e.getMessage());
            }
        }
        if (ae.getSource() == clearButton) {
            enterExpressionTextField.setText("");
            enterXTextField.setText("");
            errorTextField.setText("");
            return;
        }
        if (ae.getSource() == recallButton) {
            enterExpressionTextField.setText(previousExpression);
            enterXTextField.setText(previousXString);
            return;
        }
    }

    private boolean errorCheckingExpression(String expression, String expressionUserEntered, String XString) throws Exception {

        for (int i = 0; i < expression.length(); i++) {
            expression = expression.trim();

            if (!Character.isDigit(expression.charAt(i)) && (expression.charAt(i) != '+' && expression.charAt(i) != '-'
                    && expression.charAt(i) != '/' && expression.charAt(i) != '*' && expression.charAt(i) != '^'
                    && expression.charAt(i) != 'r' && expression.charAt(i) != '(' && expression.charAt(i) != ')'
                    && expression.charAt(i) != 'p' && expression.charAt(i) != 'i' && expression.charAt(i) != 'e'
                    && expression.charAt(i) != 'x' && expression.charAt(i) != ' ' && expression.charAt(i) != '#'
                    && expression.charAt(i) != '.')) {
                throw new Exception("Incorrect character was used :" + expression.charAt(i));
                //errorTextField.setText("Incorrect character was used :" + expression.charAt(i));
                //return false;
            }
            if ((expressionUserEntered.contains("x") || expressionUserEntered.contains("X")) && XString.contentEquals("")) {
                throw new Exception("expression contains x, but x wasn't specified");
                //errorTextField.setText("expression contains x, but x wasn't specified");
                //return false;
            }
            if (!XString.contentEquals("")) {
                try {
                    double d1 = Double.parseDouble(XString);
                } catch (NumberFormatException nfe) {
                    throw new Exception("The x value specified is not numeric.");
                }
            }

            if (!(expressionUserEntered.contains("x") || expressionUserEntered.contains("X")) && !XString.contentEquals("")) {
                throw new Exception("X value specified but expression does not contain x.");

                //errorTextField.setText("expression contains x, but x wasn't specified");
                //return false;
            }

            if (adjacentOperand(expression, errorTextField)) {

                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------------
    // Stores the index and each operand
    // ------------------------------------------------------------------------------
    void expressionMapCreate(String expression) {
        operatorIndexList.clear();
        char c;
        for (int i = 0; i < expression.length(); i++) {
            c = expression.charAt(i);
            if (operators.contains(Character.toString(c))) {
                operatorIndexList.add(i);
            }
        }
    }
}
