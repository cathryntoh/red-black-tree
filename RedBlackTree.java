import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Red-Black Tree implementation with a Node inner class for representing
 * the nodes of the tree. Currently, this implements a Binary Search Tree that
 * we will turn into a red black tree by modifying the insert functionality.
 * In this activity, we will start with implementing rotations for the binary
 * search tree insert algorithm.
 */
public class RedBlackTree<T extends Comparable<T>> implements SortedCollectionInterface<T> {

    /**
     * This class represents a node holding a single value within a binary tree.
     */
    protected static class Node<T> {
        public T data;
        // The context array stores the context of the node in the tree:
        // - context[0] is the parent reference of the node,
        // - context[1] is the left child reference of the node,
        // - context[2] is the right child reference of the node.
        // The @SupressWarning("unchecked") annotation is used to supress an unchecked
        // cast warning. Java only allows us to instantiate arrays without generic
        // type parameters, so we use this cast here to avoid future casts of the
        // node type's data field.
        @SuppressWarnings("unchecked")
        public Node<T>[] context = (Node<T>[])new Node[3];
        public Node(T data) { this.data = data; }
        // The black height for the current node: 0 = red, 1 = black, and 2 = double-black.
        public int blackHeight = 0;
        
        /**
         * @return true when this node has a parent and is the right child of
         * that parent, otherwise return false
         */
        public boolean isRightChild() {
            return context[0] != null && context[0].context[2] == this;
        }

    }

    protected Node<T> root; // reference to root node of tree, null when empty
    protected int size = 0; // the number of values in the tree

    /**
     * Performs a naive insertion into a binary search tree: adding the input
     * data value to a new node in a leaf position within the tree. After  
     * this insertion, no attempt is made to restructure or balance the tree.
     * This tree will not hold null references, nor duplicate data values.
     * @param data to be added into this binary search tree
     * @return true if the value was inserted, false if not
     * @throws NullPointerException when the provided data argument is null
     * @throws IllegalArgumentException when data is already contained in the tree
     */
    public boolean insert(T data) throws NullPointerException, IllegalArgumentException {
        // null references cannot be stored within this tree
        if(data == null) throw new NullPointerException(
                "This RedBlackTree cannot store null references.");

        Node<T> newNode = new Node<>(data);
        if (this.root == null) {
            // add first node to an empty tree
            root = newNode; size++; 
            enforceRBTreePropertiesAfterInsert(newNode);
            return true;
        } else {
            // insert into subtree
            Node<T> current = this.root;
            while (true) {
                int compare = newNode.data.compareTo(current.data);
                if (compare == 0) {
                    throw new IllegalArgumentException("This RedBlackTree already contains value " + data.toString());
                } else if (compare < 0) {
                    // insert in left subtree
                    if (current.context[1] == null) {
                        // empty space to insert into
                        current.context[1] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[1];
                    }
                } else {
                    // insert in right subtree
                    if (current.context[2] == null) {
                        // empty space to insert into
                        current.context[2] = newNode;
                        newNode.context[0] = current;
                        this.size++;
                        enforceRBTreePropertiesAfterInsert(newNode);
                        return true;
                    } else {
                        // no empty space, keep moving down the tree
                        current = current.context[2]; 
                    }
                }
            }
        }
    }
    
    /**
     * Resolves any red-black tree property violations that are introduced by inserting each new 
     * node into a red-black tree.
     * @param newNode the newly added red node
     */
    protected void enforceRBTreePropertiesAfterInsert(Node<T> newNode) {
      // instantiate child, parent, aunt, and grandparent variables
      Node<T> child = newNode;
      Node<T> parent = child.context[0];
      Node<T> grandparent = null;
      if (parent != null) {
        grandparent = parent.context[0];
      }     
      Node<T> aunt = null;
      if (grandparent != null && grandparent.context[1] == parent) {
        aunt = grandparent.context[2];
      }
      else if (grandparent != null && grandparent.context[2] == parent) {
        aunt = grandparent.context[1];
      }   
      
      // CASE 1: Parent is either black or null
      // No further action required, just set color of root node to black  
      if (parent == null || parent.blackHeight == 1) {
        this.root.blackHeight = 1;
        return;
      }
      
      // CASE 2: Parent is red
      else if (parent.blackHeight == 0) {
        // CASE 2a: Aunt is either black or null
        if (aunt == null || aunt.blackHeight == 1) {
          // if the nodes are not in a “straight line”, rotate so that they are
          if ((grandparent.context[1] == parent && parent.context[2] == newNode) ||
              (grandparent.context[2] == parent && parent.context[1] == newNode)) {
            rotate(newNode, parent);
            child = parent;
            parent = newNode;
          }
          // rotate at grandparent in the direction of aunt
          rotate(parent, grandparent);
          // re-color parent black and grandparent red
          parent.blackHeight = 1;
          grandparent.blackHeight = 0;
        }
        
        // CASE 2b: Aunt is red
        else if (aunt.blackHeight == 0) {
          // re-color grandparent red, parent and aunt black
          grandparent.blackHeight = 0;
          parent.blackHeight = 1;
          aunt.blackHeight = 1;
          
          // if parent of grandparent is null (grandparent is the root), re-color grandparent black
          if (grandparent.context[0] == null) {
            grandparent.blackHeight = 1;
          }
          // if parent of grandparent is red, recursive case (G becomes C, CASE 2a)
          else if (grandparent.context[0].blackHeight == 0) {
            enforceRBTreePropertiesAfterInsert(grandparent);
          }
        }
      }     
           
      // set color of root node to black
      this.root.blackHeight = 1;
    }
    
