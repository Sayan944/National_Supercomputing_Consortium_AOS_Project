
package snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnapshotState {

    private final String universityName;
    private final String holder;
    private final boolean hasToken;
    private final boolean asked;
    private final boolean inCriticalSection;
    private final List<String> requestQueue;

    public SnapshotState(
            String universityName,
            String holder,
            boolean hasToken,
            boolean asked,
            boolean inCriticalSection,
            List<String> requestQueue) {

        this.universityName = universityName;
        this.holder = holder;
        this.hasToken = hasToken;
        this.asked = asked;
        this.inCriticalSection = inCriticalSection;

        this.requestQueue = new ArrayList<>(requestQueue);
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getHolder() {
        return holder;
    }

    public boolean hasToken() {
        return hasToken;
    }

    public boolean isAsked() {
        return asked;
    }

    public boolean isInCriticalSection() {
        return inCriticalSection;
    }

    public List<String> getRequestQueue() {
        return Collections.unmodifiableList(requestQueue);
    }

    @Override
    public String toString() {

        return "\nUniversity: " + universityName +
               "\nHolder: " + holder +
               "\nHas Token: " + hasToken +
               "\nAsked: " + asked +
               "\nIn Critical Section: " + inCriticalSection +
               "\nRequest Queue: " + requestQueue;
    }
}
