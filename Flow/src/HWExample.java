public class HWExample {
	public static void main(String[] args) {
		E1();
		E2();
		E3();
	}

	static void E1() {
		System.out.println("Java Exercise [Random] Exercise [1]");
		int number = 1;
		number++;
		System.out.println("Number is " + number);
	}

	static void E2() {
		System.out.println("Java Exercise [Random] Exercise [2]");
		int number = 5;
		System.out.println("Number modulo 2 is " + (number % 2));
	}

	static void E3() {
		System.out.println("Java Exercise [Random] Exercise [3]");
		int number = 10;
		if (number >= 10) {
			System.out.println("Hurray");
		}
	}
}