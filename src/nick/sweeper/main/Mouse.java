package nick.sweeper.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Mouse implements MouseListener {

	private final Grid g;

	public Mouse(final Grid g) {

		this.g = g;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {

		int x = e.getX( ), y = e.getY( );

		g.onClick(x, y, e.getButton( ) != MouseEvent.BUTTON1);

	}

	@Override
	public void mouseEntered(final MouseEvent arg0) {

	}

	@Override
	public void mouseExited(final MouseEvent arg0) {

	}

	@Override
	public void mousePressed(final MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(final MouseEvent arg0) {

	}

}
