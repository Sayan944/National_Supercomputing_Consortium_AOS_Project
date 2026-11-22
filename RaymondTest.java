
import raymond.*;

public class RaymondTest {

    public static void main(String[] args) {

        // Create the initial token
        Token token = new Token("TOKEN-1");

        // Create CU as the initial token holder
        RaymondAlgorithm cu = new RaymondAlgorithm(
                "CU",
                "CU",
                token,

                // Request sender (not needed in this test)
                university -> System.out.println(
                        "Sending REQUEST to " + university
                ),

                // Token sender (not needed in this test)
                (university, transferredToken) -> System.out.println(
                        "Sending TOKEN to " + university
                )
        );

        // Display initial state
        cu.displayStatus();

        // Request the critical section
        cu.requestCS();

        // Display state after requesting
        cu.displayStatus();

        // Release the critical section
        cu.releaseCS();

        // Display final state
        cu.displayStatus();
    }
}
