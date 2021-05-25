package main;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import merkle.*;


public class Client {
	// Client
	private static String fromServer, fromUser;
	private static MerkleTree clientTree = null;
	private static Node currNode = null;
	private static byte[] byteFile = null;
	private static boolean isReceiving = true;
	private static boolean downloading = false;
	private static boolean corruptionFlag = true;
	private static int leftTracker = 0;
	private static int rightTracker = 31;
	
	// Store data from server
	private static String serverRootHash = null;
	private static String fileName = null;

	
	public static void main(String[] args) throws IOException {
		String hostName = "localhost";
		int portNumber = 8210;

		// Attempt to connect with server
		try (
		    Socket ftSocket = new Socket(hostName, portNumber);
		    PrintWriter out = new PrintWriter(ftSocket.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(new InputStreamReader(ftSocket.getInputStream()));
		)
		{
			
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			
			
			//----------------------------------------------------------//
			/*                 MAIN ACTION STARTS HERE                  */
			//----------------------------------------------------------//
			while ((fromServer = in.readLine()) != null) {
				// Server-Check 0. - End Conversation
				if (fromServer.equals("-1")) {
			        break;
			    }

				// Server-Check 1. - Receive Download
				if (fromServer.startsWith("1")) {
					// Ignore Indicator
					fromServer = fromServer.substring(2);
					
					handleDownload();
				}
				
				// Server-Check 2. - Verification
				else if (fromServer.startsWith("2")) {
					if (serverRootHash == null) {
						serverRootHash = fromServer.substring(2);
					}
					handleVerification();
				}
				
				// Server-Check 3. - Fix Corruption
				else if (fromServer.startsWith("3")) {
					// Only notify user once if there was an error in transfer
					if (corruptionFlag) {
						System.out.println("Server: Corrupt File. Fixing file...");
						System.out.println("Client Root: " + clientTree.getRoot());
						System.out.println("Server Root: " + serverRootHash);
						corruptionFlag = false;
					}
					
					// Repair the bad bytes
					fixCorruption();
				}
				
				// Conversation
				else {
				    // Check to see if upload
				    if (fromServer.startsWith("4")) {
						isReceiving = false;
				    } else {
				    	isReceiving = true;
				    }
				    
				    // Catch successful transfer
				    if (fromServer.startsWith("5")) {
				    	// Write file to directory
				    	if (downloading) {
				    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ClientFiles/" + fileName;
				    		FileOutputStream outFile = new FileOutputStream(filePath);
				    		outFile.write(byteFile);
				    		outFile.close();
				    		downloading = false;
				    	}
				    	
						// Ignore Indicator
						fromServer = fromServer.substring(2);
						System.out.println("Client Root: " + clientTree.getRoot());
						System.out.println("Server Root: " + serverRootHash);
						System.out.println("Server: " + fromServer);
						
						// Reset client variables
						serverRootHash = null;
						corruptionFlag = true;
						
				    } else {
				    	
						// Ignore Indicator
						fromServer = fromServer.substring(2);
						System.out.println("Server: " + fromServer);
				    }

				    // Get input from user
				    fromUser = stdIn.readLine();
				    
				    // Echo user input
				    if (fromUser != null) {
				        System.out.println("Client: " + fromUser);
				    }
				    
				    if (!(isReceiving)) {
				    	handleUpload(stdIn);
				    }
				    
			    }

			    // Send response to server
		        out.println(fromUser);
			}
			
			// Close streams
			out.close();
			in.close();
			ftSocket.close();
		}

	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//                                     Helper Functions                                      //
	
	//-------------------------------------------------------
	// Name: handleDownload
	// Precondition: fromServer must be "filename length b1 b2 ... bK" where b1, b2, and bK are bytes.
    //            *** CAN MANUALLY CORRUPT FILE BY UNCOMMENTING TWO BLOCKS OF CODE *** 
	// Postcondition: Stores the file sent from the client to byteFile data member and updates
    //              fromUser data member with response for client. Also prepares for verification.
	//-------------------------------------------------------
	private static void handleDownload() {
		// Let us know if we are currently downloading
		isReceiving = true;
		downloading = true;
		
		System.out.println("Server: File sent.");
		
    	// Get file name
    	int idx = 0;
    	while (fromServer.charAt(idx) != ' ') {
    		++idx;
    	}
    	fileName = fromServer.substring(0,idx);
    	
    	////////////////////////////////////
    	// UNCOMMENT TO CORRUPT FILE
    	//corruptFile();
    	////////////////////////////////////
		
		// Get file from string
		byteFile = ProcessFile.stringToFile(fromServer.substring(idx + 1));
		
		///////////////////////////////////////////////////
		// UNCOMMENT SECTION TO SEE COPY OF UNVERIFIED FILE
		//saveCorruptedFile();
		///////////////////////////////////////////////////
		
		// Prepare for verification
		clientTree = new MerkleTree(byteFile);
		currNode = clientTree.getRoot();
		
		fromUser = "0";
	}
	
	//-------------------------------------------------------
	// Name: handleVerification
	// Precondition: isReceiving must be true if client is downloading, false if client is uploading.
	// Postcondition: Updates fromUser with response for client. Either accept and prepare for another
	//              file or reject and prepare to fix file.
	//-------------------------------------------------------
	private static void handleVerification() {
		// Discern upload or download
		if (isReceiving) {
			// Client has built its own Merkle Tree and Server has sent over its root hash
			fromServer = fromServer.substring(2);
			
			// Only tell user that server is verifying once
			if (fromUser.equals("0")) {
				System.out.println("Server: Verifying file transfer.\n");
			}
			
			// Compare root hashes
    		if (fromServer.equals(clientTree.getRoot().toString())) {
        		fromUser = "1";  // Positive is good
    		} else {
    			fromUser = "-1";  // Negative is bad (file was corrupted)
    		}
		} else {
			System.out.println("Server: Verifying file transfer.\n");

			// Client must send over root hash so Server can compare with the Merkle Tree it built
			fromUser = clientTree.getRoot().toString();
		}
	}
	
	//-------------------------------------------------------
	// Name: fixCorruption
	// Precondition: isReceiving must be true if client is downloading, false if client is uploading.
	// Postcondition: Updates fromUser with response for client. Either need to send/request another
	//              level of the merkle tree or send/exchange correct bytes.
	//-------------------------------------------------------
	private static void fixCorruption() {
		if (isReceiving) {
			// Client is downloading
			// Server needs to send over Merkle Nodes bit by bit until we find the screw up
			
			// Check to see if we've reached the bottom
			if (fromServer.charAt(1) == 'F') {
				System.out.println("\nFound corruption. Replacing bad bytes...");
				// Eureka! Fix the corrupted file
				// Ignore indicator
				fromServer = fromServer.substring(2);
				
				// Prepare to parse new data
				StringTokenizer tokenizer = new StringTokenizer(fromServer);
				
				// Create a bin the same size as the bottom row of bins in merkle tree
				int binWidth = byteFile.length / 31;
				
				// Create an index that will point to the starting byte in the file
				int i = 0;
				for (int j = 0; j < leftTracker; ++j) {
					i = i + binWidth;
				}

				// Replace old data with new data
				int k = 0;
				while (tokenizer.hasMoreTokens()) {
					byteFile[i + k] = Byte.parseByte(tokenizer.nextToken());
					++k;
				}
				
				// Restart verification process
	    		clientTree = new MerkleTree(byteFile);
	    		currNode = clientTree.getRoot();
    			fromUser = "0";
    			leftTracker = 0;
    			rightTracker = 31;
    			
			} else {
				// Keep going down the tree. Ignore Indicator.
				fromServer = fromServer.substring(2);
				
				// The Server has sent over the next level
				// Set up client and server children for comparison
				System.out.println("leftTracker: " + leftTracker);
				System.out.println("rightTracker: " + rightTracker + "\n");
				String clientLeft = currNode.getLeftChild().toString();
				String clientRight = currNode.getRightChild().toString();
				String[] serverChildren = fromServer.split(" ");
				
				// Compare children
				if (clientLeft.equals(serverChildren[0])) {
					System.out.println("Error in right...");
					System.out.println("ClientRight: " + clientRight);
					System.out.println("ServerRight: " + serverChildren[1]);
					// Right one is messed up
					fromUser = "R";
					// Follow along with server
					currNode = currNode.getRightChild();
					leftTracker = ((leftTracker + rightTracker) / 2) + 1;
				} else {
					System.out.println("Error in left...");
					System.out.println("ClientLeft: " + clientLeft);
					System.out.println("ServerLeft: " + serverChildren[0]);
					// Left one is messed up
					fromUser = "L";
					// Follow along with server
					currNode = currNode.getLeftChild();
					rightTracker = (leftTracker + rightTracker) / 2;
				}
			}
			
		} else {
			// Client is uploading
			// Client needs to send over Merkle Nodes bit by bit until we find the screw up
			
			// Find out which way to go from server
			if (fromServer.charAt(1) == 'L') {
				// Go left
				currNode = currNode.getLeftChild();
				rightTracker = (leftTracker + rightTracker) / 2;
				System.out.println("Error in left...");
			} else if (fromServer.charAt(1) == 'R'){
				// Go right
				currNode = currNode.getRightChild();
				leftTracker = ((leftTracker + rightTracker) / 2) + 1;
				System.out.println("Error in right...");
			}
			
			System.out.println("leftTracker: " + leftTracker);
			System.out.println("rightTracker: " + rightTracker + "\n");
			
			// Determine response
			if (currNode.isLeaf()) {
				// Found the screw up
				System.out.println("Found corruption. Replacing bad bytes...");
				
				// Create a bin the same size as the bottom row of bins in merkle tree
				int binWidth = byteFile.length / 31;
				
				// Create an index that will point to the starting byte in the file
				int i = 0;
				for (int j = 0; j < leftTracker; ++j) {
					i = i + binWidth;
				}
				
				// Now create new localized byte array to replace erroneous one
				int k = 0;
				byte[] fixedData;
				// Ensure byte array has exact length
				if ((byteFile.length - i) > binWidth) {
					fixedData = new byte[binWidth];
				} else {
					fixedData = new byte[byteFile.length - i];
				}
				
				// Put the correct data in the array
				while (k < fixedData.length) {
					fixedData[k] = byteFile[i+k];
					++k;
				}
				
				// Send over correct data and prepare to verify ("F" for found)
				fromUser = "F" + ProcessFile.byteArrayToString(fixedData);
				currNode = clientTree.getRoot();
	    		leftTracker = 0;
    			rightTracker = 31;
    			serverRootHash = null;
	    		
			} else {
				// Send over another level ("M" for more)
				fromUser = "M" + clientTree.getNextLevel(currNode);
			}
		}
	}
	
	//-------------------------------------------------------
	// Name: handleUpload
	// Precondition: None. Handles invalid inputs.
	// Postcondition: Updates fromUser with file for server or notifies client they gave an invalid input.
	//-------------------------------------------------------
	private static void handleUpload(BufferedReader stdIn) throws IOException {
    	// Ensure valid input on client side instead of on server
    	boolean flag = true;
    	
    	while (flag) {
    		// Choose from 2 options to upload
	    	if (fromUser.equals("1")) {
	    		// Turn file into byte array
	    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ClientFiles/lupoliIsTheBest.txt";
	    		byteFile = FileToByteArray.read(filePath);
	    		
	    		// Turn byte array into string
	    		fromUser = ProcessFile.fileToString(byteFile, "lupoliIsTheBest.txt");
	    		
	    		// Prepare for verification
	    		clientTree = new MerkleTree(byteFile);
	    		currNode = clientTree.getRoot();
	    		
	    		// Exit loop
	    		flag = false;
	    		
	    	} else if (fromUser.equals("2")) {
	    		// Turn file into byte array
	    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ClientFiles/favoriteLanguage.jpg";
	    		byteFile = FileToByteArray.read(filePath);
	    		
	    		// Turn byte array into string
	    		fromUser = ProcessFile.fileToString(byteFile, "favoriteLanguage.jpg");
	    		
	    		// Prepare for verification
	    		clientTree = new MerkleTree(byteFile);
	    		currNode = clientTree.getRoot();
	    		
	    		// Exit loop
	    		flag = false;
	    		
	    	} else {
		    	// Force user to give "1" or "2"
		    	System.out.println("Invalid input." + Menu.uploadOptions());
			    // Get input from user
			    fromUser = stdIn.readLine();
	    	}
    	}
    	System.out.println("Server: File received.");
	}

	//-------------------------------------------------------
	// Name: corruptFile
	// Precondition: None.
	// Postcondition: Changes one byte of the file.
	//-------------------------------------------------------
	private static void corruptFile() {
    	int mischiefLeftIdx = fromServer.length() * 1 / 3;
    	int mischiefRightIdx = mischiefLeftIdx;
    	if (fromServer.charAt(mischiefLeftIdx) == ' ') {
    		// Find entire byte
    		++mischiefLeftIdx;
    		mischiefRightIdx = mischiefLeftIdx;
    		while (fromServer.charAt(mischiefRightIdx + 1) != ' ') {
    			++mischiefRightIdx;
    		}
    	} else {
    		// Find entire byte
    		while (fromServer.charAt(mischiefLeftIdx - 1) != ' ') {
    			--mischiefLeftIdx;
    		}
    		while (fromServer.charAt(mischiefRightIdx + 1) != ' ') {
    			++mischiefRightIdx;
    		}
    	}
    	// Add one to byte
    	int newByte = Integer.parseInt(fromServer.substring(mischiefLeftIdx, mischiefRightIdx));
    	++newByte;
    	// Wrap byte around critical values
    	if (newByte == 128) {
    		newByte = 100;
    	} else if (newByte == 100) {
    		newByte = 10;
    	} else if (newByte == 10) {
    		newByte = 0;
    	}
    	// Create modified data
    	StringBuilder corruptData = new StringBuilder(fromServer);
    	corruptData.replace(mischiefLeftIdx, mischiefRightIdx, Integer.toString(newByte));
    	fromServer = corruptData.toString();
	}

	//-------------------------------------------------------
	// Name: saveCorruptedFile
	// Precondition: None.
	// Postcondition: Writes corrupted file to specified directory
	//-------------------------------------------------------
	private static void saveCorruptedFile() {
		String filePath;
		if (fileName.endsWith("txt")) {
			filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ClientFiles/aggieWarHymnCorrupted.txt";
		} else {
			filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ClientFiles/reveilleCorrupted.jpg";
		}
		try (
				FileOutputStream outFile = new FileOutputStream(filePath);
		) {
			outFile.write(byteFile);
			outFile.close();
		} catch (IOException e) {
			;  // Do nothing
		}
	}
}