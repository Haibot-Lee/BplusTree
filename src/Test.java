import tree.BPlusTree;
import java.util.*;

public class Test {
    public static void main(String[] args) {
        int[] testData = {13, 2, 16, 14, 10, 13, 16, 7};
        String filename = "";
        BPlusTree tree = new BPlusTree(filename);

        userInterface();
    }

    public static void userInterface() {
        System.out.println("Welcome to User Interface");

        Scanner in = new Scanner(System.in);

        String command = "";

        String option = "";
        String argument = "";
        while (!command.equals("exit")) {
            System.out.print(">");
            command = in.nextLine();
            String[] s = handler(command);
            option = s[0];
            argument = s[1];

            switch (option) {
                case "btree":
                    if (argument.equals("-help")) {
                        System.out.println("Usage: btree [fname]\n"
                                + "fname: the name of the data file "
                                + "storing the search key values");
                    }else {
                        //createBPlusTree(argument);
                        System.out.println("Building an initial B+-Tree...\n" +
                                "Launching B+-Tree test program…\n" +
                                "Waiting for your commands: ");
                    }
                    break;
                case "insert":
                    //insert(argument);
                    break;
                case "delete":
                    //delete(argument);
                    break;
                case "print":
                    //print(argument);
                    break;
                case "stat":
                    //stat(argument);
                    break;
                case "exit":
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
        System.out.println("Thanks! Byebye\uF04A");
        in.close();
    }

    public static String[] handler(String command) {
        String[] s = new String[2];
        s[0] = "";
        s[1] = "";

        int i = 0;
        while ((i < command.length()) && !(command.charAt(i) == ' ')) {
            s[0] += command.charAt(i);
            i++;
            // System.out.println(s[0]);
        }

        i++;

        while (i < command.length()) {
            s[1] += command.charAt(i);
            i++;
            // System.out.println(s[1]);
        }

        return s;
    }
}