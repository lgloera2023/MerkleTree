/*****************************************
** File:    BinaryTree.java
** Project: CSCE 314 Project, Fall 2020
** Author:  Luke Loera
** Date:    11/7/2020
** Section: 501
** E-mail:  lgloera2023@tamu.edu 
**
**   This file contains the definition of the BinaryTree class. This
** class gives the basic helper functions of a binary tree. Intended
** to be inherited by children using the binary tree structure.
**
**
***********************************************/

package merkle;

// ** ABSTRACTION ** //
public abstract class BinaryTree {
	protected Node root;
	
	public Node getRoot() {return root;}
	
	public abstract void displayRoot();
	
	//////////////////////////////////////////////////////////////////////
	////                     Default Constructor                      ////
	public BinaryTree() {
		root = null;
	}
	
	//////////////////////////////////////////////////////////////////////
	////                  Protected Helper Functions                  ////
	
	//-------------------------------------------------------
	// Name: count
	// Precondition: None.
	// Postcondition: Returns how many nodes are beneath current node plus one
	//---------------------------------------------------------
	protected int count(Node subtree) {
		if (subtree == null) {
			return 0;
		} else {
			return 1 + count(subtree.getLeftChild()) + count(subtree.getRightChild());
		}
	}
	
	//-------------------------------------------------------
	// Name: height
	// Precondition: None.
	// Postcondition: Returns the max height between children of node plus one
	//---------------------------------------------------------
	protected int height(Node subtree) {
		if (subtree == null) {
			return 0;
		} else {
			// Find the heights of children
			int leftHeight = height(subtree.getLeftChild());
			int rightHeight = height(subtree.getRightChild());
			
			// Return the max plus 1
			if (leftHeight > rightHeight) {
				return leftHeight + 1;
			} else {
				return rightHeight + 1;
			}
		}
	}
	
	//-------------------------------------------------------
	// Name: display
	// Precondition: None.
	// Postcondition: Returns a string containing the current node's
	//    data followed by the strings formed from children.
	//---------------------------------------------------------
	protected String display(Node subtree) {
		if (subtree == null) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			
			// Add current node
			sb.append("Data: ");
			sb.append(subtree.toString());
			
			// Add children if they exist
			sb.append("\nleftChild: ");
			if (subtree.getLeftChild() != null) {
				sb.append(subtree.getLeftChild().toString());
			} else {
				sb.append("null");
			}
			sb.append("\nrightChild: ");
			if (subtree.getRightChild() != null) {
				sb.append(subtree.getRightChild().toString());	
			} else {
				sb.append("null");
			}
			
			// Recursive calls
			sb.append("\n\n");
			sb.append(display(subtree.getLeftChild()));
			sb.append(display(subtree.getRightChild()));
			
			return sb.toString();
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	////                   Public Helper Functions                    ////
	
	//-------------------------------------------------------
	// Name: countNodes
	// Precondition: None.
	// Postcondition: Returns the number of nodes in tree
	//---------------------------------------------------------
	public int countNodes() {
		return count(root);		
	}
	
	//-------------------------------------------------------
	// Name: maxHeight
	// Precondition: None.
	// Postcondition: Returns the longest path from root to leaf
	//---------------------------------------------------------
	public int maxHeight() {
		return height(root);
	}
	
	//-------------------------------------------------------
	// Name: toString
	// Precondition: None.
	// Postcondition: Returns a string with the data of all the nodes in tree
	//---------------------------------------------------------
	public final String toString() {
		return display(root);
	}
}