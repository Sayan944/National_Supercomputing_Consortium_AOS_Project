
package config;

import java.util.*;
import java.io.*;

public class RaymondSetup {

    static Scanner sc = new Scanner(System.in);

    // Find representative of a set
    static int find(int[] parent, int x) {

        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }

        return parent[x];
    }

    // Join two sets and detect cycles
    static boolean union(int[] parent, int a, int b) {

        int rootA = find(parent, a);
        int rootB = find(parent, b);

        if (rootA == rootB) {
            return false;
        }

        parent[rootA] = rootB;
        return true;
    }

    // Read a valid integer within a given range
    static int readInt(String message, int min, int max) {

        while (true) {

            System.out.print(message);

            if (sc.hasNextInt()) {

                int value = sc.nextInt();
                sc.nextLine();

                if (value >= min && value <= max) {
                    return value;
                }
            }
            else {
                sc.nextLine();
            }

            System.out.println(
                "Invalid input. Enter a value between "
                + min + " and " + max + ".");
        }
    }

    // Display the physical tree
    static void displayTree(
            ArrayList<Integer>[] adj,
            String[] names,
            int node,
            int parent,
            String prefix,
            boolean last) {

        System.out.println(
            prefix + (last ? "\\-- " : "|-- ") + names[node]);

        ArrayList<Integer> children = new ArrayList<>();

        for (int v : adj[node]) {

            if (v != parent) {
                children.add(v);
            }
        }

        for (int i = 0; i < children.size(); i++) {

            int child = children.get(i);

            displayTree(
                adj,
                names,
                child,
                node,
                prefix + (last ? "    " : "|   "),
                i == children.size() - 1
            );
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("==========================================");
        System.out.println(" NATIONAL SUPERCOMPUTING CONSORTIUM");
        System.out.println(" RAYMOND'S TREE-BASED MUTUAL EXCLUSION");
        System.out.println("==========================================");

        // STEP 1: Number of universities

        int n = readInt(
            "\nEnter number of universities (minimum 5): ",
            5, 100
        );

        String[] names = new String[n];
        int[] ports = new int[n];

        // STEP 2: University names and ports

        System.out.println("\n--- UNIVERSITY DETAILS ---");

        for (int i = 0; i < n; i++) {

            while (true) {

                System.out.print(
                    "Enter university " + (i + 1) + " name: ");

                String name = sc.nextLine().trim();

                if (name.isEmpty() || name.matches(".*\\s+.*")) {
                    System.out.println(
                        "Use a non-empty name without spaces.");
                    continue;
                }

                boolean duplicate = false;

                for (int j = 0; j < i; j++) {

                    if (names[j].equalsIgnoreCase(name)) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    System.out.println("Duplicate university name.");
                    continue;
                }

                names[i] = name;
                break;
            }

            while (true) {

                int port = readInt(
                    "Enter port for " + names[i] + ": ",
                    1024, 65535
                );

                boolean duplicate = false;

                for (int j = 0; j < i; j++) {

                    if (ports[j] == port) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    System.out.println("Port already in use.");
                    continue;
                }

                ports[i] = port;
                break;
            }
        }

        // STEP 3: Select root

        System.out.println("\n--- SELECT ROOT UNIVERSITY ---");

        for (int i = 0; i < n; i++) {
            System.out.println((i + 1) + ". " + names[i]);
        }

        int root = readInt(
            "Enter root university number: ", 1, n
        ) - 1;

        // STEP 4: Initialize adjacency list

        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] adj = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>();
        }

        int[] dsu = new int[n];

        for (int i = 0; i < n; i++) {
            dsu[i] = i;
        }

        int[][] edges = new int[n - 1][2];

        // STEP 5: Enter physical edges

        System.out.println("\n--- DEFINE PHYSICAL EDGES ---");

        System.out.println(
            "Exactly " + (n - 1) + " edges are required.");

        for (int i = 0; i < n - 1; i++) {

            System.out.println("\nAvailable universities:");

            for (int j = 0; j < n; j++) {
                System.out.println((j + 1) + ". " + names[j]);
            }

            int u = readInt(
                "Enter first university number: ", 1, n
            ) - 1;

            int v = readInt(
                "Enter second university number: ", 1, n
            ) - 1;

            if (u == v) {
                System.out.println(
                    "A university cannot connect to itself.");
                i--;
                continue;
            }

            if (adj[u].contains(v)) {
                System.out.println("Duplicate edge.");
                i--;
                continue;
            }

            if (!union(dsu, u, v)) {
                System.out.println(
                    "This connection creates a cycle.");
                i--;
                continue;
            }

            adj[u].add(v);
            adj[v].add(u);

            edges[i][0] = u;
            edges[i][1] = v;

            System.out.println(
                "Connected " + names[u] +
                " <----> " + names[v]);
        }

        // STEP 6: Select initial token holder

        System.out.println("\n--- INITIAL TOKEN LOCATION ---");

        for (int i = 0; i < n; i++) {
            System.out.println((i + 1) + ". " + names[i]);
        }

        int token = readInt(
            "Enter initial token holder number: ", 1, n
        ) - 1;

        // STEP 7: Create configuration object

        NetworkConfig config = new NetworkConfig(
            names,
            ports,
            edges,
            root,
            token
        );

        // STEP 8: Save configuration

        try (PrintWriter out =
                new PrintWriter("raymond.conf")) {

            out.println(n);
            out.println(root);
            out.println(token);

            for (int i = 0; i < n; i++) {
                out.println(names[i] + " " + ports[i]);
            }

            for (int[] edge : edges) {
                out.println(edge[0] + " " + edge[1]);
            }
        }

        // STEP 9: Display generated tree

        System.out.println("\n==========================================");
        System.out.println(" PHYSICAL COMMUNICATION TREE");
        System.out.println("==========================================");

        System.out.println(names[root] + " (ROOT)");

        // BFS to determine tree orientation
        boolean[] visited = new boolean[n];

        Queue<Integer> queue = new LinkedList<>();

        queue.add(root);
        visited[root] = true;

        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        while (!queue.isEmpty()) {

            int u = queue.poll();

            for (int v : adj[u]) {

                if (!visited[v]) {

                    visited[v] = true;
                    parent[v] = u;
                    queue.add(v);
                }
            }
        }

        // Display root's children
        ArrayList<Integer> rootChildren = new ArrayList<>();

        for (int i = 0; i < n; i++) {

            if (parent[i] == root) {
                rootChildren.add(i);
            }
        }

        for (int i = 0; i < rootChildren.size(); i++) {

            int child = rootChildren.get(i);

            displayTree(
                adj,
                names,
                child,
                root,
                "",
                i == rootChildren.size() - 1
            );
        }

        // STEP 10: Display final configuration

        config.displayConfiguration();

        System.out.println(
            "Configuration saved successfully in raymond.conf");
    }
}
