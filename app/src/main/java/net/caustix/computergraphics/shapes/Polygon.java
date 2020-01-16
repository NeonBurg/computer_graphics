package net.caustix.computergraphics.shapes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import net.caustix.computergraphics.android.Vertex;
import net.caustix.computergraphics.android.GFXUtils;
import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;

/**
 * Created by Николай on 30.09.2015.
 */

public class Polygon {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private volatile float xTrans, yTrans, zTrans;
    float mAngle=0;
    float mAngleTrans = 0;
    float mScaleX=1, mScaleY=1;
    float mScaleTrans=1.0f;
    int mLayer=0;

    float height;

    private final int mProgram;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    // ---------------- МАТРИЦЫ -----------------
    private float[] mTranslationMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    float[] mRotationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    // ------------------------------------------

    protected Vertex vtxVertices;

    float polygonCoords[];
    short drawOrder[];

    //Число вершин
    public int numVertices;

    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    float xCR = 0;
    float yCR = 0;

    float xLocTrans=0, yLocTrans=0;

    // ============== * КОНСТРУКТОР Полигона * ==============
    public Polygon(float scale, float[] polygon_Coords, short[] draw_Order, float[] pColor) {

        setColor(pColor); //Установим цвет полигону
        if(scale!=0) { mScaleX = scale; mScaleY = scale; }
        //Выделим место в массиве вершин
        polygonCoords = new float[polygon_Coords.length];
        drawOrder = new short[draw_Order.length];

        //Проинициализируем массив с координатами вершин
        for(int i=0; i<polygon_Coords.length; i++) { polygonCoords[i] = polygon_Coords[i]; }

        //И массив с порядком обхода вершин
        for(int i=0; i<draw_Order.length; i++) {drawOrder[i] = draw_Order[i]; }

        height = Math.abs(polygon_Coords[1] - polygon_Coords[4]);

        //Создаем буфер для координат
        vtxVertices = new Vertex(polygonCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);

        numVertices = vtxVertices.numVertices;

        //Получим ссылку на программу с шейдерами
        mProgram = GFXUtils.mColorShaderProgram;

    }

    public Polygon(Polygon polygonCopy) {
        polygonCoords = new float[polygonCopy.polygonCoords.length];
        drawOrder = new short[polygonCopy.drawOrder.length];

        //Проинициализируем массив с координатами вершин
        for(int i=0; i<polygonCopy.polygonCoords.length; i++) { polygonCoords[i] = polygonCopy.polygonCoords[i]; }

        //И массив с порядком обхода вершин
        for(int i=0; i<polygonCopy.drawOrder.length; i++) {drawOrder[i] = polygonCopy.drawOrder[i]; }

        height = polygonCopy.height;
        vtxVertices = new Vertex(polygonCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);
        numVertices = vtxVertices.numVertices;
        mProgram = GFXUtils.mColorShaderProgram;

        xTrans = polygonCopy.xTrans;
        yTrans = polygonCopy.xTrans;
        xLocTrans = polygonCopy.xLocTrans;
        yLocTrans = polygonCopy.yLocTrans;
        mScaleX = polygonCopy.mScaleX;
        xCR = polygonCopy.xCR;
        yCR = polygonCopy.yCR;
        mAngle = polygonCopy.mAngle;
        mAngleTrans = polygonCopy.mAngleTrans;
        mScaleTrans = polygonCopy.mScaleTrans;
        setColor(polygonCopy.color);
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

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vtxVertices.numIndeces,
                GLES20.GL_UNSIGNED_SHORT, vtxVertices.indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }
    //--------------------------------------------------------------


    // ----------------- Сеттеры (установка значений)  ------------------
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

    public void setColor(float[] sColor) {
        for(int i=0; i<sColor.length; i++) { color[i] = sColor[i]; }
    }

    public void setLayer(int layer) { mLayer = layer; zTrans = MainGLRenderer.setLayer(layer); }

    public void setXCR(float x_CR) { xCR=x_CR; }
    public void setYCR(float y_CR) { yCR=y_CR; }
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

    public float getAngle() { return mAngle; }

    public float getX(int numCoord) {

        float xTransformed = (float) (polygonCoords[numCoord*3]* Math.cos(Math.toRadians(mAngle)) - polygonCoords[numCoord*3+1]* Math.sin(Math.toRadians(mAngle)) );
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
        float yTransformed = (float) (polygonCoords[numCoord*3]* Math.sin(Math.toRadians(mAngle)) + polygonCoords[numCoord*3+1]* Math.cos(Math.toRadians(mAngle)) );

        yTransformed *= mScaleX;

        //xTransformed += xLocTrans;
        yTransformed += yCR;
        yTransformed += yLocTrans;

        //yTransformed = fromGlobalToLocal_y(xTransformed, yTransformed, mGlobalAngle);

        return yTransformed;
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

    /*float fromLocalToGlobal_x(float locX, float locY) {

        locX = (float) (locX* Math.cos(Math.toRadians((360 - mGlobalAngle))) - locY* Math.sin(Math.toRadians((360 - mGlobalAngle))) );

        locX /= mScaleX;

        return locX;
    }

    float fromLocalToGlobal_y(float locX, float locY) {

        locY = (float) (locX* Math.sin(Math.toRadians((360-mGlobalAngle))) + locY* Math.cos(Math.toRadians((360-mGlobalAngle))) );

        locY /= mScaleX;

        return locY;
    }*/

}