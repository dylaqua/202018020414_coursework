public class NumberleController {
    private final INumberleModel model;
    private NumberleView view;

    public NumberleController(INumberleModel model) {
        this.model = model;
    }

    public void setView(NumberleView view) {
        this.view = view;
    }

    public boolean processInput(String input) {
        boolean validInput = model.processInput(input);
        return validInput;
    }

    public void startNewGame() {
        model.startNewGame();
//        System.out.println("Target Equation: " + model.getTargetNumber());
        if (view != null) {
            view.updateAttemptsLabel();
        }
    }

    public boolean isGameOver() {
        return model.isGameOver();
    }

    public boolean isGameWon() {
        return model.isGameWon();
    }

    public String getTargetWord() {
        return model.getTargetNumber();
    }

    public StringBuilder getCurrentGuess() {
        return model.getCurrentGuess();
    }

    public int getRemainingAttempts() {
        return model.getRemainingAttempts();
    }
}
