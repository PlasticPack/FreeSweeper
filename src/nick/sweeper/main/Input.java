package nick.sweeper.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Input implements MouseListener, MouseMotionListener, KeyListener {

	private static int		mX, mY;

	private static boolean	mouseIn	= false;

	public static boolean isMouseIn( ) {

		return mouseIn;
	}

	public static int mouseX( ) {

		return mX;
	}

	public static int mouseY( ) {

		return mY;
	}

	private final Grid g;

	public Input(final Grid g) {

		this.g = g;
	}

	@Override
	public void keyPressed(final KeyEvent e) {

		System.out.println("Key Pressed: " + e.getKeyChar( ) + " in Thread " + Thread.currentThread( ).getName( ));

		if (e.getKeyCode( ) == KeyEvent.VK_A) {
			if (!MineSweeper.getAI( ).isRunning( )) {
				MineSweeper.getAI( ).start( );
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent arg0) {

	}

	@Override
	public void keyTyped(final KeyEvent arg0) {

	}

	@Override
	public void mouseClicked(final MouseEvent e) {

		//System.out.println("In Thread " + Thread.currentThread( ).getName( ));
		int x = e.getX( ), y = e.getY( );

		g.onClick(x, y, e.getButton( ) != MouseEvent.BUTTON1);

	}

	@Override
	public void mouseDragged(final MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(final MouseEvent arg0) {

		mouseIn = true;
	}

	@Override
	public void mouseExited(final MouseEvent arg0) {

		mouseIn = false;
	}

	@Override
	public void mouseMoved(final MouseEvent e) {

		mX = e.getX( );
		mY = e.getY( );
	}

	@Override
	public void mousePressed(final MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {

	}

}
