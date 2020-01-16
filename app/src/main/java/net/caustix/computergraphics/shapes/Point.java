package net.caustix.computergraphics.shapes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import net.caustix.computergraphics.android.Vertex;
import net.caustix.computergraphics.android.GFXUtils;
import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;

/**
 * Created by Николай on 14.10.2015.
 */

public class Point {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private volatile float xTrans, yTrans, zTrans;

    float mAngle;

    float mScale = 2;

    //Размеры экрана
    float half_width, half_height;
    float screenWidth, screenHeight;

    private final int mProgram;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    // ---------------- МАТРИЦЫ -----------------
    float[] mTranslationMatrix = new float[16];
    float[] mScaleMatrix = new float[16];
    float[] mRotationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];
    float[] mTempMatrix = new float[16];
    // ------------------------------------------

    protected Vertex vtxVertices;

    float pointCoords[];
    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    float between_points = 0.01f;

    private final int vertexCount;
    private int lastVertex;

    boolean horizontal;

    float x1move, y1move;

    // ============== * КОНСТРУКТОР Линии из точек * ==============
    public Point(boolean _horizontal) {
        horizontal = _horizontal;

        half_width = MainGLRenderer.convertXpix(MainGLRenderer.half_width);
        half_height = MainGLRenderer.convertYpix(MainGLRenderer.half_height);

        screenHeight = half_height*2;
        screenWidth = half_width*2;

        initCoords();
        vertexCount = pointCoords.length / GFXUtils.COORDS_PER_VERTEX;
        lastVertex = 0;
        vtxVertices = new Vertex(pointCoords, GFXUtils.COORDS_PER_VERTEX);
        mProgram = GFXUtils.mColorShaderProgram;
    }

    // ============== * КОНСТРУКТОР Точек по массиву координат * ==============
    public Point(float pCoords[], float pColor[]) {
        half_width = MainGLRenderer.convertXpix(MainGLRenderer.half_width);
        half_height = MainGLRenderer.convertYpix(MainGLRenderer.half_height);

        screenHeight = half_height*2;
        screenWidth = half_width*2;

        setColor(pColor);
        lastVertex = 0;
        vertexCount = pCoords.length / GFXUtils.COORDS_PER_VERTEX;
        vtxVertices = new Vertex(pCoords, GFXUtils.COORDS_PER_VERTEX);
        mProgram = GFXUtils.mColorShaderProgram;
    }

    private void initCoords() {

        int numPoints = Math.round(screenWidth/between_points); //Нужное количество точек
        pointCoords = new float[numPoints*3];

        float start_left = 0;
        for(int i=0; i<numPoints; i++)
        {
            pointCoords[i*3] = start_left; //x координата
            pointCoords[i*3+1] = 0; //y
            pointCoords[i*3+2] = 0; //z
            start_left += between_points;
        }
    }

    private void getHandlers() {
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        if (mPositionHandle == -1) Log.e(TAG, "vPosition not found");
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        if (mColorHandle == -1) Log.e(TAG, "vColor not found");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        if (mMVPMatrixHandle == -1) Log.e(TAG, "uMVPMatrix not found");
    }

    // ------------------------ РИСОВАНИЕ ------------------------
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, xTrans, yTrans, zTrans);
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);

        //Вращение
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);

        //Масштабирование
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScale, mScale, 0);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mScaleMatrix, 0);

        getHandlers(); //!!! Важно !!!

        // Set color for drawing the shape
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Prepare the shape coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, GFXUtils.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, GFXUtils.vertexStride, vtxVertices.vertexBuffer);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpModifiedMatrix, 0);

        // Enable a handle to the shape vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //GLES20.glDrawElements(GLES20.GL_POINTS, vtxVertices.numIndeces, GLES20.GL_UNSIGNED_SHORT, vtxVertices.indexBuffer);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, Math.abs(lastVertex));

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    //--------------------------------------------------------------


    // ----------------- Сеттеры (установка значений)  ------------------
    public void setColor(float[] sColor) {
        for(int i=0; i<sColor.length; i++) { color[i] = sColor[i]; }
    }

    public void setTranslate(float x_Trans, float y_Trans) {
        xTrans = x_Trans;
        yTrans = y_Trans;
    }

    public void setxTrans(float x_trans) {
        xTrans = x_trans;
    }

    public void setyTrans(float y_trans) {
        yTrans = y_trans;
    }

    public void Translate(float x_Trans, float y_Trans) {
        xTrans += x_Trans;
        yTrans += y_Trans;
    }

    public void Scale(float scale) {
        mScale += scale;
    }

    public void setLastVertex(int last_vert) {
        if(last_vert<pointCoords.length/3)
            lastVertex = last_vert;

        x1move = lastVertex*between_points;
        y1move = lastVertex*between_points;

        if(lastVertex<0) {
            if(horizontal)
                mAngle = 180.0f;
            else mAngle = 180.0f + 90.0f;
        }
        else
        {
            if(horizontal)
                mAngle = 0;
            else mAngle = 90.0f;
        }
    }
    // ------------------------------------------------------------------


    // ----------------- Геттеры (получение значений)  ------------------
    public float getxTrans() { return xTrans; }
    public float getyTrans() {
        return yTrans;
    }

    public float getBetweenDist() { return between_points; }

    public float getX1move() { return x1move; }
    public float getY1move() { return y1move; }
    // ------------------------------------------------------------------

}
