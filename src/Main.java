import tree.BPlusTree;

import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        //int[] testData = {13, 2, 16, 14, 10, 13, 16, 7};
        userInterface();
    }

    public static void userInterface() {
        System.out.println("Welcome to User Interface");

        Scanner in = new Scanner(System.in);

        BPlusTree tree;

        String command = "";
        String option = "";

        while (!option.equals("exit")) {
            System.out.print("> ");
            command = in.nextLine();
            String[] s = handler(command);
            option = s[0];
            if (s.length > 1) {
                switch (option) {
                    case "btree":
                        // argument 1 = "-help"
                        if (s[1].equals("-help")) {
                            System.out.println("Usage: btree [fname]\n"
                                    + "fname: the name of the data file "
                                    + "storing the search key values");
                        } else {
                            try {
                                tree = new BPlusTree(s[1]);
                                System.out.println("Building an initial B+-Tree...\n" +
                                        "Launching B+-Tree test program…\n");
                            } catch (FileNotFoundException e) {
                                System.out.println("File not found");
                                continue;
                            }

                            while (!option.equals("quit")) {
                                System.out.print("Waiting for your commands: ");
                                command = in.nextLine();
                                s = handler(command);
                                option = s[0];

                                switch (option) {
                                    case "insert":
                                        if (s.length != 4) {
                                            System.out.println("Invalid number of arguments\nUsage: insert <low> <high> <num>");
                                            break;
                                        }
                                        try {
                                            // parse key to int
                                            int low = Integer.parseInt(s[1]);
                                            int high = Integer.parseInt(s[2]);
                                            int num = Integer.parseInt(s[3]);
                                            // insert %num% keys ranged from %low% to %high% into the tree
                                            for (int i = 0; i < num; i++) {
                                                tree.insert(new Random().nextInt(high - low) + low, null);
                                            }
                                        } catch (NumberFormatException e) {
                                            System.out.println("Only integer keys are supported");
                                        }
                                        break;
                                    case "delete":
                                        if (s.length != 2 && s.length != 3){
                                            System.out.println("Invalid number of arguments\n" +
                                                    "Usage: delete <integer value> OR delete <low> <high>");
                                            break;
                                        }
                                        if (s.length == 2){
                                            try {
                                                // parse key to int
                                                int value = Integer.parseInt(s[1]);
                                                tree.delete(value);
                                            } catch (NumberFormatException e) {
                                                System.out.println("Only integer keys are supported");
                                            }
                                        }else if (s.length == 3) {
                                            try {
                                                // parse key to int
                                                int low = Integer.parseInt(s[1]);
                                                int high = Integer.parseInt(s[2]);
                                                tree.delete(low, high);
                                            } catch (NumberFormatException e) {
                                                System.out.println("Only integer keys are supported");
                                            }
                                        }
                                        break;
                                    case "print":
                                        tree.printTree();
                                        break;
                                    case "stats":
                                        tree.dumpStatistics();
                                        break;
                                    case "search":
                                        if (s.length != 3) {
                                            System.out.println("Invalid number of arguments\nUsage: search <lower> <upper>");
                                            break;
                                        }
                                        try {
                                            tree.search(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
                                        } catch (NumberFormatException e) {
                                            System.out.println("Only integer keys are supported");
                                        }
                                        break;
                                    case "quit":
                                        System.out.println("Thanks! Byebye\uF04A");
                                        break;
                                    default:
                                        System.out.println("Invalid command");
                                }
                            }
                        }

                        break;
                    case "exit":
                        break;
                    default:
                        System.out.println("Invalid command");
                }
            } else if (!option.equals("exit")) {
                System.out.println("Invalid command");
            }
        }
        System.out.println("Tree End.");
        in.close();
    }

    public static String[] handler(String command) {
        String[] arr = command.trim().split("\\s+");
        return arr;
    }
}