
public class Test {
	private Test x;

	public void foo() {
		String s = "foo";
		foo();
		s = s.substring(1, 2);
		Test o = this;
		o = o.x;
		do {
			o = o.x;
		}
		while(o != null);
	}
}
