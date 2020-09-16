public class DoWhile {
	public static void main(String[] args) {
		// runs at least once
		int a = 0;
		do {
			System.out.println("a is greater than zero");
		} while (a > 0);

		a = 10;
		do {
			System.out.println("a is " + a);
			a--;
		} while (a > 0);
	}
}