import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileSystemSample {
	//Main
	public static void main(String[] args) {
		String fileName = "test.txt";
		FileSystemSample fss = new FileSystemSample();
		fss.createFileAndGetDetails(fileName);
		fss.writeToFile(fileName, "Hello world! We're writing to files");
		fss.readFromFile(fileName);
		//fss.appendToFile(fileName, "This text is appended to the file now!");
		//fss.readFromFile(fileName);
	}
	public void readFromFile(String fileName) {
		File file = new File(fileName);
		try(Scanner reader = new Scanner(file)){
			String fullText = "";
			while (reader.hasNextLine()) {
				String nl = reader.nextLine();
				System.out.println("Next line: " + nl);
		        fullText += nl;
		        //Scanner.nextLine() returns the line but excludes the line separator
		        //so just append it back so it'll show correctly in the console
		        if(reader.hasNextLine()) {//just a check to not append an extra line ending at the end
		        	fullText += System.lineSeparator();
		        }
		    }
			System.out.println("Contents of " + fileName + ": ");
			System.out.println(fullText);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writeToFile(String fileName, String msg) {
		//Hint: use BufferedWriter for less IO operations (better performance)
		try(FileWriter fw = new FileWriter(fileName)){
			fw.write(msg);
			System.out.println("Wrote " + msg + " to " + fileName);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFileAndGetDetails(String fileName) {
		try {
			File fileReference = new File(fileName);
			if(fileReference.createNewFile()) {
				System.out.println("Didn't exist, created new");
			}
			else {
				System.out.println("File already exists");
			}
			System.out.println(fileName + " is located at " + fileReference.getAbsolutePath());
			if(fileReference.canRead()) {
				System.out.println(fileName + " is readable");
			}
			else {
				System.out.println(fileName + " is not readable");
			}
			if(fileReference.canWrite()) {
				System.out.println(fileName + " is writable");
			}
			else {
				System.out.println(fileName + " is not writable");
			}
			if(fileReference.canExecute()) {
				System.out.println(fileName + " is executable");
			}
			else {
				System.out.println(fileName + " is not executable");
			}
		}
		catch(IOException ie) {
			ie.printStackTrace();
		
		}
	}
	
	
	public void appendToFile(String fileName, String msg) {
		//Hint: use BufferedWriter for less IO operations (better performance)
		try(FileWriter fw = new FileWriter(fileName, true);){
			fw.write(System.lineSeparator());
			fw.write(msg);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}