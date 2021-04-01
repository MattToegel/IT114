package com.example.app;

public class Inheritance {
	public static void main(String[] args) {
		FourLeggedAnimal a = new FourLeggedAnimal();
		System.out.println("Animal has " + a.getNumberOfLegs() + " legs");
		a.speak();
		Dog d = new Dog("Max");
		d.speak();
		System.out.println("Dog has " + d.getNumberOfLegs() + " legs");
		Cat c = new Cat("Mittens");
		c.speak();
		System.out.println("Cat has " + c.getNumberOfLegs() + " legs");
		Duck duck = new Duck("Quackers");
		System.out.println("Duck has " + duck.getNumberOfLegs() + " legs");
		duck.speak();
	}

}

abstract class Animal {
	protected int numberOfLegs = 0;
	protected String name = "";

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

class TwoLeggedAnimal extends Animal {
	public TwoLeggedAnimal() {
		this.numberOfLegs = 2;
	}

	@Override
	public void speak() {
		// TODO Auto-generated method stub
		System.out.println("I don't know what type of animal I am");

	}
}

class Duck extends TwoLeggedAnimal {
	public Duck(String name) {
		this.name = name;
	}

	@Override
	public void speak() {
		System.out.println("Quack?");
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