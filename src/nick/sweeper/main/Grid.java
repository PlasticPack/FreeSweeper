package nick.sweeper.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import nick.sweeper.main.Square.Type;

public class Grid {

	/**
	 * The minimum length of either side of the grid
	 */
	public static final byte	minDimension	= 5;

	public static final short	squareDrawSize	= 35;

	private final int			sizeX, sizeY, numMines;

	private final Square[ ][ ]	grid;

	private boolean				ready			= false;

	private int					flagsUsed		= 0;

	private int					xOff;

	private int					yOff;

	public Grid(final int xSize, final int ySize, final int mines) {

		if ((xSize < minDimension) || (ySize < minDimension)) {
			System.out.println("This grid may be too small");
		}

		sizeX = xSize;
		sizeY = ySize;
		numMines = mines;

		grid = new Square[sizeX][sizeY];
		initGrid( );
	}

	public void draw(final Graphics g) {

		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {

				Square s = getTileAt(x, y);
				int rendX = (int) (((x * squareDrawSize) + xOff) - (getGridRenderSize( ).getWidth( ) / 2));
				int rendY = (int) (((y * squareDrawSize) + yOff) - (getGridRenderSize( ).getHeight( ) / 2));

				if (s.isHidden( )) {

					g.setColor(Color.DARK_GRAY);
					g.fillRect(rendX, rendY, squareDrawSize, squareDrawSize);

					if (s.isFlagged( )) {

						g.setColor(Color.RED);
						g.fillOval(rendX, rendY, squareDrawSize, squareDrawSize);
					}

				} else if (s.getType( ) == Square.Type.EMPTY) {

					g.setColor(Color.GRAY);
					g.fillRect(rendX, rendY, squareDrawSize, squareDrawSize);
				} else if (s.getType( ) == Square.Type.NUMBER) {

					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(rendX, rendY, squareDrawSize, squareDrawSize);

					g.setColor(Color.BLUE);
					String txt = s.getDisplayNum( );
					final int stringMidHigh = g.getFontMetrics( ).getHeight( ) / 2;
					final int stringMidWide = g.getFontMetrics( ).stringWidth(txt) / 2;
					final short squareMid = squareDrawSize / 2;
					g.drawString(txt, (rendX - stringMidWide) + squareMid, rendY + stringMidHigh + squareMid);
				}

				// Outline for each square
				g.setColor(Color.BLACK);
				g.drawRect(rendX, rendY, squareDrawSize, squareDrawSize);
			}
		}
	}

	public int getFlagsUsed( ) {

		return flagsUsed;
	}

	public Dimension getGridRenderSize( ) {

		return new Dimension(sizeX * squareDrawSize, sizeY * squareDrawSize);
	}

	public Square[ ] getNeighbors(final int x, final int y) {

		final Square[ ] toRet = new Square[8];

		toRet[0] = getTileAt(x - 1, y - 1);
		toRet[1] = getTileAt(x, y - 1);
		toRet[2] = getTileAt(x + 1, y - 1);
		toRet[3] = getTileAt(x - 1, y);
		toRet[4] = getTileAt(x + 1, y);
		toRet[5] = getTileAt(x - 1, y + 1);
		toRet[6] = getTileAt(x, y + 1);
		toRet[7] = getTileAt(x + 1, y + 1);

		return toRet;
	}

	public int getNumMines( ) {

		return numMines;
	}

	public float getPercentofMines( ) {

		return (float) (numMines) / getTotalSquares( );
	}

	public int getSizeX( ) {

		return sizeX;
	}

	public int getSizeY( ) {

		return sizeY;
	}

	public Square getTileAt(final int x, final int y) {

		if ((x < 0) || (y < 0) || (x >= sizeX) || (y >= sizeY)) return null;

		return grid[x][y];
	}

	public int getTotalSquares( ) {

		return sizeX * sizeY;
	}

	private void initGrid( ) {

		int minesToPlace = numMines;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				int spotsLeft = (getTotalSquares( ) - y) - (x * sizeY);

				final double chanceOfMine = (float) (minesToPlace) / spotsLeft;
				if (Math.random( ) <= chanceOfMine) {
					grid[x][y] = new Square(this, x, y, true);
					minesToPlace--;
				} else {
					grid[x][y] = new Square(this, x, y, false);
				}

			}
		}

		if (minesToPlace <= 0) {
			System.out.println("All mines placed!");
		}

		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				getTileAt(x, y).determineType( );

			}
		}

		ready = true;
	}

	public int numUncovered( ) {

		int uncovered = 0;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!getTileAt(x, y).isHidden( )) {
					++uncovered;
				}
			}
		}
		return uncovered;
	}

	public void onClick(final int pX, final int pY, final boolean flag) {

		if (!ready) return;

		final int midX = (sizeX * squareDrawSize) / 2;
		final int midY = (sizeY * squareDrawSize) / 2;
		final int tX = ((pX + midX) - xOff) / squareDrawSize;
		final int tY = ((pY + midY) - yOff) / squareDrawSize;

		final Square s = getTileAt(tX, tY);

		if (s.isHidden( )) {
			if (flag) {
				s.flag( );

				if (s.isFlagged( )) {
					++flagsUsed;
				} else {
					--flagsUsed;
				}

				return;
			}

			if (s.isFlagged( )) return;

			s.reveal( );
			if (s.getType( ) == Type.MINE) {
				MineSweeper.stop(true);
			}
		}

		if (s.getType( ) == Type.EMPTY) {
			Square[ ] neighbors = getNeighbors(tX, tY);
			for (Square n : neighbors) {
				if (n == null) {} else if (n.getType( ) != Type.MINE) {
					n.reveal( );
				}
			}

		}
	}

	public float percentComplete( ) {

		return ((numUncovered( ) + flagsUsed) * 100.0f) / getTotalSquares( );
	}

	public void setOffsets(final int x, final int y) {

		xOff = x;
		yOff = y;
	}

	public void update( ) {

	}

}
