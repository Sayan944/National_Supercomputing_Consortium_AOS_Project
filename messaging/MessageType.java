
package messaging;

public enum MessageType {

    HELLO,
    REQUEST,
    TOKEN,

    MARKER,
    SNAPSHOT_START,
    SNAPSHOT_REPORT,

    TREE_REQUEST,
    TREE_RESPONSE,

    SHUTDOWN
}
