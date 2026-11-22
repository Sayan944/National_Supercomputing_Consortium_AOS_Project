
package config;

import java.util.*;

public class NetworkConfig {

    private final String[] universityNames;
    private final int[] ports;
    private final int[][] edges;

    private final int rootIndex;
    private final int initialTokenIndex;

    public NetworkConfig(
            String[] universityNames,
            int[] ports,
            int[][] edges,
            int rootIndex,
            int initialTokenIndex) {

        this.universityNames = universityNames.clone();
        this.ports = ports.clone();

        this.edges = new int[edges.length][2];

        for (int i = 0; i < edges.length; i++) {
            this.edges[i] = edges[i].clone();
        }

        this.rootIndex = rootIndex;
        this.initialTokenIndex = initialTokenIndex;
    }

    public int getUniversityCount() {
        return universityNames.length;
    }

    public String getUniversityName(int index) {
        return universityNames[index];
    }

    public int getPort(int index) {
        return ports[index];
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public int getInitialTokenIndex() {
        return initialTokenIndex;
    }

    public String getInitialTokenHolder() {
        return universityNames[initialTokenIndex];
    }

    public String getRootName() {
        return universityNames[rootIndex];
    }

    public int[][] getEdges() {

        int[][] copy = new int[edges.length][2];

        for (int i = 0; i < edges.length; i++) {
            copy[i] = edges[i].clone();
        }

        return copy;
    }

    // Find all physical neighbours of a university
    public List<Integer> getNeighbours(int universityIndex) {

        List<Integer> neighbours = new ArrayList<>();

        for (int[] edge : edges) {

            if (edge[0] == universityIndex) {
                neighbours.add(edge[1]);
            }

            else if (edge[1] == universityIndex) {
                neighbours.add(edge[0]);
            }
        }

        return neighbours;
    }

    public void displayConfiguration() {

        System.out.println("\n========== NETWORK CONFIGURATION ==========");

        System.out.println("Total Universities: " + getUniversityCount());
        System.out.println("Root: " + getRootName());
        System.out.println("Initial Token Holder: " + getInitialTokenHolder());

        System.out.println("\nUniversity Details:");

        for (int i = 0; i < getUniversityCount(); i++) {

            System.out.println(
                i + " | " +
                universityNames[i] +
                " | Port: " + ports[i]
            );
        }

        System.out.println("\nPhysical Edges:");

        for (int[] edge : edges) {

            System.out.println(
                universityNames[edge[0]] +
                " <----> " +
                universityNames[edge[1]]
            );
        }

        System.out.println("===========================================\n");
    }
}
