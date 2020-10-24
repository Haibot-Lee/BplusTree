package tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
            }

            keys[keyCnt++] = key;
            childNodes[keyCnt] = newChild;
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

            for (int i = 1; i < fanOut / 2 - 1; i++) {
                sib.keys[i] = this.keys[(fanOut - 1) / 2 + 1 + i];
                sib.childNodes[i + 1] = this.childNodes[(fanOut - 1) / 2 + 2 + i];
                this.keyCnt--;
                sib.keyCnt++;
            }
            // add the largest
            sib.keys[(fanOut - 2) / 2] = key;
            sib.childNodes[(fanOut - 2) / 2 + 1] = newChild;
            sib.keyCnt++;

            // if this is the root
            if (parentNode == null) {
                new InternalNode(keys[keyCnt / 2], this, sib);
            } else {
                // otherwise, insert key to the parent
                parentNode.insert(keys[keyCnt / 2], sib);
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
            }

        }

        @Override
        void delete(int key) {

        }
    }

    // Constructors
    public BPlusTree(String filename) {
        this.fanOut = 5;
        this.root = new LeafNode();  // root is initially a leaf node

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

        // Insert
        for (int i = 1; i < initialData.size(); i++) {
            insert(initialData.get(i), null);
        }

        // Bulk loading
        // ...

    }

    // Private methods
    private void printTree(Node n) {
        printNode(n);
        System.out.println();
        if (n instanceof InternalNode) {
            for (int i = 0; i < n.keyCnt + 1; i++) {
                printNode(((InternalNode) n).childNodes[i]);
                System.out.print(" - ");
            }
        }
    }

    // Public methods
    public void insert(int key, Object record) {
        // TEST CODE START
        ((LeafNode) root).insert(key);
        // TEST CODE END
    }

    public void delete(int key) {

    }

    public void search(int key) {
        Node n = root;

        while (n != null) {
            // layer of node
            if (n instanceof InternalNode) {
                for (int i = 0; i < n.keyCnt; i++) {
                    int delta = key - n.keys[i];
                    if (delta <= 0) {
                        n = ((InternalNode) n).childNodes[i - 1];
                        break;
                    }
                }
            } else if (n instanceof LeafNode) {
                for (int i = 0; i < n.keyCnt; i++) {
                    int delta = key - n.keys[i];
                    if (delta == 0) {
                        System.out.println(n.keys[i] + " is here!");
                        return;
                    }
                }
            }
        }
        System.out.println("No Found");
    }

    public void search(int key1, int key2) {

    }

    public void dumpStatistics() {

    }

    public void printNode(Node n) {
        for (int i = 0; i < n.keyCnt; i++) {
            System.out.print("| " + n.keys[i] + " |");
        }
    }

    public void printTree() {
        printTree(root);
    }

    // Getters and setters
    public int getNumOfNodes() {
        return this.numOfNodes;
    }

    public int getHeight() {
        return this.height;
    }

    public int getFanOut() {
        return this.fanOut;
    }

    public int getNumOfDataEntries() {
        return this.numOfDataEntries;
    }

    public int getNumOfIndexEntries() {
        return this.numOfIndexEntries;
    }

    public double getFillFactor() {
        return this.fillFactor;
    }

    //Test Area
    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree("testData.txt");
        for (int i = 0; i < 5; i++) {
            tree.insert(10 - i, null);
        }

        tree.printTree();

    }

}
