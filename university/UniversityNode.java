
package university;

import config.ConfigLoader;
import config.NetworkConfig;

public class UniversityNode {

    public static void main(String[] args) {

        try {

            // Load network configuration
            NetworkConfig config =
                    ConfigLoader.load("raymond.conf");

            if (args.length != 1) {
                System.out.println(
                    "Usage: java university.UniversityNode <UniversityName>");
                return;
            }

            String universityName = args[0];

            // Find university index
            int nodeIndex = -1;

            for (int i = 0; i < config.getUniversityCount(); i++) {

                if (config.getUniversityName(i)
                        .equalsIgnoreCase(universityName)) {

                    nodeIndex = i;
                    break;
                }
            }

            if (nodeIndex == -1) {
                System.out.println(
                    "University not found: " + universityName);
                return;
            }

            // Create and start university node
            NodeManager manager =
                    new NodeManager(config, nodeIndex);

            manager.start();

            // Start command handler
            NodeCommandHandler commandHandler =
                    new NodeCommandHandler(manager);

            commandHandler.start();

        }
        catch (Exception e) {

            System.out.println(
                "Error starting university node: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
