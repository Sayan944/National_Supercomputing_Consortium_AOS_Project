
package snapshot;

import java.util.*;
import java.util.function.Supplier;

public class SnapshotManager {

    public interface MarkerSender {
        void send(String neighbour, String snapshotId, String coordinator);
    }

    public interface ReportSender {
        void send(SnapshotReport report);
    }

    private final String nodeId;
    private final Map<String, SnapshotSession> sessions = new HashMap<>();

    private final MarkerSender markerSender;
    private final ReportSender reportSender;

    public SnapshotManager(
            String nodeId,
            MarkerSender markerSender,
            ReportSender reportSender) {

        this.nodeId = nodeId;
        this.markerSender = markerSender;
        this.reportSender = reportSender;
    }

    public void startSnapshot(
            String snapshotId,
            List<String> neighbours,
            Supplier<SnapshotState> stateSupplier) {

        List<String> markers;
        SnapshotReport report;

        synchronized (this) {

            if (sessions.containsKey(snapshotId)) {
                return;
            }

            SnapshotSession session = new SnapshotSession(
                    snapshotId,
                    nodeId,
                    neighbours,
                    stateSupplier.get()
            );

            sessions.put(snapshotId, session);

            markers = new ArrayList<>(neighbours);
            report = completeIfReady(session);
        }

        sendMarkers(snapshotId, nodeId, markers);

        if (report != null) {
            reportSender.send(report);
        }
    }

    public void receiveMarker(
            String snapshotId,
            String coordinator,
            String from,
            List<String> neighbours,
            Supplier<SnapshotState> stateSupplier) {

        List<String> markers = new ArrayList<>();
        SnapshotReport report = null;

        synchronized (this) {

            SnapshotSession session = sessions.get(snapshotId);

            if (session == null) {

                if (!neighbours.contains(from)) {
                    return;
                }

                session = new SnapshotSession(
                        snapshotId,
                        coordinator,
                        neighbours,
                        stateSupplier.get()
                );

                sessions.put(snapshotId, session);

                // Send markers on ALL outgoing channels.
                // Each direction of a TCP connection is a separate channel.
                markers.addAll(neighbours);

                // The incoming channel from the first marker is now closed.
                session.openChannels.remove(from);

                report = completeIfReady(session);

            } else {

                if (!session.coordinator.equals(coordinator)) {
                    return;
                }

                // A marker closes the corresponding incoming channel.
                if (session.openChannels.remove(from)) {
                    report = completeIfReady(session);
                }
            }
        }

        // Send markers outside the synchronized block.
        sendMarkers(snapshotId, coordinator, markers);

        if (report != null) {
            reportSender.send(report);
        }
    }

    public synchronized void recordMessage(
            String from,
            String message) {

        for (SnapshotSession session : sessions.values()) {

            if (!session.completed &&
                    session.openChannels.contains(from)) {

                session.channelStates
                        .computeIfAbsent(from, k -> new ArrayList<>())
                        .add(message);
            }
        }
    }

    public synchronized boolean hasSnapshot(String snapshotId) {
        return sessions.containsKey(snapshotId);
    }

    public synchronized String getCoordinator(String snapshotId) {

        SnapshotSession session = sessions.get(snapshotId);

        if (session == null) {
            return null;
        }

        return session.coordinator;
    }

    private SnapshotReport completeIfReady(SnapshotSession session) {

        if (session.openChannels.isEmpty() && !session.completed) {

            session.completed = true;

            return new SnapshotReport(
                    session.snapshotId,
                    nodeId,
                    session.localState,
                    session.channelStates
            );
        }

        return null;
    }

    private void sendMarkers(
            String snapshotId,
            String coordinator,
            List<String> neighbours) {

        for (String neighbour : neighbours) {
            markerSender.send(neighbour, snapshotId, coordinator);
        }
    }

    private static class SnapshotSession {

        String snapshotId;
        String coordinator;

        SnapshotState localState;

        Set<String> openChannels = new HashSet<>();

        Map<String, List<String>> channelStates = new HashMap<>();

        boolean completed = false;

        SnapshotSession(
                String snapshotId,
                String coordinator,
                List<String> neighbours,
                SnapshotState localState) {

            this.snapshotId = snapshotId;
            this.coordinator = coordinator;
            this.localState = localState;

            openChannels.addAll(neighbours);

            for (String neighbour : neighbours) {
                channelStates.put(neighbour, new ArrayList<>());
            }
        }
    }
}
