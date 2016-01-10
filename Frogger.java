/**
 * A Frogger game created based on Nifty Assignments - General Scrolling Game
 * (http://nifty.stanford.edu/2011/feinberg-generic-scrolling-game/)
 * @author Boris Shou
 */

import java.awt.event.KeyEvent;
import java.util.*;

public class Frogger
{
  
  // set to false to use your code

  
  // Game window should be wider than tall:   H_DIM < W_DIM   
  // (more effectively using space)
  private static final int H_DIM = 5;   // # of cells vertically by default: height of game
  private static final int W_DIM = 10;  // # of cells horizontally by default: width of game
  private static final int U_ROW = 0;
  private static final int U_COL = 0;

  private int uRow_default; // record the start position (row) that the user wishes to be in
  private static final int UCOL_DEFAULT = 0; // the start position is always at the 1st column
  
  private Grid grid;
  private Splash instr;
  private int userRow;
  private int userColumn;
  private int msElapsed;

  private int lives = 1; // A player has one life to begin with
  
  private int pauseTime = 100;

  private static Random rand = new Random();

  private boolean paused = false;
  private boolean dead = false;
  private boolean won = false;

  // All image file names - game elements
  public static final String IMG_USER = "images/user.gif";
  public static final String IMG_LOG = "images/log.gif";
  public static final String IMG_USER_ON_LOG = "images/log_user.gif";
  public static final String IMG_TILE = "images/tile.gif";
  public static final String IMG_LOG_RUBY = "images/log_ruby.gif";
  public static final String IMG_BROKEN_LOG = "images/avoid.gif";
  public static final String IMG_DEAD = "images/dead.gif";
  public static final String IMG_LOG_DEAD = "imagesbroken.gif";

  // All image file names - other
  public static final String IMG_BG = "images/bg.png";
  public static final String[] IMG_INSTRUCTIONS = {
          "images/instr1.png",
          "images/instr2.png",
          "images/instr3.png",
          "images/instr4.png",
          "images/instr5.png"
  };

  // Constructors
  public Frogger()
  {
    this(H_DIM, W_DIM, U_ROW);
  }
  
  public Frogger(int hdim, int wdim, int uRow)
  {
    uRow_default = uRow;
    init(hdim, wdim);
  }

  // Helper method to show slides of instructions and wait for user to press any key to continue
  private void showInstruction(String image) {
    instr = new Splash(image, 850, 450);
    while (!instr.checkKeyPressed()) {
      instr.pause(100);
    }
    instr.close();
  }

  // Helper method to initialize the game
  private void init(int hdim, int wdim)
  {

    // Slides of instructions
    for (String instr : IMG_INSTRUCTIONS) {
      showInstruction(instr);
    }

    grid = new Grid(IMG_BG, hdim, wdim);

    initGameBoard();


  }

  // Helper method to initialize / restart every element on the game board
  private void initGameBoard() {
    userRow = uRow_default;
    userColumn = UCOL_DEFAULT;
    msElapsed = 0;
    updateTitle();
    populateScreen();
    populateEnds();
    grid.setImage(new Location(userRow, 0), IMG_USER);
  }

  // The main function that controls the flow of the game
  public void play()
  {
    while (!won && lives > 0)
    {
      grid.pause(pauseTime);
      //System.out.println("User at (" + userRow + ", " + userColumn + ")"); // Debugging
      handleKeyPress();
      if (!dead && !paused) {
        if (msElapsed % (5 * pauseTime) == 0) {
          scrollUpAndDown();
        }
        updateTitle();
        msElapsed += pauseTime;
      }
    }
  }

