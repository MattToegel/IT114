
import java.util.Arrays;
import java.util.List;

public class Foreach {
	public static void main(String[] args) {
		// in java we define arrays with []
		// note the parameter in the main method
		String[] arr = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
		// no I don't know why I chose to do days of the week

		// this will look a bit backwards if you come from other language backgrounds
		// note we take the array first, then we get the value "as" the next variable we
		// declare
		for (String day : arr) {
			System.out.println(day);
		}

		// newer in Java some iterator types have a built in forEach
		// regular arrays don't but the class collections do
		// you'll need to import other parts of the library (see the top of the file)
		List<String> days = Arrays.asList(arr);
		days.forEach((day) -> {
			System.out.println(day);
		});
	}
}