package com.knezevic.edaf.examples.gp.ant;

/**
 * Represents the 2D world for the Santa Fe Ant Trail problem.
 * The grid contains a trail of food pellets.
 */
public class Grid {
    private final boolean[][] food;
    private final int width;
    private final int height;
    private int foodEaten = 0;

    // The standard Santa Fe trail map. All lines must be the same length.
    private static final String[] TRAIL_MAP = {
        "#.##############################",
        "#. . . . . . . . . . . . . . . #",
        "# . ######################### . #",
        "# . # . . . . . . . . . . . # . #",
        "# . # . ### . ########### . # . #",
        "# . # . # . # . # . . . # . # . # ",
        "# . # . # . # . # . ### . # . # ",
        "# . # . # . ##### . # # . # . #",
        "# . # . # . . . . . # # . # . #",
        "# . # . ######### . # # . # . #",
        "# . # . . . . . . . # . . # . #",
        "# . ############# . ####### . #",
        "# . . . . . . . . . . . . . # . #",
        "######################### . ### ",
        "# . . . . . . . . . . . . . . . #",
        "###############################."
    };

    public Grid() {
        this.height = TRAIL_MAP.length;
        this.width = TRAIL_MAP[0].length();
        this.food = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Defensive check to prevent out of bounds
                if (x < TRAIL_MAP[y].length() && TRAIL_MAP[y].charAt(x) == '.') {
                    food[x][y] = true;
                }
            }
        }
    }

    public boolean hasFood(int x, int y) {
        return food[x][y];
    }

    public void eatFood(int x, int y) {
        if (food[x][y]) {
            food[x][y] = false;
            foodEaten++;
        }
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
