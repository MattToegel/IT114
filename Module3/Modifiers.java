package Module3;

public class Modifiers {
    final String name = "Mods";
    private int number = 0;
    static int count = 0;

    private void showNumber() {
        System.out.println("My number is " + number);
    }

    public void publicShowNumber() {
        showNumber();
    }

    public static void showCount() {
        System.out.println("The count is " + count);
    }

    public static void increment() {
        count++;
    }

    public static void main(String[] args) {
        Modifiers m1 = new Modifiers();
        System.out.println("Line 27");
        m1.showNumber();// even though it's private it's accessible here because this main method is in
                        // the same class
        // m1.name = "test";//this doesn't even compile because you can't change a final
        // value
        System.out.println("Line 32");
        m1.showCount();// this should show an IDE warning saying "it must be accessed in a static way
        System.out.println("Line 34");
        Modifiers.showCount();// this is the proper way to access a static method/attribute, we use the class
                              // name
        Modifiers.increment();
        System.out.println("Line 38");
        Modifiers.showCount();
        System.out.println("Line 40");
        m1.showCount();
        Test2 t = new Test2();
        // t.showNumber();//this is unavailable because it's outside of our class even
        // though it's the same file
        System.out.println("Line 45");
        t.publicShowNumber();// we can call this since it's public
    }
}

class Test2 {
    private int number = 10;

    private void showNumber() {
        System.out.println("My number is " + number);
    }

    public void publicShowNumber() {
        showNumber();
    }
}