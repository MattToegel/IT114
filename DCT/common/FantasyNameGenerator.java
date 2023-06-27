package DCT.common;

import java.util.Random;

public class FantasyNameGenerator {
    private static final Random random = new Random();
    private static final String[] SYLLABLES = { "dra", "ba", "gon", "mor", "zak", "rax", "gel", "dar", "lon", "fer",
            "ris", "dor", "nax", "qui", "zan", "xor", "nor", "gar", "ven", "xil", "zen", "qua", "zar", "xen", "nir",
            "gan", "len", "qar", "zin", "xan", "lor", "far", "rin", "dax", "qor", "lix", "nar", "mel", "lun", "sor",
            "bel", "ron", "tin", "fal", "vor", "min", "tol", "lan", "pax", "ter", "bol" };

    public static String generateCharacterName() {
        int syllableCount = random.nextInt(3) + 1; // Randomly choose between 1 and 3 syllables
    StringBuilder name = new StringBuilder();
    for (int i = 0; i < syllableCount; i++) {
        String syllable = SYLLABLES[random.nextInt(SYLLABLES.length)]; // Get a random syllable
        if (i == 0 || (i > 0 && name.charAt(name.length() - 1) == '\'')) { // If it's the first syllable or the previous character is an apostrophe
            syllable = syllable.substring(0, 1).toUpperCase() + syllable.substring(1); // Capitalize the first letter of the syllable
        }
        name.append(syllable);
        if (syllableCount == 2 && i == 0 && random.nextBoolean()) { // 50% chance to add an apostrophe between syllables for two syllable names
            name.append("'");
        }
    }
    return name.toString();
    }

    public static String convertToRomanNumerals(int number) {
        String[] thousands = { "", "M", "MM", "MMM" };
        String[] hundreds = { "", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM" };
        String[] tens = { "", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC" };
        String[] ones = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX" };

        String romanThousands = thousands[number / 1000];
        String romanHundreds = hundreds[(number % 1000) / 100];
        String romanTens = tens[(number % 100) / 10];
        String romanOnes = ones[number % 10];

        return romanThousands + romanHundreds + romanTens + romanOnes;
    }
    //test
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            System.out.println(generateCharacterName());
        }
    }
}
