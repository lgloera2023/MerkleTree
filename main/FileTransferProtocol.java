package main;

import java.io.*;

public class FileTransferProtocol {
    private static final int WAITING = -1;
    private static final int CONNECTED = 0;
    private static final int RECEIVING = 1;
    private static final int SENDING = 4;
    private static final int ANOTHER = 5;

	//-------------------------------------------------------
	// Name: processInput
	// Precondition: State must be WAITING, CONNECTED, RECEIVING, SENDING, or ANOTHER.
	// Postcondition: Returns the server's response as a string.
	//-------------------------------------------------------
    public String processInput(String theInput, State state) throws IOException {
        String theOutput = null;

        if (state.equals(WAITING)) {
            theOutput = "__Client connected." + Menu.mainOptions();
            state.set(CONNECTED);
        }
        
        else if (state.equals(CONNECTED)) {
        	
            if (theInput.equals("1")) {
            	theOutput = "4 " + Menu.uploadOptions();
            	state.set(RECEIVING);
            }
            else if (theInput.equals("2")) {
            	theOutput = "6 " + Menu.downloadOptions();
            	state.set(SENDING);
            }
            else {
                theOutput = "Invalid input." + Menu.mainOptions();
            }
        }
        
        else if (state.equals(ANOTHER)) {
            if (theInput.equalsIgnoreCase("y")) {
                theOutput = Menu.mainOptions();
                state.set(CONNECTED);
            } else {
                theOutput = "-1";
                state.set(WAITING);
            }
        }
        return theOutput;
    }
}
