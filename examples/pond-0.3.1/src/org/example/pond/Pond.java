package org.example.pond;
import org.example.duck.Duck;

public class Pond {
	private Duck duck = new Duck();

	public void run() {
		System.out.println(duck.quack());
	}

	public static void main(String[] args) {
		Pond pond = new Pond();

		pond.run();
	}
}

