
public class ImmutablePosition {
	int x;
	int y;

	public ImmutablePosition(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ImmutablePosition setX(int newX) {
		return new ImmutablePosition(newX, y);
	}

	public ImmutablePosition setY(int newY) {
		return new ImmutablePosition(x, newY);
	}

}
