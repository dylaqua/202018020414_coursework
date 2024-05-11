import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumberleModelTest {

    private NumberleModel model;

    @BeforeEach
    void setUp() {
        model = new NumberleModel();
        model.initialize();  // Assume this sets a known equation like "1+2+3=6"
    }
    @Test
    public void testLoadEquationsFromFile() {
        // Check that the equation has been loaded into the list
        assertFalse(model.equations.isEmpty(), "Equations list should not be empty");
    }


    @Test
    void testValidInputProcessing() {
        // Assuming "1+2+3=6" is a valid equation and the initialized target
        String validInput = "1+2+3=6";
        assertTrue(model.processInput(validInput), "The model should return true for a valid input that balances.");
        assertEquals(5, model.getRemainingAttempts(), "Remaining attempts should decrease by one after processing valid input.");
    }

    @Test
    void testInvalidInputFormat() {
        // Invalid format (non-mathematical expression)
        String invalidInput = "asdasd";
        assertFalse(model.processInput(invalidInput), "The model should return false for a non-mathematical input format.");
        assertEquals(6, model.getRemainingAttempts(), "Remaining attempts should not decrease on invalid format input.");
    }

    @Test
    void testInvalidEquationBalance() {
        // Incorrect but validly formatted equation
        String invalidEquation = "1+2+3=7";
        assertFalse(model.processInput(invalidEquation), "The model should return false for a correctly formatted but incorrect equation.");
        assertEquals(6, model.getRemainingAttempts(), "Remaining attempts should not decrease on equation that doesn't balance.");
    }

    @Test
    void testGameOverLogic1() {
        // Use up all attempts with correct equations
        for (int i = 0; i < 6; i++) {
            model.processInput("1+2+3=6");
        }
        assertEquals(0, model.getRemainingAttempts(), "There should be no remaining attempts left.");
    }
    @Test
    void testGameOverLogic2() {
        // Use up all attempts with incorrect equations
        for (int i = 0; i < 6; i++) {
            model.processInput("1+2+3=7");
        }
        assertEquals(6, model.getRemainingAttempts(), "There should be 6 remaining attempts left.");
    }

}
