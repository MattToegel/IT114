
public class Overflow {
	public static void main(String[] args) {
		// in this overview we got introduced to data types and their limits
		// for this example we'll use a byte since it's small and reduces the output to
		// the console
		byte myByte = 127;// remember the range of a byte is -128 - 127
		System.out.println("My byte is " + myByte);
		// let's see what happens when we add 1
		myByte++;// shorthand for incrementing by 1
		System.out.println("My byte is " + myByte);
		// huh, it's not 128 like we expected.
		// this is called an overflow, it's when the value overflows its container
		// and cycles around again from the opposite end of the range (i.e., positive to
		// negative, negative to positive)
		// let's reset it and try it the other way
		myByte = -128;
		System.out.println("My byte is " + myByte);
		myByte--;// shorthand for decrementing by 1
		System.out.println("My byte is " + myByte);
		// as expected now it's a positive value now that we learned about overflow.
		// This is an important thing to consider when picking and utilizing your data
		// types.
	}
}