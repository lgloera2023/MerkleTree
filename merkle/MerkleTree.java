/*****************************************
** File:    MerkleTree.java
** Project: CSCE 314 Project, Fall 2020
** Author:  Luke Loera
** Date:    11/5/2020
** Section: 501
** E-mail:  lgloera2023@tamu.edu 
**
**   This file contains the definition of the MerkleTree class.
** This MerkleTree inherits the basic functions defined in BinaryTree.
** The MerkleTree stores a tree of hash values where the data of
** each node is the hash value derived from its two children.
**
**
***********************************************/

package merkle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

// ** INHERITANCE AND ABSTRACTION ** //
public class MerkleTree extends BinaryTree {	
	//////////////////////////////////////////////////////////////////////
	////                     Default Constructor                      ////
	public MerkleTree() {
		super();
	}

	//////////////////////////////////////////////////////////////////////
	////                         Constructor                          ////
	public MerkleTree(String filePath) {
		// Convert file to byte array
		// ** FILE IO ** //
		byte[] fileInBytes = FileToByteArray.read(filePath);
		
		// Separate file into leaves
		// ** COLLECTIONS and GENERICS ** //
		LinkedList<Node> listOfNodes = new LinkedList<Node>();
		final int numBins = 31;
		int binWidth = fileInBytes.length / numBins;
		for (int i = 0; i < fileInBytes.length; i += binWidth) {
			// Create byte array chunk
			byte[] chunk = new byte[binWidth];
			int k = 0;
			while ((k < binWidth) && (i + k < fileInBytes.length)) {
				chunk[k] = fileInBytes[i+k];
				++k;
			}
			
			// Add the hash of the chunk to a node and then add node to list
			Node newLeaf = new Node(hash(chunk));
			listOfNodes.add(newLeaf);
		}
		
		// Generate Levels of Merkle Tree until coming to a peak
		buildTree(listOfNodes);
	}
	
	//////////////////////////////////////////////////////////////////////
	////                         Constructor                          ////
	public MerkleTree(byte[] fileInBytes) {		
		// Separate file into leaves
		LinkedList<Node> listOfNodes = new LinkedList<Node>();
		final int numBins = 31;
		int binWidth = fileInBytes.length / numBins;
		for (int i = 0; i < fileInBytes.length; i += binWidth) {
			// Create byte array chunk
			byte[] chunk = new byte[binWidth];
			int k = 0;
			while ((k < binWidth) && (i + k < fileInBytes.length)) {
				chunk[k] = fileInBytes[i+k];
				++k;
			}
			
			// Add the hash of the chunk to a node and then add node to list
			Node newLeaf = new Node(hash(chunk));
			listOfNodes.add(newLeaf);
		}
		
		// Generate Levels of Merkle Tree until coming to a peak
		buildTree(listOfNodes);
	}
	
	//////////////////////////////////////////////////////////////////////
	////                           Getters                            ////
	public byte[] getHashRoot() {return root.getNodeData();}
	
	
	//-------------------------------------------------------
	// Name: getNextLevel
	// Precondition: The node passed cannot be null.
	// Postcondition: Returns the left hash and the right hash separated by a space.
	//-------------------------------------------------------
	public String getNextLevel(Node subtree) {
		StringBuilder nextLevel = new StringBuilder();
		
		nextLevel.append(subtree.getLeftChild().toString());
		nextLevel.append(" ");
		nextLevel.append(subtree.getRightChild().toString());
		
		return nextLevel.toString();
	}

	
	//////////////////////////////////////////////////////////////////////
	////                       Helper Functions                       ////
	
	//-------------------------------------------------------
	// Name: buildTree
	// Precondition: Needs a linked list of 32 nodes.
	// Postcondition: Calls the recursive function that constructs the tree.
	//				  Results in a full tree with 63 nodes, 32 leaves, and height 5.
	//-------------------------------------------------------
	private void buildTree(LinkedList<Node> listOfNodes) {
		generateLevel(listOfNodes);
	}
	
	//-------------------------------------------------------
	// Name: generateLevel
	// Precondition: None.
	// Postcondition: Creates list of nodes all on that level (height).
	//-------------------------------------------------------
	private void generateLevel(LinkedList<Node> listOfNodes) {
		if (listOfNodes.size() == 1) {
			// Base case
			root = listOfNodes.getFirst();
			return;
		} else {
			// Recursive case
			// Go through list combining every other Node
			LinkedList<Node> newNodeList = new LinkedList<Node>();
			for (int i = 0; i < listOfNodes.size(); ++i) {
				if (i % 2 == 1) {
					// Combine
					Node left = listOfNodes.get(i-1);
					Node middle = listOfNodes.get(i);
					byte[] combined = combine(left, middle);
					
					// Hash the combined and create node
					combined = hash(combined);
					Node newNode = new Node(combined);
					newNode.setLeftChild(left);
					newNode.setRightChild(middle);
					
					// Add node to new list
					newNodeList.add(newNode);
					
					// Check to see if only one element left
					if (listOfNodes.size() - i == 2) {
						// Hash the last node
						Node last = listOfNodes.get(i + 1);
						byte[] lastParentData = hash(last.getNodeData());
						
						// Create node and add to new list
						Node lastParent = new Node(lastParentData);
						lastParent.setLeftChild(last);
						newNodeList.add(lastParent);
					}
				}
			}
			
			// Recursive call
			generateLevel(newNodeList);
		}
	}
	
	//-------------------------------------------------------
	// Name: combine
	// Precondition: Cannot be given null nodes.
	// Postcondition: Returns the b array concatenated to the a array.
	//-------------------------------------------------------
	private byte[] combine(Node a, Node b) {
		byte[] array1 = a.getNodeData();
		byte[] array2 = b.getNodeData();
		byte[] combined = new byte[array1.length + array2.length];
		int k = 0;  // keep track of position in combined[]
		for (int i = 0; i < array1.length; ++i) {
			combined[k] = array1[i];
			++k;
		}
		for (int j = 0; j < array2.length; ++j) {
			combined[k] = array2[j];
			++k;
		}
		return combined;
	}
	
	//-------------------------------------------------------
	// Name: displayHashRoot
	// Precondition: None.
	// Postcondition: Prints the cryptographic hash stored in the root
	//---------------------------------------------------------
	public void displayRoot() {
		if (root == null) {
			System.out.println("Empty tree.");
		} else {
			System.out.println(root);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	////                       Member Functions                       ////
	
	//-------------------------------------------------------
	// Name: hash
	// Precondition: None.
	// Postcondition: Returns the cryptographic hash of the byte array.
	//---------------------------------------------------------
	private byte[] hash(byte[] valuesToHash) {
		// Initialize instance of the 256-bit secure hash algorithm
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
		
		// Use SHA-256 algorithm to hash given byte array
		byte[] results = md.digest(valuesToHash);
		
        return results;
	}
}
