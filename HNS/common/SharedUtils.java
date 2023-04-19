package HNS.common;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class SharedUtils {
    public static Set<Integer> pickRandomNumbers(int x, int a, int b) {
        if (x > (b - a + 1)) {
            throw new IllegalArgumentException("The count of random numbers to pick is larger than the range.");
        }
        Random random = new Random();
        Set<Integer> randomNumbers = new HashSet<>();

        while (randomNumbers.size() < x) {
            int randomNumber = a + random.nextInt(b - a + 1);
            randomNumbers.add(randomNumber);
        }

        return randomNumbers;
    }
}
