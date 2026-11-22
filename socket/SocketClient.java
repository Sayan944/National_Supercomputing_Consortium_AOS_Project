
package socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {

    public static CommunicationChannel connect(
            String host, int port) throws IOException {

        Socket socket = new Socket();

        socket.connect(
            new InetSocketAddress(host, port),
            5000
        );

        System.out.println(
            "Connected to " + host + ":" + port
        );

        return new CommunicationChannel(socket);
    }
}
