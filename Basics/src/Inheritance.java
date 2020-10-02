public class Inheritance {
	public static void main(String[] args) {
		FourLeggedAnimal a = new FourLeggedAnimal();
		System.out.println("Animal has " + a.getNumberOfLegs() + " legs");
		a.speak();
		Dog d = new Dog();
		d.speak();
		System.out.println("Dog has " + d.getNumberOfLegs() + " legs");
		Cat c = new Cat();
		c.speak();
		System.out.println("Cat has " + d.getNumberOfLegs() + " legs");
	}

}

abstract class Animal {
	protected int numberOfLegs = 0;

	public int getNumberOfLegs() {
		return numberOfLegs;
	}

	abstract public void speak();
}

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
	@Override
	public void speak() {
		System.out.println("Bark!");
	}
}

class Cat extends FourLeggedAnimal {
	@Override
	public void speak() {
		System.out.println("Meow");
	}
}