package tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class BPlusTree {
    private Node root;
    private int numOfNodes;
    private int height;
    private final int fanOut;
    private int numOfDataEntries;
    private int numOfIndexEntries;
    private int numOfLeafNodes;
    private double fillFactor;
    private int nodeCnt;  // Used for printing tree structure
    private List<Node> levelOrderedNodes;  // Used for printing node contents

    // Inner class Node
    private abstract class Node {
        int[] keys;
        int keyCnt = 0;
        InternalNode parentNode;
        int printId;  // Used for identifying a node when printing

        Node() {
            keys = new int[fanOut - 1];
        }

        @Override
        public String toString() {
            String str = "(";
            for (int i = 0; i < this.keyCnt; i++) {
                str += String.format("%d, ", this.keys[i]);
            }
            str = str.replaceAll(", $", "");
            str += ")";
            return str;
        }
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
            } else {
                // otherwise, insert key to the parent
                parentNode.insert(keys[keyCnt - 1], sib);
                sib.parentNode = parentNode;
            }
            this.keyCnt--; // the key moved to par
        }

        // TODO: reset the parentNode of LeafNode
        private void reset (LeafNode thisNode) {
            for (int i = 0; i < thisNode.parentNode.keyCnt; i++) {
                if (thisNode.parentNode.childNodes[i + 1] == thisNode) {
                    thisNode.parentNode.keys[i] = thisNode.keys[0];
                    break;
                }
            }
        }

        // TODO: delete operation for InternalNode
        private void delete (LeafNode thisNode) {
            for (int i = 0; i < thisNode.parentNode.keyCnt - 1; i++) {
                if (thisNode.parentNode.childNodes[i] == thisNode) {
                    for (int j = i; j < thisNode.parentNode.keyCnt - 1; j++) {
                        thisNode.parentNode.keys[j] = thisNode.parentNode.keys[j + 1];
                        thisNode.parentNode.childNodes[j + 1] = thisNode.parentNode.childNodes[j + 2];
                    }
                    break;
                }
            }
            thisNode.parentNode.keys[thisNode.parentNode.keyCnt - 1] = 0;
            thisNode.parentNode.childNodes[thisNode.parentNode.keyCnt] = null;
            thisNode.parentNode.keyCnt--;
            // TODO: check whether underflow or not
            if (thisNode.parentNode.keyCnt < fanOut / 2) {
                // redistribute the parentNode
                redistribute(thisNode.parentNode);
            }
        }

        // TODO: redistribute the InternalNode to avoid underflow
        private void redistribute (InternalNode pointer) {
            if (pointer != root) {
                // find the left sibling node and the right sibling node
                InternalNode leftSibling = null;
                InternalNode rightSibling = null;
                int position = 0;
                for (int i = 0; i < pointer.parentNode.keyCnt; i++) {
                    if (pointer.parentNode.childNodes[i] == pointer) {
                        // TODO: set the leftSibling and rightSibling for InternalNode
                        leftSibling = i > 0 ? (InternalNode)pointer.parentNode.childNodes[i - 1] : null;
                        rightSibling = i < pointer.parentNode.keyCnt ? (InternalNode)pointer.parentNode.childNodes[i + 1] : null;
                        position = i;
                        break;
                    }
                }
                if (leftSibling != null && leftSibling.keyCnt > fanOut / 2) {
                    // TODO: borrow from left InternalNode
                    for (int j = pointer.keyCnt; j > 0; j--) {
                        // to have one space for the borrow key
                        pointer.keys[j] = pointer.keys[j - 1];
                        pointer.childNodes[j + 1] = pointer.childNodes[j];
                    }
                    pointer.childNodes[1] = pointer.childNodes[0];
                    pointer.keys[0] = pointer.parentNode.keys[position - 1];
                    pointer.childNodes[0] = leftSibling.childNodes[leftSibling.keyCnt];
                    pointer.childNodes[0].parentNode = pointer; // reset the new childNode's parentNode
                    pointer.keyCnt++;
                    pointer.parentNode.keys[position - 1] = leftSibling.keys[keyCnt - 1];
                    leftSibling.keyCnt--;
                }else if (rightSibling != null && rightSibling.keyCnt > fanOut / 2) {
                    // TODO:borrow from right InternalNode
                    pointer.keys[pointer.keyCnt] = pointer.parentNode.keys[position];
                    pointer.parentNode.keys[position] = rightSibling.keys[0];
                    pointer.childNodes[pointer.keyCnt + 1] = rightSibling.childNodes[0];
                    pointer.childNodes[pointer.keyCnt + 1].parentNode = pointer; // reset the new childNode's parentNode
                    for (int j = 0; j < rightSibling.keyCnt - 1; j++) {
                        rightSibling.keys[j] = rightSibling.keys[j + 1];
                        rightSibling.childNodes[j] = rightSibling.childNodes[j + 1];
                    }
                    rightSibling.childNodes[rightSibling.keyCnt - 1] = rightSibling.childNodes[rightSibling.keyCnt];
                    pointer.keyCnt++;
                    rightSibling.keyCnt--;
                }else if (leftSibling != null){
                    // TODO: move down parentNode and combine with left InternalNode together
                    leftSibling.keys[leftSibling.keyCnt] = pointer.parentNode.keys[position - 1];
                    leftSibling.keyCnt++;
                    // combine to the left InternalNode
                    for (int j = 0; j < keyCnt; j++) {
                        leftSibling.keys[leftSibling.keyCnt + j] = pointer.keys[j];
                        leftSibling.childNodes[leftSibling.keyCnt + j] = pointer.childNodes[j];
                        leftSibling.childNodes[leftSibling.keyCnt + j].parentNode = leftSibling; // reset parentNode of leftSibling childNodes
                    }
                    leftSibling.keyCnt += pointer.keyCnt;
                    leftSibling.childNodes[leftSibling.keyCnt] = pointer.childNodes[pointer.keyCnt];
                    leftSibling.childNodes[leftSibling.keyCnt].parentNode = leftSibling; // reset parentNode of the last leftSibling childNode
                    // rebuild on parent level
                    for (int k = position; k < pointer.parentNode.keyCnt; k++) {
                        pointer.parentNode.keys[k - 1] = pointer.parentNode.keys[k];
                        pointer.parentNode.childNodes[k] = pointer.parentNode.childNodes[k + 1];
                    }
                    pointer.parentNode.keyCnt--;
                }else if (rightSibling != null) {
                    // TODO: move down parentNode and combine with right InternalNode together
                    pointer.keys[pointer.keyCnt] = pointer.parentNode.keys[position];
                    pointer.keyCnt++;
                    // combine with the right InternalNode
                    for (int j = 0; j < rightSibling.keyCnt; j++) {
                        pointer.keys[pointer.keyCnt + j] = rightSibling.keys[j];
                        pointer.childNodes[pointer.keyCnt + j] = rightSibling.childNodes[j];
                        pointer.childNodes[pointer.keyCnt + j].parentNode = pointer; // reset parentNode of rightSibling childNodes
                    }
                    pointer.keyCnt += rightSibling.keyCnt;
                    pointer.childNodes[pointer.keyCnt] = rightSibling.childNodes[rightSibling.keyCnt];
                    pointer.childNodes[pointer.keyCnt].parentNode = pointer; // reset parentNode of the last rightSibling childNode
                    // rebuild on parent level
                    for (int k = position; k < pointer.parentNode.keyCnt - 1; k++) {
                        pointer.parentNode.keys[k] = pointer.parentNode.keys[k + 1];
                        pointer.parentNode.childNodes[k + 1] = pointer.parentNode.childNodes[k + 2];
                    }
                    pointer.parentNode.keyCnt--;
                }
                // TODO: check whether underflow or not
                if (pointer.parentNode.keyCnt < fanOut / 2) {
                    // redistribute the parentNode
                    redistribute(pointer.parentNode);
                }
            }else {// this is root
        		if (pointer.keyCnt == 0 && pointer.childNodes[0] != null) { // there is a childNode
        		    // TODO: reset the root
        			root = pointer.childNodes[0];
        			root.parentNode = null;
        		}
        		// else no matter there is keyCnt >= 1 or there is no childNode, we do not need to operate
            }
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

        void insert(int key, Object record) {
            if (keyCnt < keys.length) {         //insert directly
                boolean ifInsert = false;
                int temp = 0;
                for (int i = 0; i < keyCnt + 1; i++) {
                    if (!ifInsert) {
                        if (key < keys[i]) {
                            temp = keys[i];
                            keys[i] = key;
                            records[i] = record;
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
                    records[keyCnt] = record;
                }
                keyCnt++;
            } else {                            //spilt needed
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
                        if (keys[i] == key) {
                            records[i] = record;
                        }
                        keyCnt++;
                    } else {
                        keys[i] = 0;
                    }

                    if (i <= temp.length / 2) {
                        rightSibling.keys[i] = temp[i + temp.length / 2];
                        if (rightSibling.keys[i] == key) {
                            rightSibling.records[i] = record;
                        }
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

        // TODO: delete operation for LeafNode
        private void delete(int key) {
            // TODO: reset the keyCnt
            boolean treeChanged = false;
            for (int i = 0; i < keyCnt; i++) {
                if (key == keys[i]) {
                    if (i + 1 <= keyCnt) {
                        for (int j = i; j < keyCnt - 1; j++) {
                            keys[j] = keys[j+1];
                            records[j] = records[j+1];
                        }
                        keys[keyCnt - 1] = 0;
                        records[keyCnt - 1] = null;
                    }
                    keyCnt--;
                    treeChanged = true;
                    i--; // to check the value which is original in next position
                }
            }
            // TODO: check whether delete successfully or not
            if (!treeChanged) {
                System.out.println(key + " is not exist, delete failed!");
                return;
            }
            // TODO: check underflow and do corresponding operation
            if (keyCnt < fanOut / 2) {
                // TODO: try to re-distribute, borrowing from sibling
                if (leftSibling != null && leftSibling.parentNode == parentNode && leftSibling.keyCnt > fanOut / 2 ) {
                    // TODO: borrow from left sibling
                    for (int j = keyCnt; j > 0; j--) {
                        keys[j] = keys[j-1];
                        records[j] = records[j-1];
                    }
                    keys[0] = leftSibling.keys[leftSibling.keyCnt - 1];
                    records[0] = leftSibling.records[leftSibling.keyCnt - 1];
                    keyCnt++;
                    leftSibling.keys[leftSibling.keyCnt - 1] = 0;
                    leftSibling.records[leftSibling.keyCnt - 1] = null;
                    leftSibling.keyCnt --;
                    parentNode.reset(this);
                }
                else if (rightSibling != null && rightSibling.parentNode == parentNode && rightSibling.keyCnt > fanOut / 2) {
                    // TODO: borrow from right sibling
                    keys[keyCnt] = rightSibling.keys[0];
                    records[keyCnt] = rightSibling.records[0];
                    keyCnt++;
                    for (int j = 0; j < rightSibling.keyCnt - 1; j++) {
                        rightSibling.keys[j] = rightSibling.keys[j+1];
                        rightSibling.records[j] = rightSibling.records[j+1];
                    }
                    rightSibling.keys[rightSibling.keyCnt - 1] = 0;
                    rightSibling.records[rightSibling.keyCnt - 1] = null;
                    rightSibling.keyCnt --;
                    parentNode.reset(rightSibling);
                }/* if re-distribution fails, merge this node and sibling
                      put all the keep value to the node on the left
                      e.g: merge with left choose leftSibling
                           merge with right choose current one*/
                else if (leftSibling != null && leftSibling.parentNode == parentNode) {
                    // TODO: merge leftSibling and current one
                    for (int j = 0; j < keyCnt; j++) {
                        leftSibling.keys[leftSibling.keyCnt + j] = keys[j];
                        leftSibling.records[leftSibling.keyCnt + j] = records[j];
                    }
                    leftSibling.keyCnt += keyCnt;
                    if (rightSibling != null)
                        rightSibling.leftSibling = leftSibling;
                    leftSibling.rightSibling = rightSibling;
                    parentNode.delete(leftSibling);
                }else if (rightSibling != null && rightSibling.parentNode == parentNode) {
                    // TODO: merge rightSibling and current one
                    for (int j = 0; j < rightSibling.keyCnt; j++) {
                        keys[keyCnt + j] = rightSibling.keys[j];
                        records[keyCnt + j] = rightSibling.records[j];
                    }
                    keyCnt += rightSibling.keyCnt;
                    rightSibling = rightSibling.rightSibling;
                    if (rightSibling != null)
                        rightSibling.leftSibling = this;
                    parentNode.delete(this);
                }
            }
        }
    }

    // Constructors
    public BPlusTree(String filename) throws FileNotFoundException {
        this.fanOut = 5;
        this.root = new LeafNode(); // root is initially a leaf node

        // Read file
        List<Integer> initialData = new ArrayList<Integer>();
        File file = new File(filename);
        Scanner in = new Scanner(file);
        while (in.hasNextLine()) {
            initialData.add(in.nextInt());
        }

        // Bulk loading
        Collections.sort(initialData);      // sort

        List<LeafNode> leaf = new ArrayList<LeafNode>();     // construct leaf node
        LeafNode temp = new LeafNode();
        for (int i = 0; i < initialData.size(); i++) {
            temp.keys[i % (fanOut - 1)] = initialData.get(i);
            temp.keyCnt++;
            if (i % (fanOut - 1) == (fanOut - 2)) {
                leaf.add(temp);
                temp = new LeafNode();
            }
        }

        if (temp.keyCnt == 1 && initialData.size() > 1) {
            int giveNext = leaf.get(leaf.size() - 1).keys[leaf.get(leaf.size() - 1).keyCnt - 1];

            leaf.get(leaf.size() - 1).keys[leaf.get(leaf.size() - 1).keyCnt - 1] = 0;
            leaf.get(leaf.size() - 1).keyCnt--;
            temp.keys[temp.keyCnt] = temp.keys[temp.keyCnt - 1];
            temp.keys[temp.keyCnt - 1] = giveNext;
            temp.keyCnt++;
            leaf.add(temp);
        } else if (temp.keyCnt > 1 || initialData.size() == 1) {
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

        //test
//        LeafNode current = leaf.get(0);
//        while (current.rightSibling != null) {
//            for (int j = 0; j < current.keys.length; j++) {
//                System.out.print(current.keys[j] + " ");
//            }
//            current = current.rightSibling;
//            System.out.println();
//        }

        if (leaf.size() == 1) {     // Construct tree
            root = leaf.get(0);
            return;
        } else {
            root = new InternalNode(leaf.get(1).keys[0], leaf.get(0), leaf.get(1));
        }
        for (int i = 2; i < leaf.size(); i++) {
            LeafNode prev = leaf.get(i - 1);
            LeafNode curr = leaf.get(i);
            prev.parentNode.insert(curr.keys[0], curr);
        }

    }

    // Private methods
    private void assignPrintId(List<Node> nodes) {
        List<Node> nextLevel = new LinkedList<>();
        // print current level
        for (Node n : nodes) {
            n.printId = nodeCnt++;
            levelOrderedNodes.add(n);
        }

        // if this is an internal level, add children to `nextLevel` and recurse
        // otherwise, return;
        if (nodes.get(0) instanceof InternalNode) {
            for (Node n : nodes) {
                for (int i = 0; i < n.keyCnt + 1; i++) {
                    nextLevel.add(((InternalNode) n).childNodes[i]);
                }
            }
            assignPrintId(nextLevel);
        }
    }

    private void printLevel(List<Node> nodes) {
        List<Node> nextLevel = new LinkedList<>();

        // if this is an internal level, add children to `nextLevel` and recurse
        // otherwise, return;
        if (nodes.get(0) instanceof InternalNode) {
            for (Node n : nodes) {
                String str = String.format("%d -> ", ((InternalNode) n).printId);
                for (int i = 0; i < n.keyCnt + 1; i++) {
                    str += String.format(((InternalNode) n).childNodes[i].printId + ", ");
                    nextLevel.add(((InternalNode) n).childNodes[i]);
                }
                System.out.println(str.replaceAll(", $", ""));
            }
            printLevel(nextLevel);
        }
    }

    private void printNodes(List<Node> nodes) {
        for (Node n : nodes) {
            System.out.println(n.printId + ": " + n.toString());
        }
    }

    private void countLevel(List<Node> nodes) {
        System.out.println("countLevel");
        List<Node> nextLevel = new LinkedList<>();

        if (nodes.get(0) instanceof InternalNode) {
            for (Node n : nodes) {
                for (int i = 0; i < n.keyCnt + 1; i++) {
                    nextLevel.add(((InternalNode) n).childNodes[i]);
                    numOfIndexEntries++;
                }
            }
            countLevel(nextLevel);
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
        target.insert(key, record); // TODO: insert record pointer
    }

    // TODO: realize delete one by one
    public void delete (int key) {
        System.out.println("Delete "+ key);
        // TODO: find the delete target
        LeafNode target = search(key);
        // TODO: delete target
        target.delete(key);

    }

    // TODO: execute the command from UI
    public void delete (int lowerbound, int upperbound) {
        ArrayList<Integer> deleteTargets = search(lowerbound, upperbound);
        for (Integer key : deleteTargets) {
            // TODO: delete one by one
            delete(key);
        }
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

    public ArrayList<Integer> search(int key1, int key2) {
        LeafNode n = search(key1);
        ArrayList<Integer> results = new ArrayList<>();

        while (true) {
            // traverse a leaf node
            for (int i = 0; i < n.keyCnt; i++) {
                int key = n.keys[i];
            //for (int key : n.keys) {
                //if (key != 0 && key >= key1) {// key != 0 to avoid error message because of the deleted space
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
                        return results;
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
                return results;
            }
        }
    }

    public void dumpStatistics() {
        height = 0;
        numOfLeafNodes = 0;
        numOfDataEntries = 0;
        numOfIndexEntries = 0;
        numOfNodes = 0;

        // Count height
        Node current = root;
        while (current instanceof InternalNode) {
            current = ((InternalNode) current).childNodes[0];
            height++;
        }

        // Count data entries and leaf nodes
        while (((LeafNode) current).rightSibling != null) {
            numOfDataEntries += current.keyCnt;
            current = ((LeafNode) current).rightSibling;
            numOfLeafNodes++;
        }
        numOfDataEntries += current.keyCnt;
        numOfLeafNodes++;

        // Count index entries
        List<Node> rootLevel = new LinkedList<>();
        rootLevel.add(root);
        countLevel(rootLevel);

        // Calculate the number of nodes;
        numOfNodes = numOfIndexEntries + 1;

        // Print
        System.out.println("Statistics of the B+ tree:");
        System.out.println("Total No. of nodes: " + numOfNodes);
        System.out.println("Total No. of data entries: " + numOfDataEntries);
        System.out.println("Total No. of index entries: " + numOfIndexEntries);
        System.out.println("Average fill factor (used space/total space) of the nodes: "
                + ((double) (numOfIndexEntries + numOfDataEntries)) / (numOfNodes * (fanOut - 1)));
        System.out.println("Height of tree: " + height);
    }


    public void printTree() {
        nodeCnt = 0;
        levelOrderedNodes = new LinkedList<>();
        List<Node> rootLevel = new LinkedList<>();
        rootLevel.add(root);
        assignPrintId(rootLevel);
        System.out.println("Tree Structure: ");
        printLevel(rootLevel);
        System.out.println();
        System.out.println("Node content: ");
        printNodes(levelOrderedNodes);
    }

    // Test Area
//    public static void main(String[] args) {
//        BPlusTree tree = new BPlusTree("testData.txt");
//
//        tree.printTree();
//        System.out.println("\n");
//
//        // basic test for deleting all
//        ArrayList<Integer> test = tree.search(0,9000);
//        for (Integer k : test) {
//            System.out.println("\nAfter delete:");
//            tree.delete(k);
//            tree.printTree();
//        }
////        tree.search(23, 100);
//        tree.dumpStatistics();
//    }

}