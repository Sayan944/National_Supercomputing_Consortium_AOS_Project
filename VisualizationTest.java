
import config.ConfigLoader;
import config.NetworkConfig;
import visualization.TreeVisualizer;

public class VisualizationTest {

    public static void main(String[] args) {

        try {
            NetworkConfig config =
                    ConfigLoader.load("raymond.conf");

            System.out.println("\n========== TEST 1 ==========");
            System.out.println("Initial token holder: CU");

            // Initial holder pointers
            String[] initialPointers = {
                "CU",
                "CU",
                "CU",
                "BU",
                "BU"
            };

            TreeVisualizer.displayBothTrees(
                    config, initialPointers
            );

            System.out.println("\n========== TEST 2 ==========");
            System.out.println("Token has moved to JU");

            // Example holder pointers when JU holds the token
            String[] updatedPointers = {
                "BU",
                "JU",
                "CU",
                "JU",
                "BU"
            };

            TreeVisualizer.displayBothTrees(
                    config, updatedPointers
            );

        } catch (Exception e) {
            System.out.println("Visualization test failed!");
            e.printStackTrace();
        }
    }
}
