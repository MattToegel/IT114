public class Continue {
	public static void main(String[] args) {
		int number = 0;
		while (number < 20) {
			number++;
			if (number == 5) {
				continue;
			}
			// number++;
			// see what happens if we move number++; here (don't forget to comment out line
			// 5 before trying)
			System.out.println("Number: " + number);
		}
		System.out.println("Done looping");// for the comment on line 10, notice the output and notice that the program
											// doesn't terminate
	}
}