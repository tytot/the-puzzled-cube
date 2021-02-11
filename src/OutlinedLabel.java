import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JLabel;

public class OutlinedLabel extends JLabel {

	private String text;
	private int width;

	OutlinedLabel(String text) {
		this.text = text;
		this.setOpaque(false);
		
		width = calculateWidth();
	}

	public void setText(String text) {
		this.text = text;
		
		int newWidth = calculateWidth();
		if (width != newWidth) {
			width = newWidth;
		}
	}

	public void paintComponent(Graphics g) {
		Image[] images = new Image[text.length()];
		for (int i = 0; i < text.length(); i++) {
			String c = text.substring(i, i + 1);
			images[i] = UIFactory.OUTLINED_MAP.get(c);
		}
		
		int dx = 0;
		for (int i = 0; i < images.length; i++) {
			g.drawImage(images[i], dx, (40 - images[i].getHeight(null)) / 2, null);
			dx += images[i].getWidth(null);
		}
	}
	
	private int calculateWidth() {
		int width = 0;
		for (int i = 0; i < text.length(); i++) {
			width += UIFactory.OUTLINED_MAP.get(text.substring(i, i + 1)).getWidth(null);
		}
		return width;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, 40);
	}
}