    /**
     * Performs the rotation operation on the provided nodes within this tree.
     * When the provided child is a left child of the provided parent, this
     * method will perform a right rotation. When the provided child is a
     * right child of the provided parent, this method will perform a left rotation.
     * When the provided nodes are not related in one of these ways, this method
     * will throw an IllegalArgumentException.
     * @param child is the node being rotated from child to parent position
     *      (between these two node arguments)
     * @param parent is the node being rotated from parent to child position
     *      (between these two node arguments)
     * @throws IllegalArgumentException when the provided child and parent
     *      node references are not initially (pre-rotation) related that way
     */
    private void rotate(Node<T> child, Node<T> parent) throws IllegalArgumentException {
      // CASE 1: Provided nodes are not related -> throw exception.
      if (parent.context[1] != child && parent.context[2] != child) {
        throw new IllegalArgumentException("Provided nodes are not related!");
      }
      
      // CASE 2: Provided child is left child of provided parent -> RIGHT rotation.
      if (parent.context[1] == child) {
        // Store nodes temporarily (child -> C; parent -> P1)
        Node<T> rightChild = child.context[2]; // RC
        Node<T> parent2 = parent.context[0]; // P2
        
        // (1) Update context of provided child (C) 
        child.context[0] = parent2; // Update parent
        child.context[2] = parent; // Update right child
        // If P1 is the root, update C to be the new root
        if (parent2 == null) {
            this.root = child;
        }
        
        // (2) Update context of provided parent (P1)  
        parent.context[0] = child; // Update parent
        parent.context[1] = rightChild; // Update left child
                  
        // (3) Update context of C's right child (RC) only if RC is NOT null
        if (rightChild != null) {
          rightChild.context[0] = parent; // Update parent
        }        
              
        // (4) Update context of P1's parent (P2) only if P1 is NOT the root
        if (parent2 != null) {
          // If P1 is left child of P2, update P2's left child
          if (parent2.context[1] == parent) {
            parent2.context[1] = child;
          }
          // If P1 is right child of P2, update P2's right child
          if (parent2.context[2] == parent) {
            parent2.context[2] = child;
          }
        }      
      }
      
      // CASE 3: Provided child is right child of provided parent -> LEFT rotation.
      if (parent.context[2] == child) {
          // Store nodes temporarily (child -> C; parent -> P1)
          Node<T> leftChild = child.context[1]; // LC
          Node<T> parent2 = parent.context[0]; // P2
          
          // (1) Update context of provided child (C) 
          child.context[0] = parent2; // Update parent
          child.context[1] = parent; // Update left child
          // If P1 is the root, update C to be the new root
          if (parent2 == null) {
        	  this.root = child;
          }
          
          
          // (2) Update context of provided parent (P1)  
          parent.context[0] = child; // Update parent
          parent.context[2] = leftChild; // Update right child
                    
          // (3) Update context of C's left child (LC) only if LC is NOT null
          if (leftChild != null) {
            leftChild.context[0] = parent; // Update parent
          }
                         
          // (4) Update context of P1's parent (P2) only if P1 is NOT the root
          if (parent2 != null) {
            // If P1 is left child of P2, update P2's left child
            if (parent2.context[1] == parent) {
              parent2.context[1] = child;
            }
            // If P1 is right child of P2, update P2's right child
            if (parent2.context[2] == parent) {
              parent2.context[2] = child;
            }
          }      
        }
  
    }

