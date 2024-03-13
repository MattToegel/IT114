package Project.Common;

public abstract class TextFX {
    // For chatroom projects, this does not satisfy the text formatting
    // feature/requirements
    public enum Color {
        BLACK("\033[0;30m"),
        RED("\033[0;31m"),
        GREEN("\033[0;32m"),
        YELLOW("\033[0;33m"),
        BLUE("\033[0;34m"),
        PURPLE("\033[0;35m"),
        CYAN("\033[0;36m"),
        WHITE("\033[0;37m");

        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static final String RESET = "\033[0m";

    /**
     * Generates a String with the original message wrapped in the ASCII of the
     * color and RESET
     * <br>
     * Note: May not work for all terminals
     * 
     * @param text  Input text to colorize
     * @param color Enum of Color choice from TextFX.Color
     * @return wrapped String
     */
    public static String colorize(String text, Color color) {
        StringBuilder builder = new StringBuilder();
        builder.append(color.getCode());
        builder.append(text);
        builder.append(RESET);
        return builder.toString();
    }

    public static void main(String[] args) {
        // Example usage:
        System.out.println(TextFX.colorize("Hello, world!", Color.RED));
        System.out.println(TextFX.colorize("This is some blue text.", Color.BLUE));
        System.out.println(TextFX.colorize("And this is green!", Color.GREEN));
    }
}
