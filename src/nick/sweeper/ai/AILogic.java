package nick.sweeper.ai;

import nick.sweeper.main.Grid;
import nick.sweeper.main.MineSweeper;
import nick.sweeper.main.Tile;

public class AILogic extends Thread {

	private static volatile boolean running = false;

	private static void cooldown( ) {

		try {
			Thread.sleep(125);
		} catch (InterruptedException e) {
			System.out.println("Failed to sleep!");
			e.printStackTrace( );
		}

	}

	public static void halt( ) {

		running = false;
	}

	public static boolean isRunning( ) {

		return running;
	}

	private final Grid		grid;

	private boolean			started			= false;

	private boolean[ ][ ]	satisfied;

	private long			totalRuntime	= 0;

	public AILogic(final Grid g) {

		grid = g;

		setName("AI");
		setPriority(MIN_PRIORITY + 1);
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

		Tile.Type clickedOn = Tile.Type.UNSET;
		while (clickedOn != Tile.Type.EMPTY) {
			final int rX = (int) (Math.random( ) * grid.sizeX( )) / 2;
			final int rY = (int) (Math.random( ) * grid.sizeY( )) / 2;

			grid.onClick(grid.tileAt(rX, rY), false);

			clickedOn = grid.tileAt(rX, rY).getType( );
		}

		satisfied = new boolean[grid.sizeX( )][grid.sizeY( )];

		started = true;

		System.out.println("AI initialized");
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

		if (grid.percentComplete( ) >= 100) {
			System.out.println("Total Runtime: " + totalRuntime + "ms (" + (totalRuntime / 1000) + "s)");
			running = false;
			return;
		} else if (!started) {
			init( );
		}

		final long startTime = System.currentTimeMillis( );

		for (int x = 0; x < grid.sizeX( ); x++) {
			for (int y = 0; y < grid.sizeY( ); y++) {

				final Tile t = grid.tileAt(x, y);

				if (t.isVisible( ) && (t.getType( ) == Tile.Type.NUMBER) && !satisfied[x][y]) {

					grid.setHighLight(t);

					final byte minesInNeighbors = Byte.parseByte(t.getDisplayNum( ));
					final Tile[ ] neighbors = grid.neighbors(x, y);
					final byte hiddenMines = (byte) (minesInNeighbors - minesVisible(neighbors));
					final byte hiddenTiles = hiddenIn(neighbors);
					final byte flagsUsed = flagsUsed(neighbors);

					if (MineSweeper.debug) {
						System.out.println("(" + x + ", " + y + "): Mines=" + minesInNeighbors + ", Hidden Mines=" + hiddenMines + ", Hidden Tiles=" + hiddenTiles + ", Flags Used=" + flagsUsed);
					}

					if (flagsUsed == minesInNeighbors) {
						for (final Tile n : neighbors) {
							if ((n != null) && !n.isFlagged( ) && n.isHidden( )) {
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
		totalRuntime += (System.currentTimeMillis( ) - startTime);
		System.out.println("AI completed 1 scan in " + (System.currentTimeMillis( ) - startTime) + "ms");
		cooldown( );
	}

	@Override
	public void run( ) {

		running = true;
		while (running) {
			process( );
		}
	}

}
