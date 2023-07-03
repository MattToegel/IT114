package DCT.server.PoorMansDB;


import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.logging.Logger;



public class AsyncFileChecker {
    private final Path directoryPath;
    private static final Logger logger = Logger.getLogger(AsyncFileChecker.class.getName());

    public AsyncFileChecker(String baseDirectory, String subDirectory) {
        this.directoryPath = Paths.get(baseDirectory, subDirectory);
        logger.info("Checking path " + this.directoryPath.toAbsolutePath());

        // Create directory if it doesn't exist
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
                logger.info("Directory created: " + this.directoryPath.toAbsolutePath());
            } catch (IOException e) {
                logger.severe("Error creating directory: " + e.getMessage());
            }
        }
        else{
            logger.info("Directory exists");
        }
    }

    public CompletableFuture<Boolean> fileExists(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Checking filename: " + fileName);
            Path filePath = directoryPath.resolve(fileName);
            return Files.exists(filePath);
        });
    }
}