package net.caustix.computergraphics;

import net.caustix.computergraphics.shapes.Square;

/**
 *
 *  ����� ������
 * Created by ������� on 25.09.2015.
 */

public class Button {

    private volatile float xTrans, yTrans;

    private int mTexture = -1; //�������� �������� ������
    private int mPressedTexture = -1; //�������� ������� ������

    private float butt_width, butt_height; //������ � ������ ������
    private float x1, y1; //���������� ������ ������� (������� �����)

    Square mButtSquare;
    Square upperPicture;
    boolean showUpPic = false;

    // ----------========== ����������� =========----------
    public Button(int pWidth, int pHeight, int texture, int texturePressed, int layer) {

        mButtSquare = new Square(pWidth, pHeight, texture, layer);
        mTexture = texture;
        mPressedTexture = texturePressed;

        butt_width = mButtSquare.getWidth();
        butt_height = mButtSquare.getHeight();
        x1 = mButtSquare.getX();
        y1 = mButtSquare.getY();
    }

    public void draw(float[] mvpMatrix) {
        mButtSquare.draw(mvpMatrix);

        if(showUpPic) {
            upperPicture.draw(mvpMatrix);
        }
    }

    //�������� ������� ������
    public boolean checkTouch(float xTouch, float yTouch) {
        if(xTouch >= x1 && xTouch <= x1+butt_width && yTouch <= y1 && yTouch >= y1-butt_height)
        {
            mButtSquare.setTexture(mPressedTexture);
            return true;
        }
        else
        {
            mButtSquare.setTexture(mTexture);
            return false;
        }
    }

    //�������� ������� ������
    public boolean checkTouch2(float xTouch, float yTouch) {
        if(xTouch >= x1 && xTouch <= x1+butt_width && yTouch <= y1 && yTouch >= y1-butt_height)
        {
            mButtSquare.setTexture(mPressedTexture);
            return true;
        }
        return false;
    }

    public void release() {
        mButtSquare.setTexture(mTexture);
    }

    public void select() { mButtSquare.setTexture(mPressedTexture); }

    public void setTranslate(float x_Trans, float y_Trans) {
        xTrans = x_Trans;
        yTrans = y_Trans;
        mButtSquare.setTranslate(xTrans, yTrans);
        if(showUpPic) upperPicture.setTranslate(xTrans, yTrans);
        x1 = mButtSquare.getX();
        y1 = mButtSquare.getY();
    }

    public float getWidth() { return butt_width; }
    public float getHeight() { return butt_height; }

    public float getyTrans() { return yTrans; }

    public void addUpperPicture(int pWidth, int pHeight, int textureResourceId) {
        upperPicture = new Square(pWidth, pHeight, textureResourceId, 2);
        upperPicture.setTranslate(xTrans, yTrans);
        showUpPic = true;
    }

}
