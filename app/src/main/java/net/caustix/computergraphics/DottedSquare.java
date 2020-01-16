package net.caustix.computergraphics;

import android.opengl.Matrix;

import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.shapes.Point;

import java.util.ArrayList;

/**
 * Created by Николай on 15.10.2015.
 */

public class DottedSquare {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private volatile float xTrans, yTrans, zTrans;
    float mAngle;
    float mScaleX=1;

    // ---------------- МАТРИЦЫ -----------------
    float[] mTranslationMatrix = new float[16];
    float[] mRotationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];
    float[] mTempMatrix = new float[16];
    // ------------------------------------------

    float[] x_dotted = new float[4];
    float[] y_dotted = new float[4];

    float dist_between;

    Point mDotted_line;
    ArrayList<Point> mDottedList = new ArrayList<Point>();
    float x0center, y0center;

    float mGlobalAngle = 0;
    float mLocalAngle = 0;

    // ============== * КОНСТРУКТОР Пунктирного прямоугольника * ==============
    public DottedSquare() {

        mDotted_line = new Point(true);   mDottedList.add(mDotted_line);
        mDotted_line = new Point(true);   mDottedList.add(mDotted_line);
        mDotted_line = new Point(false);  mDottedList.add(mDotted_line);
        mDotted_line = new Point(false);  mDottedList.add(mDotted_line);

        dist_between = mDotted_line.getBetweenDist();

        x_dotted[0] = 0;
        y_dotted[0] = 0;
    }


    // ------------------------ РИСОВАНИЕ ------------------------
    public void draw(float[] mvpMatrix) {

        //Вращение глобальное
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, mGlobalAngle, 0, 0, 1.0f);
        //mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, mRotationMatrix, 0);

        //Перемещение
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, x0center, y0center, zTrans);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mTranslationMatrix, 0);

        //Вращение локальное
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.rotateM(mRotationMatrix, 0, mLocalAngle, 0, 0, 1.0f);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mRotationMatrix, 0);

        //Перемещение
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, -x0center, -y0center, zTrans);
        mTempMatrix = mvpModifiedMatrix.clone();
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mTempMatrix, 0, mTranslationMatrix, 0);

        for(int i=0; i<mDottedList.size(); i++) {
            mDottedList.get(i).draw(mvpModifiedMatrix);
        }
    }
    //--------------------------------------------------------------


    // --------- * Увлеличиваем площадь очерчиваемую пунктиром * ---------
    public void growSquare(float xTouch, float yTouch) {
        if(x_dotted[0]==0 && y_dotted[0] == 0)
        {
            x_dotted[0] = xTouch;
            y_dotted[0] = yTouch;
            x_dotted[1] = x_dotted[0];
            y_dotted[3] = y_dotted[0];

            x_dotted[2] = x_dotted[0];
            y_dotted[2] = y_dotted[0];

            y_dotted[1] = y_dotted[2];
            x_dotted[3] = x_dotted[2];

            for(int i=0; i<mDottedList.size(); i++) {
                mDottedList.get(i).setTranslate(x_dotted[0], y_dotted[0]);
            }
        }
        else
        {
            for(int i=0; i<mDottedList.size(); i++) {
                if(i<2)
                {
                    mDottedList.get(i).setLastVertex(Math.round((xTouch - x_dotted[0]) / dist_between)/2);
                }
                else
                {
                    mDottedList.get(i).setLastVertex(Math.round((yTouch - y_dotted[0])/dist_between)/2);
                }
            }

            x0center = x_dotted[0]+(xTouch - x_dotted[0])/2;
            y0center = y_dotted[0]+(yTouch - y_dotted[0])/2;

            x_dotted[2] = x_dotted[0] + mDottedList.get(1).getX1move() * 2;
            y_dotted[2] = y_dotted[0] + mDottedList.get(3).getY1move() * 2;
            y_dotted[1] = y_dotted[2];
            x_dotted[3] = x_dotted[2];


            mDottedList.get(1).setyTrans(y_dotted[2]);
            mDottedList.get(3).setxTrans(x_dotted[2]);
            //Log.d(TAG, " | x_line3 = " + (x0_dotted + mDottedList.get(1).getX1move() * 2) + "y_line1 = " + (y0_dotted + mDottedList.get(3).getY1move() * 2));
        }
    }

    // ---------- * Снимаем выделение с пунктира (обнуляем значения) * -----------
    public void free() {
        for(int i=0; i<x_dotted.length; i++) {
            x_dotted[i] = 0;
            y_dotted[i] = 0;
        }
        for(int i=0; i<mDottedList.size(); i++) {
            mDottedList.get(i).setLastVertex(0);
        }
        xTrans = 0; yTrans = 0;
        x0center = 0; y0center = 0;
        mGlobalAngle = 0;
        mLocalAngle = 0;
    }


    // ---------- * Проверка на Bounding box * -----------
    public boolean checkIntersect (float xv[], float yv[]) {

        //Проходим по 4-ем ребрам (отрезкам) пунктирного прямоугольника
        for(int i=0; i<x_dotted.length; i++)
        {
            int j;
            if(i<x_dotted.length-1) j=i+1;
            else j=0;

            float x1 = Math.min(x_dotted[i], x_dotted[j]);
            float y1 = Math.min(y_dotted[i], y_dotted[j]);
            float x2 = Math.max(x_dotted[i], x_dotted[j]);
            float y2 = Math.max(y_dotted[i], y_dotted[j]);

            for(int k=0; k<xv.length; k++)
            {
                int l;
                if(i<xv.length-1) l=i+1;
                else l=0;

                float x3 = Math.min(xv[k], xv[l]);
                float y3 = Math.min(yv[k], yv[l]);
                float x4 = Math.max(xv[k], yv[l]);
                float y4 = Math.max(xv[k], yv[l]);

                if(innerBox(x3, y3, x4, y4)) return true;

                if(     boundingBox(x1, x2, x3, x4) && boundingBox(y1, y2, y3, y4)
                        && area(x1, y1, x2, y2, x3, y3) * area(x1, y1, x2, y2, x4, y4) < 0.0f
                        && area(x3, y3, x4, y4, x1, y1) * area(x3, y3, x4, y4, x2, y2) < 0.0f) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean boundingBox(float a, float b, float c, float d) {

        return Math.max(a,c) <= Math.min(b,d);

    }

    boolean innerBox (float x1, float y1, float x2, float y2) {
        float xDot_min, xDot_max;
        float yDot_min, yDot_max;

        if(x_dotted[0]<x_dotted[2]) { xDot_min = x_dotted[0]; xDot_max = x_dotted[2]; }
        else { xDot_min = x_dotted[2]; xDot_max = x_dotted[0]; }
        if(y_dotted[0]<y_dotted[2]) { yDot_min = y_dotted[0]; yDot_max = y_dotted[2]; }
        else { yDot_min = y_dotted[2]; yDot_max = y_dotted[0]; }

        if(x1>=xDot_min && x2<=xDot_max && y1>=yDot_min && y2<=yDot_max) return true;
        else return false;
    }

    float area(float ax, float ay, float bx, float by, float cx, float cy) {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
    }

    boolean checkTouch(float xTouch, float yTouch) {
        float xDot_min, xDot_max;
        float yDot_min, yDot_max;

        if(x_dotted[0]<x_dotted[2]) { xDot_min = x_dotted[0]; xDot_max = x_dotted[2]; }
        else { xDot_min = x_dotted[2]; xDot_max = x_dotted[0]; }
        if(y_dotted[0]<y_dotted[2]) { yDot_min = y_dotted[0]; yDot_max = y_dotted[2]; }
        else { yDot_min = y_dotted[2]; yDot_max = y_dotted[0]; }

        if(xTouch>=xDot_min && yTouch>=yDot_min && xTouch<=xDot_max && yTouch <= yDot_max) {
            return true;
        }
        else {
            return false;
        }
    }


    public void setTranslate(float x_Trans, float y_Trans) {
        xTrans = fromLocalToGlobal_x(x_Trans, y_Trans);
        yTrans = fromLocalToGlobal_y(x_Trans, y_Trans);
    }

    public void Translate(float x_Trans, float y_Trans) {
        x0center += fromLocalToGlobal_x(x_Trans, y_Trans);
        y0center += fromLocalToGlobal_y(x_Trans, y_Trans);
        for(int i=0; i<mDottedList.size(); i++) {
            mDottedList.get(i).Translate(fromLocalToGlobal_x(x_Trans, y_Trans), fromLocalToGlobal_y(x_Trans, y_Trans));
        }
        for(int i=0; i<x_dotted.length; i++) {
            x_dotted[i] += fromLocalToGlobal_x(x_Trans, y_Trans);
            y_dotted[i] += fromLocalToGlobal_y(x_Trans, y_Trans);
        }
    }

    public void setAngle(float angle) { mAngle = angle; }

    public void Rotate(float angle, boolean global) {

        if (!global) {
            if (mLocalAngle >= 360) mLocalAngle = 0;
            mLocalAngle += angle;
        }
        else if(global)
        {
            if(mGlobalAngle>=360) mGlobalAngle = 0;
            mGlobalAngle += angle;
        }
    }

    public void Scale(float scale) {
        mScaleX += scale;
    }

    float fromGlobalToLocal_x(float gX, float gY) {

        gX *= mScaleX;
        gY *= mScaleX;
        gX = (float) (gX* Math.cos(Math.toRadians(mGlobalAngle)) - gY* Math.sin(Math.toRadians(mGlobalAngle)) );

        return gX;
    }

    float fromGlobalToLocal_y(float gX, float gY) {

        gX *= mScaleX;
        gY *= mScaleX;
        gY = (float) (gX* Math.sin(Math.toRadians(mGlobalAngle)) + gY* Math.cos(Math.toRadians(mGlobalAngle)) );

        return gY;
    }

    float fromLocalToGlobal_x(float locX, float locY) {

        locX = (float) (locX* Math.cos(Math.toRadians((360 - mGlobalAngle))) - locY* Math.sin(Math.toRadians((360 - mGlobalAngle))) );

        locX /= mScaleX;

        return locX;
    }

    float fromLocalToGlobal_y(float locX, float locY) {

        locY = (float) (locX* Math.sin(Math.toRadians((360 - mGlobalAngle))) + locY* Math.cos(Math.toRadians((360 - mGlobalAngle))) );

        locY /= mScaleX;

        return locY;
    }

    public float getXcenter() { return fromGlobalToLocal_x(x0center, y0center); }
    public float getYcenter() { return fromGlobalToLocal_y(x0center, y0center); }

    public float getXdotted(int num) {
        if(num>0 && num <x_dotted.length) { return x_dotted[num]; }
        else return 0;
    }

    public float getYdotted(int num) {
        if(num>0 && num <y_dotted.length) { return y_dotted[num]; }
        else return 0;
    }

}