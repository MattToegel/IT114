import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Samples2 {
	//https://www.w3schools.com/java/
	public void one() {
		System.out.println("One");
		try {
		int[] ints = new int[1];
			for(int i = 0; i < 10; i++) {
				ints[i] = i;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void two() {
		System.out.println("Two");
		try {
			int[] ints = new int[1];
				for(int i = 0; i < 10; i++) {
					ints = Arrays.copyOf(ints, ints.length+1);
					ints[i] = i;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	}
	public void three() {
		System.out.println("Three");
		List<Integer> ints = new ArrayList<Integer>();
		for(int i = 0; i < 10; i++) {
			ints.add(i);
			ints.set(i, i);
		}
		System.out.println(ints);
	}
	public void four() {
		System.out.println("Four");
		for(int i = 0; i < 10; i++) {
			if(i % 2 == 0) {
				System.out.println(i + " is Even");
			}
			else {
				System.out.println(i + " is Odd");
			}
		}
	}
	public void five() {
		System.out.println("Five");
		float x = 0;
		for(int i = 0; i < 5; i++) {
			x++;
			System.out.println("Increment is " + x);
			if(i % 3 == 0) {
				x++;
				System.out.println("i % 3 is " + x);
			}
			if(i % 2 == 1) {
				x--;
				System.out.println("i % 2 is " + x);
			}
			System.out.println("End loop");
		}
		System.out.println(x);
	}
	
	public void six() {
		System.out.println("Six");
		FileWriter fw = null;
		
		try {
			fw = new FileWriter("myfile.txt");
			fw.append("hello");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				fw.close();
			} catch (IOException | NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void seven() {
		System.out.println("Seven");
		int x = 10;
		for(int i = 0; i < 10; i++) {
		
		}
		x *= 10;
		System.out.println(x);
	}
	public void eight() {
		System.out.println("Eight");
		double dbl = 3;
		String msg = Double.toString(dbl);
		System.out.println(msg);
	}
	public void nine() {
		System.out.println("Nines");
		Whatever we = new Whatever();
		System.out.println(we);
	}
	public void ten() {
		System.out.println("Ten");
		Wherever we = new Wherever();
		System.out.println(we);
	}
	public void eleven() {
		short sh = Short.MIN_VALUE;
		sh--;
		System.out.println(sh + " = " + Short.MIN_VALUE);
		
	}
	public static void main(String[] args) {
		Samples2 samples = new Samples2();
		samples.one();
		samples.two();
		samples.three();
		samples.four();
		samples.five();
		samples.six();
		samples.seven();
		samples.eight();
		samples.nine();
		samples.ten();
		samples.eleven();
	}
}
class Whatever{
	protected String str = "hello there";
	
	@Override
	public String toString() {
		return str;
	}
}
class Wherever extends Whatever{
	public Wherever() {
		str = "hi";
	}
}