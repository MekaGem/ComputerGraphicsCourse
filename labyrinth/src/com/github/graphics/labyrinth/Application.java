package com.github.graphics.labyrinth;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GLContext;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application {
    private static final String LIBRARIES_PATH = "./lib/native";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;

    private long window;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    private int rotationDirction;
    private int movementDirection;

    private FloatBuffer vertexData;
    private FloatBuffer colorData;

    private Vector3f position;
    private float angle;

    public static void main(String[] args) {
        System.setProperty("java.library.path", LIBRARIES_PATH);
        new Application().run();
    }

    public void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.release();
        } finally {
            glfwTerminate();
            errorCallback.release();
        }
    }

    private void init() {
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        if (glfwInit() != GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Labyrinth", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, GL_TRUE);
                }
                if (key == GLFW_KEY_LEFT) {
                    if (action == GLFW_PRESS) {
                        leftPressed = true;
                    } else if (action == GLFW_RELEASE){
                        leftPressed = false;
                    }
                }
                if (key == GLFW_KEY_RIGHT) {
                    if (action == GLFW_PRESS) {
                        rightPressed = true;
                    } else if (action == GLFW_RELEASE){
                        rightPressed = false;
                    }
                }
                if (key == GLFW_KEY_UP) {
                    if (action == GLFW_PRESS) {
                        upPressed = true;
                    } else if (action == GLFW_RELEASE){
                        upPressed = false;
                    }
                }
                if (key == GLFW_KEY_DOWN) {
                    if (action == GLFW_PRESS) {
                        downPressed = true;
                    } else if (action == GLFW_RELEASE){
                        downPressed = false;
                    }
                }
                rotationDirction = 0;
                if (leftPressed) {
                    rotationDirction += 1;
                }
                if (rightPressed) {
                    rotationDirction -= 1;
                }
                movementDirection = 0;
                if (upPressed) {
                    movementDirection += 1;
                }
                if (downPressed) {
                    movementDirection -= 1;
                }
            }
        });

        ByteBuffer videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (GLFWvidmode.width(videoMode) - WINDOW_WIDTH) / 2,
                (GLFWvidmode.height(videoMode) - WINDOW_HEIGHT) / 2
        );

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        Labyrinth labyrinth = new Labyrinth(10, 10);
        labyrinth.fillRandomly();
        labyrinth.print();

        float[] vertices = labyrinth.getVertices();
        float[] colors = labyrinth.getColors();

        angle = 0;
        position = labyrinth.getStartPosition();

        System.err.println(position.x + " : " + position.z);

        vertexData = BufferUtils.createFloatBuffer(vertices.length);
        vertexData.put(vertices);
        vertexData.flip();

        colorData = BufferUtils.createFloatBuffer(colors.length);
        colorData.put(colors);
        colorData.flip();
    }

    private void loop() {
        GLContext.createFromCurrent();

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float ratio = WINDOW_WIDTH / (float)WINDOW_HEIGHT;
        float sz = 0.03f;
        glFrustum(-sz * ratio, sz * ratio, -sz, sz, 0.03f, 2f);

        glEnable(GL_DEPTH_TEST);

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboColorHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
        glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        double prevTime = glfwGetTime();
        while (glfwWindowShouldClose(window) == GL_FALSE) {
            double time = glfwGetTime();
            float delta = (float)(time - prevTime);
            angle += -90 * rotationDirction * delta;
            position.plus(
                    0.05f * movementDirection * delta * (float)Math.cos(Math.toRadians(angle - 90)),
                    0,
                    0.05f * movementDirection * delta * (float)Math.sin(Math.toRadians(angle - 90))
            );

            prevTime = time;

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glRotated(angle, 0, 1, 0);
            glTranslatef(-position.x, -position.y, -position.z);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0l);

            glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
            glColorPointer(3, GL_FLOAT, 0, 0l);

            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_COLOR_ARRAY);

            glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
            glDrawArrays(GL_TRIANGLES, 0, vertexData.limit());

//            glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
//            glColor3f(0, 0, 0);
//            glLineWidth(3);
//            glDrawArrays(GL_TRIANGLES, 0, vertexData.limit());

            glDisableClientState(GL_COLOR_ARRAY);
            glDisableClientState(GL_VERTEX_ARRAY);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteBuffers(vboVertexHandle);
        glDeleteBuffers(vboColorHandle);
    }

//    private FloatBuffer initVertexBuffer() {
//        FloatBuffer buffer = BufferUtils.createFloatBuffer(3 * 3 * 2 * 6);
//        float x = -0.5f;
//        float y = -0.5f;
//        float z = -0.5f;
//        float w = 1.0f;
//        float h = 1.0f;
//        float d = 1.0f;
//        float[][] points = new float[8][3];
//
//        points[0][0] = x;
//        points[0][1] = y;
//        points[0][2] = z;
//
//        points[1][0] = x;
//        points[1][1] = y + h;
//        points[1][2] = z;
//
//        points[2][0] = x + w;
//        points[2][1] = y + h;
//        points[2][2] = z;
//
//        points[3][0] = x + w;
//        points[3][1] = y;
//        points[3][2] = z;
//
//        points[4][0] = x;
//        points[4][1] = y;
//        points[4][2] = z + d;
//
//        points[5][0] = x;
//        points[5][1] = y + h;
//        points[5][2] = z + d;
//
//        points[6][0] = x + w;
//        points[6][1] = y + h;
//        points[6][2] = z + d;
//
//        points[7][0] = x + w;
//        points[7][1] = y;
//        points[7][2] = z + d;
//
//        buffer.put(points[0]).put(points[2]).put(points[1]);
//        buffer.put(points[0]).put(points[3]).put(points[2]);
//
//        buffer.put(points[0]).put(points[1]).put(points[5]);
//        buffer.put(points[0]).put(points[5]).put(points[4]);
//
//        buffer.put(points[3]).put(points[6]).put(points[2]);
//        buffer.put(points[3]).put(points[7]).put(points[6]);
//
//        buffer.put(points[1]).put(points[6]).put(points[5]);
//        buffer.put(points[1]).put(points[2]).put(points[6]);
//
//        buffer.put(points[0]).put(points[7]).put(points[4]);
//        buffer.put(points[0]).put(points[3]).put(points[7]);
//
//        buffer.put(points[4]).put(points[5]).put(points[6]);
//        buffer.put(points[4]).put(points[6]).put(points[7]);
//
//        buffer.flip();
//
//        return buffer;
//    }
}
