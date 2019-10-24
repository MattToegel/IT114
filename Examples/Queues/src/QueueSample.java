
import java.util.Queue;
import java.util.LinkedList;

public class QueueSample {
	public static void main(String[] args) {
		Queue<CustomKeyValuePair> queue = new LinkedList<CustomKeyValuePair>();
		for(int i = 0; i < 10; i++) {
			queue.add(new CustomKeyValuePair(i, "A Value"));
		}
		
		System.out.println("Show queue: " + queue);
		
		CustomKeyValuePair first = queue.remove();
		System.out.println("Pulled first: " + first);
		System.out.println("Show altered queue: " + queue);
		
		CustomKeyValuePair peek = queue.peek();
		System.out.println("Just viewing: " + peek);
		System.out.println("Show unaltered queue: " + queue);
		
		
	}
}
class CustomKeyValuePair{
	public int key;
	public String value;
	public CustomKeyValuePair(int k, String v) {
		this.key = k;
		this.value = v;
	}
	@Override
	public String toString() {
		return "{'key':'" + this.key + "', 'value':'" + this.value + "'}";
	}
}