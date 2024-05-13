import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;

public class NumberleView implements Observer {
    private final NumberleModel model;
    private final NumberleController controller;
    private final JFrame frame = new JFrame("Numberle");
    private final JTextField inputTextField = new JTextField(7);
    private final JLabel attemptsLabel = new JLabel("Attempts remaining: ");
    private final JTextPane guessHistoryPane = new JTextPane();
    private final JScrollPane scrollPane = new JScrollPane(guessHistoryPane);
    private final Map<Character, JButton> buttonMap = new HashMap<>();
    private final Map<Character, Color> buttonColorCache = new HashMap<>(); // Button color cache
    private final JButton newGameButton = new JButton("New Game");
    private final Map<Color, Integer> colorPriorityMap = new HashMap<>() {{
        put(Color.GREEN, 3);
        put(Color.ORANGE, 2);
        put(Color.GRAY, 1);
    }};

    public NumberleView(NumberleModel model, NumberleController controller) {
        this.model = model;
        this.controller = controller;
        this.controller.setView(this);
        this.controller.startNewGame();
        this.model.addObserver(this);
        initializeFrame();
        update(model, null);
        clearHistory();
    }

    private void initializeFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800); // Window size
        frame.setLayout(new BorderLayout());

        guessHistoryPane.setEditable(false);
        SimpleAttributeSet centerAlign = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAlign, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(centerAlign, 48); // Font size
        StyleConstants.setFontFamily(centerAlign, "Monospaced"); // Fixed width font
        guessHistoryPane.setParagraphAttributes(centerAlign, true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        inputPanel.add(new JLabel("Enter your guess: "));
        inputPanel.add(inputTextField);

        JButton submitButton = new JButton("Submit");
        submitButton.setFocusPainted(false);
        newGameButton.setFocusPainted(false);
        submitButton.addActionListener(e -> {
            boolean validInput = controller.processInput(inputTextField.getText());
            if (validInput) {
                newGameButton.setEnabled(true);
            }
            inputTextField.setText("");
        });

        inputPanel.add(submitButton);
        inputPanel.add(attemptsLabel);
        inputPanel.add(newGameButton); //new game button

        // Set the new game button to start off disabled
        newGameButton.setEnabled(false);

        newGameButton.addActionListener(e -> {
            controller.startNewGame();
            resetButtonColors();
            clearHistory();
            newGameButton.setEnabled(false);
        });

        JPanel buttonPanel = new JPanel(new GridLayout(5, 5, 5, 5)); // Button panel layout
        String[] buttons = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "-", "*", "/", "=", "Back"};
        for (String b : buttons) {
            JButton btn = new JButton(b);
            btn.setPreferredSize(new Dimension(60, 60)); //Button size
            btn.setFont(btn.getFont().deriveFont(24f)); // Button text font size
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                if (b.equals("Back")) {
                    String currentText = inputTextField.getText();
                    if (!currentText.isEmpty()) {
                        inputTextField.setText(currentText.substring(0, currentText.length() - 1));
                    }
                } else {
                    inputTextField.setText(inputTextField.getText() + b);
                }
            });
            char key = b.charAt(0);
            buttonMap.put(key, btn);
            buttonColorCache.put(key, null); // Initializes the color cache
            buttonPanel.add(btn);
        }

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        if (o instanceof NumberleModel) {
            attemptsLabel.setText("Attempts remaining: " + controller.getRemainingAttempts());
            if (arg == null || arg.equals("")) {
                updateGuessHistory();
                updateAllButtonColors();
            }

            if (controller.isGameOver()) {
                String targetEquation = controller.getTargetWord();
                if (controller.isGameWon()) {
                    int option = JOptionPane.showConfirmDialog(
                            frame,
                            "Congratulations! You won! The correct equation is: " + targetEquation + "\nDo you want to start a new game?",
                            "Game Over",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        controller.startNewGame();
                        resetButtonColors(); // Reset button color
                        clearHistory(); // Clear history
                    } else {
                        System.exit(0);
                    }
                } else {
                    int option = JOptionPane.showConfirmDialog(
                            frame,
                            "Game over! The correct equation is: " + targetEquation + "\nDo you want to start a new game?",
                            "Game Over",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        controller.startNewGame();
                        resetButtonColors();
                        clearHistory();
                    } else {
                        System.exit(0);
                    }
                }
                newGameButton.setEnabled(true); // Enable the new game button
                resetButtonColors();
                clearHistory();
            }

            if (arg instanceof String) {
                JOptionPane.showMessageDialog(frame, arg, "Invalid Input", JOptionPane.ERROR_MESSAGE);
                inputTextField.selectAll();
                inputTextField.requestFocus();
            }
        }
    }


    // Update the label for the number of remaining attempts
    public void updateAttemptsLabel() {
        attemptsLabel.setText("Attempts remaining: " + controller.getRemainingAttempts());
    }

    private void updateGuessHistory() {
        StyledDocument doc = guessHistoryPane.getStyledDocument();
        try {
            String guess = controller.getCurrentGuess().toString();
            if (guess.trim().isEmpty()) {
                return;
            }

            final int equationLength = 7; // Length of the equation (including operators)

            for (int i = 0; i < guess.length() - 1; i += 3) {
                char guessChar = guess.charAt(i);
                char statusChar = guess.charAt(i + 1);
                SimpleAttributeSet attributeSet = new SimpleAttributeSet();

                switch (statusChar) {
                    case '✓':
                        StyleConstants.setBackground(attributeSet, Color.GREEN);
                        break;
                    case '?':
                        StyleConstants.setBackground(attributeSet, Color.ORANGE);
                        break;
                    default:
                        StyleConstants.setBackground(attributeSet, Color.GRAY);
                        break;
                }
                StyleConstants.setBold(attributeSet, true);
                doc.insertString(doc.getLength(), " " + guessChar + " ", attributeSet);
            }

            // Pad remaining spaces to maintain consistent equation length
            int remainingLength = equationLength - (guess.length() / 3);
            if (remainingLength > 0) {
                doc.insertString(doc.getLength(), String.format("%" + (remainingLength * 3) + "s", ""), null);
            }

            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void updateAllButtonColors() {
        String guess = controller.getCurrentGuess().toString();
        if (guess.isEmpty()) {
            return; // If guess is empty, it simply returns without any update
        }

        // Iterate over each character-status pair, updating the button color
        for (int i = 0; i < guess.length() - 1; i += 3) {
            char guessChar = guess.charAt(i);
            char statusChar = guess.charAt(i + 1);
            Color newColor;

            switch (statusChar) {
                case '✓':
                    newColor = Color.GREEN;
                    break;
                case '?':
                    newColor = Color.ORANGE;
                    break;
                default:
                    newColor = Color.GRAY;
                    break;
            }

            // Gets the current color of the button
            JButton button = buttonMap.get(guessChar);
            if (button != null) {
                Color currentColor = buttonColorCache.getOrDefault(guessChar, null);

                // Determine whether the new color is better than the current color
                if (shouldUpdateButtonColor(currentColor, newColor)) {
                    button.setBackground(newColor);
                    buttonColorCache.put(guessChar, newColor);
                }
            }
        }
    }

    private boolean shouldUpdateButtonColor(Color currentColor, Color newColor) {
        // If both the current color and the new color are green, green has the highest priority and false is returned directly
        if (currentColor == Color.GREEN && newColor == Color.GREEN) {
            return false;
        }

        // Gets the priority of the current color and the new color
        Integer currentPriority = colorPriorityMap.getOrDefault(currentColor, 0);
        Integer newPriority = colorPriorityMap.getOrDefault(newColor, 0);

        // Comparative priority
        return newPriority > currentPriority;
    }

    private void resetButtonColors() {
        for (Map.Entry<Character, JButton> entry : buttonMap.entrySet()) {
            JButton button = entry.getValue();
            button.setBackground(null);
        }
        buttonColorCache.replaceAll((k, v) -> null);
    }

    private void clearHistory() {
        guessHistoryPane.setText("");
    }
}
