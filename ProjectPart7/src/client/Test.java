package client;

public class Test {
    Thing myThing = null;

    public static void main(String[] t) {
	Test test = new Test();
	Thing t1 = new Thing();
	test.setMyThing(t1);
	System.out.println("t1" + t1);
	System.out.println(test.takeThing());
	System.out.println(test.takeThing());
	System.out.println("t1(again) " + t1);
	Thing t2 = new Thing();
	test.setMyThing(t2);
	System.out.println("t2" + t2);
	System.out.println(test.takeThing());
	System.out.println(test.takeThing());
    }

    public void setMyThing(Thing inc) {
	myThing = inc;
    }

    public Thing takeThing() {
	Thing t = myThing;
	myThing = null;
	return t;
    }
}

class Thing {
    public String name = "test";

}