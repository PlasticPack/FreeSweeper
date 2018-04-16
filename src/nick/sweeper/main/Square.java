package nick.sweeper.main;

public final class Square {

	public enum Type {
		NUMBER, MINE, EMPTY, UNSET
	}

	private final Grid	grid;

	private final int	x, y;

	private Type		type				= Type.UNSET;

	private boolean		hidden				= true, flagged = false;

	private int			numMineNeighbors	= 0;

	public Square(final Grid g, final int xPos, final int yPos, final boolean isMine) {

		grid = g;
		x = xPos;
		y = yPos;
		if (isMine) {
			type = Type.MINE;
		}

	}

	public void determineType( ) {

		if (type != Type.UNSET) return;

		final Square[ ] neighbors = grid.getNeighbors(x, y);

		for (Square n : neighbors) {

			if (n == null) {} else if ((n.getType( ) == Type.MINE)) {
				numMineNeighbors++;
			}
		}

		if (numMineNeighbors > 0) {
			type = Type.NUMBER;
		} else {
			type = Type.EMPTY;
		}

	}

	public void flag( ) {

		if (hidden) {
			flagged = !flagged;
		}
	}

	public String getDisplayNum( ) {

		return String.valueOf(numMineNeighbors);
	}

	public Type getType( ) {

		if (type == Type.UNSET) {
			// System.out.println("UNSET type here");
		}

		return type;
	}

	public boolean isFlagged( ) {

		return flagged;
	}

	public boolean isHidden( ) {

		return hidden;
	}

	public void reveal( ) {

		hidden = false;
	}

}
