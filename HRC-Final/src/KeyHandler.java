import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class KeyHandler implements KeyListener {

	private Robot r;
	private EnvironmentPanel frame;

	public KeyHandler(Robot robotspecial, EnvironmentPanel envPanel) {
		this.r = robotspecial;
		this.frame = envPanel;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		System.out.println(code);
		if (code == KeyEvent.VK_RIGHT) {
			System.out.println("right");
			this.r.getAction("right");
		} else if (code == KeyEvent.VK_LEFT) {
			this.r.getAction("left");
		} else if (code == KeyEvent.VK_UP) {
			this.r.getAction("up");
		} else if (code == KeyEvent.VK_DOWN) {
			this.r.getAction("down");
		} else if (code == KeyEvent.VK_SPACE) {
			this.r.getAction("clean");
		} else if (code == KeyEvent.VK_A) {
			this.r.getAction("auto clean");
		} else if (code == KeyEvent.VK_E)
			System.exit(0);
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// do nothing
	}
}