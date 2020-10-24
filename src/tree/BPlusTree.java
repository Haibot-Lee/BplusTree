package tree;

import java.io.File;
import java.util.*;

public class BPlusTree {
    private Node root;
    private int numOfNodes;
    private int height;
    private final int fanOut;
    private int numOfDataEntries;
    private int numOfIndexEntries;
    private double fillFactor;

    // Inner class Node
    private abstract class Node {
        int[] keys;
        int keyCnt = 0;
        InternalNode parentNode;

        Node() {
            keys = new int[fanOut - 1];
        }

        abstract void delete(int key);

    }

    private class InternalNode extends Node {
        Node[] childNodes;

        InternalNode(int key, Node leftChild, Node rightChild) {
            super();
            childNodes = new Node[fanOut];
            keys[0] = key;
            keyCnt = 1;
            childNodes[0] = leftChild;
            childNodes[1] = rightChild;
            leftChild.parentNode = this;
            rightChild.parentNode = this;
        }

        void insert(int key, Node newChild) {
            if (keyCnt == fanOut - 1) {
                split(key, newChild);
                numOfNodes++;
            } else {

                keys[keyCnt++] = key;
                childNodes[keyCnt] = newChild;
                newChild.parentNode = this; // TODO: delete redundant lines setting newChild's parentNode in other
                                            // places
                int tmpKey;
                Node tmpNode;
                for (int i = keyCnt - 1; i > 0; i--) {
                    if (keys[i] < keys[i - 1]) {
                        tmpKey = keys[i];
                        keys[i] = keys[i - 1];
                        keys[i - 1] = tmpKey;

                        tmpNode = childNodes[i + 1];
                        childNodes[i + 1] = childNodes[i];
                        childNodes[i] = tmpNode;
                    } else {
                        break;
                    }
                }
            }
            numOfIndexEntries++;
        }

        private void split(int key, Node newChild) {
            // Sort
            int tmpKey;
            Node tmpNode;
            if (keys[keyCnt - 1] > key) { // the new key is smaller than the largest key in the node
                tmpKey = key;
                key = keys[keyCnt - 1];
                keys[keyCnt - 1] = tmpKey;

                tmpNode = newChild;
                newChild = childNodes[keyCnt];
                childNodes[keyCnt] = tmpNode;
            }
            // sort keys and succeeding children in the node
            for (int i = keyCnt - 1; i > 0; i--) {
                if (keys[i] < keys[i - 1]) {
                    tmpKey = keys[i];
                    keys[i] = keys[i - 1];
                    keys[i - 1] = tmpKey;

                    tmpNode = childNodes[i + 1];
                    childNodes[i + 1] = childNodes[i];
                    childNodes[i] = tmpNode;
                } else {
                    break;
                }
            }

            // Create sibling
            InternalNode sib = new InternalNode(keys[keyCnt / 2 + 1], childNodes[keyCnt / 2 + 1],
                    childNodes[keyCnt / 2 + 2]);
            this.keyCnt--; // the key moved to sib

            // Move keys and children to sib
            for (int i = 1; i < fanOut / 2 - 1; i++) {
                sib.keys[i] = this.keys[(fanOut - 1) / 2 + 1 + i];
                sib.childNodes[i + 1] = this.childNodes[(fanOut - 1) / 2 + 2 + i];
                sib.childNodes[i + 1].parentNode = sib; // change parent of the moved children from `this` to `sib`
                this.keyCnt--;
                sib.keyCnt++;
            }
            // add the largest
            sib.keys[(fanOut - 2) / 2] = key;
            sib.childNodes[(fanOut - 2) / 2 + 1] = newChild;
            sib.childNodes[(fanOut - 2) / 2 + 1].parentNode = sib;
            sib.keyCnt++;

            // if this is the root
            if (parentNode == null) {
                parentNode = new InternalNode(keys[keyCnt - 1], this, sib);
                sib.parentNode = parentNode;
                root = parentNode;
                height++;
            } else {
                // otherwise, insert key to the parent
                parentNode.insert(keys[keyCnt - 1], sib);
                sib.parentNode = parentNode;
            }
            this.keyCnt--; // the key moved to par
        }

        void delete(int key) {

        }

    }

    private class LeafNode extends Node {
        Object[] records;
        LeafNode leftSibling;
        LeafNode rightSibling;

        LeafNode() {
            super();
            records = new Object[fanOut - 1];
        }

