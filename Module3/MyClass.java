package Module3;

public class MyClass {
	String name = "";
	int number = 0;

	// we keep this so we can run our sample
	public static void main(String[] args) {
		MyClass sample = new MyClass();
		sample.name = "Test";
		sample.number = 1;
		System.out.println("Object 1: " + sample.name + " " + sample.number);

		MyClass sample2 = new MyClass();
		System.out.println("Object 2: " + sample2.name + " " + sample2.number);
	}
}