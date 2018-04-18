package nick.sweeper.main;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public final class MineSweeper extends Canvas implements Runnable {

	private static final long			serialVersionUID	= 1L;

	public static final short			height				= 15, width = 15, numMines = 25;

	private static JFrame				frame;

	private static Grid					grid;

	private static boolean				isRunning			= true;

	private static final MineSweeper	game				= new MineSweeper( );

	private static final Thread			thread				= new Thread(game, "Main Thread");

	public static final String			name				= "MineSweeper";

	private static final Mouse			mouseInput			= new Mouse(grid);

	public static final boolean			debug				= false;

	public static void main(final String[ ] args) {

		frame = new JFrame( );

		frame.setResizable(true);
		frame.setTitle(name);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(grid.getGridRenderSize( ));
		game.setPreferredSize(grid.getGridRenderSize( ));
		frame.addMouseListener(mouseInput);
		game.addMouseListener(mouseInput);
		frame.add(game);
		frame.pack( );

		frame.setVisible(true);
		thread.start( );
	}

	public static void stop(final boolean lost) {

		isRunning = false;

		if (lost) {
			System.out.println("Hit a mine!");
		}

		try {
			thread.join(1000);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace( );
		}
	}

	public MineSweeper( ) {

		grid = new Grid(width, height, numMines);

	}

	private void render( ) {

		final BufferStrategy bs = getBufferStrategy( );

		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		final Graphics g = bs.getDrawGraphics( );

		g.clearRect(0, 0, getWidth( ), getHeight( ));
		grid.draw(g);

		g.dispose( );
		bs.show( );
	}

	@Override
	public void run( ) {

		System.out.printf("%.2f", grid.getPercentofMines( ) * 100);
		System.out.println("% of the map is mined.");

		final double delta = 1000 / 60f;
		int fps = 0, ups = 0;
		long lastUpdate = System.currentTimeMillis( );
		long lastPrint = System.currentTimeMillis( );
		while (isRunning) {

			while ((lastUpdate + delta) < System.currentTimeMillis( )) {
				update( );
				lastUpdate += delta;
				ups++;
			}

			render( );
			fps++;

			if ((lastPrint + 1000) < System.currentTimeMillis( )) {

				final String lastSec = " | UPS: " + ups + " | FPS: " + fps;
				final String basePrint = name + " (" + grid.getSizeX( ) + ", " + grid.getSizeY( ) + ") | Flags Used: " + grid.getFlagsUsed( ) + " | Mines: " + grid.getNumMines( ) + " | " + String.format("%.2f", grid.percentComplete( )) + "% Complete";

				if (debug) {
					frame.setTitle(basePrint + lastSec);
				} else {
					frame.setTitle(basePrint);
				}

				fps = 0;
				ups = 0;
				lastPrint += 1000;
			}

		}
	}

	// TODO: Check for win
	private void update( ) {

		grid.setOffsets(getWidth( ) / 2, getHeight( ) / 2);
		grid.update( );
	}

}