    /**
     * Get the size of the tree (its number of nodes).
     * @return the number of nodes in the tree
     */
    public int size() {
        return size;
    }

    /**
     * Method to check if the tree is empty (does not contain any node).
     * @return true of this.size() return 0, false if this.size() > 0
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Removes the value data from the tree if the tree contains the value.
     * This method will not attempt to rebalance the tree after the removal and
     * should be updated once the tree uses Red-Black Tree insertion.
     * @return true if the value was remove, false if it didn't exist
     * @throws NullPointerException when the provided data argument is null
     * @throws IllegalArgumentException when data is not stored in the tree
     */
    public boolean remove(T data) throws NullPointerException, IllegalArgumentException {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // throw exception if node with data does not exist
            if (nodeWithData == null) {
                throw new IllegalArgumentException("The following value is not in the tree and cannot be deleted: " + data.toString());
            }  
            boolean hasRightChild = (nodeWithData.context[2] != null);
            boolean hasLeftChild = (nodeWithData.context[1] != null);
            if (hasRightChild && hasLeftChild) {
                // has 2 children
                Node<T> successorNode = this.findMinOfRightSubtree(nodeWithData);
                // replace value of node with value of successor node
                nodeWithData.data = successorNode.data;
                // remove successor node
                if (successorNode.context[2] == null) {
                    // successor has no children, replace with null
                    this.replaceNode(successorNode, null);
                } else {
                    // successor has a right child, replace successor with its child
                    this.replaceNode(successorNode, successorNode.context[2]);
                }
            } else if (hasRightChild) {
                // only right child, replace with right child
                this.replaceNode(nodeWithData, nodeWithData.context[2]);
            } else if (hasLeftChild) {
                // only left child, replace with left child
                this.replaceNode(nodeWithData, nodeWithData.context[1]);
            } else {
                // no children, replace node with a null node
                this.replaceNode(nodeWithData, null);
            }
            this.size--;
            return true;
        } 
    }

    /**
     * Checks whether the tree contains the value *data*.
     * @param data the data value to test for
     * @return true if *data* is in the tree, false if it is not in the tree
     */
    public boolean contains(T data) {
        // null references will not be stored within this tree
        if (data == null) {
            throw new NullPointerException("This RedBlackTree cannot store null references.");
        } else {
            Node<T> nodeWithData = this.findNodeWithData(data);
            // return false if the node is null, true otherwise
            return (nodeWithData != null);
        }
    }

    /**
     * Helper method that will replace a node with a replacement node. The replacement
     * node may be null to remove the node from the tree.
     * @param nodeToReplace the node to replace
     * @param replacementNode the replacement for the node (may be null)
     */
    protected void replaceNode(Node<T> nodeToReplace, Node<T> replacementNode) {
        if (nodeToReplace == null) {
            throw new NullPointerException("Cannot replace null node.");
        }
        if (nodeToReplace.context[0] == null) {
            // we are replacing the root
            if (replacementNode != null)
                replacementNode.context[0] = null;
            this.root = replacementNode;
        } else {
            // set the parent of the replacement node
            if (replacementNode != null)
                replacementNode.context[0] = nodeToReplace.context[0];
            // do we have to attach a new left or right child to our parent?
            if (nodeToReplace.isRightChild()) {
                nodeToReplace.context[0].context[2] = replacementNode;
            } else {
                nodeToReplace.context[0].context[1] = replacementNode;
            }
        }
    }

    /**
     * Helper method that will return the inorder successor of a node with two children.
     * @param node the node to find the successor for
     * @return the node that is the inorder successor of node
     */
    protected Node<T> findMinOfRightSubtree(Node<T> node) {
        if (node.context[1] == null && node.context[2] == null) {
            throw new IllegalArgumentException("Node must have two children");
        }
        // take a steop to the right
        Node<T> current = node.context[2];
        while (true) {
            // then go left as often as possible to find the successor
            if (current.context[1] == null) {
                // we found the successor
                return current;
            } else {
                current = current.context[1];
            }
        }
    }

    /**
     * Helper method that will return the node in the tree that contains a specific
     * value. Returns null if there is no node that contains the value.
     * @return the node that contains the data, or null of no such node exists
     */
    protected Node<T> findNodeWithData(T data) {
        Node<T> current = this.root;
        while (current != null) {
            int compare = data.compareTo(current.data);
            if (compare == 0) {
                // we found our value
                return current;
            } else if (compare < 0) {
                // keep looking in the left subtree
                current = current.context[1];
            } else {
                // keep looking in the right subtree
                current = current.context[2];
            }
        }
        // we're at a null node and did not find data, so it's not in the tree
        return null; 
    }

    /**
     * This method performs an inorder traversal of the tree. The string 
     * representations of each data value within this tree are assembled into a
     * comma separated string within brackets (similar to many implementations 
     * of java.util.Collection, like java.util.ArrayList, LinkedList, etc).
     * @return string containing the ordered values of this tree (in-order traversal)
     */
    public String toInOrderString() {
        // generate a string of all values of the tree in (ordered) in-order
        // traversal sequence
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            Stack<Node<T>> nodeStack = new Stack<>();
            Node<T> current = this.root;
            while (!nodeStack.isEmpty() || current != null) {
                if (current == null) {
                    Node<T> popped = nodeStack.pop();
                    sb.append(popped.data.toString());
                    if(!nodeStack.isEmpty() || popped.context[2] != null) sb.append(", ");
                    current = popped.context[2];
                } else {
                    nodeStack.add(current);
                    current = current.context[1];
                }
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * This method performs a level order traversal of the tree. The string
     * representations of each data value
     * within this tree are assembled into a comma separated string within
     * brackets (similar to many implementations of java.util.Collection).
     * This method will be helpful as a helper for the debugging and testing
     * of your rotation implementation.
     * @return string containing the values of this tree in level order
     */
    public String toLevelOrderString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        if (this.root != null) {
            LinkedList<Node<T>> q = new LinkedList<>();
            q.add(this.root);
            while(!q.isEmpty()) {
                Node<T> next = q.removeFirst();
                if(next.context[1] != null) q.add(next.context[1]);
                if(next.context[2] != null) q.add(next.context[2]);
                sb.append(next.data.toString());
                if(!q.isEmpty()) sb.append(", ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    public String toString() {
        return "level order: " + this.toLevelOrderString() +
                "\nin order: " + this.toInOrderString();
    }
    
    /**
     * Tester class for the enforceRBTreePropertiesAfterInsert() method of the RedBlackTree class. 
     * It ensures the correctness of the implementation of the method on various scenarios. 
     */
    public class enforceRBTreeTester {
      
      /**
       * Tests the functionality and correctness of the enforceRBTreePropertiesAfterInsert() method 
       * in the event of CASE 1: parent is either black or null.
       */    
      @Test
      public void test1() {
        // (1) parent is null
        {
          // create tree
          RedBlackTree<Integer> tree1 = new RedBlackTree<Integer>();
          tree1.insert(55);
          
          // instantiate actual and expected variables
          int actualBlackHeight = tree1.root.blackHeight;
          int expectedBlackHeight = 1;
          String actualString = tree1.toString();
          String expectedString = "level order: [ 55 ]"
                                + "\nin order: [ 55 ]";
          
          // check if actual and expected variables match
          assertEquals(actualBlackHeight, expectedBlackHeight);
          assertEquals(actualString, expectedString);        
        }
        
        // (2) parent is black
        {
          // create tree
          RedBlackTree<Integer> tree2 = new RedBlackTree<Integer>();
          tree2.insert(55);
          tree2.insert(81);
          
          // instantiate actual and expected variables
          int actualBlackHeight55 = tree2.root.blackHeight;
          int expectedBlackHeight55 = 1;
          String actualString = tree2.toString();
          String expectedString = "level order: [ 55, 81 ]"
                                + "\nin order: [ 55, 81 ]";
          
          // check if actual and expected variables match
          assertEquals(actualBlackHeight55, expectedBlackHeight55);
          assertEquals(actualString, expectedString);  
        }
      }
      
      /**
       * Tests the functionality and correctness of the enforceRBTreePropertiesAfterInsert() method 
       * in the event of CASE 2a: parent is red and aunt is either black or null.
       */
      @Test
      public void test2() {
        // (1) parent is red and aunt is null; nodes ARE NOT in a “straight line”; right rotation at G
        {
          // create the tree
          RedBlackTree<Integer> tree1 = new RedBlackTree<Integer>();
          tree1.insert(55);
          tree1.insert(31);
          tree1.insert(40);
          
          // instantiate actual and expected variables
          int actualBlackHeight40 = tree1.root.blackHeight;
          int expectedBlackHeight40 = 1;
          int actualBlackHeight55 = tree1.root.context[2].blackHeight;
          int expectedBlackHeight55 = 0;
          String actualString = tree1.toString();
          String expectedString = "level order: [ 40, 31, 55 ]"
                                + "\nin order: [ 31, 40, 55 ]";
          
          // check if actual and expected variables match
          assertEquals(actualBlackHeight40, expectedBlackHeight40);
          assertEquals(actualBlackHeight55, expectedBlackHeight55);
          assertEquals(actualString, expectedString);
        }
        
        // (2) parent is red and aunt is black; nodes ARE in a “straight line”; left rotation at G
        {
          // create the tree
          // note: tree will go through red parent, red aunt case before reaching the current case
          RedBlackTree<Integer> tree2 = new RedBlackTree<Integer>();
          tree2.insert(14);
          tree2.insert(7);
          tree2.insert(20);
          tree2.insert(1);
          tree2.insert(11);
          tree2.insert(18);
          tree2.insert(25);
          tree2.insert(23);
          tree2.insert(29);
          tree2.insert(27);
          
          // instantiate actual and expected variables
          int actualBlackHeight14 = tree2.root.context[1].blackHeight;
          int expectedBlackHeight14 = 0;
          int actualBlackHeight20 = tree2.root.blackHeight;
          int expectedBlackHeight20 = 1;
          String actualString = tree2.toString();
          String expectedString = "level order: [ 20, 14, 25, 7, 18, 23, 29, 1, 11, 27 ]"
                                + "\nin order: [ 1, 7, 11, 14, 18, 20, 23, 25, 27, 29 ]";
          
          // check if actual and expected variables match
          assertEquals(actualBlackHeight14, expectedBlackHeight14);
          assertEquals(actualBlackHeight20, expectedBlackHeight20);
          assertEquals(actualString, expectedString);
        }
      }
      
      /**
       * Tests the functionality and correctness of the enforceRBTreePropertiesAfterInsert() method 
       * in the event of CASE 2b: parent is red and aunt is red.
       */
      @Test
      public void test3() {
        // (1) parent is red and aunt is red; parent of grandparent is null (grandparent is the root)
        {
          // create the tree
          RedBlackTree<Integer> tree1 = new RedBlackTree<Integer>();
          tree1.insert(55);
          tree1.insert(31);
          tree1.insert(64);
          tree1.insert(27);
          
          // instantiate actual and expected variables
          int actualBlackHeight55 = tree1.root.blackHeight;
          int expectedBlackHeight55 = 1;
          int actualBlackHeight31 = tree1.root.context[1].blackHeight;
          int expectedBlackHeight31 = 1;
          int actualBlackHeight64 = tree1.root.context[2].blackHeight;
          int expectedBlackHeight64 = 1;
          String actualString = tree1.toString();
          String expectedString = "level order: [ 55, 31, 64, 27 ]"
                                + "\nin order: [ 27, 31, 55, 64 ]";
          
        // check if actual and expected variables match
        assertEquals(actualBlackHeight55, expectedBlackHeight55);
        assertEquals(actualBlackHeight31, expectedBlackHeight31);
        assertEquals(actualBlackHeight64, expectedBlackHeight64);
        assertEquals(actualString, expectedString);
        }
        
        // (2) parent is red and aunt is red; parent of grandparent is red (recursive case)
        {
          // create the tree
          // note: case will return to case 2a (grandparent becomes the child)
          RedBlackTree<Integer> tree2 = new RedBlackTree<Integer>();
          tree2.insert(14);
          tree2.insert(7);
          tree2.insert(20);
          tree2.insert(1);
          tree2.insert(11);
          tree2.insert(18);
          tree2.insert(25);
          tree2.insert(23);
          tree2.insert(29);
          tree2.insert(27);
          
          // instantiate actual and expected variables
          int actualBlackHeight14 = tree2.root.context[1].blackHeight;
          int expectedBlackHeight14 = 0;
          int actualBlackHeight20 = tree2.root.blackHeight;
          int expectedBlackHeight20 = 1;
          String actualString = tree2.toString();
          String expectedString = "level order: [ 20, 14, 25, 7, 18, 23, 29, 1, 11, 27 ]"
                                + "\nin order: [ 1, 7, 11, 14, 18, 20, 23, 25, 27, 29 ]";
          
          // check if actual and expected variables match
          assertEquals(actualBlackHeight14, expectedBlackHeight14);
          assertEquals(actualBlackHeight20, expectedBlackHeight20);
          assertEquals(actualString, expectedString);
        }
      }     
    }


}
