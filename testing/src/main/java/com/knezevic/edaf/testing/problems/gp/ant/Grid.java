package com.knezevic.edaf.testing.problems.gp.ant;

public class Grid {
    private final boolean[][] food;
    private final int width;
    private final int height;
    private int foodEaten = 0;

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
                if (x < TRAIL_MAP[y].length() && TRAIL_MAP[y].charAt(x) == '.') {
                    food[x][y] = true;
                }
            }
        }
    }

    public boolean hasFood(int x, int y) { return food[x][y]; }
    public void eatFood(int x, int y) { if (food[x][y]) { food[x][y] = false; foodEaten++; } }
    public int getFoodEaten() { return foodEaten; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}


