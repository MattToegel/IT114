
public class MyClass {

	public static void main(String[] args) {
		System.out.println("Hello");// shortcut is sysout (ctrl + space)
		/*
		 * TODO Fill this in I'm a comment and won't run
		 */
		
		int myInt = 5;
		long myLong = 1l;
		float myFloat = 1.0f;
		double myDouble = 2.0d;

		System.out.println("My int is " + myInt);
		System.out.println("My float is " + myFloat);
		System.out.println("My Double is " + myDouble);
		System.out.println("My Long is " + myLong);

		/*
		 * BigDecimal a = new java.math.BigDecimal(0); BigDecimal b = new BigDecimal(1);
		 * 
		 * for(int i = 0; i < 10; i++) { a = a.add(new java.math.BigDecimal(0.1)); }
		 * System.out.println("A equals B: " + (a == b)); System.out.println("A: " + a);
		 * System.out.println("B: " + b);
		 */
		String s = new String("Hello");
		String s2 = "";
		
		
		int pointToAdd = 1000;
		int score = 0;
		int looped = 0;
		for (int i = 0; i < 5000000; i++) {
			// this is wrong
			//if ( (score + pointToAdd) < Integer.MAX_VALUE){
			if (score < Integer.MAX_VALUE - pointToAdd) {
				score += pointToAdd;

			}
			if (score < 0) {
				looped++;
			}

		}
		System.out.println("My Score: " + score);
		System.out.println("We overflow " + looped + " times");
	}

}