        void insert(int key) {
            if (keyCnt < keys.length) {
                boolean ifInsert = false;
                int temp = 0;
                for (int i = 0; i < keyCnt + 1; i++) {
                    if (!ifInsert) {
                        if (key < keys[i]) {
                            temp = keys[i];
                            keys[i] = key;
                            ifInsert = true;
                        }
                    } else {
                        int temp2 = temp;
                        temp = keys[i];
                        keys[i] = temp2;
                    }
                }
                if (!ifInsert) {
                    keys[keyCnt] = key;
                }
                keyCnt++;
            } else {
                if (rightSibling == null) {
                    rightSibling = new LeafNode();
                    rightSibling.leftSibling = this;
                } else {
                    LeafNode temp = rightSibling;
                    rightSibling = new LeafNode();
                    rightSibling.leftSibling = this;
                    rightSibling.rightSibling = temp;
                    rightSibling.rightSibling.leftSibling = rightSibling;
                }

                int[] temp = new int[keys.length + 1];
                boolean ifInsert = false;
                for (int i = 0; i < temp.length; i++) {
                    if (!ifInsert) {
                        if (i < keys.length) {
                            if (key < keys[i]) {
                                temp[i] = key;
                                ifInsert = true;
                            } else {
                                temp[i] = keys[i];
                            }
                        } else {
                            temp[i] = key;
                        }
                    } else {
                        temp[i] = keys[i - 1];
                    }
                }

                keyCnt = 0;
                for (int i = 0; i < keys.length; i++) {
                    if (i < temp.length / 2) {
                        keys[i] = temp[i];
                        keyCnt++;
                    } else {
                        keys[i] = 0;
                    }

                    if (i <= temp.length / 2) {
                        rightSibling.keys[i] = temp[i + temp.length / 2];
                        rightSibling.keyCnt++;
                    }
                }

                if (parentNode == null) {
                    parentNode = new InternalNode(temp[temp.length / 2], this, rightSibling);
                    root = parentNode;
                    rightSibling.parentNode = parentNode;
                } else {
                    parentNode.insert(temp[temp.length / 2], rightSibling);
                    rightSibling.parentNode = parentNode;
                }

                numOfNodes++;

            }
        }

        @Override
        void delete(int key) {

        }
    }