  // Helper function that handles all key presses
  public void handleKeyPress()
  {
    int key = grid.checkLastKeyPressed();

    if (key == KeyEvent.VK_Q) {
      System.exit(0);
    } else if (key == KeyEvent.VK_R && dead) { // Press "R" to restart the game; available only when dead
      dead = false;
      clearScreen();
      initGameBoard();
    } else if (key == KeyEvent.VK_UP && !dead && !paused) { // Move up; available only when not dead and not paused
      if (userRow > 0) {
        handleCollision(new Location(userRow-1, userColumn));
        userRow--;
      } else {
        handleCollision(new Location(grid.getNumRows()-1 ,userColumn));
        userRow = grid.getNumRows() - 1;
      }
    } else if (key == KeyEvent.VK_DOWN && !dead && !paused) { // Move down; available only when not dead and not paused
      if (userRow < grid.getNumRows()-1) {
        handleCollision(new Location(userRow+1, userColumn));
        userRow++;
      } else {
        handleCollision(new Location(0, userColumn));
        userRow = 0;
      }
    } else if (key == KeyEvent.VK_LEFT && !dead && !paused) { // Move to the left; available only when not dead and not paused
      handleCollision(new Location(userRow, userColumn-1));
      userColumn--;
    } else if (key == KeyEvent.VK_RIGHT && !dead && !paused) { // Move to the right; available only when not dead and not paused
      handleCollision(new Location(userRow, userColumn+1));
      userColumn++;
    } else if (key == KeyEvent.VK_COMMA && !dead && !paused) { // Slow down the game; available only when not dead and not paused
      if (pauseTime < 200) {
        pauseTime += 20;
        msElapsed = 0;
        System.out.println("You set the speed to " + Math.round(10000.0 / pauseTime) + "%." );
      }
    } else if (key == KeyEvent.VK_PERIOD && !dead && !paused) { // Speed up the game; available only when not dead and not paused
      if (pauseTime > 20) {
        pauseTime -= 20;
        msElapsed = 0;
        System.out.println("You set the speed to " + Math.round(10000.0 / pauseTime) + "%." );
      }
    } else if (key == KeyEvent.VK_P && !dead) { // Pause the game; available only when not dead
      paused = !paused;
      // Print to the console
      if (paused) {
        System.out.println("You paused the game.");
      } else {
        System.out.println("You resumed the game.");
      }
    }
  }

  // Helper method to deal with a user jumping
  public void handleCollision(Location loc)
  {
    Location current = new Location(userRow, userColumn);
    if (loc.getCol() == 0) { // If the location that the user jumps to is at the first column
      grid.setImage(loc, IMG_USER);
      if (userColumn == 0) { // If the user is already at the first column
        grid.setImage(current, IMG_TILE);
      } else { // If the user is jumping from other columns (i.e. columns representing the pond)
        grid.setImage(current, IMG_LOG);
      }
    } else if (loc.getCol() == grid.getNumCols()-1) { // If the location that the user jumps to is at the last column
      grid.setImage(loc, IMG_USER);
      grid.setImage(current, IMG_LOG);
      won = true;
    } else if (grid.getImage(loc) == null) { // If the location that the user jumps to has nothing, i.e. no log, no road
      if (userColumn != 0) {
        grid.setImage(current, IMG_LOG);
      } else {
        grid.setImage(current, IMG_TILE);
      }
      grid.setImage(loc, IMG_DEAD);
      dead = true;
      lives--;
      updateTitle();
    } else if (grid.getImage(loc) == IMG_BROKEN_LOG) { // If the location that the user jumps to has a broken log
      if (userColumn != 0) {
        grid.setImage(current, IMG_LOG);
      } else {
        grid.setImage(current, IMG_TILE);
      }
      grid.setImage(loc, IMG_LOG_DEAD);
      dead = true;
      lives--;
      updateTitle();
    } else if (grid.getImage(loc).equals(IMG_LOG)) { // If the user is jumping onto a log
      grid.setImage(loc, IMG_USER_ON_LOG);
      if (userColumn == 0) {
        grid.setImage(current, IMG_TILE);
      } else {
        grid.setImage(current, IMG_LOG);
      }
    } else if (grid.getImage(loc).equals(IMG_LOG_RUBY)) { // If the user is jumping onto a log with a ruby
      grid.setImage(loc, IMG_USER_ON_LOG);
      if (userColumn == 0) {
        grid.setImage(current, IMG_TILE);
      } else {
        grid.setImage(current, IMG_LOG);
      }
      lives++;
    }


  }

  // Helper method to restart the game by clearing the screen
  public void clearScreen() {
    for (int i = 0; i < grid.getNumRows(); i++) {
      for (int j = 1; j < grid.getNumCols()-1; j++) {
        Location current = new Location(i, j);
        grid.setImage(current, null);
      }
    }
  }

