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

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}