    // Constructors
    public BPlusTree(String filename) {
        this.fanOut = 5;
        this.root = new LeafNode(); // root is initially a leaf node

        // Read file
        List<Integer> initialData = new ArrayList<Integer>();
        try {
            File file = new File(filename);
            Scanner in = new Scanner(file);
            while (in.hasNextLine()) {
                initialData.add(in.nextInt());
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        // Update statistics
        numOfDataEntries = initialData.size();

        // Insert
        // for (int i = 0; i < initialData.size(); i++) {
        // insert(initialData.get(i), null);
        // }

        // Bulk loading
        Collections.sort(initialData); // sort

        List<LeafNode> leaf = new ArrayList<LeafNode>(); // construct leaf node
        LeafNode temp = new LeafNode();
        for (int i = 0; i < initialData.size(); i++) {
            temp.keys[i % (fanOut - 1)] = initialData.get(i);
            temp.keyCnt++;
            if (i % (fanOut - 1) == 3) {
                leaf.add(temp);
                temp = new LeafNode();
            }
        }
        if (temp.keyCnt == 1) {
            int giveNext = leaf.get(leaf.size() - 1).keys[leaf.get(leaf.size() - 1).keyCnt - 1];
            leaf.get(leaf.size() - 1).keys[leaf.get(leaf.size() - 1).keyCnt - 1] = 0;
            leaf.get(leaf.size() - 1).keyCnt--;
            temp.keys[temp.keyCnt] = temp.keys[temp.keyCnt - 1];
            temp.keys[temp.keyCnt - 1] = giveNext;
            temp.keyCnt++;
            leaf.add(temp);
        } else if (temp.keyCnt > 1) {
            leaf.add(temp);
        }
        for (int i = 0; i < leaf.size(); i++) {
            if (i != 0) {
                leaf.get(i).leftSibling = leaf.get(i - 1);
            }
            if (i != leaf.size() - 1) {
                leaf.get(i).rightSibling = leaf.get(i + 1);
            }
        }
        // Update statistics
        numOfNodes = leaf.size();

        // test leaf
        // for (int i = 0; i < leaf.size(); i++) {
        // System.out.print(leaf.get(i).keyCnt + " || ");
        // for (int j = 0; j < leaf.get(i).keys.length; j++) {
        // System.out.print(leaf.get(i).keys[j] + " ");
        // }
        // System.out.println();
        // }

        // Construct tree
        if (leaf.size() == 1) {
            root = leaf.get(0);
            return;
        } else {
            root = new InternalNode(leaf.get(1).keys[0], leaf.get(0), leaf.get(1));
            // Update statistics
            numOfNodes++;
            height = 1;
            numOfIndexEntries = 1;
        }
        for (int i = 2; i < leaf.size(); i++) {
            LeafNode prev = leaf.get(i - 1);
            LeafNode curr = leaf.get(i);
            prev.parentNode.insert(curr.keys[0], curr);
        }

    }

    // Private methods
    private void printLevel(List<Node> nodes) {
        List<Node> nextLevel = new LinkedList<>();
        // print current level
        for (Node n : nodes) {
            printNode(n);
            System.out.print(" - ");
        }
        System.out.println();

        // if this is an internal level, add children to `nextLevel` and recurse
        // otherwise, return;
        if (nodes.get(0) instanceof InternalNode) {
            for (Node n : nodes) {
                for (int i = 0; i < n.keyCnt + 1; i++) {
                    nextLevel.add(((InternalNode) n).childNodes[i]);
                }
            }
            printLevel(nextLevel);
        }

    }

    // Public methods
    public void insert(int key, Object record) {
        // TEST CODE START
        // ((LeafNode) root).insert(key);
        // TEST CODE END
        // System.out.println("Searching");
        System.out.println("Insert " + key);
        LeafNode target = search(key);
        // System.out.println(target.keyCnt);
        target.insert(key); // TODO: insert record pointer
        numOfDataEntries++;
        this.printTree();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void delete(int key) {

    }

    public LeafNode search(int key) {
        Node n = root;

        while (n != null) {
            // layer of node
            if (n instanceof InternalNode) {
                Node preChild = null;
                for (int i = 0; i < n.keyCnt; i++) {
                    int delta = key - n.keys[i];
                    if (delta < 0) { // if key < currentKey(i), then go to the preceding child
                        preChild = ((InternalNode) n).childNodes[i];
                        break;
                    }
                }
                if (preChild != null) {
                    n = preChild;
                } else { // key is greater or equal than all existing keys
                    n = ((InternalNode) n).childNodes[n.keyCnt];
                }

                // n = ((InternalNode) n).childNodes[n.keyCnt]; // key is larger than all
                // existing keys. go to the last child
            } else if (n instanceof LeafNode) {
                // for (int i = 0; i < n.keyCnt; i++) {
                // // TODO: this comparison can be omitted since only target leafnode is needed
                // int delta = key - n.keys[i];
                // if (delta == 0) {
                // System.out.println(n.keys[i] + " is here!");
                // return (LeafNode) n;
                // }
                // }
                return (LeafNode) n;
            }
        }
        // Useless because not care the content of the leaf node

        System.out.println("No Found");
        return (LeafNode) n;

    }

    public void search(int key1, int key2) {
        LeafNode n = search(key1);
        ArrayList<Integer> results = new ArrayList<>();

        while (true) {
            // traverse a leaf node
            for (int key : n.keys) {
                if (key >= key1) {
                    if (key <= key2) {
                        results.add(key);
                    } else {
                        String result = "";
                        for (Integer k : results) {
                            result += k + " ";
                        }
                        if (result.equals("")) {
                            System.out.println("Found nothing! ");
                        } else {
                            System.out.println(result);
                        }
                        return;
                    }
                }
            }

            // move to the next leaf node
            if (n.rightSibling != null) {
                n = n.rightSibling;
            } else {
                String result = "";
                for (Integer k : results) {
                    result += k + " ";
                }
                System.out.println(result);
                return;
            }
        }
    }

    public void dumpStatistics() {

        System.out.println("Total No. of nodes in the tree: " + numOfNodes);
        System.out.println("Total No. of data entries in the tree: " + numOfDataEntries);
        System.out.println("Total No. of index entries in the tree: " + numOfIndexEntries);
        System.out.println("Average fill factor (used space/total space) of the nodes: " + ((double) (numOfIndexEntries + numOfDataEntries)) / (numOfNodes * (fanOut - 1)));  // ???
        System.out.println("Height of tree: " + height);
    }

    public void printNode(Node n) {
        for (int i = 0; i < n.keyCnt; i++) {
            System.out.print("| " + n.keys[i] + " |");
        }
    }

    public void printTree() {
        List<Node> rootLevel = new LinkedList<>();
        rootLevel.add(root);
        printLevel(rootLevel);
    }

    // Test Area
    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree("testData.txt");

        tree.printTree();

        System.out.println("\n");
        tree.search(1, 9);
        tree.dumpStatistics();
    }

}
