public class SwitchMissingCase {
	public static void main(String[] args) {
		int age = 22;// note the value that doesn't have a matchign case
		switch (age) {
		case 21:
			System.out.println("You have all the priviledges given at the legal age of 21");
		case 18:
			System.out.println("You have all the priviledges given at the legal age of 18");
			break;
		// note the missing default
		}
		System.out.println("Java seems ok with not having a case for " + age);
	}
}