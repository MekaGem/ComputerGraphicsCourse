package com.github.graphics.labyrinth;

import java.util.ArrayList;
import java.util.Random;

public class Labyrinth {
    private static final float CELL_SIZE = 0.1f;

    private static final int[] dRow = {-1, 0, 1, 0};
    private static final int[] dColumn = {0, 1, 0, -1};

    private static final float[] dx = {0, 1, 1, 0};
    private static final float[] dz = {-1, -1, 0, 0};

    private final int rows;
    private final int columns;
    private final boolean[][] empty;
    private final Random random;

    private int startRow = -1;
    private int startColumn = -1;

    public Labyrinth(int rows, int columns, boolean isRandom) {
        this.rows = rows;
        this.columns = columns;
        if (isRandom) {
            random = new Random(System.currentTimeMillis());
        } else {
            random = new Random(10);
        }
        empty = new boolean[rows][columns];
    }

    public void fillRandomly() {
        int emptyCount = 0;
        for (int row = 1; row < rows - 1; ++row) {
            for (int column = 1; column < columns - 1; ++column) {
                if (random.nextInt(3) != 0) {
                    empty[row][column] = true;
                    ++emptyCount;
                }
            }
        }
        if (emptyCount != 0) {
            int index = random.nextInt(emptyCount);
            for (int row = 1; row < rows - 1; ++row) {
                for (int column = 1; column < columns - 1; ++column) {
                    if (empty[row][column]) {
                        if (index == 0) {
                            startRow = row;
                            startColumn = column;
                        }
                        --index;
                    }

                }
            }
        }
    }

    public ArrayList<Quad> getQuads() {
        ArrayList<Quad> quads = new ArrayList<>();
        for (int row = 1; row < rows - 1; ++row) {
            for (int column = 1; column < columns - 1; ++column) {
                if (empty[row][column]) {
                    quads.add(createFloor(row, column));
                    for (int direction = 0; direction < 4; ++direction) {
                        int nRow = row + dRow[direction];
                        int nColumn = column + dColumn[direction];
                        if (!empty[nRow][nColumn]) {
                            quads.add(createWall(row, column, direction));
                        }
                    }
                }
            }
        }
        return quads;
    }

    private Quad createFloor(int row, int column) {
        return new Quad(
                new Vector3f(column * CELL_SIZE, 0, (row + 1 - rows) * CELL_SIZE),
                new Vector3f(CELL_SIZE, 0, 0),
                new Vector3f(0, 0, -CELL_SIZE),
                (row == startRow && column == startColumn) ? new Color(0, 0, 1) : new Color(0, 1, 0)
        );
    }

    private Quad createWall(int row, int column, int direction) {
        Vector3f origin = new Vector3f(
                (column + dx[direction]) * CELL_SIZE,
                0,
                (row + 1 - rows + dz[direction]) * CELL_SIZE
        );
        int nextDirection = (direction + 1) % 4;
        Vector3f v = new Vector3f(dx[nextDirection], 0, dz[nextDirection]);
        v.minus(new Vector3f(dx[direction], 0, dz[direction]));
        return new Quad(
                origin,
                new Vector3f(0, CELL_SIZE, 0),
                v.scale(CELL_SIZE),
                new Color(1, 1, 0)
        );
    }

    private Quad createRoof(int row, int column) {
        return new Quad(
                new Vector3f(column * CELL_SIZE, CELL_SIZE, (row + 1 - rows) * CELL_SIZE),
                new Vector3f(CELL_SIZE, 0, 0),
                new Vector3f(0, 0, -CELL_SIZE),
                new Color(1, 0, 0)
        );
    }

    public float[] getVertices() {
        ArrayList<Quad> quads = getQuads();
        float[] vertices = new float[quads.size() * Quad.QUAD_SIZE];

        int index = 0;
        for (Quad quad : quads) {
            float[] qv = quad.getVertices();
            System.arraycopy(qv, 0, vertices, index, Quad.QUAD_SIZE);
            index += Quad.QUAD_SIZE;
        }
        return vertices;
    }

    public float[] getColors() {
        ArrayList<Quad> quads = getQuads();
        float[] colors = new float[quads.size() * Quad.QUAD_SIZE];

        int index = 0;
        for (Quad quad : quads) {
            float[] qc = quad.getColors();
            System.arraycopy(qc, 0, colors, index, Quad.QUAD_SIZE);
            index += Quad.QUAD_SIZE;
        }
        return colors;
    }

    public void print() {
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if (empty[row][column]) {
                    System.out.print('X');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
    }

    public float getWidth() {
        return CELL_SIZE * columns;
    }

    public float getHeight() {
        return CELL_SIZE * rows;
    }

    public Vector3f getStartPosition() {
        return new Vector3f(
                (startColumn + 0.5f) * CELL_SIZE,
                0.5f * CELL_SIZE,
                (startRow + 1 - rows - 0.5f) * CELL_SIZE
        );
    }
}
