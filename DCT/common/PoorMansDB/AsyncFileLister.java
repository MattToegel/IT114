package DCT.common.PoorMansDB;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class AsyncFileLister {
    // Method to list all files in a directory asynchronously
    public static void listFilesInDirectory(String directoryPath, String fileExtension, Consumer<List<Path>> callback) {
        // Run the operation asynchronously
        CompletableFuture<List<Path>> future = CompletableFuture.supplyAsync(() -> {
            try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(fileExtension))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                // If an exception occurs, print the error message
                System.out.println("Error listing files in directory: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        });

        // Once the CompletableFuture completes, pass the result to the callback
        future.thenAccept(callback);
    }

    // Main method for testing
    public static void main(String[] args) {
        // Call the listFilesInDirectory method with a directory path, file extension
        // and a callback
        listFilesInDirectory("path/to/your/directory", ".txt", paths -> {
            System.out.println("Found files: ");
            paths.forEach(System.out::println);
        });
    }
}
