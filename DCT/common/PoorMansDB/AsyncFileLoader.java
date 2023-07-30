package DCT.common.PoorMansDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.*;

public class AsyncFileLoader {
    // Method to load file content asynchronously and cast it to a specified class
    public static <T> void loadFileContent(String filePath, Class<T> cls, Supplier<T> emptyObjectSupplier,
            Consumer<T> callback) {
        // Run the operation asynchronously
        CompletableFuture.supplyAsync(() -> {
            try (
                    // Open the file as an InputStream
                    InputStream input = Files.newInputStream(Paths.get(filePath));
                    // Wrap the InputStream in an ObjectInputStream
                    ObjectInputStream ois = new ObjectInputStream(input)) {
                // Read an object from the ObjectInputStream and cast it to the specified class
                return cls.cast(ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                // If an exception occurs, return an empty object of the specified class
                return emptyObjectSupplier.get();
            }
        })
                // Once the CompletableFuture completes, pass the result to the callback
                .thenAccept(callback)
                // If an exception occurs during the CompletableFuture, print the error message
                .exceptionally(ex -> {
                    System.out.println("Error loading file content: " + ex.getMessage());
                    return null;
                });
    }

    // Main method for testing
    public static void main(String[] args) {
        // Call the loadFileContent method with a file path, a class, an empty object
        // supplier, and a callback
        loadFileContent("path/to/your/file", DCT.common.Character.class, DCT.common.Character::new, myObject -> {
            // Use myObject as needed
            System.out.println(myObject);
        });
    }
}
