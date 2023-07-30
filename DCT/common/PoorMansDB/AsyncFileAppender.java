package DCT.common.PoorMansDB;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class AsyncFileAppender {

    // Method to append data to a file asynchronously
    public static void appendToFile(String filePath, String data, Consumer<Boolean> callback, LineIndexer indexer) {
        try {
            // Convert the file path string to a Path object
            Path path = Paths.get(filePath);

            // Open the file channel for writing. If the file doesn't exist, it will be
            // created.
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE);

            // Allocate a byte buffer to hold the data to be written
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Get the current size of the file. We'll start writing at this position, which
            // effectively appends to the file.
            long position = fileChannel.size();

            // Put the data into the buffer, then flip the buffer to prepare it for writing
            buffer.put((data + System.lineSeparator()).getBytes());
            buffer.flip();

            // Start the asynchronous write operation
            Future<Integer> operation = fileChannel.write(buffer, position);

            // Create a CompletableFuture that completes when the write operation is done
            CompletableFuture.runAsync(() -> {
                try {
                    // Wait for the write operation to complete
                    operation.get();
                    // Check if the write operation was successful
                    if (operation.isDone() && !operation.isCancelled() && indexer != null) {
                        // Update the line index
                        indexer.updateIndexWithNewLine(fileChannel.size());
                    }
                    // Once the write operation is done, invoke the callback with success
                    callback.accept(true);
                } catch (Exception e) {
                    // If an exception occurred during the write operation, print the stack trace
                    e.printStackTrace();

                    // Then invoke the callback with failure
                    callback.accept(false);
                }
            });

        } catch (Exception e) {
            // If an exception occurred during the write operation, print the stack trace
            e.printStackTrace();

            // Then invoke the callback with failure
            callback.accept(false);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        // Call the appendToFile method with a file path, some data, and a callback that
        // prints a message on success or failure
        appendToFile("output.txt", "Data to write", success -> {
            if (success) {
                System.out.println("Write done");
            } else {
                System.out.println("Write failed");
            }
        }, null);
    }
}
