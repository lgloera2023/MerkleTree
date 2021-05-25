package main;

// ** WRAPPER CLASS ** //
public class State {
	private int state;
	
	/////////////////////////////////////////////
	// Default Constructor
	public State() {
		state = 0;
	}
	
	/////////////////////////////////////////////
	// Constructor
	public State(int state) {
		this.state = state;
	}
	
	/////////////////////////////////////////////
	// Setter
	public void set(int state) {
		this.state = state;
	}
	
	/////////////////////////////////////////////
	// Helper
	
	//-------------------------------------------------------
	// Name: equals
	// Precondition: None.
	// Postcondition: Returns true if argument equals the integer stored in data member.
	//-------------------------------------------------------
	public boolean equals(int rhs) {
		return state == rhs;
	}
}
