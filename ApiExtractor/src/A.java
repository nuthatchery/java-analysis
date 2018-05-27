
import main.Calleable;
import main.Extendable;

public class A {
	public void m(){
		new Object().toString();
		new Calleable().print();
		Calleable.call();
		class B extends Extendable{

			@Override
			public void method() {
				System.out.println("method@extendableB");
			}
		}
		new B().method();
		new B().call();
	}
}
