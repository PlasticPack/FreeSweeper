package nick.sweeper.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

	private boolean				completed		= false;

	private boolean				hitMine			= false;

	private final MineSweeper	game;

	private Square				highlight;

	public Grid(final int xSize, final int ySize, final int mines, final MineSweeper gameObj) {

		if ((xSize < minDimension) || (ySize < minDimension)) {
			System.out.println("This grid may be too small");
		}

		sizeX = xSize;
		sizeY = ySize;
		numMines = mines;
		game = gameObj;

		grid = new Square[sizeX][sizeY];
		initGrid( );
	}

	public void draw(final Graphics g) {

		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {

				Square s = tileAt(x, y);
				int rendX = (int) (((x * squareDrawSize) + xOff) - (renderSize( ).getWidth( ) / 2));
				int rendY = (int) (((y * squareDrawSize) + yOff) - (renderSize( ).getHeight( ) / 2));

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

		if (highlight != null) {
			final int tX = highlight.getX( ) - 1;
			final int tY = highlight.getY( ) - 1;

			final int rX = (int) (((tX * squareDrawSize) + xOff) - (renderSize( ).getWidth( ) / 2));
			final int rY = (int) (((tY * squareDrawSize) + yOff) - (renderSize( ).getHeight( ) / 2));

			g.setColor(Color.ORANGE);

			g.drawRect(rX, rY, squareDrawSize * 3, squareDrawSize * 3);
		}

		if (completed) {
			g.setFont(new Font("Courier New", Font.BOLD, 25));
			g.setColor(Color.GREEN);
			final String win = "Congratulations!";
			final int stringMidWidth = g.getFontMetrics( ).stringWidth(win) / 2;
			g.drawString(win, (game.renderWidth( ) / 2) - stringMidWidth, (game.renderHeight( ) / 2) - (g.getFontMetrics( ).getHeight( ) / 2));
		}
		if (hitMine) {
			g.setFont(new Font("Courier New", Font.BOLD, 25));
			g.setColor(Color.RED);
			final String lose = "Hit a Mine!";
			final int stringMidWidth = g.getFontMetrics( ).stringWidth(lose) / 2;
			g.drawString(lose, (game.renderWidth( ) / 2) - stringMidWidth, (game.renderHeight( ) / 2) - (g.getFontMetrics( ).getHeight( ) / 2));
		}
	}

	public int flagsUsed( ) {

		return flagsUsed;
	}

	private void initGrid( ) {

		int minesToPlace = numMines;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				int spotsLeft = (totalSquares( ) - y) - (x * sizeY);

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
				tileAt(x, y).determineType( );
			}
		}

		ready = true;
	}

	public Square[ ] neighbors(final int x, final int y) {

		final Square[ ] toRet = new Square[8];

		toRet[0] = tileAt(x - 1, y - 1);
		toRet[1] = tileAt(x, y - 1);
		toRet[2] = tileAt(x + 1, y - 1);
		toRet[3] = tileAt(x - 1, y);
		toRet[4] = tileAt(x + 1, y);
		toRet[5] = tileAt(x - 1, y + 1);
		toRet[6] = tileAt(x, y + 1);
		toRet[7] = tileAt(x + 1, y + 1);

		return toRet;
	}

	public int numKnown( ) {

		int known = 0;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (!tileAt(x, y).isHidden( ) || tileAt(x, y).isFlagged( )) {
					++known;
				}
			}
		}
		return known;
	}

	public int numMines( ) {

		return numMines;
	}

	public void onClick(final int pX, final int pY, final boolean flag) {

		if (!ready) return;

		final int midX = (sizeX * squareDrawSize) / 2;
		final int midY = (sizeY * squareDrawSize) / 2;
		final int tX = ((pX + midX) - xOff) / squareDrawSize;
		final int tY = ((pY + midY) - yOff) / squareDrawSize;

		final Square s = tileAt(tX, tY);

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
				hitMine = true;
			}
		}

		if (s.getType( ) == Type.EMPTY) {
			Square[ ] neighbors = neighbors(tX, tY);
			for (Square n : neighbors) {
				if (n == null) {} else if (n.getType( ) != Type.MINE) {
					n.reveal( );
				}
			}

		}
	}

	public float percentComplete( ) {

		return (numKnown( ) * 100.0f) / totalSquares( );
	}

	/**
	 * @return The percentage of tiles that are mines.
	 */
	public float percentMines( ) {

		return (float) (numMines * 100) / totalSquares( );
	}

	public Dimension renderSize( ) {

		return new Dimension(sizeX * squareDrawSize, sizeY * squareDrawSize);
	}

	public void setOffsets(final int x, final int y) {

		xOff = x;
		yOff = y;
	}

	public int sizeX( ) {

		return sizeX;
	}

	public int sizeY( ) {

		return sizeY;
	}

	public Square tileAt(final int x, final int y) {

		if ((x < 0) || (y < 0) || (x >= sizeX) || (y >= sizeY)) return null;

		return grid[x][y];
	}

	public int totalSquares( ) {

		return sizeX * sizeY;
	}

	public void update( ) {

		if (Mouse.isMouseIn( )) {
			final int mX = Mouse.mouseX( );
			final int mY = Mouse.mouseY( );
			final int midX = (sizeX * squareDrawSize) / 2;
			final int midY = (sizeY * squareDrawSize) / 2;
			final int tX = ((mX + midX) - xOff) / squareDrawSize;
			final int tY = ((mY + midY) - yOff) / squareDrawSize;

			if (tileAt(tX, tY) != null) {
				highlight = tileAt(tX, tY);
			}
		}
		if (numKnown( ) == totalSquares( )) {
			completed = true;
		}

		if (hitMine) {
			game.stop(true);
		}

	}

}
