import java.util.Scanner;

public class CLIApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;

        while (playAgain) {
            NumberleModel model = new NumberleModel();
            model.initialize();
            assert model.getTargetNumber() != null : "Target number must be initialized";
            System.out.println("Symbols: √ is correct character and position, ? is Correct character, wrong position, X is Character not in equation");
            while (!model.isGameOver()) {
                System.out.println("Current Guess: " + model.getCurrentGuess());
                System.out.println("Remaining Attempts: " + model.getRemainingAttempts());
                assert model.getRemainingAttempts() > 0 : "There should be remaining attempts if game is not over";
                System.out.println("Enter your equation (7 characters, e.g., '1+2+3=6'): ");
                String input = scanner.nextLine();
                assert input != null : "Input should not be null";
                if (!model.processInput(input)) {
                    System.out.println("Invalid input or equation does not balance. Please try again.");
                }
                assert model.getCurrentGuess() != null : "Current guess should be updated";
                if (model.isGameWon()) {
                    System.out.println("Congratulations! You have guessed correctly! The correct equation was:" + model.getTargetNumber());
                    break;
                }
            }

            if (!model.isGameWon()) {
                System.out.println("Game over! The correct equation was: " + model.getTargetNumber());
            }

            // Provide options to continue or exit, and verify the input
            String choice;
            do {
                System.out.println("\nDo you want to play again? (Y/N)");
                choice = scanner.nextLine().trim().toUpperCase();
                assert choice.equals("Y") || choice.equals("N") : "Choice must be 'Y' or 'N'";
                if (!choice.equals("Y") && !choice.equals("N")) {
                    System.out.println("Invalid input. Please enter 'Y' or 'N'.");
                }
            } while (!choice.equals("Y") && !choice.equals("N"));

            playAgain = choice.equals("Y");
        }

        scanner.close();
    }
}
