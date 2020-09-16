public class IfBad {
	public static void main(String[] args) {
		String a = "test";
		if (a == "test") {
			System.out.println("'a' matches what we expect");
		}
		// remember "!" negates (translates to "not equal to")
		if (a != "test") {
			System.out.println("'a' doesn't match what we expect");
		}
		// note: each condition gets evaluated
	}
}