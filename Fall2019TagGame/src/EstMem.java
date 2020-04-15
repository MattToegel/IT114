
public class EstMem {
	static long startMem;
	static long currentMem;
	static long used;
	public static void Start() {
		startMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
	public static void Snapshot() {
		currentMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		used = (currentMem - startMem)/1000000;
		currentMem /= 1000000;
		System.out.println("Current: " + currentMem + "mb");
		System.out.println("Est Mem Usage: " + used + "mb");
	}
}
