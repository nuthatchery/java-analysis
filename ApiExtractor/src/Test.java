import java.util.function.Supplier;

public class Test {
	private Test x;

	public void foo() {
		String s = new Supplier<String>() {

			@Override
			public String get() {
				return "foo";
			}
		}.get();
		foo();
		s = s.substring(1, 2);
		Test o = this;
		o = o.x;
		do {
			o = o.x;
		} while (o != null);
	}

	class Bar {

	}
}
