/*****************************************
** File:    FileToByteArray.java
** Project: CSCE 314 Project, Fall 2020
** Author:  Luke Loera
** Date:    11/6/2020
** Section: 501
** E-mail:  lgloera2023@tamu.edu 
**
**   This file contains a function that will turn a text file
** into a byte array.
**
**
***********************************************/

package merkle;

import java.io.File;
import java.io.FileInputStream;

// ** FILE IO ** //
public class FileToByteArray {
	//-------------------------------------------------------
	// Name: read
	// Precondition: There is a file that exists with the path provided
	// Postcondition: Returns the contents of the file as a byte array
	//---------------------------------------------------------
	public static byte[] read(String filePath) {
		File file = new File(filePath);
		FileInputStream inFileStream = null;
		byte[] fileInBytes = new byte[(int) file.length()];
		
		// Open input file stream and convert file into an array of bytes
		try {
			inFileStream = new FileInputStream(file);
			inFileStream.read(fileInBytes);
			inFileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileInBytes;
	}
}
