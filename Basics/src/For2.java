
public class For2 {
	public static void main(String[] args) {
		String[] arr = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
		// no I don't know why I chose to do days of the week

		int count = arr.length;// FWIW this is similar syntax to javascript arrays
		System.out.println("The array has " + count + " elements");
		for (int i = 0; i < count; i++) {
			System.out.println(arr[i]);
		}
	}
}