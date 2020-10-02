class Factorial {
	// from https://www.programiz.com/java-programming/recursion
	static int factorial(int n) {
		if (n != 0) { // termination condition
			System.out.println(n);
			int t = n * factorial(n - 1);
			System.out.println("F: " + t);
			return t; // recursive call
		} else {
			return 1;
		}
	}

	public static void main(String[] args) {
		int number = 3, result;
		result = factorial(number);
		System.out.println(number + " factorial = " + result);
	}
}