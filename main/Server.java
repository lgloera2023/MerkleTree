package main;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.net.ServerSocket;
import java.net.Socket;
import merkle.FileToByteArray;
import merkle.MerkleTree;
import merkle.Node;

public class Server {
	// States
    private static final int WAITING = -1;
    private static final int RECEIVING = 1;
    private static final int VERIFYING = 2;
    private static final int FIXING = 3;
    private static final int SENDING = 4;
    private static final int ANOTHER = 5;
    private static State state = new State(WAITING);
    
	// Server
	private static String fromUser, fromServer;
	private static byte[] byteFile = null;
	private static MerkleTree serverTree = null;
	private static Node currNode = null;
	private static boolean isReceiving = false;
	private static int leftTracker = 0;
	private static int rightTracker = 31;
	
	// Store data from client
	private static String fileName = null;
	
	
    public static void main(String[] args) throws IOException {

        int portNumber = 8210;

        // Attempt to connect with client
        try ( 
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
        
            //String inputLine, outputLine;
            
            // Initiate conversation with client
            FileTransferProtocol ftp = new FileTransferProtocol();
            fromServer = ftp.processInput(null, state);
            out.println(fromServer);

            
            
			//----------------------------------------------------------//
			/*                 MAIN ACTION STARTS HERE                  */
			//----------------------------------------------------------//
            while ((fromUser = in.readLine()) != null) {
            	// Emergency exit clause
            	if (fromUser.equals("quit")) {
            		fromServer = "-1";
            		state.set(WAITING);
            		break;
            	}
            	
            	// Client-Check 1. - Receive Upload
            	if (state.equals(RECEIVING)) {          		
					handleUpload();
            	}
            	
            	// Client-Check 2. - Verification
            	else if (state.equals(VERIFYING)) {
            		handleVerification();
            	}
            	
            	// Client-Check 3. - Initialize Repairs
            	else if (state.equals(FIXING)) {          		
            		fixCorruption();
            	}
            	
            	// Client-Check 4. - Send download
                else if (state.equals(SENDING)) {
                	handleDownload();
                }
            	
            	// Conversation
            	else {
				    // Get output from protocol
				    fromServer = ftp.processInput(fromUser, state);
				    
				    if (state.equals(RECEIVING)) {
				    	isReceiving = true;
				    } else {
				    	isReceiving = false;
				    }
            	}
            	
            	// Send output to client
                out.println(fromServer);
                // Exit clause (check 0.)
                if (fromServer.equals("-1")) {
                    break;
                }
            }
            
		    // Close streams
		    out.close();
		    in.close();
		    serverSocket.close();
		    clientSocket.close();
		    
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
    
    
    
	///////////////////////////////////////////////////////////////////////////////////////////////
	//                                     Helper Functions                                      //
	
	//-------------------------------------------------------
	// Name: handleUpload
	// Precondition: fromUser must be "filename length b1 b2 ... bK" where b1, b2, and bK are bytes.
    //            *** CAN MANUALLY CORRUPT FILE BY UNCOMMENTING TWO BLOCKS OF CODE *** 
	// Postcondition: Stores the file sent from the client to byteFile data member and updates
    //              fromServer data member with response for client. Also prepares for verification.
	//-------------------------------------------------------
	private static void handleUpload() {
		// Let us know if we are currently downloading
		isReceiving = true;
		
    	// Get file name
    	int idx = 0;
    	while (fromUser.charAt(idx) != ' ') {
    		++idx;
    	}
    	fileName = fromUser.substring(0,idx);
		
    	////////////////////////////////////
    	// UNCOMMENT SECTION TO CORRUPT FILE
    	//corruptFile();
    	////////////////////////////////////
    	
		// Get file from string
		byteFile = ProcessFile.stringToFile(fromUser.substring(idx + 1));
		
		///////////////////////////////////////////////////
		// UNCOMMENT SECTION TO SEE COPY OF UNVERIFIED FILE
		//saveCorruptedFile();
		///////////////////////////////////////////////////
		
		// Prepare for verification
		serverTree = new MerkleTree(byteFile);
		currNode = serverTree.getRoot();
		fromServer = "2 " + serverTree.getRoot().toString();
		state.set(VERIFYING);
	}
	
	//-------------------------------------------------------
	// Name: handleVerification
	// Precondition: isReceiving must be true if client is uploading, false if client is downloading.
	// Postcondition: Updates fromServer with response for client. Either accept and prepare for another
	//              file or reject and prepare to fix file.
	//-------------------------------------------------------
	private static void handleVerification() {
		// Discern upload or download
    	if (isReceiving) {
    		// Server has built its own Merkle Tree and Client has sent over its root hash
    		if (fromUser.equals(serverTree.getRoot().toString())) {
    			// Write file to directory
	    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ServerFiles/" + fileName;
	    		try {
		    		FileOutputStream outFile = new FileOutputStream(filePath);
		    		outFile.write(byteFile);
		    		outFile.close();
	    		}
	    		catch (IOException e) {
	    			;  // Do nothing
	    		}
	    		
    			// Get next action
        		fromServer = "5 Successful transfer. Want to upload/download another? (y/n)";
        		state.set(ANOTHER);
        		
        		// Reset server variables
    			serverTree = null;
    			currNode = null;
    			fileName = null;
    			byteFile = null;
    			
    		} else {
    			fromServer = "3 " + serverTree.getRoot().toString();
    			state.set(FIXING);
    		}
    	} else {
    		// Server must send over root hash so Client can compare with the Merkle Tree it built
    		// Key:		-1 = file corrupted		 0 = not yet verified	  1 = file verified
    		
    		if (fromUser.equals("-1")) {
    			fromServer = "3 " + serverTree.getNextLevel(currNode);
    			state.set(FIXING);
    		}
    		else if (fromUser.equals("0")) {
    			fromServer = "2 " + serverTree.getRoot().toString();
    			currNode = serverTree.getRoot();
    		}
    		else {
    			fromServer = "5 Successful transfer. Want to upload/download another? (y/n)";
    			state.set(ANOTHER);
    			
    			// Reset server variables
    			serverTree = null;
    			currNode = null;
    			fileName = null;
    			byteFile = null;
    			
    		}
    	}
	}
	
	//-------------------------------------------------------
	// Name: fixCorruption
	// Precondition: isReceiving must be true if client is uploading, false if client is downloading.
	// Postcondition: Updates fromServer with response for client. Either need to send/request another
	//              level of the merkle tree or send/exchange correct bytes.
	//-------------------------------------------------------
	private static void fixCorruption() {
		// Discern upload or download
		if (isReceiving) {
			// Client is uploading
			// Enter quasi-recursive relationship between client and server
			
			// Check to see if we've reached the bottom
			if (fromUser.charAt(0) == 'F') {
				// Eureka! Fix the corrupted file
				// Ignore indicator
				fromUser = fromUser.substring(1);
				
				// Prepare to parse new data
				StringTokenizer tokenizer = new StringTokenizer(fromUser);
				
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
	    		serverTree = new MerkleTree(byteFile);
	    		currNode = serverTree.getRoot();
    			fromServer = "2 " + serverTree.getRoot().toString();
    			state.set(VERIFYING);
    			leftTracker = 0;
    			rightTracker = 31;
    			
			} else {
				// Keep going down the tree. Ignore Indicator.
				fromUser = fromUser.substring(1);
				
				// The Client has sent over the next level
				// Set up client and server children for comparison
				String serverLeft = currNode.getLeftChild().toString();
				String[] clientChildren = fromUser.split(" ");
				
				// Compare children
				if (serverLeft.equals(clientChildren[0])) {
					// Right one is messed up
					fromServer = "3R";
					// Follow along with client
					currNode = currNode.getRightChild();
					leftTracker = ((leftTracker + rightTracker) / 2) + 1;
				} else {
					// Left one is messed up
					fromServer = "3L";
					// Follow along with server
					currNode = currNode.getLeftChild();
					rightTracker = (leftTracker + rightTracker) / 2;
				}
				
			}
		} else {
			// Client is downloading
			// Enter quasi-recursive relationship between client and server

			// Find out which way to go from client
			if (fromUser.equals("L")) {
				// Go left
				currNode = currNode.getLeftChild();
				rightTracker = (leftTracker + rightTracker) / 2;
			} else {
				// Go right
				currNode = currNode.getRightChild();
				leftTracker = ((leftTracker + rightTracker) / 2) + 1;
			}
			
			// Determine response
			if (currNode.isLeaf()) {
				// Found the screw up
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
				
				// Send over correct data and prepare to verify
				fromServer = "3F" + ProcessFile.byteArrayToString(fixedData);
				currNode = serverTree.getRoot();
	    		state.set(VERIFYING);
	    		leftTracker = 0;
    			rightTracker = 31;
	    		
			} else {
				// Send over another level
				fromServer = "3 " + serverTree.getNextLevel(currNode);
			}
		}
	}
	
	//-------------------------------------------------------
	// Name: handleDownload
	// Precondition: None. Handles invalid inputs.
	// Postcondition: Updates fromServer with file for client or notifies client they gave an invalid input.
	//-------------------------------------------------------
	private static void handleDownload() {
    	// Find which file client wants to download
    	if (fromUser.equals("1")) {
    		// Turn file into byte array
    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ServerFiles/aggieWarHymn.txt";
    		byteFile = FileToByteArray.read(filePath);

    		// Turn byte array into string
    		fromServer = "1 " + ProcessFile.fileToString(byteFile, "aggieWarHymn.txt");

    		// Prepare to verify download
    		serverTree = new MerkleTree(byteFile);
    		currNode = serverTree.getRoot();
    		state.set(VERIFYING);
        	
    	} else if (fromUser.equals("2")) {
    		// Turn file into byte array
    		String filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ServerFiles/reveille.jpg";
    		byteFile = FileToByteArray.read(filePath);

    		// Turn byte array into string
    		fromServer = "1 " + ProcessFile.fileToString(byteFile, "reveille.jpg");
        	
    		// Prepare to verify download
    		serverTree = new MerkleTree(byteFile);
    		currNode = serverTree.getRoot();
    		state.set(VERIFYING);

    	} else {
    		fromServer = "--Invalid input." + Menu.downloadOptions();
    	}
	}

	//-------------------------------------------------------
	// Name: corruptFile
	// Precondition: None.
	// Postcondition: Changes one byte of the file.
	//-------------------------------------------------------
	private static void corruptFile() {
    	int mischiefLeftIdx = fromUser.length() * 3 /4;
    	int mischiefRightIdx = mischiefLeftIdx;
    	if (fromUser.charAt(mischiefLeftIdx) == ' ') {
    		// Find entire byte
    		++mischiefLeftIdx;
    		mischiefRightIdx = mischiefLeftIdx;
    		while (fromUser.charAt(mischiefRightIdx + 1) != ' ') {
    			++mischiefRightIdx;
    		}
    	} else {
    		// Find entire byte
    		while (fromUser.charAt(mischiefLeftIdx - 1) != ' ') {
    			--mischiefLeftIdx;
    		}
    		while (fromUser.charAt(mischiefRightIdx + 1) != ' ') {
    			++mischiefRightIdx;
    		}
    	}
    	// Add one to byte
    	int newByte = Integer.parseInt(fromUser.substring(mischiefLeftIdx, mischiefRightIdx));
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
    	StringBuilder corruptData = new StringBuilder(fromUser);
    	corruptData.replace(mischiefLeftIdx, mischiefRightIdx, Integer.toString(newByte));
    	fromUser = corruptData.toString();
	}
	
	//-------------------------------------------------------
	// Name: saveCorruptedFile
	// Precondition: None.
	// Postcondition: Writes corrupted file to specified directory
	//-------------------------------------------------------
	private static void saveCorruptedFile() {
		String filePath;
		if (fileName.endsWith("txt")) {
			filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ServerFiles/lupoliIsTheBestCorrupted.txt";
		} else {
			filePath = "C:/Users/lgloe/School/semester3/csce314/Project/Code/ServerFiles/favoriteLanguageCorrupted.jpg";
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