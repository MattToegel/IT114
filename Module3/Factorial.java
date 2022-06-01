package Module3;

public class Factorial {
    // from https://www.programiz.com/java-programming/recursion
    static int factorial(int n) {
        System.out.println("On the stack " + n);
        if (n != 0) { // termination condition
            // return n * factorial(n - 1); // recursive call
            int f = factorial(n - 1);
            System.out.println(n + " * " + f + " = " + (n * f));

            int nn = n * f;
            System.out.println("Returning " + nn);
            return nn;
        } else {
            return 1;
        }
    }

    public static void main(String[] args) {
        int number = 5, result;
        result = factorial(number);
        System.out.println(number + " factorial = " + result);
    }
}
