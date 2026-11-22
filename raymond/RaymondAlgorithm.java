
package raymond;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

import snapshot.SnapshotState;

public class RaymondAlgorithm {

    private String nodeId;
    private String holder;

    private Token token;
    private boolean asked;
    private boolean inCriticalSection;

    private RequestQueue requestQueue;

    // Network communication callbacks
    private Consumer<String> requestSender;
    private BiConsumer<String, Token> tokenSender;

    public RaymondAlgorithm(
            String nodeId,
            String holder,
            Token token,
            Consumer<String> requestSender,
            BiConsumer<String, Token> tokenSender) {

        this.nodeId = nodeId;
        this.holder = holder;
        this.token = token;

        this.requestSender = requestSender;
        this.tokenSender = tokenSender;

        this.requestQueue = new RequestQueue();

        this.asked = false;
        this.inCriticalSection = false;
    }

    // Check whether this university has the token
    public synchronized boolean hasToken() {
        return token != null;
    }

    // Local university requests the critical section
    public synchronized void requestCS() {

        if (!requestQueue.contains(nodeId) && !inCriticalSection) {

            requestQueue.enqueue(nodeId);

            System.out.println(nodeId + " requested the token.");
        }

        processQueue();
    }

    // Send a request towards the current holder
    private void sendRequestToHolder() {

        if (!hasToken() && !asked && !holder.equals(nodeId)) {

            asked = true;

            System.out.println(
                nodeId + " sends REQUEST to " + holder
            );

            requestSender.accept(holder);
        }
    }

    // Receive a request from another university
    public synchronized void receiveRequest(String from) {

        System.out.println(
            nodeId + " received REQUEST from " + from
        );

        requestQueue.enqueue(from);

        processQueue();
    }

    // Receive the token from another university
    public synchronized void receiveToken(Token receivedToken) {

        if (hasToken()) {
            throw new IllegalStateException(
                nodeId + " already has the token!"
            );
        }

        token = receivedToken;
        holder = nodeId;
        asked = false;

        System.out.println(
            nodeId + " received the token."
        );

        processQueue();
    }

    // Process the pending request queue
    private void processQueue() {

        if (hasToken() && !inCriticalSection) {

            if (!requestQueue.isEmpty()) {

                String next = requestQueue.dequeue();

                if (next.equals(nodeId)) {

                    inCriticalSection = true;

                    System.out.println(
                        nodeId + " ENTERED critical section."
                    );

                } else {

                    System.out.println(
                        nodeId + " transfers TOKEN to " + next
                    );

                    Token transferredToken = token;

                    token = null;
                    holder = next;
                    asked = false;

                    tokenSender.accept(next, transferredToken);

                    // If requests remain, request the token again
                    if (!requestQueue.isEmpty()) {
                        sendRequestToHolder();
                    }
                }
            }

        } else if (!hasToken()) {

            sendRequestToHolder();
        }
    }

    // Release the critical section
    public synchronized void releaseCS() {

        if (!inCriticalSection) {

            System.out.println(
                nodeId + " is not in the critical section."
            );

            return;
        }

        inCriticalSection = false;

        System.out.println(
            nodeId + " RELEASED critical section."
        );

        processQueue();
    }

    // Display the current local state
    public synchronized void displayStatus() {

        System.out.println("\n--- Raymond Status ---");

        System.out.println("University: " + nodeId);
        System.out.println("Holder: " + holder);
        System.out.println("Has Token: " + hasToken());
        System.out.println("Asked: " + asked);

        System.out.println(
            "In Critical Section: " + inCriticalSection
        );

        System.out.println(
            "Request Queue: " + requestQueue
        );

        System.out.println("----------------------");
    }

    // Existing getters
    public synchronized String getHolder() {
        return holder;
    }

    public synchronized boolean isInCriticalSection() {
        return inCriticalSection;
    }

    public synchronized String getQueueState() {
        return requestQueue.toString();
    }

    public synchronized boolean isAsked() {
        return asked;
    }

    // Capture all local Raymond state together
    public synchronized SnapshotState captureSnapshotState() {

        return new SnapshotState(
            nodeId,
            holder,
            hasToken(),
            asked,
            inCriticalSection,
            requestQueue.getRequestsSnapshot()
        );
    }
}
