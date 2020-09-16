public class IfElseIfElse {
	public static void main(String[] args) {
		int age = 20;// <--- Change this value and rerun it
		if (age >= 21) {// if this is true
			System.out.println("You're at least 21 years old!");
		} else if (age >= 18) {// otherwise if this is true
			System.out.println("You're at least 18 years old!");
		} else {// if no other condition was met
			System.out.println("You're under 18 years old");
		}
		/*
		 * Note: The order of the logic matters, the first true condition is what's
		 * evaluated, anything else after is not evaluated, not even to check if it's
		 * true. If you switched lines 3 and 6 (and their respective echo statements),
		 * both 18 and 21 would trigger "You're at least 18 years old!". Try it. It's
		 * also important to not that this if, else if, else order is the only order you
		 * can have these in. else if if else is wrong else else if if is wrong else if
		 * else if is wrong etc
		 */
	}
}