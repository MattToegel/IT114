public class Recursion {

	public static int sum(int num) {
		if (num > 0) {
			System.out.println(num);
			int v = num + sum(num - 1);
			System.out.println("Current sum: " + v);
			return v;
		}
		return 0;
	}

	public static void main(String[] args) {
		System.out.println(sum(3));
	}
}