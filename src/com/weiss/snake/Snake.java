package com.weiss.snake;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.weiss.snake.wrappers.Direction;
import com.weiss.snake.wrappers.block.Block;
import com.weiss.snake.wrappers.block.BlockChain;
import com.weiss.snake.wrappers.field.GameField;
import com.weiss.util.Time;

public class Snake extends JPanel implements KeyListener {

	private JFrame mainFrame;

	/** Time in ms between each game tick */
	private static final int tickSpeed = 15;
	public GameField field;
	public BlockChain snakeLine;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> currentScheduledTask;

	public Snake(int size) {
		this.field = new GameField(this, size, size);
		this.snakeLine = new BlockChain(this, this.field.getInitialHead());

		this.mainFrame = new JFrame();
		this.setScore(0);
		this.mainFrame.add(this);
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);
		this.mainFrame.setResizable(false);
		this.mainFrame.setLocationRelativeTo(null);
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addKeyListener(this);
		this.setDoubleBuffered(true);
		this.requestFocus();
	}

	public void start() {
		Time.sleep(1000); //Let the frame display for a second before we start
		this.currentScheduledTask = this.createScheduledTask();
	}

	public void tick() {
		this.snakeLine.update();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Snake.this.repaint();
			}
		});
	}

	public void setScore(int score) {
		this.mainFrame.setTitle("Snake | Score: " + score);
	}

	public void end(int finalScore) {
		System.out.println("Game Over. Final score: " + finalScore);
		System.exit(0);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.field.width * Block.SIZE + 1, this.field.height * Block.SIZE + 1);
	}

	@Override
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D) g1;

		this.field.draw(g);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Direction nextDirection = this.getDirection(e);
		Direction currentDirection = Snake.this.snakeLine.getDirection();
		if (nextDirection != Direction.NONE && nextDirection != currentDirection) {
			if (nextDirection != Direction.getOppositeDirection(currentDirection) || this.snakeLine.chain.size() == 1) {
				Snake.this.snakeLine.setDirection(this.getDirection(e));
				this.currentScheduledTask = this.createScheduledTask();
			}
		}
	}
	@Override
	public void keyReleased(KeyEvent e) { }
	@Override
	public void keyTyped(KeyEvent e) { }

	private ScheduledFuture<?> createScheduledTask() {
		if (this.currentScheduledTask != null) {
			this.currentScheduledTask.cancel(true);
		}
		return this.executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Snake.this.tick();
			}
		}, 0L, tickSpeed, TimeUnit.MILLISECONDS);
	}

	private Direction getDirection(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
				return Direction.NORTH;

			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
				return Direction.SOUTH;

			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A:
				return Direction.WEST;

			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D:
				return Direction.EAST;

			default:
				return Direction.NONE;
		}
	}

	public static void main(String[] args) {
		new Snake(30).start();
	}
}
