package com.knezevic.edaf.examples.gp.ant;

/**
 * Represents the state of an ant in the simulation.
 */
public class Ant {

    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    private int x;
    private int y;
    private Direction direction;
    private final Grid grid;

    public Ant(Grid grid) {
        this.grid = grid;
        this.x = 0;
        this.y = 0;
        this.direction = Direction.EAST;
    }

    public void move() {
        switch (direction) {
            case NORTH: y = (y - 1 + grid.getHeight()) % grid.getHeight(); break;
            case EAST:  x = (x + 1) % grid.getWidth(); break;
            case SOUTH: y = (y + 1) % grid.getHeight(); break;
            case WEST:  x = (x - 1 + grid.getWidth()) % grid.getWidth(); break;
        }
        grid.eatFood(x, y);
    }

    public void turnLeft() {
        direction = Direction.values()[(direction.ordinal() + 3) % 4];
    }

    public void turnRight() {
        direction = Direction.values()[(direction.ordinal() + 1) % 4];
    }

    public boolean isFoodAhead() {
        int frontX = x;
        int frontY = y;
        switch (direction) {
            case NORTH: frontY = (y - 1 + grid.getHeight()) % grid.getHeight(); break;
            case EAST:  frontX = (x + 1) % grid.getWidth(); break;
            case SOUTH: frontY = (y + 1) % grid.getHeight(); break;
            case WEST:  frontX = (x - 1 + grid.getWidth()) % grid.getWidth(); break;
        }
        return grid.hasFood(frontX, frontY);
    }
}
