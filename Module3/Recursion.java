package Module3;

public class Recursion {

    public static int sum(int num) {
        System.out.println("On the stack " + num);
        if (num > 0) {
            int s = sum(num - 1);
            System.out.println(num + " + " + s + " = " + (num + s));
            int n = num + s;
            System.out.println("Returning " + n);
            return n;
        }
        return 0;
    }

    public static void main(String[] args) {

        System.out.println(sum(2));

    }
}
