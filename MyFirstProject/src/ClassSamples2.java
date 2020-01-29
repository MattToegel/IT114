import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClassSamples2{
	
	
	public static void main(String[] args) {
		if(args.length > 0) {
			System.out.println(args[0]);
		}
		
		Object[] objects = new Object[1];
		System.out.println(objects.length);
		
		objects[0] = "Test";
		
		List<String> myList = new ArrayList<String>();
		myList.add("test");
		System.out.println("My List has a size of " + myList.size());
		
		System.out.println("Intro collection/iterator");
		Collection<Integer> col = new ArrayList<Integer>();
		col.add(12);
		col.add(11);
		col.add(2);
		//don't do this
		/*while(col.iterator().hasNext()) {
			//infinite loop because the while loop
			//gets a new iterator each loop
			System.out.println(col.iterator().next());
		}*/
		//do this
		Iterator it = col.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		System.out.println("Intro for loop (again)");
		List<Integer> breakIt = new ArrayList<Integer>();
		for(int i = 0; i < 10; i++) {
			breakIt.add(i);
		}
		System.out.println("Intro do while");
		//don't run it as infinite (i.e., as true)
		do {
			System.out.println("Hi");
		}while(false);
		System.out.println("Intro for each loop");
		//called a foreach loop
		for(int i : breakIt) {
			//creates excess garbage
			System.out.println(i);
		}
		System.out.println("For each loop sample breakIt");
		//time to breakit
		for(int i : breakIt) {
			if(i == 5) {
				//breakIt.remove(i);
			}
		}
		System.out.println("For loop Sample breakIt");
		int size = breakIt.size();
		/*for(int i = 0; i < size; i++) {
			//System.out.println(breakIt.get(i));
			if(i == 2) {
				int v = breakIt.remove(i);
				System.out.println("Removed: " + v);
			}
			if(i == 3) {
				System.out.println("Here's the missing value: " + breakIt.get(i-1));
			}
		}*/
		System.out.println("Iterator Remove Sample");
		Iterator bIt = breakIt.iterator();
		int i = 0;
		while(bIt.hasNext()) {
			bIt.next();
			if(i == 2) {
				//removes the latest call to next()
				bIt.remove();
			}
			
			i++;//shorthand for  i += 1 or i = i + 1
		}
		System.out.println("Viewing bIt");
		bIt = breakIt.iterator();
		while(bIt.hasNext()) {
			System.out.println(bIt.next());
		}
		
		System.out.println("Queue is a Q");
		Queue<String> myQ = new LinkedList<String>();
		myQ.add("Hello");
		String peeked = myQ.peek();
		System.out.println("Peeked " + peeked);
		String polled = myQ.poll();
		System.out.println("Polled " + polled);
		
		System.out.println("Next Peek" + myQ.peek());
		
	}
}