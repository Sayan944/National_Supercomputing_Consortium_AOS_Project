
package raymond;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

public class RequestQueue {

    private Queue<String> requests;

    public RequestQueue() {
        requests = new LinkedList<>();
    }

    // Add a university request
    public synchronized void enqueue(String university) {

        if (!requests.contains(university)) {
            requests.add(university);
        }
    }

    // Remove and return the first request
    public synchronized String dequeue() {
        return requests.poll();
    }

    // View the first request
    public synchronized String peek() {
        return requests.peek();
    }

    // Check whether queue is empty
    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }

    // Check whether a university is already waiting
    public synchronized boolean contains(String university) {
        return requests.contains(university);
    }

    // Return a safe copy of the current queue
    public synchronized List<String> getRequestsSnapshot() {
        return new ArrayList<>(requests);
    }

    @Override
    public synchronized String toString() {
        return requests.toString();
    }
}
