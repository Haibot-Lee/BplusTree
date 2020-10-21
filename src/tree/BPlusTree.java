package tree;

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
        int keyCnt;
        InternalNode parentNode;

        Node() {
            keys = new int[fanOut - 1];
        }

        abstract void split();
        abstract void merge();
    }

    private class InternalNode extends Node {
        Node[] childNodes;

        InternalNode() {
            super();
            childNodes = new Node[fanOut];
        }

        void split() {

        }

        void merge() {

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

        void split() {

        }

        void merge() {

        }
    }


    // Constructors
    public BPlusTree(String filename) {
        this.fanOut = 5;
        this.root = new LeafNode();  // root is initially a leaf node

        // Read file
        // ...

        // Bulk loading
        // ...

    }


    // Public methods
    public void insert(int key, Object record) {

    }

    public void delete(int key) {

    }

    public void search(int key) {

    }

    public void search(int key1, int key2) {
        
    }

    public void dumpStatistics() {

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

}
