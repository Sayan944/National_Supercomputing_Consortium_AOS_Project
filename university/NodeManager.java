
package university;

import config.NetworkConfig;
import messaging.Message;
import messaging.MessageType;
import raymond.RaymondAlgorithm;
import raymond.Token;
import snapshot.SnapshotManager;
import snapshot.SnapshotState;
import snapshot.SnapshotReport;
import socket.CommunicationChannel;
import socket.ConnectionManager;
import socket.SocketClient;
import socket.SocketServer;
import visualization.TreeVisualizer;

import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class NodeManager {

    private final NetworkConfig config;
    private final int nodeIndex;
    private final String nodeId;

    private final RaymondAlgorithm algorithm;
    private final ConnectionManager connections;

    private final SnapshotManager snapshotManager;

    private final Map<String, Map<String, SnapshotReport>>
            collectedSnapshots = new HashMap<>();

    private final Map<String, Map<String, String>>
            treeResponses = new HashMap<>();

    private SocketServer server;
    private volatile boolean running = true;

    // Constructor
    public NodeManager(NetworkConfig config, int nodeIndex) {

        this.config = config;
        this.nodeIndex = nodeIndex;
        this.nodeId = config.getUniversityName(nodeIndex);

        this.connections = new ConnectionManager();

        String initialHolder = findInitialHolder();

        Token token = null;

        if (nodeIndex == config.getInitialTokenIndex()) {
            token = new Token("TOKEN-1");
        }

        this.algorithm = new RaymondAlgorithm(
            nodeId,
            initialHolder,
            token,
            this::sendRequest,
            this::sendToken
        );

        this.snapshotManager = new SnapshotManager(
            nodeId,
            this::sendMarker,
            this::sendSnapshotReport
        );
    }

    // Find the neighbour towards the initial token holder
    private String findInitialHolder() {

        int n = config.getUniversityCount();
        int tokenIndex = config.getInitialTokenIndex();

        if (nodeIndex == tokenIndex) {
            return nodeId;
        }

        int[] parent = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = -1;
        }

        Queue<Integer> queue = new LinkedList<>();

        queue.add(tokenIndex);
        parent[tokenIndex] = tokenIndex;

        while (!queue.isEmpty()) {

            int current = queue.poll();

            for (int neighbour : config.getNeighbours(current)) {

                if (parent[neighbour] == -1) {
                    parent[neighbour] = current;
                    queue.add(neighbour);
                }
            }
        }

        int holderIndex = parent[nodeIndex];

        return config.getUniversityName(holderIndex);
    }

    // Start university node
    public void start() throws IOException {

        int port = config.getPort(nodeIndex);

        server = new SocketServer(
            port,
            this::handleIncomingConnection
        );

        server.start();

        System.out.println("\n================================");
        System.out.println("University Node Started");
        System.out.println("University: " + nodeId);
        System.out.println("Port: " + port);
        System.out.println("Initial Holder Pointer: " + findInitialHolder());
        System.out.println("Has Token: " + algorithm.hasToken());
        System.out.println("================================\n");

        connectToHigherNeighbours();
    }

    // Get names of physical neighbours
    private List<String> getNeighbourNames() {

        List<String> names = new ArrayList<>();

        for (int neighbour : config.getNeighbours(nodeIndex)) {
            names.add(config.getUniversityName(neighbour));
        }

        return names;
    }

    // Establish outgoing connections
    private void connectToHigherNeighbours() {

        List<Integer> neighbours =
            config.getNeighbours(nodeIndex);

        for (int neighbour : neighbours) {

            if (nodeIndex < neighbour) {

                final int target = neighbour;

                Thread connectionThread = new Thread(() -> {

                    int port = config.getPort(target);

                    String neighbourName =
                        config.getUniversityName(target);

                    while (running) {

                        try {

                            CommunicationChannel channel =
                                SocketClient.connect("localhost", port);

                            Message hello = new Message(
                                MessageType.HELLO,
                                nodeId,
                                neighbourName,
                                null,
                                "Connection established"
                            );

                            channel.send(hello);

                            connections.addConnection(
                                neighbourName,
                                channel
                            );

                            System.out.println(
                                "Connected with " + neighbourName
                            );

                            startReceiver(neighbourName, channel);

                            break;

                        } catch (Exception e) {

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }
                });

                connectionThread.setDaemon(true);
                connectionThread.start();
            }
        }
    }

    // Handle incoming socket connection
    private void handleIncomingConnection(Socket socket) {

        try {

            CommunicationChannel channel =
                new CommunicationChannel(socket);

            Message hello = channel.receive();

            if (hello.getType() != MessageType.HELLO) {

                System.out.println("Invalid connection received.");
                channel.close();
                return;
            }

            String neighbourName = hello.getFrom();

            connections.addConnection(
                neighbourName,
                channel
            );

            System.out.println(
                "Connected with " + neighbourName
            );

            startReceiver(neighbourName, channel);

        } catch (Exception e) {

            System.out.println(
                "Incoming connection error: " + e.getMessage()
            );

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    // --------------------------------------------------
    // UPDATED RECEIVER THREAD
    // --------------------------------------------------

    private void startReceiver(
            String neighbourName,
            CommunicationChannel channel) {

        Thread receiver = new Thread(() -> {

            while (running) {

                Message message;

                // PART 1: Receive message from socket
                try {

                    message = channel.receive();

                    if (message == null) {
                        throw new IOException(
                            "Received null message."
                        );
                    }

                } catch (Exception e) {

                    if (running) {

                        System.out.println(
                            "\nTCP CONNECTION PROBLEM with "
                            + neighbourName
                        );

                        System.out.println(
                            "Reason: " + e.getMessage()
                        );

                        e.printStackTrace();
                    }

                    // Remove only this connection.
                    // Do not remove a newer replacement connection.
                    try {

                        if (connections.getConnection(neighbourName)
                                == channel) {

                            connections.removeConnection(neighbourName);

                        } else {

                            channel.close();
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    break;
                }

                // PART 2: Process received message separately
                try {

                    handleMessage(message);

                } catch (Exception e) {

                    System.out.println(
                        "\nMESSAGE PROCESSING ERROR at "
                        + nodeId
                        + " from "
                        + neighbourName
                    );

                    e.printStackTrace();

                    // Keep the connection alive.
                    // Continue receiving subsequent messages.
                }
            }

        });

        receiver.setName(
            "Receiver-" + nodeId + "-" + neighbourName
        );

        receiver.setDaemon(true);
        receiver.start();
    }

    // --------------------------------------------------
    // MESSAGE HANDLING
    // --------------------------------------------------

    private void handleMessage(Message message) {

        // Record application messages before processing.
        if (message.getType() == MessageType.REQUEST ||
            message.getType() == MessageType.TOKEN) {

            snapshotManager.recordMessage(
                message.getFrom(),
                message.toString()
            );
        }

        switch (message.getType()) {

            case REQUEST:

                System.out.println(
                    "REQUEST received from " + message.getFrom()
                );

                algorithm.receiveRequest(message.getFrom());
                break;

            case TOKEN:

                System.out.println(
                    "TOKEN received from " + message.getFrom()
                );

                Token receivedToken =
                    new Token(message.getPayload());

                algorithm.receiveToken(receivedToken);
                break;

            case MARKER:

                System.out.println(
                    "MARKER received from " + message.getFrom() +
                    " for snapshot " + message.getSnapshotId()
                );

                snapshotManager.receiveMarker(
                    message.getSnapshotId(),
                    message.getPayload(),
                    message.getFrom(),
                    getNeighbourNames(),
                    algorithm::captureSnapshotState
                );
                break;

            case SNAPSHOT_REPORT:

                handleSnapshotReport(message);
                break;

            case TREE_REQUEST:

                handleTreeRequest(message);
                break;

            case TREE_RESPONSE:

                handleTreeResponse(message);
                break;

            case HELLO:

                System.out.println(
                    "HELLO received from " + message.getFrom()
                );
                break;

            default:

                System.out.println(
                    "Received message: " + message
                );
        }
    }

    // --------------------------------------------------
    // RAYMOND ALGORITHM
    // --------------------------------------------------

    // Send REQUEST to holder
    private void sendRequest(String holder) {

        Message request = new Message(
            MessageType.REQUEST,
            nodeId,
            holder,
            null,
            "Requesting token"
        );

        sendMessage(holder, request);
    }

    // Send TOKEN to next university
    private void sendToken(String receiver, Token token) {

        Message tokenMessage = new Message(
            MessageType.TOKEN,
            nodeId,
            receiver,
            null,
            token.getTokenId()
        );

        sendMessage(receiver, tokenMessage);
    }

    // --------------------------------------------------
    // DISTRIBUTED SNAPSHOT
    // --------------------------------------------------

    // Start a global snapshot
    public void startSnapshot() {

        String snapshotId =
            nodeId + "-" + System.currentTimeMillis();

        System.out.println(
            "\nStarting global snapshot: " + snapshotId
        );

        synchronized (collectedSnapshots) {

            collectedSnapshots.put(
                snapshotId,
                new HashMap<>()
            );
        }

        snapshotManager.startSnapshot(
            snapshotId,
            getNeighbourNames(),
            algorithm::captureSnapshotState
        );
    }

    // Send a MARKER to a neighbour
    private void sendMarker(
            String neighbour,
            String snapshotId,
            String coordinator) {

        Message marker = new Message(
            MessageType.MARKER,
            nodeId,
            neighbour,
            snapshotId,
            coordinator
        );

        sendMessage(neighbour, marker);
    }

    // Send completed snapshot report towards coordinator
    private void sendSnapshotReport(SnapshotReport report) {

        try {

            String coordinator =
                snapshotManager.getCoordinator(report.getSnapshotId());

            if (coordinator == null) {
                System.out.println("Snapshot coordinator not found.");
                return;
            }

            String payload = encodeReport(report, coordinator);

            if (coordinator.equals(nodeId)) {
                collectSnapshotReport(report);
                return;
            }

            String nextHop = findNextHop(coordinator);

            Message message = new Message(
                MessageType.SNAPSHOT_REPORT,
                nodeId,
                nextHop,
                report.getSnapshotId(),
                payload
            );

            sendMessage(nextHop, message);

        } catch (Exception e) {

            System.out.println(
                "Snapshot report sending failed: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // --------------------------------------------------
    // PHYSICAL ROUTING
    // --------------------------------------------------

    // Find next physical hop towards a destination using BFS
    private String findNextHop(String destination) {

        int n = config.getUniversityCount();

        int destinationIndex = -1;

        for (int i = 0; i < n; i++) {

            if (config.getUniversityName(i).equals(destination)) {
                destinationIndex = i;
                break;
            }
        }

        if (destinationIndex == -1) {
            throw new IllegalArgumentException(
                "Unknown destination: " + destination
            );
        }

        if (destinationIndex == nodeIndex) {
            return nodeId;
        }

        int[] parent = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = -1;
        }

        Queue<Integer> queue = new LinkedList<>();

        queue.add(nodeIndex);
        parent[nodeIndex] = nodeIndex;

        while (!queue.isEmpty()) {

            int current = queue.poll();

            if (current == destinationIndex) {
                break;
            }

            for (int neighbour : config.getNeighbours(current)) {

                if (parent[neighbour] == -1) {
                    parent[neighbour] = current;
                    queue.add(neighbour);
                }
            }
        }

        int current = destinationIndex;

        while (parent[current] != nodeIndex &&
               parent[current] != current) {

            current = parent[current];

            if (parent[current] == -1) {
                throw new IllegalStateException(
                    "No physical path to " + destination
                );
            }
        }

        return config.getUniversityName(current);
    }

    // --------------------------------------------------
    // SNAPSHOT SERIALIZATION
    // --------------------------------------------------

    // Serialize report into Base64 payload
    private String encodeReport(
            SnapshotReport report,
            String coordinator) throws IOException {

        ByteArrayOutputStream bytes =
            new ByteArrayOutputStream();

        DataOutputStream out =
            new DataOutputStream(bytes);

        SnapshotState state = report.getLocalState();

        out.writeUTF(coordinator);
        out.writeUTF(report.getSnapshotId());
        out.writeUTF(report.getUniversityName());

        out.writeUTF(state.getHolder());
        out.writeBoolean(state.hasToken());
        out.writeBoolean(state.isAsked());
        out.writeBoolean(state.isInCriticalSection());

        List<String> queue = state.getRequestQueue();

        out.writeInt(queue.size());

        for (String request : queue) {
            out.writeUTF(request);
        }

        Map<String, List<String>> channels =
            report.getChannelStates();

        out.writeInt(channels.size());

        for (Map.Entry<String, List<String>> entry
                : channels.entrySet()) {

            out.writeUTF(entry.getKey());

            List<String> messages = entry.getValue();

            out.writeInt(messages.size());

            for (String message : messages) {
                out.writeUTF(message);
            }
        }

        out.flush();

        return Base64.getEncoder().encodeToString(
            bytes.toByteArray()
        );
    }

    // Decode a report received over the network
    private SnapshotReport decodeReport(String payload)
            throws IOException {

        byte[] bytes = Base64.getDecoder().decode(payload);

        DataInputStream in = new DataInputStream(
            new ByteArrayInputStream(bytes)
        );

        in.readUTF(); // Coordinator

        String snapshotId = in.readUTF();
        String university = in.readUTF();

        String holder = in.readUTF();
        boolean hasToken = in.readBoolean();
        boolean asked = in.readBoolean();
        boolean inCS = in.readBoolean();

        int queueSize = in.readInt();

        List<String> queue = new ArrayList<>();

        for (int i = 0; i < queueSize; i++) {
            queue.add(in.readUTF());
        }

        SnapshotState state = new SnapshotState(
            university,
            holder,
            hasToken,
            asked,
            inCS,
            queue
        );

        int channelCount = in.readInt();

        Map<String, List<String>> channels =
            new HashMap<>();

        for (int i = 0; i < channelCount; i++) {

            String channel = in.readUTF();

            int messageCount = in.readInt();

            List<String> messages = new ArrayList<>();

            for (int j = 0; j < messageCount; j++) {
                messages.add(in.readUTF());
            }

            channels.put(channel, messages);
        }

        return new SnapshotReport(
            snapshotId,
            university,
            state,
            channels
        );
    }

    // Handle a report travelling towards its coordinator
    private void handleSnapshotReport(Message message) {

        try {

            byte[] bytes =
                Base64.getDecoder().decode(message.getPayload());

            DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(bytes)
            );

            String coordinator = in.readUTF();

            if (!coordinator.equals(nodeId)) {

                String nextHop = findNextHop(coordinator);

                Message forwarded = new Message(
                    MessageType.SNAPSHOT_REPORT,
                    nodeId,
                    nextHop,
                    message.getSnapshotId(),
                    message.getPayload()
                );

                sendMessage(nextHop, forwarded);
                return;
            }

            SnapshotReport report =
                decodeReport(message.getPayload());

            collectSnapshotReport(report);

        } catch (Exception e) {

            System.out.println(
                "Snapshot report error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // Collect reports at the initiating university
    private void collectSnapshotReport(SnapshotReport report) {

        String snapshotId = report.getSnapshotId();

        Map<String, SnapshotReport> reports;

        synchronized (collectedSnapshots) {

            reports = collectedSnapshots.computeIfAbsent(
                snapshotId,
                key -> new HashMap<>()
            );

            reports.putIfAbsent(
                report.getUniversityName(),
                report
            );

            System.out.println(
                "Snapshot report received from " +
                report.getUniversityName() +
                " (" + reports.size() + "/" +
                config.getUniversityCount() + ")"
            );

            if (reports.size() != config.getUniversityCount()) {
                return;
            }
        }

        displayGlobalSnapshot(snapshotId, reports);
    }

    // Display all university reports
    private void displayGlobalSnapshot(
            String snapshotId,
            Map<String, SnapshotReport> reports) {

        System.out.println("\n");
        System.out.println("======================================");
        System.out.println("       GLOBAL SNAPSHOT COMPLETED");
        System.out.println("======================================");

        System.out.println("Snapshot ID: " + snapshotId);

        for (int i = 0; i < config.getUniversityCount(); i++) {

            String university =
                config.getUniversityName(i);

            SnapshotReport report = reports.get(university);

            if (report != null) {
                report.displayReport();
            }
        }

        System.out.println("======================================");
        System.out.println("       END OF GLOBAL SNAPSHOT");
        System.out.println("======================================");
    }

    // --------------------------------------------------
    // LIVE TREE VISUALIZATION
    // --------------------------------------------------

   
private void handleTreeRequest(Message message) {

    String destination = message.getTo();
    String requester = message.getPayload();
    String requestId = message.getSnapshotId();

    // Check whether this university is the destination.
    if (destination.equals(nodeId)) {

        // Find the next physical neighbour towards the requester.
        String nextHop = findNextHop(requester);

        // Prepare the response for the original requester.
        Message response = new Message(
            messaging.MessageType.TREE_RESPONSE,
            nodeId,
            requester,
            requestId,
            algorithm.getHolder()
        );

        try {
            // Send through the physical tree, not directly.
            sendMessage(nextHop, response);

        } catch (Exception e) {
            System.out.println(
                "Error sending tree response to " + requester
            );
            e.printStackTrace();
        }

        return;
    }

    // This university is an intermediate forwarding node.
    String nextHop = findNextHop(destination);

    Message forwardedRequest = new Message(
        messaging.MessageType.TREE_REQUEST,
        message.getFrom(),
        destination,
        requestId,
        requester
    );

    try {
        sendMessage(nextHop, forwardedRequest);

    } catch (Exception e) {
        System.out.println("Error forwarding tree request.");
        e.printStackTrace();
    }
}

    // Handle a response containing a holder pointer
    private void handleTreeResponse(Message message) {

        String destination = message.getTo();

        if (nodeId.equals(destination)) {

            synchronized (treeResponses) {

                Map<String, String> responses =
                    treeResponses.get(message.getSnapshotId());

                if (responses != null) {

                    responses.put(
                        message.getFrom(),
                        message.getPayload()
                    );

                    treeResponses.notifyAll();
                }
            }

            return;
        }

        String nextHop = findNextHop(destination);

        Message forwarded = new Message(
            MessageType.TREE_RESPONSE,
            message.getFrom(),
            destination,
            message.getSnapshotId(),
            message.getPayload()
        );

        sendMessage(nextHop, forwarded);
    }

    // Collect live holder pointers and display both trees
    public void displayLiveTrees() {

        String requestId =
            nodeId + "-TREE-" + System.currentTimeMillis();

        Map<String, String> responses = new HashMap<>();

        responses.put(nodeId, algorithm.getHolder());

        synchronized (treeResponses) {
            treeResponses.put(requestId, responses);
        }

        System.out.println("\nCollecting live tree information...");

        // Request information from every other university
        for (int i = 0; i < config.getUniversityCount(); i++) {

            String destination = config.getUniversityName(i);

            if (destination.equals(nodeId)) {
                continue;
            }

            try {

                String nextHop = findNextHop(destination);

                Message request = new Message(
                    MessageType.TREE_REQUEST,
                    nodeId,
                    destination,
                    requestId,
                    nodeId
                );

                sendMessage(nextHop, request);

            } catch (Exception e) {

                System.out.println(
                    "Could not request tree information from " +
                    destination + ": " + e.getMessage()
                );
            }
        }

        // Wait for all responses, maximum five seconds
        long deadline = System.currentTimeMillis() + 5000;

        synchronized (treeResponses) {

            while (responses.size() < config.getUniversityCount()) {

                long remaining =
                    deadline - System.currentTimeMillis();

                if (remaining <= 0) {
                    break;
                }

                try {
                    treeResponses.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            treeResponses.remove(requestId);
        }

        if (responses.size() != config.getUniversityCount()) {

            System.out.println(
                "\nTree information incomplete: received " +
                responses.size() + "/" +
                config.getUniversityCount() + " responses."
            );

            return;
        }

        String[] holderPointers =
            new String[config.getUniversityCount()];

        for (int i = 0; i < config.getUniversityCount(); i++) {

            String university = config.getUniversityName(i);

            holderPointers[i] = responses.get(university);
        }

        System.out.println("\nAll university responses received!");

        TreeVisualizer.displayBothTrees(config, holderPointers);
    }

    // --------------------------------------------------
    // COMMON MESSAGE SENDING
    // --------------------------------------------------

    // Send message through an established connection
    private void sendMessage(String neighbour, Message message) {

        int attempts = 0;

        while (running && attempts < 300) {

            CommunicationChannel channel =
                connections.getConnection(neighbour);

            if (channel != null) {

                try {

                    channel.send(message);
                    return;

                } catch (IOException e) {

                    throw new RuntimeException(
                        "Failed to send message to " + neighbour,
                        e
                    );
                }
            }

            attempts++;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                throw new RuntimeException(
                    "Message sending interrupted."
                );
            }
        }

        throw new RuntimeException(
            "No connection available with " + neighbour
        );
    }

    // --------------------------------------------------
    // GETTERS
    // --------------------------------------------------

    public String getNodeId() {
        return nodeId;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public RaymondAlgorithm getAlgorithm() {
        return algorithm;
    }

    // --------------------------------------------------
    // STOP UNIVERSITY NODE
    // --------------------------------------------------

    public void stop() {

        running = false;

        connections.closeAll();

        System.out.println(
            nodeId + " university node stopped."
        );
    }
}
