
import messaging.*;

import java.io.*;

public class MessagingTest {

    public static void main(String[] args) throws IOException {

        // Step 1: Create a REQUEST message
        Message original = new Message(
                MessageType.REQUEST,
                "CU",
                "JU",
                null,
                "Token requested"
        );

        // Step 2: Convert message into bytes
        ByteArrayOutputStream byteOutput =
                new ByteArrayOutputStream();

        DataOutputStream output =
                new DataOutputStream(byteOutput);

        MessageSerializer.send(output, original);

        // Step 3: Read the transmitted bytes
        ByteArrayInputStream byteInput =
                new ByteArrayInputStream(
                        byteOutput.toByteArray()
                );

        DataInputStream input =
                new DataInputStream(byteInput);

        // Step 4: Reconstruct the message
        Message received = MessageSerializer.receive(input);

        // Step 5: Display the received message
        System.out.println("Original Message:");
        System.out.println(original);

        System.out.println("\nReceived Message:");
        System.out.println(received);

        // Step 6: Process the received message
        MessageHandler handler = new MessageHandler();
        handler.handle(received);
    }
}
