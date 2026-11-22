
package messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageSerializer {

    public static void send(DataOutputStream out, Message message)
            throws IOException {

        out.writeUTF(message.getType().name());
        out.writeUTF(message.getFrom());
        out.writeUTF(message.getTo());

        out.writeUTF(message.getSnapshotId() == null
                     ? "" : message.getSnapshotId());

        out.writeUTF(message.getPayload() == null
                     ? "" : message.getPayload());

        out.flush();
    }

    public static Message receive(DataInputStream in)
            throws IOException {

        MessageType type =
                MessageType.valueOf(in.readUTF());

        String from = in.readUTF();
        String to = in.readUTF();
        String snapshotId = in.readUTF();
        String payload = in.readUTF();

        return new Message(
                type,
                from,
                to,
                snapshotId,
                payload
        );
    }
}
