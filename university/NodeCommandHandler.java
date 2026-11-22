
package university;

import java.util.Scanner;

public class NodeCommandHandler {

    private NodeManager manager;

    public NodeCommandHandler(NodeManager manager) {
        this.manager = manager;
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("\nWelcome to "
                + manager.getNodeId() + " University!");

        showHelp();

        while (true) {

            System.out.print(
                "\n" + manager.getNodeId() + " > "
            );

            String command = scanner.nextLine()
                    .trim()
                    .toLowerCase();

            switch (command) {

                case "request":

                    manager.getAlgorithm().requestCS();
                    break;

                case "release":

                    manager.getAlgorithm().releaseCS();
                    break;

                case "status":

                    manager.getAlgorithm().displayStatus();
                    break;

                case "tree":

    manager.displayLiveTrees();

    break;

                case "snapshot":

                    System.out.println(
                        "\nInitiating global distributed snapshot..."
                    );

                    manager.startSnapshot();
                    break;

                case "help":

                    showHelp();
                    break;

                case "exit":

                    manager.stop();

                    System.out.println(
                        "Exiting " + manager.getNodeId()
                    );

                    return;

                default:

                    System.out.println(
                        "Invalid command. Type help."
                    );
            }
        }
    }

    private void showHelp() {

        System.out.println("\n========== COMMANDS ==========");
        System.out.println("request  - Request the critical section");
        System.out.println("release  - Release the critical section");
        System.out.println("status   - Display local Raymond state");
        System.out.println("tree     - Display physical and logical trees");
        System.out.println("snapshot - Start a global distributed snapshot");
        System.out.println("help     - Display commands");
        System.out.println("exit     - Stop this university process");
        System.out.println("==============================");
    }
}
