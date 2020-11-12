# B+ Tree Implementation

Group project in COMP4035 (2020/21 SEM1)

------------------------------------------
## Group members
Student ID | Name
-------- | --------------
17251060 | WANG Tierun
18250009 | CHEN Dezhi
18252125 | LI Haipeng
18252214 | LIANG Mingcong

------------------------------------------

## I. Brief description of our project and the B+ tree


## II. The data structures used in the implementation

## III. Algorithms used
### Data Structures
An inner abstract class ***Node*** is included in the B+ tree class, which is extended
by two subclasses ***InternalNode*** and ***LeafNode***.

The ***Node*** class contains the shared attributes of both kinds of nodes:  
- **keys** (int[]): An array of integers storing the keys in current node
- **keyCnt** (int): The number of keys in current node
- **parentNode** (InternalNode): The parent of the node
- **printId** (int): An identifier of nodes when printing the tree, as defined in the 
project requirement


The ***InternalNode*** class contains following additional attributes:
- **childNodes** (Node[]): An array of *Node* (either *InternalNode* or
*LeafNode*) containing the children of this node


The ***LeafNode*** class contains following additional attributes:
- **records** (Object[]): An array of Objects (of any type) serving as data entries
- **leftSibling** (LeafNode): A pointer to the left sibling of this node
- **rightSibling** (LeafNode): A pointer to the right sibling of this node


## IV. Environment
### 1. The platform: Windows
### 2. the usage of our program:
### 3. the installation procedure:
Compile Main.java with `javac` and run the compiled class with `java Main`

## V. Highlight of features

