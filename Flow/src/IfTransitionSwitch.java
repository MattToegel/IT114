public class IfTransitionSwitch {
	public static void main(String[] args) {
		// Here's what we'd need before switch
		System.out.println("if..else if..else");
		int answer = 0;//
		if (answer == 0) {
			System.out.println("Do something for answer equals 0");
		} else if (answer == 1) {
			System.out.println("Do something for answer equals 1");
		} else if (answer == 2) {
			System.out.println("Do something for answer equals 2");
		} else if (answer == 3) {
			System.out.println("Do something for answer equals 3");
		} else if (answer == 4) {
			System.out.println("Do something for answer equals 4");
		} else {
			System.out.println("Unhandled answer");
		}
		System.out.println("Switch");
		// here's the same using switch
		switch (answer) {
		case 0:
			System.out.println("Do something for answer equals 0");
			// break;// this is important (see the end note on when to try commenting this
			break;// line out)
		case 1:
			System.out.println("Do something for answer equals 1");
			break;
		case 2:
			System.out.println("Do something for answer equals 2");
			break;
		case 3:
			System.out.println("Do something for answer equals 3");
			break;
		case 4:
			System.out.println("Do something for answer equals4");
			break;
		default:
			System.out.println("Unhandled answer");
			break;
		}
		// assuming you didn't change answer, go back to the switch and comment out the
		// line mentioned.
		// then rerun the script
	}
}