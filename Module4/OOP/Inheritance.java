package Module4.OOP;

public class Inheritance {
	public static void main(String[] args) {
        //Note: Normally we wouldn't instantiate this class and would mark it abstract
        //but in this example I wanted to demonstate the differences in the various classes
		FourLeggedAnimal a = new FourLeggedAnimal();
		System.out.println("Animal has " + a.getNumberOfLegs() + " legs");
		a.speak();
		Dog d = new Dog("Max");
		d.speak();
		System.out.println("Dog has " + d.getNumberOfLegs() + " legs");
		Cat c = new Cat("Mittens");
		c.speak();
		System.out.println("Cat has " + c.getNumberOfLegs() + " legs");
	}

}

abstract class Animal {
	protected int numberOfLegs = 0;
	protected String name = "";

	public int getNumberOfLegs() {
		return numberOfLegs;
	}
    //defines a contract for subclasses to implement
	abstract public void speak();
}

/*abstract** Commented out for sample*/
class FourLeggedAnimal extends Animal {
	public FourLeggedAnimal() {
		this.numberOfLegs = 4;
	}

	@Override
	public void speak() {
		System.out.println("I don't know what type of animal I am");
	}
}

class Dog extends FourLeggedAnimal {
	public Dog(String name) {
		this.name = name;
	}

	@Override
	public void speak() {
		System.out.println(name + ": Bark!");
	}
}

class Cat extends FourLeggedAnimal {
	public Cat(String name) {
		this.name = name;
	}

	@Override
	public void speak() {
		System.out.println(name + ": Meow");
	}
}
