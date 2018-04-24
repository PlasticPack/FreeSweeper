package nick.sweeper.ai;

import nick.sweeper.main.Grid;
import nick.sweeper.main.Tile;

public class AILogic implements Runnable {

	private final Grid			grid;

	private boolean				started	= false;

	private boolean[ ][ ]		satisfied;

	private volatile boolean	running	= false;

	private Thread				aiThread;

	public AILogic(final Grid g) {

		grid = g;
	}

	private void cooldown( ) throws InterruptedException {

		Thread.sleep(125);
		synchronized (this) {
			while (!running) {
				wait( );
			}
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

	private byte hiddenIn(final Tile[ ] list) {

		byte hidden = 0;
		for (Tile t : list) {
			if (t == null) {} else if (t.isHidden( )) {
				hidden++;
			}
		}
		return hidden;
	}

	private void init( ) {

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

	public synchronized void pause( ) {

		System.out.println("Pausing...");
		running = false;
		aiThread.interrupt( );
		aiThread = null;
	}

	@Override
	public void run( ) {

		init( );

		while (true) {
			try {
				Thread.sleep(500);
				if (!running) {
					synchronized (this) {
						while (!running) {
							wait( );
						}
					}
				}
				update( );
			} catch (InterruptedException e) {
				e.printStackTrace( );
			}
		}

	}

	public synchronized void start( ) {

		if (running) return;
		running = true;
		aiThread = new Thread(this, "AI");
		System.out.println("Starting...");
		aiThread.start( );
	}

	private void update( ) throws InterruptedException {

		if (!started) {
			init( );
		}

		if (grid.percentComplete( ) >= 100) return;

		for (int x = 0; x < grid.sizeX( ); x++) {
			for (int y = 0; y < grid.sizeY( ); y++) {

				final Tile t = grid.tileAt(x, y);

				if (!t.isHidden( ) && (t.getType( ) == Tile.Type.NUMBER) && !satisfied[x][y]) {

					grid.setHighLight(t);

					final byte minesInNeighbors = Byte.parseByte(t.getDisplayNum( ));
					final Tile[ ] neighbors = grid.neighbors(x, y);

					if (flagsUsed(neighbors) == minesInNeighbors) {
						for (final Tile n : neighbors) {
							if ((n != null) && (!n.isFlagged( ) || !n.isHidden( ))) {
								grid.onClick(n, false);
								cooldown( );
							}
						}
						satisfied[x][y] = true;
					}

					final byte hiddenMines = (byte) (minesInNeighbors - minesVisible(neighbors));
					final byte hiddenTiles = hiddenIn(neighbors);

					if (hiddenMines == hiddenTiles) {
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
	}

}
