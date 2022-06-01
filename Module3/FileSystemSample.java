package Module3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileSystemSample {
    public void createFileAndGetDetails(String fileName) {
        try {
            File fileReference = new File(fileName);
            if (fileReference.createNewFile()) {
                System.out.println("Didn't exist, created new");
            } else {
                System.out.println("File already exists");
            }
            System.out.println(fileName + " is located at " + fileReference.getAbsolutePath());
            if (fileReference.canRead()) {
                System.out.println(fileName + " is readable");
            } else {
                System.out.println(fileName + " is not readable");
            }
            if (fileReference.canWrite()) {
                System.out.println(fileName + " is writable");
            } else {
                System.out.println(fileName + " is not writable");
            }
            if (fileReference.canExecute()) {
                System.out.println(fileName + " is executable");
            } else {
                System.out.println(fileName + " is not executable");
            }
        } catch (IOException ie) {
            ie.printStackTrace();

        }
    }

    public void writeToFile(String fileName, String msg) {
        // Hint: use BufferedWriter for less IO operations (better performance)
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(msg);
            System.out.println("Wrote " + msg + " to " + fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readFromFile(String fileName) {
        File file = new File(fileName);
        try (Scanner reader = new Scanner(file)) {
            String fullText = "";
            while (reader.hasNextLine()) {
                String nl = reader.nextLine();
                System.out.println("Next line: " + nl);
                fullText += nl;
                // Scanner.nextLine() returns the line but excludes the line separator
                // so just append it back so it'll show correctly in the console
                if (reader.hasNextLine()) {// just a check to not append an extra line ending at the end
                    fullText += System.lineSeparator();
                }
            }
            System.out.println("Contents of " + fileName + ": ");
            System.out.println(fullText);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void appendToFile(String fileName, String msg) {
        // Hint: use BufferedWriter for less IO operations (better performance)
        try (FileWriter fw = new FileWriter(fileName, true);) {
            fw.write(System.lineSeparator());
            fw.write(msg);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * Generic write/append method
     * @param fileName path and name to file
     * @param msg message to add to file
     * @param append true to append, false to write
     */
    public void addText(String fileName, String msg, Boolean append){
        // Hint: use BufferedWriter for less IO operations (better performance)
        try (FileWriter fw = new FileWriter(fileName, append)) {
            fw.write(msg);
            System.out.println("Wrote " + msg + " to " + fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String fileName = "test.txt";
        FileSystemSample fss = new FileSystemSample();
        System.out.println("1) Create and get details");
        fss.createFileAndGetDetails(fileName);
        ///System.out.println("2) Write message");
        //fss.writeToFile(fileName, "Hello world! We're writing to files");
        System.out.println("4) Append message");
        fss.appendToFile(fileName, "This text is appended to the file now!");
        System.out.println("3) Read file");
        fss.readFromFile(fileName);
        //System.out.println("4) Append message");
        //fss.appendToFile(fileName, "This text is appended to the file now!");
        System.out.println("2) Write message");
        fss.writeToFile(fileName, "Hello world! We're writing to files");
        System.out.println("5) Read file");
        fss.readFromFile(fileName);
    }
}
