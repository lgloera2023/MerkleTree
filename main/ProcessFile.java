package main;

import java.util.StringTokenizer;

public class ProcessFile {
	
	//-------------------------------------------------------
	// Name: fileToString
	// Precondition: Can't be given null byte array. Filename should include file extension.
	// Postcondition: Returns string of "fileName length b1 b2 ... bK" where b1, b2, and bK are bytes. 
	//-------------------------------------------------------
	public static String fileToString(byte[] fileInBytes, String fileName) {
		StringBuilder conversion = new StringBuilder();
		// Header
		conversion.append(fileName + " ");
		conversion.append(fileInBytes.length);
		for (byte b : fileInBytes) {
			conversion.append(" ");
			conversion.append(b);
		}
		
		return conversion.toString();
	}
	
	//-------------------------------------------------------
	// Name: stringToFile
	// Precondition: String must be "length b1 b2 ... bK" where b1, b2, and bK are string representations of bytes.
	// Postcondition: Returns the byte array represented by string.
	//-------------------------------------------------------
    public static byte[] stringToFile(String file) {
    	// ** FILE IO ** //
    	StringTokenizer tokenizer = new StringTokenizer(file);
    	
    	// Find length of byte array
    	int fileLength = Integer.parseInt(tokenizer.nextToken());
    	
    	// Now turn string into byte array
    	byte[] byteFile = new byte[fileLength];
    	
    	int i = 0;
    	while (tokenizer.hasMoreTokens()) {
    		byteFile[i] = Byte.parseByte(tokenizer.nextToken());
    		++i;
    	}

    	return byteFile;
    }
    
	//-------------------------------------------------------
	// Name: byteArrayToString
	// Precondition: Cannot have a null byte array.
	// Postcondition: Returns string of "b1 b2 ... bK" where b1, b2, and bK are string representations of bytes.
	//-------------------------------------------------------
    public static String byteArrayToString(byte[] fileChunk) {
		StringBuilder conversion = new StringBuilder();
		
		conversion.append(fileChunk[0]);
		for (int i = 1; i < fileChunk.length; ++i) {
			conversion.append(" ");
			conversion.append(fileChunk[i]);
		}
		
		return conversion.toString();
    }
}