  // Helper method to start/restart the game by filling the pond with logs at random positions
  public void populateScreen()
  {

    // Fill half of a column with logs first
    for (int j = 1; j < grid.getNumCols()-1; j++) {
      grid.setImage((new Location(0 ,j)), IMG_LOG); // Ensure that there is at least one log in every column
      for (int i = 1; i < grid.getNumRows(); i++) {
        int decision = rand.nextInt(2); // Decide whether to put another log in every other position in the column - 50% chance
        if (decision == 0) {
          grid.setImage((new Location(i, j)), IMG_LOG);
        }
      }

    }

    // Fill in a fifth of a column with broken logs
    int broken = grid.getNumRows() / 5;
    for (int j = 1; j < grid.getNumCols()-1; j++) {
      for (int k = 0; k < broken; k++) {
        int i = rand.nextInt(grid.getNumRows());
        grid.setImage((new Location(i, j)), IMG_BROKEN_LOG);
      }
    }

    // Put in a ruby on a random log
    int i = rand.nextInt(grid.getNumRows());
    int j = rand.nextInt(grid.getNumCols()-2) + 1;
    grid.setImage((new Location(i, j)), IMG_LOG_RUBY);

  }

  // Helper method to fill in tiles/roads for the first and the last columns
  public void populateEnds()
  {
    int last = grid.getNumCols() - 1;
    for (int i = 0; i < grid.getNumRows(); i++) {
      grid.setImage((new Location(i, 0)), IMG_TILE);
      grid.setImage((new Location(i, last)), IMG_TILE);
    }
  }

  // Helper method to scroll the logs up and down AND at the same time update user location
  public void scrollUpAndDown()
  {
    for (int j = 1; j < grid.getNumCols()-1; j++) {
      if (j % 2 == 1){ // Move everything in the odd columns down one cell

        // Store the bottom image somewhere else
        Location bottom = new Location(grid.getNumRows()-1, j);
        String bottomImage = grid.getImage(bottom);
        grid.setImage(bottom, null);

        for (int i = grid.getNumRows()-2; i >= 0; i--) {
          Location current = new Location(i, j);
          if (grid.getImage(current) != null) {
            Location moveTo = new Location(i+1, j);
            grid.setImage(moveTo, grid.getImage(current));
            grid.setImage(current, null);
            //System.out.println("Move from " + current + " to " + moveTo);
          }
        }

        // Move the bottom image to the top
        if (bottomImage != null) {
          grid.setImage(new Location(0, j), bottomImage);
        }
      }


      else { // Move everything in the even columns up one cell

        // Store the top image somewhere else
        Location top = new Location(0, j);
        String topImage = grid.getImage(top);
        grid.setImage(top, null);

        for (int i = 1; i < grid.getNumRows(); i++) {
          Location current = new Location(i, j);
          if (grid.getImage(current) != null) {
            Location moveTo = new Location(i-1, j);
            grid.setImage(moveTo, grid.getImage(current));
            grid.setImage(current, null);
          }
        }

        // Move the top image to the bottom
        if (topImage != null) {
          grid.setImage(new Location(grid.getNumRows()-1, j), topImage);
        }

      }

    }

    // Update user location
    // ** only when user is not in the first or the last column
    if (userColumn != 0 && userColumn != grid.getNumCols()-1) {
      if (userColumn % 2 == 1) {
        if (userRow < grid.getNumRows() - 1) {
          userRow++;
        } else {
          userRow = 0;
        }
      } else {
        if (userRow > 0) {
          userRow--;
        } else {
          userRow = grid.getNumRows() - 1;
        }
      }
    }
  }


  // Helper method to update the title of the grid
  public void updateTitle() {
    String lifePlural;
    String title;
    if (lives < 2) {
      lifePlural = "life";
    } else {
      lifePlural = "lives";
    }
    title = "Frogger:  You have " + lives + " " + lifePlural + " left.";

    if (dead) {
      title += "  Press \"R\" to restart.";
    }

    if (lives <= 0) {
      title = "Frogger:  YOU LOST";
    }

    if (won) {
      title = "Frogger:  YOU WON!!!";
    }

    grid.setTitle(title);
  }
  
  public static void main(String[] args)
  {
    Frogger game = new Frogger(9, 12, 4);
    game.play();
  }
}