
public class Break {
	public static void main(String[] args) {
		while (true) {
			if (true) {
				break;
			}
			System.out.println("I'm loopin'");
		}
		System.out.println("We broke out of the loop");
		/*
		 * The code above is really pointless, it's the equivalent if we just ran line
		 * 7. It'll try to loop due to true but will exit at line 4 so won't even
		 * complete the loop. Your IDE may even highlight line 10 with "dead code"
		 * message.
		 */
	}
}