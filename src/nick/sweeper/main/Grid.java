package nick.sweeper.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import nick.sweeper.main.Tile.Type;

public class Grid {

	/**
	 * The minimum length of either side of the grid
	 */
	public static final byte	minDimension	= 5;

	/**
	 * The pixel height of each square
	 */
	public static final short	squareDrawSize	= 40;

	public static final boolean	drawHighlight	= true;

	private final int			sizeX, sizeY, numMines;

	private final Tile[ ][ ]	grid;

	private boolean				ready			= false;

	private int					flagsUsed		= 0;

	private int					xOff, yOff;

	private boolean				completed		= false;

	private boolean				hitMine			= false;

	private final MineSweeper	game;

	private Tile				highlight;

	public Grid(final int xSize, final int ySize, final int mines, final MineSweeper gameObj) {

		if ((xSize < minDimension) || (ySize < minDimension)) {
			System.out.println("This grid may be too small");
		}

		sizeX = xSize;
		sizeY = ySize;
		numMines = mines;
		game = gameObj;

		grid = new Tile[sizeX][sizeY];
		initGrid( );
	}

	/**
	 * Recursive
	 */
	private final void clearEmptyNeighbors(final Tile t) {

		if (t.getType( ) == Tile.Type.EMPTY) {
			final Tile[ ] neighbors = neighbors(t.getX( ), t.getY( ));
			for (final Tile n : neighbors) {
				if (n == null) {} else if ((n.getType( ) != Type.MINE) && n.isHidden( )) {
					n.reveal( );
					clearEmptyNeighbors(n);
				}
			}
		}
	}

	public void draw(final Graphics g) {

		drawTileGrid(g);

		if (highlight != null) {
			// Tile X and Y
			final int tX = highlight.getX( ) - 1;
			final int tY = highlight.getY( ) - 1;
			// Render X and Y
			final int rX = ((tX * squareDrawSize) + xOff) - rendMidX( );
			final int rY = ((tY * squareDrawSize) + yOff) - rendMidY( );

			g.setColor(Color.ORANGE);

			g.drawRect(rX, rY, squareDrawSize * 3, squareDrawSize * 3);
		}

		if (completed) {
			g.setFont(new Font("Courier New", Font.BOLD, squareDrawSize));
			final String win = "Congratulations!";
			final int stringMidWidth = g.getFontMetrics( ).stringWidth(win) / 2;

			final int rX = (game.renderWidth( ) / 2) - stringMidWidth;
			final int rY = (game.renderHeight( ) - g.getFontMetrics( ).getHeight( )) / 2;

			g.setColor(Color.WHITE);
			g.fillRect(rX, (rY - g.getFontMetrics( ).getHeight( )) + 10, g.getFontMetrics( ).stringWidth(win), g.getFontMetrics( ).getHeight( ));

			g.setColor(Color.GREEN);
			g.drawString(win, rX, rY);
		} else if (hitMine) {
			g.setFont(new Font("Courier New", Font.BOLD, squareDrawSize));
			final String lose = "Hit a Mine!";
			final int stringMidWidth = g.getFontMetrics( ).stringWidth(lose) / 2;

			final int rX = (game.renderWidth( ) / 2) - stringMidWidth;
			final int rY = (game.renderHeight( ) - g.getFontMetrics( ).getHeight( )) / 2;

			g.setColor(Color.WHITE);
			g.fillRect(rX, (rY - g.getFontMetrics( ).getHeight( )) + 10, g.getFontMetrics( ).stringWidth(lose), g.getFontMetrics( ).getHeight( ));

			g.setColor(Color.RED);
			g.drawString(lose, rX, rY);
		}
	}

	private final void drawTileGrid(final Graphics g) {

		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {

				final Tile s = tileAt(x, y);
				final int rendX = ((x * squareDrawSize) + xOff) - rendMidX( );
				final int rendY = ((y * squareDrawSize) + yOff) - rendMidY( );

				s.render(g, rendX, rendY, squareDrawSize);

				// Outline for each square
				g.setColor(Color.BLACK);
				g.drawRect(rendX, rendY, squareDrawSize, squareDrawSize);

			}
		}
	}

	public int flagsUsed( ) {

		return flagsUsed;
	}

	private void initGrid( ) {

		int minesToPlace = numMines;
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				final int spotsLeft = (totalSquares( ) - y) - (x * sizeY);

				final double chanceOfMine = (double) (minesToPlace) / spotsLeft;
				if (Math.random( ) <= chanceOfMine) {
					grid[x][y] = new Tile(this, x, y, true);
					minesToPlace--;
				} else {
					grid[x][y] = new Tile(this, x, y, false);
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

	/**
	 * Will have null values if the center tile is on an edge
	 *
	 * @param x
	 *            The x location of the center tile
	 * @param y
	 *            The y location of the center tile
	 * @return An 8-long array of the tiles touching the center tile
	 */
	public Tile[ ] neighbors(final int x, final int y) {

		final Tile[ ] toRet = new Tile[8];

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

		final int tX = ((pX + rendMidX( )) - xOff) / squareDrawSize;
		final int tY = ((pY + rendMidY( )) - yOff) / squareDrawSize;

		final Tile s = tileAt(tX, tY);
		onClick(s, flag);
	}

	public void onClick(final Tile s, final boolean flag) {

		System.out.println("Click on (" + s.getX( ) + ", " + s.getY( ) + "). Type: " + s.getType( ));

		if (s.isHidden( )) {
			if (flag) {
				s.flag( );

				if (s.isFlagged( )) {
					++flagsUsed;
				} else {
					--flagsUsed;
				}

			} else {

				if (!s.isFlagged( )) {

					if (s.getType( ) == Type.MINE) {
						hitMine = true;
					}
					clearEmptyNeighbors(s);
					s.reveal( );
				}
			}

		}

		clearEmptyNeighbors(s);
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

	/**
	 * @return The center X of the grid in pixels
	 */
	private int rendMidX( ) {

		return (sizeX * squareDrawSize) / 2;
	}

	/**
	 * @return The center Y of the grid in pixels
	 */
	private int rendMidY( ) {

		return (sizeY * squareDrawSize) / 2;
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

	/**
	 * Will return null if given a location outside of the defined grid
	 *
	 * @param x
	 *            The x location of the tile
	 * @param y
	 *            The y location of the tile
	 * @return The tile object at the location given
	 */
	public Tile tileAt(final int x, final int y) {

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
