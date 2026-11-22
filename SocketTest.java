
import socket.*;
import messaging.*;

import java.net.Socket;

public class SocketTest {

    public static void main(String[] args) throws Exception {

        // Step 1: Start JU's server
        SocketServer server = new SocketServer(6000, clientSocket -> {

            try {

                CommunicationChannel channel =
                        new CommunicationChannel(clientSocket);

                // Receive message from CU
                Message received = channel.receive();

                System.out.println("\nJU received:");
                System.out.println(received);

                // Process the received message
                MessageHandler handler = new MessageHandler();
                handler.handle(received);

                // Send reply to CU
                Message reply = new Message(
                        MessageType.HELLO,
                        "JU",
                        "CU",
                        null,
                        "Hello CU! Message received."
                );

                channel.send(reply);

                channel.close();

            } catch (Exception e) {
                System.out.println("JU error: " + e.getMessage());
            }
        });

        server.start();

        // Step 2: Give the server time to start
        Thread.sleep(500);

        // Step 3: CU connects to JU
        CommunicationChannel client =
                SocketClient.connect("localhost", 6000);

        // Step 4: CU sends a REQUEST message
        Message request = new Message(
                MessageType.REQUEST,
                "CU",
                "JU",
                null,
                "CU requests the token."
        );

        client.send(request);

        System.out.println("\nCU sent:");
        System.out.println(request);

        // Step 5: CU receives JU's reply
        Message response = client.receive();

        System.out.println("\nCU received:");
        System.out.println(response);

        // Step 6: Process the reply
        MessageHandler handler = new MessageHandler();
        handler.handle(response);

        // Step 7: Close connections
        client.close();
        server.stop();

        System.out.println("\nTCP communication test completed.");
    }
}
