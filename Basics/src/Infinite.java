import java.util.ArrayList;
import java.util.List;

public class Infinite {
	public static void main(String[] args) {
		// this just uses the "true" constant to prevent the loop from ending
		/*
		 * while (true) { // runs forever }
		 */

		// this creates an infinite loop by growing the array during each iteration
		// we can't easily do a basic array here since arrays are fixed size, so this
		// sample will use a list
		// we also must use the class of Integer instead of the primitive int

		List<Integer> ints = new ArrayList<Integer>();
		ints.add(1);
		for (int i = 0; i < ints.size(); i++) {
			System.out.println(i);
			ints.add(i);

		}

		// missing components of the for loop will just make it run continuously
		/*
		 * for (;;) { // runs forever }
		 */
		// there are many other ways we can fall into an infinite loop and most of the
		// time they're by accident
	}
}