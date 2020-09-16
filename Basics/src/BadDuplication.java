
public class BadDuplication {
	public static void main(String[] args) {
		// don't do this
		String name = "";
		System.out.println("Begin code block I need to duplicate");
		name = "John";
		System.out.println("Hello, " + name);
		System.out.println("End code block I need to duplicate");

		System.out.println("Begin code block I need to duplicate");
		name = "John";
		System.out.println("Hello, " + name);
		System.out.println("End code block I need to duplicate");

		System.out.println("Begin code block I need to duplicate");
		name = "John";
		System.out.println("Hello, " + name);
		System.out.println("End code block I need to duplicate");

	}
}