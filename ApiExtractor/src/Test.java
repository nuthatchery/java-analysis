
public class Test {
	private Test x;
	
	public void foo() {
		Test o = this;
		o = o.x;
		do {
			o = o.x;
		}
		while(o != null);
	}
}
