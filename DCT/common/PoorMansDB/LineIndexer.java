package DCT.common.PoorMansDB;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LineIndexer {
    // List to store the file pointer positions for each line
    private final List<Long> lineIndex = new ArrayList<>();
    // Path of the file to be indexed
    private Path filePath;

    // Constructor
    public LineIndexer(Path filePath) throws IOException {
        // Store the file path
        this.filePath = filePath;
        // Open the file with RandomAccessFile in read mode
        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
            // Add the initial file pointer (0) to the index
            lineIndex.add(file.getFilePointer());
            String line = null;
            // Read each line of the file
            while ((line = file.readLine()) != null) {
                // If the line is not empty, add the current file pointer to the index
                if (line.trim().length() > 0) {
                    lineIndex.add(file.getFilePointer());
                }
            }
        }
    }

    // Method to get a specific line from the file
    public void getLine(int incLineNumber, Consumer<String> callback) {
        incLineNumber--;// Convert to index
        final int lineNumber = incLineNumber; // Make effectively final
        // Run the operation asynchronously
        CompletableFuture.runAsync(() -> {

            // Synchronize to prevent concurrent modifications of the file
            synchronized (this) {

                // Check if the line number is valid
                if (lineNumber < 0 || lineNumber >= lineIndex.size()) {
                    throw new IllegalArgumentException("Invalid line number");
                }

                // Open the file with RandomAccessFile in read mode
                try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                    // Seek to the position of the specified line
                    long pointer = lineIndex.get(lineNumber);
                    file.seek(pointer);
                    // Read the line
                    String line = file.readLine();
                    // Pass the line to the callback
                    if (callback != null) {
                        callback.accept(line);
                    }
                } catch (IOException e) {
                    // Print the stack trace if an exception occurs
                    e.printStackTrace();
                    // Pass null to the callback
                    if (callback != null) {
                        callback.accept(null);
                    }
                }
            }
        });
    }

    // Method to update the index when a new line is added to the file
    public synchronized void updateIndexWithNewLine(long position) {
        // Add the new position to the index
        lineIndex.add(position);
    }
}
