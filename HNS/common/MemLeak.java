package HNS.common;

import java.util.ArrayList;
import java.util.List;

public class MemLeak {
    static List<Integer> stuff = new ArrayList<Integer>();
    private static boolean isRunning = true;

    public static void main(String[] args) {
        TimedEvent t = new TimedEvent(300, () -> {
            isRunning = false;
        });
        t.setTickCallback((time) -> {
            for (int i = 0; i < 10000000; i++) {
                stuff.add(i);
            }
            System.out.println(stuff.size());
        });
        while (isRunning) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
