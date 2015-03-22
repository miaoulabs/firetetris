package firetetris;

import java.awt.Color;

class TetrisGame {
	
	public final static int ANIMATION_LENGTH = 25;
	private final static int SPEED_DECREASE = 2;

	// Game properties
	private Tetromino current;
	private Shape next;
	private Grid grid;
	private Shape[] shapes = new Shape[7];

	// Timer is the interval between game "steps". Every 'timer' loops, the current block is moved down.
	private int timer = 20;
	// Represents the progress in 'timer'. Increased during every loop and reset when a block is .
	private int currTime = 0;

	// Scoring properties
	private int score;
	private int lines;
	private int level;
	
	// True if game is over, false otherwise.
	private boolean gameOver;
	
	// Countdown for animation. Animation lasts for 20 frames.
	private int animateCount;

	TetrisGame() {
		shapes[0] = new Shape(4, new int[] {4,5,6,7}, new Color(0,255,255));  // I
		shapes[1] = new Shape(3, new int[] {1,2,3,4}, new Color(0,255,0));   	// S
		shapes[2] = new Shape(3, new int[] {0,1,4,5}, new Color(255,0,0));    	// Z
		shapes[3] = new Shape(3, new int[] {0,3,4,5}, new Color(0,0,255));    	// J
		shapes[4] = new Shape(3, new int[] {2,3,4,5}, new Color(255,165,0));  	// L
		shapes[5] = new Shape(3, new int[] {1,3,4,5}, new Color(160,32,240)); 	// T
		shapes[6] = new Shape(2, new int[] {0,1,2,3}, new Color(255,255,0));  	// O
		
		grid = new Grid(20, 10);
		loadNext();

		currTime = 0;
		animateCount = -1;
	}

	public Tetromino getCurrent() {
		return current;
	}

	public Shape getNext() {
		return next;
	}

	public Grid getGrid() {
		return grid;
	}

	public int getScore() {
		return score;
	}

	public int getLines() {
		return lines;
	}

	public int getLevel() {
		return level;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public int getAnimateCount() {
		return animateCount;
	}

	// used when automatically moving the block down.
	private void stepDown() {
		if (current == null) return;

		if (current.y >= current.final_row) {
			finalizeShapePlacement();
		} else {
			current.y++;
			currTime = 0;
		}
	}

	public void update() {
		if (animateCount >= 0) {
			animateCount--;
			
			if (animateCount < 0) {
				// clear the lines, and load the next Tetromino
				grid.eraseCleared();
				loadNext();
			}
		}

		currTime++;
		
		if (currTime >= timer && animateCount < 0) {
			stepDown();
			
			// reset the timer if player is at the bottom, for wiggle room before it locks
			if (current != null && current.y == current.final_row)
				currTime = -20;
		}
	}
		
	public void down() {
		if (current == null) return;

		if (current.y >= current.final_row) {
			// if already at the bottom, down shortcuts to lock current and load next block
			finalizeShapePlacement();
		} else {
			stepDown();
			score += 1;  // get a point for manual down
		}
	}
	
	public void left() {
		if (current == null) return;
		
		if (isLegal(current.shape, current.x - 1, current.y))
			current.x--;
		else if (isLegal(current.shape, current.x - 2, current.y))
			current.x -= 2;
		current.final_row = getFinalRow();
	}

	public void right() {
		if (current == null) return;
		
		if (isLegal(current.shape, current.x + 1, current.y))
			current.x++;
		else if (isLegal(current.shape, current.x + 2, current.y))
			current.x += 2;
		current.final_row = getFinalRow();
	}

	// move block all the way to the bottom
	public void hardDown() {
		if (current == null) return;

		score += (grid.rows - current.y);
		current.y = current.final_row;
		finalizeShapePlacement();
	}

	public void rotate() {
		if (current == null) return;

		Shape rotated = current.shape.rotated();
		int currentX = current.x;
		int currentY = current.y;
		
		if (isLegal(rotated, currentX, currentY)) {
			current.shape = rotated;
			current.final_row = getFinalRow();
		} else if (isLegal(rotated, currentX + 1, currentY) || isLegal(rotated, currentX + 2, currentY)) {
			current.shape = rotated;
			right();
		} else if (isLegal(rotated, currentX - 1, currentY) || isLegal(rotated, currentX - 2, currentY)) {
			current.shape = rotated;
			left();
		}
	}
	
	private void finalizeShapePlacement() {
		for (int i = 0; i < current.shape.matrix.length; ++i)
			for (int j = 0; j < current.shape.matrix.length; ++j)
				if (current.shape.matrix[i][j] && j + current.y >= 0) 
					grid.colors[i + current.x][j + current.y] = current.getColor();
		
		if (checkLines()) {
			// Start "rows cleared" animation, next piece will be loaded at end of animation 
			animateCount = ANIMATION_LENGTH;
			current = null;
		} else {
			loadNext();			
		}
	}

	private boolean checkLines() {
		grid.updatedClearedRows();
		if (grid.clearedRows.isEmpty())
			return false;

		if (lines/10 < (lines + grid.clearedRows.size())/10) {
			level++;
			timer -= SPEED_DECREASE;
		}
		lines += grid.clearedRows.size();
		score += (1 << grid.clearedRows.size() - 1)*100;
		return true;
	}

	private void loadNext() {
		if (next != null) {
			current = new Tetromino(next);			
		} else {
			current = new Tetromino(shapes[(int)(Math.random() * 7)]);
		}
		current.final_row = getFinalRow();
		next = shapes[(int)(Math.random() * 7)];
		gameOver = !isLegal(current.shape, 3, -1);
	}

	private int getFinalRow() {
		int start = Math.max(0, current.y);
		for (int row = start; row <= grid.rows; ++row)
			if (!isLegal(current.shape, current.x, row))
				return row - 1;
		return -1;
	}

	private boolean isLegal(Shape shape, int col, int row) {
		for (int i = 0; i < shape.matrix.length; ++i)
			for (int j = 0; j < shape.matrix.length; ++j)
				if (shape.matrix[i][j] && grid.isOccupied(col + i, row + j))
					return false;
		return true;
	}
}