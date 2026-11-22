
package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import messaging.Message;
import messaging.MessageSerializer;

public class CommunicationChannel {

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public CommunicationChannel(Socket socket) throws IOException {

        this.socket = socket;

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    // Send a message through TCP
    public synchronized void send(Message message) throws IOException {

        MessageSerializer.send(output, message);
    }

    // Receive a message through TCP
    public Message receive() throws IOException {

        return MessageSerializer.receive(input);
    }

    // Close the connection
    public void close() throws IOException {

        socket.close();
    }
}
