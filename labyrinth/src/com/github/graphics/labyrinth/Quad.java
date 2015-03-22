package com.github.graphics.labyrinth;

public class Quad {
    public static final int QUAD_SIZE = 3 * 3 * 2;

    private final Vector3f[] points = new Vector3f[4];
    private final Color color;

    public Quad(Vector3f origin, Vector3f v1, Vector3f v2, Color color) {
        this.color = color;
        points[0] = new Vector3f(origin);
        points[1] = new Vector3f(origin).plus(v1);
        points[2] = new Vector3f(origin).plus(v1).plus(v2);
        points[3] = new Vector3f(origin).plus(v2);
    }

    public float[] getVertices() {
        float[] vertices = new float[QUAD_SIZE];
        for (int index = 0; index < 2; ++index) {
            for (int i = 0; i < 3; ++i) {
                int point = (index * 2 + i) % 4;
                int startIndex = index * 9 + i * 3;
                vertices[startIndex] = points[point].x;
                vertices[startIndex + 1] = points[point].y;
                vertices[startIndex + 2] = points[point].z;
            }
        }
        return vertices;
    }

    public float[] getColors() {
        float[] colors = new float[QUAD_SIZE];
        for (int index = 0; index < QUAD_SIZE; index += 3) {
            colors[index] = color.components[0];
            colors[index + 1] = color.components[1];
            colors[index + 2] = color.components[2];
        }
        return colors;
    }
}
