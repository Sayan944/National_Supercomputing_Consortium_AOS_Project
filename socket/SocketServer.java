
package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class SocketServer {

    private ServerSocket serverSocket;
    private Thread serverThread;
    private Consumer<Socket> connectionHandler;

    public SocketServer(int port, Consumer<Socket> handler)
            throws IOException {

        serverSocket = new ServerSocket(port);
        connectionHandler = handler;
    }

    // Start listening for incoming connections
    public void start() {

        serverThread = new Thread(() -> {

            System.out.println(
                "Server listening on port "
                + serverSocket.getLocalPort()
            );

            while (!serverSocket.isClosed()) {

                try {

                    Socket clientSocket = serverSocket.accept();

                    System.out.println(
                        "New connection from "
                        + clientSocket.getInetAddress()
                    );

                    
	  new Thread(() -> connectionHandler.accept(clientSocket)).start();

                } catch (SocketException e) {

                    if (!serverSocket.isClosed()) {
                        System.out.println("Socket error: " + e.getMessage());
                    }

                } catch (IOException e) {

                    System.out.println("Connection error: " + e.getMessage());
                }
            }

        });

        serverThread.start();
    }

    // Stop the server
    public void stop() throws IOException {

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
