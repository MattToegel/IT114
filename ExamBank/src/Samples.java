import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Samples {
	public void intOverflow() {
		int i = Integer.MAX_VALUE;
		i++;
		if(i > 0) {
			System.out.println("I is greater than zero");
		}
		else if (i < 0) {
			System.out.println("I is less than zero");
		}
		else {
			System.out.println("I is equal to zero");
		}
	}
	public void floatMath() {
		float num1 = 0.0f;
		float answer = 1.0f;
		for(int i = 0; i < 10; i++) {
			num1 += 0.1f;
		}
		if(num1 == answer) {
			System.out.println("Num1 equals 1.0");
		}
		else {
			System.out.println("Num1 doesn't equal 1f");
		}
	}
	public void defineList() {
		/*List<int> ints = new ArrayList<int>();
		List<Integer> ints = new ArrayList<Integer>();
		List<Number> ints = new ArrayList<Number>();
		List<Int> ints = new ArrayList<Int>();*/
	}
	public void arrayCheck() {
		try {
			int[] numbers = new int[0];
			numbers[0] = 1;
			numbers[1] = 2;
			System.out.println(numbers.length);
		}
		catch(Exception e) {
			System.out.println("An error occured");
		}
	}
	public void casting() {
		String str = 1 + "";
		/*String str = (String)1;
		String str = new String(1);
		String str = 1 as String;*/
		String str2 = Integer.toString(1);
	}
	public void loopy() {
		List<Integer> ints = new ArrayList<Integer>();
		for(int i = 0; i < 10; i++) {
			ints.add(i);
		}
		System.out.println(ints.size());
	}
	public void counting() {
		int total = 0;
		for(int i = 0; i < 5; i++) {
			total += i;
		}
		System.out.println(total);
	}
	public void shorthand() {
		String test = "hi";
		//Sample 1
		if("hi" == test) {
			System.out.println("They said hi");
		}
		else {
			System.out.println("The didn't say hi");
		}
		//Sample 2
		System.out.println(("hi"==test?"They said hi":"They didn't say hi"));
		
	}
	public void doWhile() {
		int value = 0;
		do {
			value++;
		}while(false);
		System.out.println(value);
	}
	public void whileLoop() {
		int value = 0;
		boolean flag = false;
		while(flag == true) {
			value++;
		}
		System.out.println(value);
	}
	public void errors() {
		/*1*/ //int value = 0;
		/*2*/ //value +++;
		/*3*/ //value += 1.0;
		/*4*/ //for(int i = 0; i < 10; i++) {
		/*5*/	 //value += i
		/*6*/ 
		/*7*/ //sysout(value);
	}
	public void blocking() throws IOException {
		/*1*/ServerSocket serverSocket = new ServerSocket(3000);
		/*2*/Socket socket = serverSocket.accept();
		
		/*3*/Scanner s = new Scanner(System.in);
		/*4*/s.nextLine();
		/*5*/BufferedReader in = new BufferedReader(
		/*6*/		new InputStreamReader(socket.getInputStream()));
		/*7*/String line = in.readLine();
	}
	public void communication() throws IOException, ClassNotFoundException {
		//Server
		ServerSocket serverSocket = new ServerSocket(3000);
		Socket client = serverSocket.accept();
		ObjectOutputStream server_out = new ObjectOutputStream(client.getOutputStream());
		ObjectInputStream server_in = new ObjectInputStream(client.getInputStream());
		//Client
		Socket server = new Socket("[server IP]", 3000);
		ObjectOutputStream client_out = new ObjectOutputStream(server.getOutputStream());
		ObjectInputStream client_in = new ObjectInputStream(server.getInputStream());
		//samples
		server_out.writeObject("hi");
		String str = (String)server_in.readObject();
		client_out.writeObject("hi");
		String str2 = (String)client_in.readObject();
	}
	public void brokenBind() throws IOException {
		ServerSocket serverSocket = new ServerSocket(3000);
		ServerSocket serverSocket2 = new ServerSocket(3000);
	}
	public void bindEmAll() {
		for(int i = 0; i < 65000; i++) {
			try {
				ServerSocket serverSocket = new ServerSocket(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void math() {
		System.out.println("Math");
		int x = 0;
		for(int i = 0; i < 1001; i++) {
			x++;
			if(x == 2) {
				x--;
			}
		}
		System.out.println(x);
	}
	public void mod() {
		int x = 0;
		for(int i = 0; i < 1002; i++) {
			x++;
			if(i % 2 == 0) {
				x--;
			}
		}
		System.out.println(x);
	}
	public void inc() {
		int x = 0;
		x++;
		x = 0;
		x+=1;
		x = 0;
		x = x + 1;
		x = 0;
		++x;
	}
	public void tc() {
		//Block 1
		try(FileWriter fw = new FileWriter("answers.txt")){
			
		}
		catch(IOException e) {}
		
		//Block 2
		FileWriter fw = null;
		try {
			fw = new FileWriter("answers.txt");
		}
		catch(IOException e) {}
		finally {
			try {
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		Samples samples = new Samples();
		samples.intOverflow();
		samples.floatMath();
		samples.arrayCheck();
		samples.loopy();
		samples.counting();
		samples.shorthand();
		samples.doWhile();
		samples.whileLoop();
		/*try {
			samples.brokenBind();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		samples.bindEmAll();*/
		samples.math();
		samples.mod();
	}
}
