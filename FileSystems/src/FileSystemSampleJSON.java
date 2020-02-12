import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FileSystemSampleJSON {
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
	public void doJSONSample() {
		//JSON
		//prep our sample file
		String jsonFile = "sample.json";
		createFileAndGetDetails(jsonFile);
		//overwrite data with an empty json object
		//comment this line if you want to load a previous json object
		writeToFile(jsonFile, "{}");
		try {
			//parse our file contents (see what happens if we have an empty string in
			//the previous writeToFile function call
			//if no issues this returns our JSONObject as a map of key/values
			JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
			//start adding key value pairs
			jo.put("name", "John");
			jo.put("age", 55);
			//this shows how we can add a sub set of key value pairs under a single key
			Map<String, String> map = new LinkedHashMap<String, String>(4);//complex details for one key
			map.put("address", "123 Fake Street");
			map.put("city", "Nowhere");
			map.put("state", "Bliss");
			map.put("zip", "01010");
			//we're adding the map under a single key here
			jo.put("fulladdress", map);
			//json object is ready to save; write it to the file
			writeToFile(jsonFile, jo.toJSONString());
			//use this to view the file contents in the console
			//or just navigate to the folder and open the appropriate file
			readFromFile(jsonFile);
			System.out.println("Done runnning JSON Sample");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		FileSystemSampleJSON fss = new FileSystemSampleJSON();
		fss.doJSONSample();
	}
}
//TODO TBD
class MyData{
	public String name;
	public int health;
	public int x;
	public int y;
}