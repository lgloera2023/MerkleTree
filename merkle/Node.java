/*****************************************
** File:    Node.java
** Project: CSCE 314 Project, Fall 2020
** Author:  Luke Loera
** Date:    11/5/2020
** Section: 501
** E-mail:  lgloera2023@tamu.edu 
**
**   This file contains the definition of the Node class
** to be used in the Merkle Tree. There are only getters, setters,
** and the toString() function for this class.
**
**
***********************************************/

package merkle;

public class Node {
	private byte[] data;
	private Node leftChild;
	private Node rightChild;
	
	//////////////////////////////////////////////////////////////////////
	////                     Default Constructor                      ////
	public Node() {
		leftChild = null;
		rightChild = null;
	}
	
	//////////////////////////////////////////////////////////////////////
	////                         Constructor                          ////
	public Node(byte[] hashedData) {
		// Create deep copy of hashedData
		data = new byte[hashedData.length];
		for (int i = 0; i < hashedData.length; ++i) {
			data[i] = hashedData[i];
		}

		leftChild = null;
		rightChild = null;
	}
	
	//////////////////////////////////////////////////////////////////////
	////                           Getters                            ////
	public byte[] getNodeData() {return data;}
	public Node getLeftChild() {return leftChild;}
	public Node getRightChild() {return rightChild;}
	
	//////////////////////////////////////////////////////////////////////
	////                           Setters                            ////
	public void setNodeData(byte[] data) {
		// Create deep copy of data
		for (int i = 0; i < data.length; ++i) {
			this.data[i] = data[i];
		}
	}
	public void setLeftChild(Node child) {this.leftChild = child;}
	public void setRightChild(Node child) {this.rightChild = child;}

	//////////////////////////////////////////////////////////////////////
	////                       Helper Functions                       ////
	
	//-------------------------------------------------------
	// Name: isLeaf
	// Precondition: The node exists (not null)
	// Postcondition: Returns true if node does not have any children
	//---------------------------------------------------------
	public boolean isLeaf() {
		return (leftChild == null) && (rightChild == null);
	}
	
	//-------------------------------------------------------
	// Name: toString
	// Precondition: None.
	// Postcondition: Returns string of cryptographic hash in hexadecimal
	//---------------------------------------------------------
	public String toString() {
		if (data != null) {
	        StringBuilder sb = new StringBuilder();

	        for (byte b : data) {
	            sb.append(String.format("%02x", b));
	        }

	        return sb.toString();
		} else {
			return "null";
		}
	}
}
