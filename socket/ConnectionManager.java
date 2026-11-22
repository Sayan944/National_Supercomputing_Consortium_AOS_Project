
package socket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import messaging.Message;

public class ConnectionManager {

    private ConcurrentHashMap<String, CommunicationChannel> connections;

    public ConnectionManager() {

        connections = new ConcurrentHashMap<>();
    }

    // Register a connection with a university
    public void addConnection(
            String university,
            CommunicationChannel channel) {

        connections.put(university, channel);

        System.out.println(
            "Connection registered with " + university
        );
    }

    // Send a message to a connected university
    public void sendTo(
            String university, Message message) throws IOException {

        CommunicationChannel channel = connections.get(university);

        if (channel == null) {
            throw new IOException(
                "No connection available with " + university
            );
        }

        channel.send(message);
    }

    // Get an existing connection
    public CommunicationChannel getConnection(String university) {

        return connections.get(university);
    }

    // Remove one connection
    public void removeConnection(String university) throws IOException {

        CommunicationChannel channel = connections.remove(university);

        if (channel != null) {
            channel.close();
        }
    }

    // Close all connections
    public void closeAll() {

        for (CommunicationChannel channel : connections.values()) {

            try {
                channel.close();
            } catch (IOException e) {
                System.out.println("Error closing connection.");
            }
        }

        connections.clear();
    }
}
