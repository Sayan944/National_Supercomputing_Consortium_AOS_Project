
package snapshot;

import java.util.*;

public class SnapshotReport {

    private final String snapshotId;
    private final String universityName;
    private final SnapshotState localState;

    private final Map<String, List<String>> channelStates;

    public SnapshotReport(
            String snapshotId,
            String universityName,
            SnapshotState localState,
            Map<String, List<String>> channelStates) {

        this.snapshotId = snapshotId;
        this.universityName = universityName;
        this.localState = localState;

        this.channelStates = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry
                : channelStates.entrySet()) {

            this.channelStates.put(
                entry.getKey(),
                new ArrayList<>(entry.getValue())
            );
        }
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public SnapshotState getLocalState() {
        return localState;
    }

    public Map<String, List<String>> getChannelStates() {
        Map<String, List<String>> copy = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry
                : channelStates.entrySet()) {

            copy.put(
                entry.getKey(),
                Collections.unmodifiableList(entry.getValue())
            );
        }

        return Collections.unmodifiableMap(copy);
    }

    public void displayReport() {

        System.out.println("\n========== SNAPSHOT REPORT ==========");
        System.out.println("Snapshot ID: " + snapshotId);
        System.out.println(localState);

        System.out.println("\nMessages in Transit:");

        if (channelStates.isEmpty()) {
            System.out.println("No recorded channel messages.");
        } else {
            for (String channel : channelStates.keySet()) {
                System.out.println(
                    channel + " -> " + channelStates.get(channel)
                );
            }
        }

        System.out.println("=====================================");
    }
}
