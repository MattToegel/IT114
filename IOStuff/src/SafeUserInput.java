
import java.util.Scanner;

public class SafeUserInput {
	public static void main(String[] args) {
		/*
		 * normally if a piece of code could throw an error we wrap it in a try/catch
		 * block, in this case we're using a try-with-resources block so that if an
		 * error occurs or if we're done it'll auto close the stream for us
		 */
		try (Scanner input = new Scanner(System.in);) {
			System.out.println("Enter some text then hit enter");
			while (input.hasNext()) {
				String message = input.nextLine();
				System.out.println("You entered " + message);
				if (message.equalsIgnoreCase("quit")) {
					System.out.println("We hear ya loud and clear, good bye.");
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}