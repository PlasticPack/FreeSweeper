package nick.sweeper.main;

import java.awt.Color;
import java.awt.Graphics;

public final class Tile {

	public enum Type {
		NUMBER, MINE, EMPTY, UNSET
	}

	private final Grid	grid;

	private final int	x, y;

	private Type		type				= Type.UNSET;

	private boolean		hidden				= true, flagged = false;

	private byte		numMineNeighbors	= 0;

	public Tile(final Grid g, final int xPos, final int yPos, final boolean isMine) {

		grid = g;
		x = xPos;
		y = yPos;
		if (isMine) {
			type = Type.MINE;
		}

	}

	public void determineType( ) {

		if (type != Type.UNSET) return;

		final Tile[ ] neighbors = grid.neighbors(x, y);

		for (final Tile n : neighbors) {

			if (n == null) {} else if (n.getType( ) == Type.MINE) {
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

	public int getX( ) {

		return x;
	}

	public int getY( ) {

		return y;
	}

	public boolean isFlagged( ) {

		return flagged;
	}

	public boolean isHidden( ) {

		return hidden;
	}

	public void render(final Graphics g, final int pX, final int pY, final short size) {

		if (hidden) {

			g.setColor(Color.DARK_GRAY);
			g.fillRect(pX, pY, size, size);

			if (isFlagged( )) {

				g.setColor(Color.RED);
				g.fillOval(pX, pY, size, size);
			}

		} else if (getType( ) == Tile.Type.EMPTY) {

			g.setColor(Color.GRAY);
			g.fillRect(pX, pY, size, size);
		} else if (getType( ) == Tile.Type.NUMBER) {

			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(pX, pY, size, size);

			g.setColor(Color.BLUE);
			String txt = getDisplayNum( );
			final int stringMidHigh = g.getFontMetrics( ).getHeight( ) / 2;
			final int stringMidWide = g.getFontMetrics( ).stringWidth(txt) / 2;
			final short squareMid = (short) (size / 2);
			g.drawString(txt, (pX - stringMidWide) + squareMid, pY + stringMidHigh + squareMid);
		}
	}

	public void reveal( ) {

		hidden = false;
	}

	@Override
	public String toString( ) {

		return "(" + x + ", " + y + ")";
	}

}
