package com.weiss.snake;

import com.weiss.algorithms.pathfinding.algorithms.star.AStarPath;
import com.weiss.algorithms.pathfinding.wrappers.grid.Grid;
import com.weiss.algorithms.pathfinding.wrappers.node.Node;
import com.weiss.algorithms.pathfinding.wrappers.node.NodeType;
import com.weiss.snake.wrappers.Direction;
import com.weiss.snake.wrappers.block.Block;
import com.weiss.snake.wrappers.block.BlockType;
import com.weiss.snake.wrappers.field.GameField;

public class SnakeAutomation extends Snake {

	private Grid grid;

	private volatile boolean pathAvailable = true;
	private AStarPath pathToFood = null;	

	public SnakeAutomation(int size) {
		super(size);
		System.out.println("Starting point: " + this.field.initialHead);
	}

	@Override
	public void tick() {
		if (this.pathAvailable) {
			if (this.pathToFood == null) { //We need to generate a path to the food
				long start = System.currentTimeMillis();
				this.grid = convertToGrid(this.field);
				this.pathToFood = new AStarPath(this.toNode(this.snakeLine.chain.getFirst()), this.toNode(this.field.food), this.grid);
				this.pathAvailable = this.pathToFood.toNodeQuery(false) != null;
				if (this.pathAvailable) {
					for (Node n : this.pathToFood.toNodeQuery(false)) {
						toBlock(n).isAutomaticPath = true;
					}
				}
				else {
					System.err.println("Can't find path to food. Trying to find path to tail of snake");
					this.pathToFood = new AStarPath(this.toNode(this.snakeLine.chain.getFirst()), this.toNode(this.snakeLine.chain.getLast()), this.grid);
					this.pathAvailable = this.pathToFood.toNodeQuery(false) != null;
					if (!this.pathAvailable) {
						System.err.println("We ran out of paths to easily compute. Game over.");
					}
				}
				System.out.println("Computed path in " + (System.currentTimeMillis() - start) + "ms.");
				tick();
			}
			else {
				Block current = this.snakeLine.chain.getFirst();
				Block next = toBlock(this.pathToFood.step(false));
				if (next.type == BlockType.FOOD) {
					for (Node n : this.pathToFood.toNodeQuery(false)) {
						toBlock(n).isAutomaticPath = false;
					}
					this.pathToFood = null;
				}
				this.snakeLine.chain.getFirst().direction = getNextDirection(toNode(current), toNode(next));
				super.tick();
			}
		}
	}

	private Grid convertToGrid(GameField field) {
		Grid grid = new Grid(field.width, field.height);
		for (int h = 0; h < field.height; h++) {
			for (int w = 0; w < field.width; w++) {
				if (field.getBlock(w, h).type == BlockType.SNAKE) {
					grid.getNode(w, h).type = NodeType.BLOCKED;
				}
			}
		}
		return grid;
	}

	private Node toNode(Block block) {
		if (this.grid == null) {
			this.grid = convertToGrid(this.field);
		}
		return this.grid.getNode(block.x, block.y);
	}

	private Block toBlock(Node node) {
		return this.field.getBlock(node.x, node.y);
	}

	private Direction getNextDirection(Node current, Node next) {
		int dX = next.x - current.x;
		int dY = next.y - current.y;
		if (dX == 0) {
			return dY == 1 ? Direction.SOUTH: Direction.NORTH;
		}
		return dX == 1 ? Direction.EAST : Direction.WEST;
	}

	public static void main(String[] args) {
		new SnakeAutomation(50).start();
	}

}
