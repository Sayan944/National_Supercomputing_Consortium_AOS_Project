
package messaging;

public class MessageHandler {

    public void handle(Message message) {

        System.out.println("\n--- Incoming Message ---");

        switch (message.getType()) {

            case HELLO:
                System.out.println("Connection established with "
                                   + message.getFrom());
                break;

            case REQUEST:
                System.out.println(message.getFrom()
                                   + " is requesting the token.");
                break;

            case TOKEN:
                System.out.println("Token received from "
                                   + message.getFrom());
                break;

            case MARKER:
                System.out.println("Snapshot marker received from "
                                   + message.getFrom());
                break;

            case SNAPSHOT_START:
                System.out.println("Snapshot operation started.");
                break;

            case SNAPSHOT_REPORT:
                System.out.println("Snapshot report received from "
                                   + message.getFrom());
                break;

            case SHUTDOWN:
                System.out.println(message.getFrom()
                                   + " is shutting down.");
                break;
        }

        System.out.println("------------------------");
    }
}
