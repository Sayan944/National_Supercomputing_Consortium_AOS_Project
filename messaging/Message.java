
package messaging;

public class Message {

    private MessageType type;
    private String from;
    private String to;
    private String snapshotId;
    private String payload;

    public Message(MessageType type, String from, String to,
                   String snapshotId, String payload) {

        this.type = type;
        this.from = from;
        this.to = to;
        this.snapshotId = snapshotId;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {

        return "Message Type: " + type +
               "\nFrom: " + from +
               "\nTo: " + to +
               "\nSnapshot ID: " + snapshotId +
               "\nPayload: " + payload;
    }
}
