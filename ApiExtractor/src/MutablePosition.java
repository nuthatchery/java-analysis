
public class MutablePosition implements IMutablePosition {
	int x;
	int y;

	public MutablePosition(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}


	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}


	@Override
	public void setX(int x) {
		MutablePosition[] foo = new MutablePosition[5];
		if (x < 3 || foo == null) {
			this.x = foo[x].x;
		}
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

}
