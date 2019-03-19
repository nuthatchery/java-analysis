
public class Exceptions {

	public static void main(String[] args) {
		try {
			System.out.println(1 / 0);
		} catch (ArithmeticException e) {
			System.out.println("arithmetic!");
		} catch (Throwable e) {
			System.out.println("throwable!");
		} finally {
			System.out.println("finally!");
		}
	}
}
