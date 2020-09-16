import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorSample {
	public static void main(String[] args) {
		// Normally this is a quick way to create a list in "one line" however, this
		// sample fails
		// since a list created this way wraps the original array which doesn't support
		// what we're trying to do
		// see https://stackoverflow.com/a/28112444 for further info
		/*
		 * List<String> months = Arrays.asList( new String[] { "Jan", "Feb", "Mar",
		 * "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" });
		 */
		// This too doesn't work, try it and check the error logs in the console
		/*
		 * List<String> months = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun",
		 * "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
		 */
		// we have to use the standard .add() method
		List<String> months = new ArrayList<String>() {
			{
				add("Jan");
				add("Feb");
				add("Mar");
				add("Apr");
				add("May");
				add("Jun");
				add("Jul");
				add("Aug");
				add("Sep");
				add("Oct");
				add("Nov");
				add("Dec");
			}
		};
		// iterators are super helpful and are one way we can safely remove an element
		// from a collection during a loop
		Iterator<String> iter = months.iterator();
		while (iter.hasNext()) {
			String month = iter.next();// we must call this to get the value at the current iteration
			if (month.indexOf("M") > -1) {
				iter.remove();
			}
			System.out.println("Month: " + month);
		}
		System.out.println("You may think it didn't remove the values based on the output. Check again.");
		iter = months.iterator();
		while (iter.hasNext()) {
			String month = iter.next();// we must call this to get the value at the current iteration
			System.out.println("Month: " + month);
		}
	}
}