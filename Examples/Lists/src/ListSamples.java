
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSamples {
	List<String> myStrings = new ArrayList<String>();
	public ListSamples() {
		myStrings.add("b");
		myStrings.add("c");
		myStrings.add("a");
		myStrings.add("d");
	}
	/***
	 * private helper for cloning the list so the original order from the
	 * constructor persists no matter what methods are ran during the ample
	***/
	List<String> clone(List<String> strs){
		List<String> newList = new ArrayList<String>();
		for(int i = 0; i < strs.size(); i++) {
			newList.add(strs.get(i));
		}
		return newList;
	}
	/***
	 * Helper to dump the list to console
	 * @param list
	 */
	public static void showMyList(List<String> list) {
		for(int i = 0; i < list.size(); i++) {
			System.out.println("[" + i + "] => " + list.get(i));
		}
	}
	public void ReverseCollection() {
		System.out.println("Note this should be the colleciton literally in reverse order");
		List<String> reverse = clone(myStrings);
		Collections.reverse(reverse);
		showMyList(reverse);
	}
	public void ReverseSortCollection() {
		System.out.println("Uses natural sort");
		List<String> natural = clone(myStrings);
		Collections.sort(natural, Collections.reverseOrder());
		showMyList(natural);
	}
	public void SimpleShuffle() {
		List<String> shuffle = clone(myStrings);
		Collections.shuffle(shuffle);
		showMyList(shuffle);
	}
	public void KnuthShuffle() {
		List<String> knuth = clone(myStrings);
		int n = knuth.size();
        for (int i = 0; i < n; i++) {
            // choose index uniformly in [0, i]
            int r = (int) (Math.random() * (i + 1));
            Object swap = knuth.get(r);
            knuth.set(r, knuth.get(i));
            knuth.set(i, (String)swap);
        }
	    showMyList(knuth);
	}
	public void AddValueToListAndTotal() {
		//Use the index as the value for each slot in a 10 element list
		List<Integer> ints = new ArrayList<Integer>();
		for(int i = 0; i < 10; i++) {
			ints.add(i);
		}
		//total it
		int total = 0;
		for(int i = 0; i < ints.size();i++) {
			total += ints.get(i);
		}
		System.out.println("My total is " + total);
	}
	public void EvenOrOdd() {
		List<Integer> ints = new ArrayList<Integer>();
		for(int i = 0; i < 10; i++) {
			ints.add(i);
		}
		for(int i = 0; i < ints.size(); i++) {
			int temp = ints.get(i);
			//shows the index, its value, and whether it's odd or even
			// (test?true:false) is shorthand ternary operator, similar to if(test){true;} else { false;}
			System.out.println("[" + i + "] => " + temp + " is " + (temp%2==0?"even":"odd"));
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ListSamples test = new ListSamples();
		//Problem 1/2 (list creation is covered in Samples constructor
		//incorrect assumption
		test.ReverseCollection();
		//correct
		test.ReverseSortCollection();
		//Problem 3
		test.SimpleShuffle();
		//problem 4.1
		test.AddValueToListAndTotal();
		//problem 4.2
		test.EvenOrOdd();
		//problem 5
		test.KnuthShuffle();
		
	}

}
