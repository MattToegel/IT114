public class Scope {
	int c = 4;

	public static void main(String[] args) {
		int a = 1;
		System.out.println("a: " + a);
		{
			int b = 2;
			System.out.println("b: " + b);
		}
		// b isn't accessible here because we're no longer
		/// in the block it was defined in
		// uncomment the following line to see
		// System.out.println("b: " + b);

		// variable can be redefined here because it only exists in the previous block's
		// scope, not this block's scope
		int b = 3;
		System.out.println("b: " + b);

		// Later we'll learn about modifiers but for now just follow the below
		// c isn't accessible here because it's outside of the current scope of main()
		// uncomment the below line to see
		// System.err.println("c: " + c);
		// Normally you'd acces a class level variable via "this"
		// but that won't work here because this method is static (which we'll cover
		// later)
		// uncomment the below line to see
		// System.out.println("c: " + this.c);

		// for completeness let's see how we can see variable c
		// We need to create an object from this class definition
		// then usign the object reference we can now access the object's value of c
		Scope scope = new Scope();
		System.out.println("c: " + scope.c);
	}
}