
package config;

import java.io.*;
import java.util.*;

public class ConfigLoader {

    public static NetworkConfig load(String filename)
            throws IOException {

        try (Scanner sc = new Scanner(new File(filename))) {

            // Read number of universities
            int n = sc.nextInt();

            if (n < 5) {
                throw new IOException(
                    "At least 5 universities are required.");
            }

            // Read root and initial token holder
            int root = sc.nextInt();
            int token = sc.nextInt();

            if (root < 0 || root >= n ||
                token < 0 || token >= n) {

                throw new IOException(
                    "Invalid root or token holder index.");
            }

            String[] names = new String[n];
            int[] ports = new int[n];

            // Read university details
            Set<String> uniqueNames = new HashSet<>();
            Set<Integer> uniquePorts = new HashSet<>();

            for (int i = 0; i < n; i++) {

                names[i] = sc.next();
                ports[i] = sc.nextInt();

                if (!uniqueNames.add(names[i].toLowerCase())) {
                    throw new IOException(
                        "Duplicate university name: " + names[i]);
                }

                if (ports[i] < 1024 || ports[i] > 65535 ||
                    !uniquePorts.add(ports[i])) {

                    throw new IOException(
                        "Invalid or duplicate port: " + ports[i]);
                }
            }

            // Read physical edges
            int[][] edges = new int[n - 1][2];

            int[] parent = new int[n];

            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }

            for (int i = 0; i < n - 1; i++) {

                int u = sc.nextInt();
                int v = sc.nextInt();

                if (u < 0 || u >= n ||
                    v < 0 || v >= n || u == v) {

                    throw new IOException("Invalid edge.");
                }

                int a = find(parent, u);
                int b = find(parent, v);

                if (a == b) {
                    throw new IOException(
                        "Cycle detected in network.");
                }

                parent[a] = b;

                // Store original physical edge
                edges[i][0] = u;
                edges[i][1] = v;
            }

            // Check connectivity
            int representative = find(parent, 0);

            for (int i = 1; i < n; i++) {

                if (find(parent, i) != representative) {
                    throw new IOException(
                        "Network is disconnected.");
                }
            }

            if (sc.hasNext()) {
                throw new IOException(
                    "Unexpected extra configuration data.");
            }

            return new NetworkConfig(
                names,
                ports,
                edges,
                root,
                token
            );
        }
        catch (NoSuchElementException e) {

            throw new IOException(
                "Configuration file is incomplete.", e);
        }
    }

    private static int find(int[] parent, int x) {

        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }

        return parent[x];
    }
}
