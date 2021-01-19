import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Level extends JPanel
implements MouseListener, MouseMotionListener, ActionListener {
	private JFrame frame;
	private JPanel last;
	private int levelNumber;

	private Timer timer = new Timer(10, this);

	private Image bg;

	private Map map, startMap;
	private int[] storedPowerUps = { 0, 0, 0 };
	private double powerUpAngle = 0;

	private Player player = new Player();
	private boolean firstMovement = true;
	private boolean complete = false;

	private JButton lastLevel, nextLevel, exit, reset;
	private Image[] powerUpImages = new Image[3];
	private Image[] numbers = new Image[10];
	private Image cross;
	private JPanel moveHelp, growHelp, mergeHelp, finishHelp;
	private JPanel[] splitHelp = new JPanel[3];
	private Font levelFont = FontLoader.loadFont("src/res/font.ttf",
	36);
	private Font helpFont = FontLoader.loadFont("src/res/font.ttf",
	20);

	private HashMap<String, SoundEffect> soundEffects = new HashMap<String, SoundEffect>();

	Level(int levelNumber, String bgPath, JFrame frame, JPanel last) {
		this.frame = frame;
		this.last = last;
		this.levelNumber = levelNumber;
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		requestFocus();
		requestFocusInWindow();
		try {
			bg = ImageIO.read(new File(bgPath));
			List<String> lines = Files.readAllLines(
			Paths.get("src/res/levels/level" + levelNumber + ".txt"));
			map = new Map(lines);
			startMap = new Map(map);
			for (Point startPos : map.getStartingPositions()) {
				player.addBlock(startPos.x, startPos.y);
			}
			player.calculateRelativePositions(player.getBlocks());
			storedPowerUps = new int[] { 0, 0, 0 };
			initializeUI();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bindKeys();
		initializeSoundEffects();
//		soundEffects.get("start").play(false);
		timer.start();
	}

	private void initializeUI() throws IOException {
		exit = initializeButton("src/res/img/ui/exit.png",
		"src/res/img/ui/exitPressed.png",
		new Rectangle(30, 15, 70, 70));
		add(exit);
		lastLevel = initializeButton("src/res/img/ui/lastLevel.png",
		"src/res/img/ui/lastLevelPressed.png",
		new Rectangle(130, 25, 45, 49));
		if (levelNumber == 1) {
			lastLevel.setEnabled(false);
		}
		add(lastLevel);
		JLabel levelText = new JLabel("Level " + levelNumber,
		SwingConstants.CENTER);
		levelText.setFont(levelFont);
		levelText.setForeground(Color.WHITE);
		levelText.setBounds(190, 22, 271, 70);
		add(levelText);
		JLabel levelBar = new JLabel(
		new ImageIcon("src/res/img/ui/levelBar.png"));
		levelBar.setBounds(190, 15, 271, 70);
		add(levelBar);
		nextLevel = initializeButton("src/res/img/ui/nextLevel.png",
		"src/res/img/ui/nextLevelPressed.png",
		new Rectangle(476, 25, 45, 49));
		if (levelNumber == 24) {
			nextLevel.setEnabled(false);
		}
		add(nextLevel);
		reset = initializeButton("src/res/img/ui/reset.png",
		"src/res/img/ui/resetPressed.png",
		new Rectangle(551, 15, 70, 70));
		add(reset);
		powerUpImages[0] = ImageIO
		.read(new File("src/res/img/sprites/powerups/grow.png"));
		powerUpImages[1] = ImageIO
		.read(new File("src/res/img/sprites/powerups/split.png"));
		powerUpImages[2] = ImageIO
		.read(new File("src/res/img/sprites/powerups/merge.png"));
		for (int i = 0; i < 10; i++) {
			numbers[i] = ImageIO
			.read(new File("src/res/img/ui/hud_" + i + ".png"));
		}
		cross = ImageIO.read(new File("src/res/img/ui/hud_x.png"));

		moveHelp = initializeHelpPanel(
		"Press A or left arrow key to move left. Press D or right arrow key to move right.",
		300, 125);
		if (levelNumber == 1) {
			add(moveHelp);
			revalidate();
		}
		growHelp = initializeHelpPanel(
		"Hover your cursor next to your block. Click to grow there.",
		"src/res/img/sprites/powerups/grow.png", 900, 150);
		splitHelp[0] = initializeHelpPanel(
		"Right click to begin to split.",
		"src/res/img/sprites/powerups/split.png", 250, 350);
		splitHelp[1] = initializeHelpPanel(
		"Click the line where you want to split.",
		"src/res/img/sprites/powerups/split.png", 250, 350);
		splitHelp[2] = initializeHelpPanel("Choose a side to keep.",
		"src/res/img/sprites/powerups/split.png", 250, 350);
		mergeHelp = initializeHelpPanel(
		"Press SHIFT to merge with adjacent abandoned blocks.",
		"src/res/img/sprites/powerups/merge.png", 950, 300);
		finishHelp = initializeHelpPanel(
		"To complete a level, all blocks must either be on a button or off the ground.",
		100, 600);
		if (levelNumber == 4) {
			add(finishHelp);
			revalidate();
		}
	}

	private JButton initializeButton(String imgPath,
	String pressedImgPath, Rectangle bounds) {
		JButton b = new JButton(new ImageIcon(imgPath));
		b.setPressedIcon(new ImageIcon(pressedImgPath));
		b.setBounds(bounds);
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.addActionListener(this);
		return b;
	}

	private JPanel initializeHelpPanel(String text, int x, int y) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		panel.setBounds(x, y, 300, 150);
		JLabel helpText = new JLabel("<html>" + text + "</html>");
		helpText.setFont(helpFont);
		helpText.setForeground(Color.WHITE);
		helpText.setBounds(20, 20, 260, 110);
		panel.add(helpText);
		JLabel helpBG = new JLabel(
		new ImageIcon("src/res/img/ui/help.png"));
		helpBG.setBounds(0, 0, 300, 150);
		panel.add(helpBG);
		return panel;
	}

	private JPanel initializeHelpPanel(String text, String imagePath,
	int x, int y) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		panel.setBounds(x, y, 300, 150);
		ImageIcon icon = new ImageIcon(imagePath);
		Image image = icon.getImage();
		image = image.getScaledInstance(50, 50, Image.SCALE_DEFAULT);
		JLabel powerUp = new JLabel(new ImageIcon(image));
		powerUp.setBounds(8, 50, 50, 50);
		panel.add(powerUp);
		JLabel helpText = new JLabel("<html>" + text + "</html>");
		helpText.setFont(helpFont);
		helpText.setForeground(Color.WHITE);
		helpText.setBounds(60, 20, 220, 110);
		panel.add(helpText);
		JLabel helpBG = new JLabel(
		new ImageIcon("src/res/img/ui/help.png"));
		helpBG.setBounds(0, 0, 300, 150);
		panel.add(helpBG);
		return panel;
	}

	private void bindKeys() {
		Action leftDown = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (player.getState() == Player.NORMAL) {
					player.setMovement(Movement.LEFT);
					if (firstMovement) {
						firstMovement = false;
					}
				}
			}
		};
		Action rightDown = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (player.getState() == Player.NORMAL) {
					player.setMovement(Movement.RIGHT);
					if (firstMovement) {
						firstMovement = false;
					}
				}
			}
		};
		Action leftUp = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (player.getState() == Player.NORMAL) {
					if (player.getMovement() != Movement.RIGHT) {
						player.setMovement(Movement.STILL_LEFT);
					}
				}
			}
		};
		Action rightUp = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (player.getState() == Player.NORMAL) {
					if (player.getMovement() != Movement.LEFT) {
						player.setMovement(Movement.STILL_RIGHT);
					}
				}
			}
		};
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false),
		"left-press", leftDown);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left-press",
		leftDown);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false),
		"right-press", rightDown);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false),
		"right-press", rightDown);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true),
		"left-release", leftUp);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true),
		"left-release", leftUp);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true),
		"right-release", rightUp);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true),
		"right-release", rightUp);
		registerKeyBinding(
		KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT,
		InputEvent.SHIFT_DOWN_MASK, false), "shift-press",
		new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (player.getState() == Player.NORMAL) {
					if (storedPowerUps[2] > 0) {
						ArrayList<PlayerBlock> mergedBlocks = player
						.merge(map);
						if (mergedBlocks.size() > 0) {
							for (PlayerBlock pBlock : mergedBlocks) {
								Point worldCoords = pBlock
								.getWorldCoords();
								map
								.getChars()[worldCoords.y][worldCoords.x] = '.';
								map
								.getBlocks()[worldCoords.y][worldCoords.x] = new SpaceBlock();
								player.addBlock(worldCoords.x,
								worldCoords.y);
							}
							player.calculateRelativePositions(
							player.getBlocks());
							storedPowerUps[2]--;
							soundEffects.get("merge").play(false);
							if (levelNumber == 15) {
								remove(mergeHelp);
								revalidate();
							}
						}
					}
				}
			}
		});
	}

	private void registerKeyBinding(KeyStroke keyStroke, String name,
	Action action) {
		InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = getActionMap();
		im.put(keyStroke, name);
		am.put(name, action);
	}

	private void initializeSoundEffects() {
		soundEffects.put("click", new SoundEffect(SoundEffect.CLICK));
		soundEffects.put("pickup",
		new SoundEffect(SoundEffect.PICKUP));
		soundEffects.put("grow", new SoundEffect(SoundEffect.GROW));
		soundEffects.put("split", new SoundEffect(SoundEffect.SPLIT));
		soundEffects.put("sad", new SoundEffect(SoundEffect.SAD));
		soundEffects.put("merge", new SoundEffect(SoundEffect.MERGE));
		soundEffects.put("buttonDown",
		new SoundEffect(SoundEffect.BUTTON_DOWN));
		soundEffects.put("buttonUp",
		new SoundEffect(SoundEffect.BUTTON_UP));
		soundEffects.put("death", new SoundEffect(SoundEffect.DEATH));
		soundEffects.put("success",
		new SoundEffect(SoundEffect.SUCCESS));
		soundEffects.put("start", new SoundEffect(SoundEffect.START));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		drawBackground(g);
		drawHUD(g);
		MapBlock[][] blocks = map.getBlocks();
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[0].length; j++) {
				MapBlock block = blocks[i][j];
				Image img = block.getImage();
				if (img != null) {
					if (block instanceof PowerUp) {
						AffineTransform old = g2.getTransform();
						g2.rotate(powerUpAngle,
						(j + 0.5) * Block.SIZE,
						(i + 0.5) * Block.SIZE);
						g2.drawImage(img, j * Block.SIZE,
						i * Block.SIZE, Block.SIZE, Block.SIZE, this);
						g2.setTransform(old);
					} else {
						g2.drawImage(img, j * Block.SIZE,
						i * Block.SIZE, Block.SIZE, Block.SIZE, this);
					}
				}
			}
		}
		ArrayList<PlayerBlock> pBlocks = player.getBlocks();
		for (PlayerBlock pBlock : pBlocks) {
			Point pixelCoords = pBlock.getPixelCoords();
			g2.drawImage(pBlock.getImage(), pixelCoords.x,
			pixelCoords.y, Block.SIZE, Block.SIZE, this);
//			g2.setColor(Color.RED.darker());
//			Point worldCoords = pBlock.getWorldCoords();
//			for (Point offset : sides.keySet()) {
//				if (!pBlocks.contains(new PlayerBlock(worldCoords.x + offset.x, worldCoords.y + offset.y))) {
//					Rectangle rect = sides.get(offset);
//					g2.fillRect(pixelCoords.x + rect.x, pixelCoords.y + rect.y, rect.width, rect.height);
//				}
//			}
		}
		int state = player.getState();
		if (state == Player.NORMAL) {
			PlayerBlock highlightedBlock = player
			.getHighlightedBlock();
			if (highlightedBlock != null) {
				Point highlightedPoint = highlightedBlock
				.getPixelCoords();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(highlightedBlock.getImage(),
				highlightedPoint.x, highlightedPoint.y, Block.SIZE,
				Block.SIZE, this);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
		} else if (state == Player.SPLITTING) {
			Point[] splitLine = player.getSplitLine();
			if (splitLine != null) {
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER));
				g2.drawLine(splitLine[0].x * Block.SIZE,
				splitLine[0].y * Block.SIZE,
				splitLine[1].x * Block.SIZE,
				splitLine[1].y * Block.SIZE);
			}
		} else if (state == Player.CHOOSING) {
			int chosenSide = player.getChosenSide();
			if (chosenSide != -1) {
				AlphaComposite ac = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER, 0.5f);
				g2.setComposite(ac);
				for (PlayerBlock pBlock : player
				.getSplitBlocks(chosenSide == 0 ? 1 : 0)) {
					Point pixelCoords = pBlock.getPixelCoords();
					g2.drawImage(pBlock.getImage(), pixelCoords.x,
					pixelCoords.y, Block.SIZE, Block.SIZE, this);
				}
			}
		}
		g2.setComposite(AlphaComposite.SrcOver);
	}

	private void drawBackground(Graphics g) {
		Dimension size = getPreferredSize();
		double ratio = size.getWidth() / size.getHeight();
		double imgRatio = (double) bg.getWidth(this)
		/ bg.getHeight(this);
		int width, height;
		if (ratio > imgRatio) {
			width = (int) size.getWidth();
			height = (int) (size.getWidth() / bg.getWidth(this)
			* bg.getHeight(this));
		} else {
			height = (int) size.getHeight();
			width = (int) (ratio * height);
		}
		g.drawImage(bg, -(width - (int) size.getWidth()) / 2,
		-(height - (int) size.getHeight()) / 2, width, height, this);
	}

	private void drawHUD(Graphics g) {
		int x = getPreferredSize().width;
		int[] startX = { x - 600, x - 400, x - 200 };
		for (int i = 0; i < 3; i++) {
			Image[] images = new Image[4];
			images[0] = powerUpImages[i];
			images[1] = cross;
			int num = storedPowerUps[i];
			images[2] = num < 10 ? numbers[num] : numbers[num / 10];
			images[3] = num < 10 ? null : numbers[num % 10];

			int dx = 0;
			for (int j = 0; j < images.length; j++) {
				if (images[j] != null) {
					g.drawImage(images[j], startX[i] + dx,
					(images[0].getHeight(null)
					- images[j].getHeight(null)) / 2, null);
					int width = images[j].getWidth(null);
					if (j == 0) {
						width -= 15;
					}
					dx += width;
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (player.getMovement() == Movement.STILL
		&& !player.isFalling()) {
			int state = player.getState();
			if (state == Player.NORMAL) {
				if (SwingUtilities.isRightMouseButton(arg0)
				&& storedPowerUps[1] > 0
				&& player.getBlocks().size() > 1) {
					player.startSplitting();
					if (levelNumber == 7) {
						remove(splitHelp[0]);
						add(splitHelp[1]);
						revalidate();
					}
				} else if (SwingUtilities.isLeftMouseButton(arg0)
				&& player.getHighlightedBlock() != null) {
					player.confirmBuild(map);
					--storedPowerUps[0];
					soundEffects.get("grow").play(false);
					if (levelNumber == 2) {
						remove(growHelp);
						revalidate();
					}
				}
			} else if (state == Player.SPLITTING) {
				if (SwingUtilities.isLeftMouseButton(arg0)
				&& player.getSplitLine() != null) {
					player.splitIntoSides();
					--storedPowerUps[1];
					soundEffects.get("split").play(false);
					if (levelNumber == 7) {
						remove(splitHelp[1]);
						add(splitHelp[2]);
						revalidate();
					}
				}
			} else if (state == Player.CHOOSING) {
				int chosenSide = player.getChosenSide();
				if (SwingUtilities.isLeftMouseButton(arg0)
				&& chosenSide != -1) {
					ArrayList<PlayerBlock> abandonedBlocks = player
					.chooseSide(chosenSide);
					for (PlayerBlock pBlock : abandonedBlocks) {
						Point worldCoords = pBlock.getWorldCoords();
						map
						.getChars()[worldCoords.y][worldCoords.x] = 'C';
					}
					for (int i = 0; i < map.getChars().length; i++) {
						for (int j = 0; j < map
						.getChars()[0].length; j++) {
							if (map.getChars()[i][j] == 'C') {
								try {
									map
									.getBlocks()[i][j] = new CryingPlayerBlock(
									map.relativePosition(i, j));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				soundEffects.get("sad").play(false);
				if (levelNumber == 7) {
					remove(splitHelp[2]);
					revalidate();
				}
			}
			mouseAction(arg0);
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseAction(arg0);
	}

	private void mouseAction(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int state = player.getState();
		if (state == Player.NORMAL) {
			if (storedPowerUps[0] > 0)
				player.highlightBlock(x, y);
		} else if (state == Player.SPLITTING) {
			player.highlightSplitLine(map, x, y);
		} else if (state == Player.CHOOSING) {
			player.setChosenSide(x, y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == lastLevel) {
			changeScreen(new Level(--levelNumber,
			"src/res/img/backgrounds/grasslands.png", frame, last));
			soundEffects.get("click").play(false);
		} else if (arg0.getSource() == nextLevel) {
			changeScreen(new Level(++levelNumber,
			"src/res/img/backgrounds/grasslands.png", frame, last));
			soundEffects.get("click").play(false);
		} else if (arg0.getSource() == reset) {
			resetLevel();
			soundEffects.get("click").play(false);
		} else if (arg0.getSource() == exit) {
			changeScreen(last);
			soundEffects.get("click").play(false);
		} else if (arg0.getSource() == timer) {
			powerUpAngle += 0.05;
			if (powerUpAngle >= 2 * Math.PI)
				powerUpAngle = 0;
			player.move(map);
			pickUpPowerUp();

			HashSet<PlayerBlock> blocksToSquish = new HashSet<PlayerBlock>();
			for (Point gbLoc : map.getGoalBlocks()) {
				GoalBlock g = (GoalBlock) map
				.getBlocks()[gbLoc.y][gbLoc.x];
				ArrayList<PlayerBlock> blocksOn = player
				.blocksOnGoalBlock(gbLoc);
				if (blocksOn.size() > 0 && !g.isPressed()) {
					g.press();
					soundEffects.get("buttonDown").play(false);
				} else if (blocksOn.size() == 0 && g.isPressed()) {
					g.unpress();
					soundEffects.get("buttonUp").play(false);
				}
				blocksToSquish.addAll(blocksOn);
			}
			player.squishBlocks(blocksToSquish);

			if (player.isOutOfBounds(map)) {
				resetLevel();
				soundEffects.get("death").play(false);
			}
			if (!complete && player.reachedGoal(map)) {
				complete = true;
				if (player.getMovement() == Movement.LEFT)
					player.setMovement(Movement.STILL_LEFT);
				else if (player.getMovement() == Movement.RIGHT)
					player.setMovement(Movement.STILL_RIGHT);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
				lastLevel.setEnabled(false);
				nextLevel.setEnabled(false);
				reset.setEnabled(false);
			} else if (complete
			&& player.getMovement() == Movement.STILL) {
				soundEffects.get("success").play(false);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				timer.stop();
				if (levelNumber != 24)
					changeScreen(new Level(++levelNumber,
					"src/res/img/backgrounds/grasslands.png", frame,
					last));
			}
		}
		repaint();
	}

	private void changeScreen(JPanel screen) {
		frame.setContentPane(screen);
		frame.revalidate();
		frame.repaint();
		timer.stop();
	}

	private void resetLevel() {
		map = new Map(startMap);
		player.resetPositions(map.getStartingPositions());
		player.setState(Player.NORMAL);
		storedPowerUps = new int[] { 0, 0, 0 };
		remove(growHelp);
		for (int i = 0; i < 3; i++) {
			remove(splitHelp[i]);
		}
	}

	private void pickUpPowerUp() {
		for (int i = 0; i < player.getBlocks().size(); i++) {
			PlayerBlock pBlock = player.getBlocks().get(i);
			Point check = new Point(
			(int) (Math
			.round(pBlock.getPixelCoords().getX() / Block.SIZE)),
			pBlock.getWorldCoords().y);
			if (map.isValidBlock(check.x, check.y)) {
				MapBlock block = map.getBlocks()[check.y][check.x];
				if (block instanceof GrowPowerUp) {
					++storedPowerUps[0];
					if (levelNumber == 2) {
						add(growHelp);
						revalidate();
					}
				} else if (block instanceof SplitPowerUp) {
					++storedPowerUps[1];
					if (levelNumber == 7) {
						add(splitHelp[0]);
						revalidate();
					}
				} else if (block instanceof MergePowerUp) {
					++storedPowerUps[2];
					if (levelNumber == 15) {
						add(mergeHelp);
						revalidate();
					}
				} else if (block instanceof QuadPowerUp) {
					storedPowerUps[0] += 4;
				}
				if (block instanceof PowerUp) {
					map
					.getBlocks()[check.y][check.x] = new SpaceBlock();
					soundEffects.get("pickup").play(false);
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(map.getBlocks()[0].length * Block.SIZE,
		map.getBlocks().length * Block.SIZE);
	}

	private static void runGame() {
		JFrame frame = new JFrame("Level 15");
		Level level = new Level(15,
		"src/res/img/backgrounds/grasslands.png", frame, null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addMouseListener(level);
		frame.addMouseMotionListener(level);
		frame.add(level);
		frame.pack();
		frame.setVisible(true);
	}

//	public static void main(String[] args) {
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				runGame();
//			}
//		});
//	}
}