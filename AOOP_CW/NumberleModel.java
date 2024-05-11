import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Observable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
public class NumberleModel extends Observable implements INumberleModel {
    protected List<String> equations = new ArrayList<>();
    private String targetNumber;
    private StringBuilder currentGuess;
    private int remainingAttempts;
    private boolean gameWon;

    public NumberleModel() {
        loadEquationsFromFile();
        assert !equations.isEmpty() : "Equations list must not be empty after loading";
    }

    private void loadEquationsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("E:\\AOOP\\AOOP_CW\\equations.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                equations.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            equations.add("1+2+3=6"); // Default equation in case of file read error
        }
    }

    @Override
    public void initialize() {
        assert equations != null && !equations.isEmpty() : "Equations list must not be empty before initialization";

        Random rand = new Random();
        targetNumber = !equations.isEmpty() ? equations.get(rand.nextInt(equations.size())) : "1+2+3=6";
        currentGuess = new StringBuilder("       ");
        remainingAttempts = MAX_ATTEMPTS;
        gameWon = false;
        setChanged();
        notifyObservers();

        assert targetNumber != null && !targetNumber.isEmpty() : "Target number must be initialized";
        assert currentGuess.length() == 7 : "Current guess must be initialized to 7 spaces";
        assert remainingAttempts == MAX_ATTEMPTS : "Remaining attempts must be initialized to max attempts";
        assert !gameWon : "Game must not be won at initialization";
    }

    @Override
    public boolean processInput(String input) {
        assert input != null : "Input cannot be null";

        if (!isValidInput(input) || !evaluateEquation(input)) {
            setChanged();
            notifyObservers("Invalid Input or Equation does not balance");
            return false;
        }

        remainingAttempts--;
        updateGuessAndCheckWin(input);

        setChanged();
        notifyObservers();

        assert remainingAttempts >= 0 : "Remaining attempts should never be negative";
        return true;
    }
    private void updateGuessAndCheckWin(String input) {
        int[] charCounts = new int[256];
        for (char c : targetNumber.toCharArray()) {
            charCounts[c]++;
        }


        boolean isCorrect = true;
        char[] feedbackChars = new char[targetNumber.length() * 3]; // Stores characters, status symbols, and delimiters

        // First process and mark all the correct characters
        for (int i = 0; i < targetNumber.length(); i++) {
            char inChar = input.charAt(i);
            char targetChar = targetNumber.charAt(i);
            int baseIndex = i * 3;
            feedbackChars[baseIndex] = inChar;
            if (inChar == targetChar) {
                feedbackChars[baseIndex + 1] = '✓';
                charCounts[inChar]--;
            } else {
                isCorrect = false;
                feedbackChars[baseIndex + 1] = ' ';  // Leave blank for later
            }
            feedbackChars[baseIndex + 2] = ' ';  // Add separator
        }

        // Handles and marks characters in the wrong position
        for (int i = 0; i < feedbackChars.length; i += 3) {
            if (feedbackChars[i + 1] == ' ') { // Only unmarked characters are processed
                char inChar = feedbackChars[i];
                if (charCounts[inChar] > 0) {
                    feedbackChars[i + 1] = '?'; // Position error
                    charCounts[inChar]--;
                } else {
                    feedbackChars[i + 1] = 'X'; // Dead wrong（unused）
                }
            }
        }

        currentGuess = new StringBuilder(new String(feedbackChars)); // Update the status of the current guess
        gameWon = isCorrect; // Update game win status
        assert currentGuess != null : "Current guess must be updated";
        assert gameWon == isCorrect : "Game won state must correctly reflect the accuracy of the guess";
    }



    private boolean isValidInput(String input) {
        return input.matches("[0-9+\\-*/=]{7}");
    }

    // Updated to handle more complex expressions
    private boolean evaluateEquation(String equation) {
        assert equation != null && equation.contains("=") : "Equation must not be null and must contain '=' symbol";

        String[] parts = equation.split("=");
        if (parts.length != 2) return false;
        try {
            double left = eval(parts[0]);
            double right = eval(parts[1]);
            boolean result = Math.abs(left - right) < 0.001;
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    private double eval(String expression) {
        assert expression != null : "Expression cannot be null for evaluation";

        Stack<Double> numbers = new Stack<>();
        Stack<Character> operations = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isDigit(c)) {
                int num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + (expression.charAt(i) - '0');
                    i++;
                }
                i--;
                numbers.push((double) num);
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operations.isEmpty() && precedence(operations.peek()) >= precedence(c)) {
                    double output = applyOp(operations.pop(), numbers.pop(), numbers.pop());
                    numbers.push(output);
                }
                operations.push(c);
            }
        }

        while (!operations.isEmpty()) {
            double output = applyOp(operations.pop(), numbers.pop(), numbers.pop());
            numbers.push(output);
        }

        assert !numbers.isEmpty() : "Number stack should not be empty after evaluation";
        return numbers.pop();
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') {
            return 1;
        }
        if (op == '*' || op == '/') {
            return 2;
        }
        return -1;
    }

    private double applyOp(char op, double b, double a) {
        assert op == '+' || op == '-' || op == '*' || op == '/' : "Operator must be a valid mathematical operator";
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                assert b != 0 : "Division by zero is not permitted";
                return a / b;
            default:
                throw new IllegalStateException("Unexpected operator: " + op);
        }
    }

    // Check if the game is over
    @Override
    public boolean isGameOver() {
        return remainingAttempts <= 0 || gameWon;
    }

    // Check if the game is won
    @Override
    public boolean isGameWon() {
        return gameWon;
    }

    // Get the target number
    @Override
    public String getTargetNumber() {
        return targetNumber;
    }

    // Get the current guess
    @Override
    public StringBuilder getCurrentGuess() {
        return currentGuess;
    }

    // Get the remaining attempts
    @Override
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    // Start a new game by reinitializing the model
    @Override
    public void startNewGame() {
        initialize();
    }
}
