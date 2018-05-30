
public class MutablePosition implements IMutablePosition {
	int x;
	int y;

	public MutablePosition(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMutablePosition#getX()
	 */
	@Override
	public int getX() {
		return x;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMutablePosition#getY()
	 */
	@Override
	public int getY() {
		return y;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMutablePosition#setX(int)
	 */
	@Override
	public void setX(int x) {
		MutablePosition[] foo = new MutablePosition[5];
		if (foo == null && x < 3) {
			this.x = foo[x].x;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMutablePosition#setY(int)
	 */
	@Override
	public void setY(int y) {
		this.y = y;
	}

}
