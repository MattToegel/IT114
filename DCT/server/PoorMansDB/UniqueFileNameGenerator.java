package DCT.server.PoorMansDB;

import java.nio.file.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * A class that generates unique file names within a specified directory.
 */
public class UniqueFileNameGenerator {
    private final Path directoryPath;
    private final AsyncFileChecker fileChecker;
private static final Logger logger = Logger.getLogger(UniqueFileNameGenerator.class.getName());
    /**
     * Constructs a UniqueFileNameGenerator with the given directory path and subdirectory.
     *
     * @param directoryPath the path of the directory where the files will be generated
     * @param subDirectory the subdirectory within the directoryPath where the files will be generated
     */
    public UniqueFileNameGenerator(String directoryPath, String subDirectory) {
        this.directoryPath = Paths.get(directoryPath, subDirectory);
        this.fileChecker = new AsyncFileChecker(directoryPath, subDirectory);
    }

    /**
     * Generates a unique file name based on the base name, extension, and suffix.
     *
     * @param baseName the base name of the file
     * @param extension the extension of the file
     * @param getSuffix a function that provides a suffix based on a counter
     * @return a CompletableFuture that will eventually contain the generated unique file name
     * @throws RuntimeException if the counter exceeds 3999 during file name generation
     */
    public CompletableFuture<String> generateUniqueFileName(String baseName, String extension, ISuffix getSuffix) {
        logger.info("Running genererateUniqueFileName");
        return CompletableFuture.supplyAsync(() -> {
            String fileName;
            int counter = 0;
            do {
                fileName = String.format("%s%s%s%s%s",
                    directoryPath.toString(),
                    System.getProperty("file.separator"),
                    baseName,
                    (counter == 0?"":" "+(getSuffix==null?counter:getSuffix.apply(counter))),//If a name exists, append a suffix via method or index via counter
                    extension);
                logger.info("Checking fileName instance " + fileName);
                counter++;

                // Throw an exception if the counter exceeds 3999
                if (counter > 3999) {
                    throw new RuntimeException("Counter exceeded 3999");
                }
            } while (fileChecker.fileExists(fileName).join()); // .join() waits for the CompletableFuture to complete
            return fileName;
        });
    }
}
