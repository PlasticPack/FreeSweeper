package nick.sweeper.ai;

import nick.sweeper.main.Grid;
import nick.sweeper.main.Tile;

public class AILogic {

	private float[ ][ ]	probability;

	private final Grid	grid;

	private boolean		started	= false;

	public AILogic(final Grid g) {

		grid = g;
		probability = new float[g.sizeX( )][g.sizeY( )];

	}

	private byte hiddenIn(final Tile[ ] list) {

		byte hidden = 0;
		for (Tile t : list) {
			if (t == null) {} else if (t.isHidden( )) {
				hidden++;
			}
		}
		return hidden;
	}

	private byte minesVisible(final Tile[ ] list) {

		byte minesVisible = 0;
		for (Tile t : list) {
			if (t == null) {} else if (!t.isHidden( ) && (t.getType( ) == Tile.Type.MINE)) {
				minesVisible++;
			}
		}
		return minesVisible;
	}

	private void start( ) {

		System.out.println("Starting the AI routine...");

		final int rX = (int) (Math.random( ) * grid.sizeX( ));
		final int rY = (int) (Math.random( ) * grid.sizeY( ));

		grid.onClick(grid.tileAt(rX, rY), false);

		for (int x = 0; x < grid.sizeX( ); x++) {
			for (int y = 0; y < grid.sizeY( ); y++) {
				probability[x][y] = 0;
			}
		}

		if (grid.tileAt(rX, rY).getType( ) == Tile.Type.NUMBER) {
			start( );
		}

		started = true;
		System.out.println("Started the AI.");
	}

	public void update( ) {

		if (!started) {
			start( );
		}

		for (int x = 0; x < grid.sizeX( ); x++) {
			for (int y = 0; y < grid.sizeY( ); y++) {

				final Tile t = grid.tileAt(x, y);

				if (!t.isHidden( ) && (t.getType( ) == Tile.Type.NUMBER)) {

					final byte minesInNeighbors = Byte.parseByte(t.getDisplayNum( ));
					final Tile[ ] neighbors = grid.neighbors(x, y);
					final byte visibleMines = minesVisible(neighbors);

					final byte hiddenMines = (byte) (minesInNeighbors - visibleMines);
					final byte hiddenTiles = hiddenIn(neighbors);

					if (hiddenMines == hiddenTiles) {
						for (Tile n : neighbors) {
							if ((n != null) && !n.isFlagged( ) && n.isHidden( )) {
								grid.onClick(n, true);
							}
						}
					} else if (hiddenMines > hiddenTiles) {
						System.out.println("(" + x + ", " + y + ")| Hidden Mines: " + hiddenMines + " | Hidden Tiles: " + hiddenTiles);
					} else {
						for (Tile n : neighbors) {
							if (n != null) {
								probability[n.getX( )][n.getY( )] += ((float) hiddenMines) / hiddenTiles;
							}
						}
					}

				}
			}
		}
	}

}
