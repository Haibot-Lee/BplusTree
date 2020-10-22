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
        int keyCnt = 0;
        InternalNode parentNode;

        Node() {
            keys = new int[fanOut - 1];
        }

        abstract void insert(int key);

        abstract void delete(int key);

    }

    private class InternalNode extends Node {
        Node[] childNodes;

        InternalNode() {
            super();
            childNodes = new Node[fanOut];
        }

        @Override
        void insert(int key) {

        }

        @Override
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

        @Override
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
                    parentNode = new InternalNode(key, this, rightSibling);
                    rightSibling.parentNode = parentNode;
                } else {
                    parentNode.insert(key, rightSibling);
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
