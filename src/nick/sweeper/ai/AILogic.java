package nick.sweeper.ai;

import nick.sweeper.main.Grid;
import nick.sweeper.main.Tile;

public class AILogic extends Thread {

	private final Grid			grid;

	private boolean				started	= false;

	private boolean[ ][ ]		satisfied;

	private volatile boolean	running	= false;

	public AILogic(final Grid g) {

		grid = g;

		setName("AI");
		setPriority(MIN_PRIORITY + 1);
	}

	private void cooldown( ) {

		try {
			Thread.sleep(125);
		} catch (InterruptedException e) {
			System.out.println("Failed to sleep!");
			e.printStackTrace( );
		}

	}

	private byte flagsUsed(final Tile[ ] list) {

		byte flagged = 0;
		for (Tile t : list) {
			if (t == null) {} else if (t.isFlagged( )) {
				flagged++;
			}
		}
		return flagged;
	}

	public void halt( ) {

		running = false;
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

	public void init( ) {

		if (started) return;

		final int rX = (int) (Math.random( ) * grid.sizeX( )) / 2;
		final int rY = (int) (Math.random( ) * grid.sizeY( ));

		grid.onClick(grid.tileAt(rX, rY), false);

		if (grid.tileAt(rX, rY).getType( ) == Tile.Type.NUMBER) {
			init( );
		}

		satisfied = new boolean[grid.sizeX( )][grid.sizeY( )];

		started = true;
	}

	public boolean isRunning( ) {

		return running;
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

	private void process( ) {

		if (grid.percentComplete( ) >= 100) return;
		if (!started) {
			init( );
		}

		final long startTime = System.currentTimeMillis( );

		for (int x = 0; x < grid.sizeX( ); x++) {
			for (int y = 0; y < grid.sizeY( ); y++) {

				final Tile t = grid.tileAt(x, y);

				if (!t.isHidden( ) && (t.getType( ) == Tile.Type.NUMBER) && !satisfied[x][y]) {

					grid.setHighLight(t);

					final byte minesInNeighbors = Byte.parseByte(t.getDisplayNum( ));
					final Tile[ ] neighbors = grid.neighbors(x, y);
					final byte hiddenMines = (byte) (minesInNeighbors - minesVisible(neighbors));
					final byte hiddenTiles = hiddenIn(neighbors);

					if (flagsUsed(neighbors) == minesInNeighbors) {
						for (final Tile n : neighbors) {
							if ((n != null) && (!n.isFlagged( ) || !n.isHidden( ))) {
								grid.onClick(n, false);
								cooldown( );
							}
						}
						satisfied[x][y] = true;
					} else if (hiddenMines == hiddenTiles) {
						for (Tile n : neighbors) {
							if ((n != null) && !n.isFlagged( ) && n.isHidden( )) {
								grid.onClick(n, true);
								cooldown( );
							}
						}
						satisfied[x][y] = true;
					}
				}
			}
		}

		System.out.println("AI completed 1 scan in " + (System.currentTimeMillis( ) - startTime) + "ms");
	}

	@Override
	public void run( ) {

		running = true;
		while (running) {
			process( );
		}
	}

}
