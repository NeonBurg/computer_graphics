package net.caustix.computergraphics.shapes;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import net.caustix.computergraphics.android.Vertex;
import net.caustix.computergraphics.android.GFXUtils;
import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;

import static java.lang.Math.abs;

/**
 * Created by Николай on 17.09.2015.
 */

public class Square {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private volatile float xTrans, yTrans, zTrans;
    float mAngle;

    private final int mProgram;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureDataHandle = -1;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;

    // ---------------- МАТРИЦЫ -----------------
    private float[] mTranslationMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    float[] mRotationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];
    private float[] mTempMatrix = new float[16];
    // ------------------------------------------

    protected Vertex vtxVertices;
    protected Vertex vtxTexture;

    // Координаты фигуры
    private float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f }; // top right

    private float textureCoords [] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f };

    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; //Порядок для отрисовки вершин
    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    private float width, height;

    // ---------------- КОНСТРУКТОР для цветного объекта ----------------
    public Square(int pWidth, int pHeight, float[] color, int layer) {

        setCoordSizePx(pWidth, pHeight, MainGLRenderer.setLayer(layer));
        setColor(color);

        vtxVertices = new Vertex(squareCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);

        mProgram = GFXUtils.mColorShaderProgram;
    }

    public Square(float _width, float _height, float[] color, int layer) {

        width = _width;
        height = _height;
        setCoordSize(width, height, MainGLRenderer.setLayer(layer));
        setColor(color);

        vtxVertices = new Vertex(squareCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);

        mProgram = GFXUtils.mColorShaderProgram;
    }


    // -------------- КОНСТРУКТОР для текстурного объекта ---------------
    public Square(int pWidth, int pHeight, int textureResourceId, int layer) {

        setCoordSizePx(pWidth, pHeight, MainGLRenderer.setLayer(layer));

        vtxVertices = new Vertex(squareCoords, drawOrder, GFXUtils.COORDS_PER_VERTEX);
        vtxTexture = new Vertex(textureCoords, GFXUtils.COORDS_PER_TEXTURE);

        mTextureDataHandle = GFXUtils.textures.get(textureResourceId);

        mProgram = GFXUtils.mTextureShaderProgram;
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

        if(mTextureDataHandle != -1)
        {
            //get handle to texture coordinate variable
            mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
            if (mTextureCoordinateHandle == -1) Log.e(TAG, "a_TexCoordinate not found");
            //get handle to shape's texture reference
            mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
            if (mTextureUniformHandle == -1) Log.e(TAG, "u_Texture not found");
        }
    }


    // ------------------------ РИСОВАНИЕ ------------------------
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        if(mTextureDataHandle!=-1)
        {
            //Активируем альфа-канал
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glEnable(GLES20.GL_BLEND);
        }

        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, xTrans, yTrans, zTrans);
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);

        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
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

        if(mTextureDataHandle!=-1)
        {
            // Prepare the texture coordinate data
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, GFXUtils.COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, GFXUtils.textureStride, vtxTexture.vertexBuffer);
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
            // Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
            GLES20.glUniform1i(mTextureUniformHandle, 0);
        }

        // Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vtxVertices.numIndeces,
                GLES20.GL_UNSIGNED_SHORT, vtxVertices.indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if(mTextureDataHandle!=-1)
        {
            GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
            GLES20.glDisable(GLES20.GL_BLEND);
        }
    }
    //--------------------------------------------------------------


    // ----------------- Сеттеры (установка значений)  ------------------
    private void setCoordSizePx(int pWidth, int pHeight, float zLayer) {
        width = MainGLRenderer.convertXpix(pWidth);
        height = MainGLRenderer.convertYpix(pHeight);

        squareCoords = new float[] {-width/2, height/2, zLayer,
                -width/2, -height/2, zLayer,
                width/2, -height/2, zLayer,
                width/2, height/2, zLayer};
    }

    private void setCoordSize(float width, float height, float zLayer) {

        squareCoords = new float[] {-width/2, height/2, zLayer,
                -width/2, -height/2, zLayer,
                width/2, -height/2, zLayer,
                width/2, height/2, zLayer};
    }

    public void setColor(float[] sColor) {
        for(int i=0; i<sColor.length; i++) { color[i] = sColor[i]; }
    }

    public void setTranslate(float x_Trans, float y_Trans) {
        xTrans = x_Trans;
        yTrans = y_Trans;
    }

    public void Translate(float x_Trans, float y_Trans) {
        xTrans += x_Trans;
        yTrans += y_Trans;
    }

    public void setTranslatePx (int x_pTrans, int y_pTrans) {
        xTrans = x_pTrans * MainGLRenderer.koef_x;
        yTrans = y_pTrans * MainGLRenderer.koef_y;
    }

    public void setAngle(float angle) { mAngle = angle; }

    public void setTexture(int textureResourceId) {
        mTextureDataHandle = GFXUtils.textures.get(textureResourceId);
    }
    // ------------------------------------------------------------------


    // ----------------- Геттеры (получение значений)  ------------------
    public float getxTrans() {
        return xTrans;
    }

    public float getyTrans() {
        return yTrans;
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public float getX() { return squareCoords[0]+xTrans; }
    public float getY() { return squareCoords[1]+yTrans; }

    public float getAngle() { return mAngle; }
    // ------------------------------------------------------------------

}

