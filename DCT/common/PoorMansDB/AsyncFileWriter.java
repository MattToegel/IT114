package DCT.common.PoorMansDB;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncFileWriter {
    // Method to write object content to a file asynchronously
    public static <T> void writeFileContent(String filePath, T object, Consumer<Boolean> callback) {
        // Run the operation asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                // Create directory path if it doesn't exist
                Path path = Paths.get(filePath);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }

                // Open the file as an OutputStream
                try (OutputStream output = Files.newOutputStream(path);
                        // Wrap the OutputStream in an ObjectOutputStream
                        ObjectOutputStream oos = new ObjectOutputStream(output)) {

                    // Write the object to the ObjectOutputStream
                    oos.writeObject(object);
                }
                return true;
            } catch (IOException e) {
                // If an exception occurs, print the error message
                System.out.println("Error writing to file: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        })
                // Once the CompletableFuture completes, pass the result to the callback
                .thenAccept(callback);
    }

    // Main method for testing
    public static void main(String[] args) {
        // Call the writeFileContent method with a file path, an object, and a callback
        writeFileContent("path/to/your/file", new DCT.common.Character(), success -> {
            if (success) {
                System.out.println("File has been written successfully.");
            } else {
                System.out.println("Failed to write to file.");
            }
        });
    }
}
