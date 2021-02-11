
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class MainScreen extends JPanel implements ActionListener {
	private JFrame frame;
	private Image bg;
	private JButton play, levels, leaders;
	
	private SoundEffect click = new SoundEffect(SoundEffect.CLICK);

	MainScreen(JFrame frame) {
		this.frame = frame;
		setPreferredSize(new Dimension(33 * Block.SIZE, 24 * Block.SIZE));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		try {
			bg = ImageIO.read(new File("img/backgrounds/grasslands.png"));
			add(Box.createVerticalGlue());
			JLabel title = new JLabel(new ImageIcon("img/ui/title.png"));
			title.setAlignmentX(CENTER_ALIGNMENT);
			add(title);
			add(Box.createVerticalGlue());
			play = UIFactory.createButton(new ImageIcon("img/ui/play.png"), new ImageIcon("img/ui/playPressed.png"));
			play.setAlignmentX(CENTER_ALIGNMENT);
			play.addActionListener(this);
			add(play);
			add(Box.createVerticalStrut(Block.SIZE));
			levels = UIFactory.createButton(new ImageIcon("img/ui/levels.png"), new ImageIcon("img/ui/levelsPressed.png"));
			levels.setAlignmentX(CENTER_ALIGNMENT);
			levels.addActionListener(this);
			add(levels);
			add(Box.createVerticalStrut(Block.SIZE));
			leaders = UIFactory.createButton(new ImageIcon("img/ui/leaderboard.png"), new ImageIcon("img/ui/leaderboardPressed.png"));
			leaders.setAlignmentX(CENTER_ALIGNMENT);
			leaders.addActionListener(this);
			add(leaders);
			add(Box.createVerticalGlue());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void paintComponent(Graphics g) {
		drawBackground(g);
	}

	public void drawBackground(Graphics g) {
		Dimension size = getPreferredSize();
		double ratio = size.getWidth() / size.getHeight();
		double imgRatio = (double) bg.getWidth(this) / bg.getHeight(this);
		int width, height;
		if (ratio > imgRatio) {
			width = (int) size.getWidth();
			height = (int) (size.getWidth() / bg.getWidth(this) * bg.getHeight(this));
		} else {
			height = (int) size.getHeight();
			width = (int) (ratio * height);
		}
		g.drawImage(bg, -(width - (int) size.getWidth()) / 2, -(height - (int) size.getHeight()) / 2, width, height, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == play) {
			Level level = new Level(1, "img/backgrounds/grasslands.png", frame, this);
			frame.setContentPane(level);
		} else if (e.getSource() == levels) {
			frame.setContentPane(new LevelScreen(frame));
		} else if (e.getSource() == leaders) {
			frame.setContentPane(new LeaderboardScreen(frame));
		}
		click.play(false);
		frame.revalidate();
		frame.repaint();
	}
}
