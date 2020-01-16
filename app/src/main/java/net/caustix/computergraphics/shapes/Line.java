package net.caustix.computergraphics.shapes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import net.caustix.computergraphics.android.Vertex;
import net.caustix.computergraphics.android.GFXUtils;
import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;

/**
 * Created by Николай on 17.09.2015.
 */

public class Line {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    boolean lineLoop;

    private volatile float xTrans, yTrans, zTrans;
    float mAngle=0;
    float mAngleTrans = 0;
    float mScaleX=1, mScaleY=1;
    float mScaleTrans=1.0f;
    int mLayer=0;

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

    // Координаты линии
    float lineCoords[] = {
            -0.5f, 0.0f, 0.0f,
            0.5f, 0.0f, 0.0f };

    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };


    private final int vertexCount = lineCoords.length / GFXUtils.COORDS_PER_VERTEX;

    float xCR = 0;
    float yCR = 0;

    float xLocTrans=0, yLocTrans=0;

    float lineWidth = 1;

    // -------------- КОНСТРУКТОР горизотнальной линии ---------------
    public Line(float scale, float[] color) {
        setColor(color);
        if(scale!=0) { mScaleX = scale; mScaleY = scale; }
        vtxVertices = new Vertex(lineCoords, GFXUtils.COORDS_PER_VERTEX);
        mProgram = GFXUtils.mColorShaderProgram;
        lineLoop = false;
    }
    // -------------- КОНСТРУКТОР горизотнальной линии ---------------
    public Line(float scale, float[] color, float line_width) {
        lineWidth = line_width;
        setColor(color);
        if(scale!=0) { mScaleX = scale; mScaleY = scale; }
        vtxVertices = new Vertex(lineCoords, GFXUtils.COORDS_PER_VERTEX);
        mProgram = GFXUtils.mColorShaderProgram;
        lineLoop = false;
    }
    // -------------- КОНСТРУКТОР ломанной линии по массиву координат ---------------
    public Line(float[] nLineCoords, short[] drawOrder, float[] color) {

        vtxVertices = new Vertex(nLineCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);
        setColor(color);
        mProgram = GFXUtils.mColorShaderProgram;
        lineLoop = true;
    }

    public Line(Line lineCopy) {

        vtxVertices = new Vertex(lineCoords, GFXUtils.COORDS_PER_VERTEX);
        mProgram = GFXUtils.mColorShaderProgram;
        lineLoop = false;

        xTrans = lineCopy.xTrans;
        yTrans = lineCopy.xTrans;
        xLocTrans = lineCopy.xLocTrans;
        yLocTrans = lineCopy.yLocTrans;
        mScaleX = lineCopy.mScaleX;
        xCR = lineCopy.xCR;
        yCR = lineCopy.yCR;
        mAngle = lineCopy.mAngle;
        mAngleTrans = lineCopy.mAngleTrans;
        mScaleTrans = lineCopy.mScaleTrans;
        setColor(lineCopy.color);
        lineWidth = lineCopy.lineWidth;

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

        //Перемещение 1
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, xCR, yCR, 0);
        //mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);

        //Перемещение 1
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, xLocTrans, yLocTrans, 0);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mTranslationMatrix, 0);


        //Масштабирование
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.scaleM(mScaleMatrix, 0, mScaleX, mScaleX, 0);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mScaleMatrix, 0);

        //Вращение локальное
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);

        getHandlers(); //!!! Важно !!!

        // Set color for drawing the shape
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Prepare the shape coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, GFXUtils.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, GFXUtils.vertexStride, vtxVertices.vertexBuffer);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpModifiedMatrix, 0);

        // Enable a handle to the shape vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glLineWidth(lineWidth);

        // Draw the shape
        //GLES20.glDrawElements(GLES20.GL_LINE_LOOP, vtxVertices.numIndeces,
        //GLES20.GL_UNSIGNED_SHORT, vtxVertices.indexBuffer);
        if(lineLoop) {
            GLES20.glDrawElements(GLES20.GL_LINE_LOOP, vtxVertices.numIndeces, GLES20.GL_UNSIGNED_SHORT, vtxVertices.indexBuffer);
        }
        else {
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    //--------------------------------------------------------------


    // ----------------- Сеттеры (установка значений)  ------------------
    private void setCoordSize(int pSize, float zLayer) {
        float size = pSize/2* MainGLRenderer.koef_x;

        lineCoords = new float[] {-size/2, 0.0f, zLayer,
                size/2, 0.0f, zLayer };
    }

    public void setColor(float[] sColor) {
        for(int i=0; i<sColor.length; i++) { color[i] = sColor[i]; }
    }

    public void setTranslate(float x_Trans, float y_Trans) {
        xLocTrans = x_Trans;
        yLocTrans = y_Trans;
        xTrans = xLocTrans;
        yTrans = yLocTrans;
    }

    public void Translate(float x_Trans, float y_Trans) {
        if(mAngleTrans!=0) mAngleTrans = 0;
        xLocTrans += x_Trans;
        yLocTrans += y_Trans;
        xTrans = xLocTrans;
        yTrans = yLocTrans;
    }

    public void setTranslatePx (int x_pTrans, int y_pTrans) {
        xLocTrans = MainGLRenderer.convertXpix(x_pTrans);
        yLocTrans = MainGLRenderer.convertXpix(y_pTrans);
        xTrans = xLocTrans;
        yTrans = yLocTrans;
    }

    //Поворот
    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void Rotate(float angle, float xCenterRot, float yCenterRot)
    {
        if (mAngleTrans >= 360) mAngleTrans = 0;
        if (mAngle >= 360) mAngle = 0;

        if(xCR!=xCenterRot && yCR!=yCenterRot || mScaleTrans != 1.0f) {
            mAngleTrans = 0;
            mScaleTrans = 1.0f;
            xTrans = xLocTrans+xCR;
            yTrans = yLocTrans+yCR;
        }

        mAngleTrans += angle;
        mAngle += angle;

        xCR = xCenterRot;
        yCR = yCenterRot;

        xLocTrans = fromGlobalToLocal_x(xTrans - xCR, yTrans - yCR, mAngleTrans);
        yLocTrans = fromGlobalToLocal_y(xTrans-xCR, yTrans-yCR, mAngleTrans);

    }

    public void setScale(float scale) { mScaleX = scale; mScaleY = scale; }

    public void Scale(float scale_x, float xCenterScale, float yCenterScale) {

        if(xCR!=xCenterScale && yCR!=yCenterScale || mAngleTrans != 0) {
            mScaleTrans = 1.0f;
            mAngleTrans = 0;
            xTrans = xLocTrans+xCR;
            yTrans = yLocTrans+yCR;
        }

        xCR = xCenterScale;
        yCR = yCenterScale;

        mScaleX += scale_x;
        mScaleTrans += scale_x;

        xLocTrans = (xTrans-xCR)*mScaleTrans;
        yLocTrans = (yTrans-yCR)*mScaleTrans;
    }

    public void setLayer(int layer) { mLayer = layer; zTrans = MainGLRenderer.setLayer(layer); }

    public void setLineWidth(float line_width) {
        lineWidth = line_width;
    }
    // ------------------------------------------------------------------


    // ----------------- Геттеры (получение значений)  ------------------
    public float getxLocalTrans() {
        return xLocTrans;
    }
    public float getyLocalTrans() {
        return yLocTrans;
    }

    public float getxTrans() {
        return xTrans;
    }
    public float getyTrans() {
        return yTrans;
    }

    public float getX(int numCoord) {

        float xTransformed = (float) (lineCoords[numCoord*3]* Math.cos(Math.toRadians(mAngle)) - lineCoords[numCoord*3+1]* Math.sin(Math.toRadians(mAngle)) );
        //float yTransformed = (float) (polygonCoords[numCoord*3]* Math.sin(Math.toRadians(mAngle)) + polygonCoords[numCoord*3+1]* Math.cos(Math.toRadians(mAngle)));

        xTransformed *= mScaleX;

        xTransformed += xCR;
        xTransformed += xLocTrans;
        //yTransformed += yLocTrans;

        //xTransformed = fromGlobalToLocal_x(xTransformed, yTransformed, mGlobalAngle);

        return xTransformed;
    }

    public float getY(int numCoord) {

        //float xTransformed = (float) (polygonCoords[numCoord*3]* Math.cos(Math.toRadians(mAngle)) - polygonCoords[numCoord*3+1]* Math.sin(Math.toRadians(mAngle)) );
        float yTransformed = (float) (lineCoords[numCoord*3]* Math.sin(Math.toRadians(mAngle)) + lineCoords[numCoord*3+1]* Math.cos(Math.toRadians(mAngle)) );

        yTransformed *= mScaleX;

        //xTransformed += xLocTrans;
        yTransformed += yCR;
        yTransformed += yLocTrans;

        //yTransformed = fromGlobalToLocal_y(xTransformed, yTransformed, mGlobalAngle);

        return yTransformed;
    }

    public float getAngle() { return mAngle; }

    public float getColor(int index) {
        if(index<4 && index>=0) return color[index];
        else return 0;
    }

    public int getVertices() {
        return vtxVertices.numVertices;
    }

    public int getLayer() { return mLayer; }
    // ------------------------------------------------------------------


    // ------------------- Остальные методы  ---------------------

    float fromGlobalToLocal_x(float gX, float gY, float angle) {

        //gX *= mScaleX;
        //gY *= mScaleX;
        gX = (float) (gX* Math.cos(Math.toRadians(angle)) - gY* Math.sin(Math.toRadians(angle)) );
        return gX;
    }

    float fromGlobalToLocal_y(float gX, float gY, float angle) {

        //gX *= mScaleX;
        //gY *= mScaleX;
        gY = (float) (gX* Math.sin(Math.toRadians(angle)) + gY* Math.cos(Math.toRadians(angle)) );
        return gY;
    }

}
