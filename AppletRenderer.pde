import controlP5.*;

class AppletRenderer {

  private final String[] HARD_MODE_TEXT = new String[] { "WORST", "PIECE", "" };
  private final GridView boardView;
  private final GridView previewView;
  private final GridView heldPieceView;

  AppletRenderer() {
    textSize(25);
    
    boardView = new GridView(146, 33, 300, 600);
    heldPieceView = new GridView(12, 33, 120, 60);
    heldPieceView.rows = 2;
    heldPieceView.cols = 4;
    previewView = new GridView(460, 33, 90, 150);
    previewView.rows = 10;
    previewView.cols = 6;
  }

  void renderGameState(TetrisGame currentGame) {
    if (currentGame == null) return;

    boardView.rows = currentGame.getGrid().rows;
    boardView.cols = currentGame.getGrid().cols;
    boardView.drawOutline();
    boardView.drawGrid(currentGame.getGrid(), currentGame);
    if (currentGame.getCurrent() != null) {
      boardView.drawShape(currentGame.getCurrent().shape, currentGame.getCurrent().x, currentGame.getCurrent().y);
      boardView.drawShapeOutline(currentGame.getCurrent().shape, currentGame.getCurrent().x, currentGame.getCurrent().final_row);
    }
    
    previewView.drawOutline();
    ArrayList<Shape> nextShapes = currentGame.getNextShapes();
    pushStyle();
    textAlign(CENTER, CENTER);
    for (int i = 0; i < 3; ++i) {
      if (i < nextShapes.size()) {
        Shape next = nextShapes.get(i);
        previewView.drawShape(next, 1, 1 + i * 3 - next.getFirstNonEmptyRow());
      } else {
        text(HARD_MODE_TEXT[i - nextShapes.size()], previewView.x + previewView.width / 2, previewView.y + previewView.height / 6 + previewView.height / 3 * i);
      }
    }
    popStyle();

    heldPieceView.drawOutline();
    if (currentGame.getHeld() != null) {
      Shape heldShape = new Shape(currentGame.getHeld());
      if (currentGame.isHeldUsed()) heldShape.c = color(255, 255, 255);
      heldPieceView.drawShape(heldShape, 0, -heldShape.getFirstNonEmptyRow());
    }
    
    fill(255);
    
    text("HOLD", 10, 28);
    text("NEXT", 460, 28);

    String musicPlaybackMode ="";
    if (audio.isLoopSingleTrackMode()) {
      musicPlaybackMode = "(L1)";
    } else if (audio.isShuffleMode()) {
      musicPlaybackMode = "(SH)";
    }
    text("MUSIC" + musicPlaybackMode + ": " + audio.getCurrentMusic(), 10, 660);
    

    int y = 221;
    for (ScoreValue scoreValue : currentGame.getScoreValues()) {
      text(scoreValue.displayName, 460, y);
      text(scoreValue.toString(), 460, y + 23);
      y += 51;
    }
  }

  public void renderMenu(TetrisGame game, TetrisMenu menu) {
    pushStyle();

    textAlign(CENTER, BOTTOM);
    textSize(18);
    String promptMessage = "";
    if (currentGame == null) promptMessage = "start";
    else if (currentGame.isPaused()) promptMessage = "continue";
    else if (currentGame.isGameOver()) promptMessage = "start a new game";

    text("(press enter to " + promptMessage + ")", width/2, height/2);

    textSize(25);
    if (currentGame == null) {
      text("READY TO PLAY", width/2, height/2 - 35);
    } else if (currentGame.isGameOver()) {
      text("GAME OVER", width/2, height/2 - 85);
      textSize(20);
      text("SCORE: " + currentGame.getScore(), width/2, height/2 - 60);
      text("LINES: " + currentGame.getLines(), width/2, height/2 - 35);
    }

    if (currentGame == null || currentGame.isGameOver()) {
      textSize(18);
      text(menu.getCurrentOptionDisplayName(), width/2, 3*height/4);
      textSize(16);
      text(menu.getCurrentOptionValue(), width/2, 3*height/4 + 40);

      if (menu.canIncreaseCurrentOption()) {
          triangle(width/2, 3*height/4 + 10, width/2 - 5, 3*height/4 + 15, width/2 + 5, 3*height/4 + 15);
      }
      if (menu.canDecreaseCurrentOption()) {
          triangle(width/2, 3*height/4 + 51, width/2 - 5, 3*height/4 + 46, width/2 + 5, 3*height/4 + 46);
      }
    }

    popStyle();
  }
}
