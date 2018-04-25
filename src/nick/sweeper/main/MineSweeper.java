package nick.sweeper.main;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import nick.sweeper.ai.AILogic;

public final class MineSweeper extends Canvas implements Runnable {

	private static final long			serialVersionUID	= 1L;

	public static final short			height				= 25, width = 30, numMines = 90;

	private static JFrame				frame;

	private static Grid					grid;

	private static final short			maxFPS				= 240;

	private static boolean				isRunning			= true;

	private static final MineSweeper	game				= new MineSweeper( );

	private static final Thread			thread				= new Thread(game, "Main Thread");

	public static final String			name				= "MineSweeper v1.2b";

	private static final Input			input				= new Input(grid);

	public static final boolean			debug				= false;

	private static AILogic				ai;

	private static boolean				aiEngage			= false;

	public static AILogic getAI( ) {

		return ai;
	}

	public static void main(final String[ ] args) {

		frame = new JFrame( );

		frame.setResizable(true);
		frame.setTitle(name);
		frame.setLocationRelativeTo(null);
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(grid.renderSize( ));

		frame.addMouseListener(input);
		frame.addMouseMotionListener(input);

		game.addMouseListener(input);
		game.addMouseMotionListener(input);

		frame.addKeyListener(input);

		frame.add(game);
		frame.pack( );

		frame.setVisible(true);
		thread.start( );
	}

	public static void toggleAI( ) {

		aiEngage = !AILogic.isRunning( );
	}

	public MineSweeper( ) {

		grid = new Grid(width, height, numMines, this);
		setPreferredSize(grid.renderSize( ));

		ai = new AILogic(grid);
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

	public int renderHeight( ) {

		return getHeight( );
	}

	public int renderWidth( ) {

		return getWidth( );
	}

	@SuppressWarnings("unused")
	@Override
	public void run( ) {

		System.out.printf("%.1f", grid.percentMines( ));
		System.out.println("% of the map is mined.");

		final double delta = 1000.0 / 60, minFrameTime = 1000000000.0 / maxFPS;

		short fps = 0, ups = 0;
		long lastUpdate = System.currentTimeMillis( ), lastPrint = System.currentTimeMillis( ),
				lastFrameTime = System.nanoTime( );

		while (isRunning) {

			while ((lastUpdate + delta) < System.currentTimeMillis( )) {
				update( );
				lastUpdate += delta;
				++ups;
			}

			while ((lastFrameTime + minFrameTime) < System.nanoTime( )) {
				render( );
				lastFrameTime += minFrameTime;
				++fps;
			}

			if ((lastPrint + 1000) < System.currentTimeMillis( )) {

				final String basePrint = name + " (" + grid.sizeX( ) + ", " + grid.sizeY( ) + ") | Flags Used: " + grid.flagsUsed( ) + " | Mines: " + grid.numMines( ) + " | " + String.format("%.2f", grid.percentComplete( )) + "% Complete | AI Engaged: " + ai.isAlive( );

				if (debug) {
					final String lastSec = " | UPS: " + ups + " | FPS: " + fps;
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

	public synchronized void stop(final boolean lost) {

		isRunning = false;
		AILogic.halt( );

		if (lost) {
			System.out.println("Hit a mine!");
		}

		try {
			if (debug) {
				wait( );
			}
			ai.join(1000);
			thread.join(2000);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace( );
		}
	}

	private void update( ) {

		grid.setOffsets(getWidth( ) / 2, getHeight( ) / 2);
		grid.update( );

		if (aiEngage && !AILogic.isRunning( )) {
			ai.start( );
			aiEngage = false;
		}

	}

}
