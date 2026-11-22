
package visualization;

import config.NetworkConfig;
import java.util.*;

public class TreeVisualizer {

    // ==========================================
    // 1. DISPLAY PHYSICAL NETWORK TREE
    // ==========================================

    public static void displayPhysicalTree(NetworkConfig config) {

        System.out.println("\n======================================");
        System.out.println("       PHYSICAL NETWORK TREE");
        System.out.println("======================================");

        int root = config.getRootIndex();

        boolean[] visited =
            new boolean[config.getUniversityCount()];

        displayPhysicalRecursive(
            config,
            root,
            -1,
            visited,
            ""
        );

        System.out.println("======================================\n");
    }

    private static void displayPhysicalRecursive(
            NetworkConfig config,
            int current,
            int parent,
            boolean[] visited,
            String indent) {

        visited[current] = true;

        String name = config.getUniversityName(current);

        if (parent == -1) {
            System.out.println(name + " (ROOT)");
        } else {
            System.out.println(indent + "|-- " + name);
        }

        for (int neighbour : config.getNeighbours(current)) {

            if (neighbour != parent && !visited[neighbour]) {

                displayPhysicalRecursive(
                    config,
                    neighbour,
                    current,
                    visited,
                    indent + "    "
                );
            }
        }
    }


    // ==========================================
    // 2. DISPLAY DYNAMIC LOGICAL HOLDER TREE
    // ==========================================

    public static void displayLogicalTree(
            NetworkConfig config,
            String[] holderPointers) {

        int n = config.getUniversityCount();

        System.out.println("\n======================================");
        System.out.println("       LOGICAL HOLDER TREE");
        System.out.println("======================================");

        if (holderPointers == null ||
                holderPointers.length != n) {

            System.out.println(
                "Invalid holder pointer information."
            );

            return;
        }

        int tokenHolder = -1;

        // Find the university holding the token
        for (int i = 0; i < n; i++) {

            if (holderPointers[i] == null) {
                System.out.println(
                    "Missing holder pointer for " +
                    config.getUniversityName(i)
                );
                return;
            }

            if (holderPointers[i].equalsIgnoreCase(
                    config.getUniversityName(i))) {

                if (tokenHolder != -1) {
                    System.out.println(
                        "Error: Multiple token holders detected."
                    );
                    return;
                }

                tokenHolder = i;
            }
        }

        if (tokenHolder == -1) {

            System.out.println(
                "Token holder could not be identified."
            );

            return;
        }

        System.out.println(
            "Current Token Holder: " +
            config.getUniversityName(tokenHolder)
        );

        // Build logical child relationships
        List<List<Integer>> children = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            children.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {

            if (i == tokenHolder) {
                continue;
            }

            int parentIndex = findUniversityIndex(
                config,
                holderPointers[i]
            );

            if (parentIndex == -1) {

                System.out.println(
                    "Invalid holder: " + holderPointers[i]
                );

                return;
            }

            // Raymond holder must be a physical neighbour
            if (!config.getNeighbours(i).contains(parentIndex)) {

                System.out.println(
                    "Invalid holder relationship: " +
                    config.getUniversityName(i) +
                    " -> " +
                    config.getUniversityName(parentIndex)
                );

                return;
            }

            children.get(parentIndex).add(i);
        }

        // Sort children for consistent display
        for (List<Integer> childList : children) {
            Collections.sort(childList);
        }

        boolean[] visited = new boolean[n];

        // Display tree from the current token holder
        displayLogicalRecursive(
            config,
            tokenHolder,
            tokenHolder,
            children,
            visited,
            ""
        );

        // Check whether all universities are connected
        for (int i = 0; i < n; i++) {

            if (!visited[i]) {

                System.out.println(
                    "Warning: Logical holder tree is disconnected."
                );

                return;
            }
        }

        System.out.println("======================================\n");
    }


    // ==========================================
    // 3. RECURSIVE LOGICAL TREE DISPLAY
    // ==========================================

    private static void displayLogicalRecursive(
            NetworkConfig config,
            int current,
            int tokenHolder,
            List<List<Integer>> children,
            boolean[] visited,
            String indent) {

        if (visited[current]) {

            System.out.println(
                indent + "Cycle detected!"
            );

            return;
        }

        visited[current] = true;

        String name = config.getUniversityName(current);

        if (current == tokenHolder) {

            System.out.println(
                indent + name + " [TOKEN]"
            );

        } else {

            System.out.println(
                indent + "|-- " + name
            );
        }

        for (int child : children.get(current)) {

            displayLogicalRecursive(
                config,
                child,
                tokenHolder,
                children,
                visited,
                indent + "    "
            );
        }
    }


    // ==========================================
    // 4. FIND UNIVERSITY INDEX BY NAME
    // ==========================================

    private static int findUniversityIndex(
            NetworkConfig config,
            String universityName) {

        for (int i = 0;
             i < config.getUniversityCount();
             i++) {

            if (config.getUniversityName(i)
                    .equalsIgnoreCase(universityName)) {

                return i;
            }
        }

        return -1;
    }


    // ==========================================
    // 5. DISPLAY BOTH TREES
    // ==========================================

    public static void displayBothTrees(
            NetworkConfig config,
            String[] holderPointers) {

        displayPhysicalTree(config);

        displayLogicalTree(config, holderPointers);
    }
}
