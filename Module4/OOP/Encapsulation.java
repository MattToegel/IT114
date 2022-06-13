package Module4.OOP;

public class Encapsulation {
	private int number;
	private boolean isActive = false;

	// setter for number
	public void setNumber(int number) {
		// if we use the same variable name as the parameter
		// we need to prefix our desired variable with "this"
		if (number > 0) {
			this.number = number;
		}
	}

	// getter for number
	public int getNumber() {
		return number;// or return this.number; //here's it's the same
	}

	// setter for isActive
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	// getter for isActive
	public boolean isActive() {
		return this.isActive;// showing how we alternatively can prefix the variable here
	}
}