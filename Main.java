
import university.UniversityNode;

public class Main {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" NATIONAL SUPERCOMPUTING CONSORTIUM");
        System.out.println(" Distributed Mutual Exclusion System");
        System.out.println("======================================");

        if (args.length != 1) {

            System.out.println(
                "\nUsage: java Main <UniversityName>"
            );

            System.out.println("\nAvailable universities:");

            System.out.println("CU");
            System.out.println("BU");
            System.out.println("KU");
            System.out.println("JU");
            System.out.println("JNU");

            return;
        }

        System.out.println(
            "\nStarting university: " + args[0]
        );

        UniversityNode.main(args);
    }
}
