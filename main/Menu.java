package main;

public class Menu {
	
	//-------------------------------------------------------
	// Name: mainOptions
	// Precondition: None.
	// Postcondition: Returns a string of the main menu options.
	//-------------------------------------------------------
	public static String mainOptions() {
    	StringBuilder menu = new StringBuilder();
    	
    	menu.append("    1.) Upload");
    	menu.append("    2.) Download");
    	menu.append("    (Enter \"1\" or \"2\")");
    	
    	return menu.toString();
	}

	//-------------------------------------------------------
	// Name: uploadOptions
	// Precondition: None.
	// Postcondition: Returns a string of the menu for uploading a file.
	//-------------------------------------------------------
    public static String uploadOptions() {
    	StringBuilder options = new StringBuilder();
    	
    	options.append("    1.) lupoliIsTheBest.txt");
    	options.append("    2.) favoriteLanguage.jpg");
    	options.append("    (Enter \"1\" or \"2\")");
    	
    	return options.toString();
    }
    
	//-------------------------------------------------------
	// Name: downloadOptions
	// Precondition: None.
	// Postcondition: Returns a string of the menu for downloading a file.
	//-------------------------------------------------------
    public static String downloadOptions() {
    	StringBuilder options = new StringBuilder();

    	options.append("    1.) aggieWarHymn.txt");
    	options.append("    2.) reveille.jpg");
    	options.append("    (Enter \"1\" or \"2\")");
    	
    	return options.toString();
    }
}
