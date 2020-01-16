package net.caustix.computergraphics;

import android.graphics.drawable.shapes.Shape;
import android.util.Log;

import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;
import net.caustix.computergraphics.shapes.Line;
import net.caustix.computergraphics.shapes.Polygon;
import net.caustix.computergraphics.shapes.Square;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Николай on 28.09.2015.
 */

public class Shape2D {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    float xTrans, yTrans;
    float scale = 1;
    float mAngle = 0;
    float xCenterRot=0;
    float yCenterRot=0;

    final int shapeType; //Тип фигуры
    private int shapeClass; //Класс фигуры

    static final int   LINE = 0,
                TRIANGLE = 2,
                SQUARE = 3;
    static final int shapeTypesNum = 3;

    final int POLYGON = 33;

    static ArrayList<Line> linesList = new ArrayList<Line>();
    ArrayList<Square> squaresList = new ArrayList<Square>();

    //Координаты треугольника
    float[] triangleCoords =    {0.0f, 0.35f, 0.0f,
                                -0.5f, -0.5f, 0.0f,
                                0.5f, -0.5f, 0.0f};
    short[] triangleDrawOrder = new short[] {0, 1, 2};

    //Координаты квадрата
    private float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f}; // top right
    short[] squareDrawOrder = new short[] {0, 1, 2, 0, 2, 3};

    float[] color = new float[] {0, 0, 0, 1};

    Line mLine;
    Polygon mPolygon;

    public int verticeNum; //Количество вершин
    int lineWidth = 3;

    // ============== * КОНСТРУКТОР 2D формы * ==============
    public Shape2D (int shape_Type, float shape_scale, float[] shape_color) {
        shapeType = shape_Type;
        scale = shape_scale;
        setColor(shape_color);
        switch(shapeType) {
            case LINE:
                mLine = new Line(shape_scale, color, lineWidth);
                verticeNum = mLine.getVertices();
                shapeClass = LINE;
                break;
            case TRIANGLE:
                mPolygon = new Polygon(shape_scale, triangleCoords, triangleDrawOrder, color);
                shapeClass = POLYGON;
                verticeNum = mPolygon.getVertices();
                break;
            case SQUARE:
                mPolygon = new Polygon(shape_scale, squareCoords, squareDrawOrder, color);
                shapeClass = POLYGON;
                verticeNum = mPolygon.getVertices();
                break;
        }
    }
    // ----------------------------------------------

    //--------------- Конструктор копии 2D формы ---------------------
    public Shape2D(Shape2D shapeCopy) {
        shapeType = shapeCopy.shapeType;
        scale = shapeCopy.scale;
        setColor(shapeCopy.color);
        switch(shapeType) {
            case LINE:
                //mLine = new Line(scale, color);
                mLine = new Line(shapeCopy.mLine);
                verticeNum = mLine.getVertices();
                shapeClass = shapeCopy.shapeClass;
                break;
            case TRIANGLE:
                //mPolygon = new Polygon(scale, triangleCoords, triangleDrawOrder, color);
                mPolygon = new Polygon(shapeCopy.mPolygon);
                verticeNum = mPolygon.getVertices();
                shapeClass = shapeCopy.shapeClass;
                break;
            case SQUARE:
                //mPolygon = new Polygon(scale, squareCoords, squareDrawOrder, color);
                mPolygon = new Polygon(shapeCopy.mPolygon);
                verticeNum = mPolygon.getVertices();
                shapeClass = shapeCopy.shapeClass;
                break;
        }
        //Log.d(TAG, "mAngle = " + shapeCopy.mAngle);
    }

    // --------------- * Рисование форм * ----------------
    public void draw(float[] mvpMatrix) {
        switch(shapeClass) {
            case LINE:
                mLine.draw(mvpMatrix);
                break;
            case POLYGON:
                mPolygon.draw(mvpMatrix);
                break;
        }
    }


    // --------------- * Проверка нажатия на форму * ---------------
    public boolean checkTouch(float xTouch, float yTouch) {

        switch(shapeClass) {
            //Для линии
            case LINE:
                    //Крайние координаты линии
                    float[] lineX = new float[2];
                    float[] lineY = new float[2];

                    lineX[0] = mLine.getX(0);
                    lineX[1] = mLine.getX(1);

                    lineY[0] = mLine.getY(0);
                    lineY[1] = mLine.getY(1);

                    float xMin, xMax;
                    float yMin, yMax;
                    if (lineX[0] < lineX[1]) {
                        xMin = lineX[0];
                        xMax = lineX[1];
                    } else {
                        xMin = lineX[1];
                        xMax = lineX[0];
                    }
                    if (lineY[0] < lineY[1]) {
                        yMin = lineY[0];
                        yMax = lineY[1];
                    } else {
                        yMin = lineY[1];
                        yMax = lineY[0];
                    }

                    if(Math.abs(xMax-xMin)<0.1 && xTouch>xMin-0.1 && xTouch<xMin+0.1)
                    {
                        if(yTouch>yMin && yTouch<yMax) return true;
                    }
                    else if(Math.abs(yMax-yMin)<0.1 && yTouch>yMin-0.1 && yTouch<yMin+0.1)
                    {
                        if(xTouch>xMin && xTouch<xMax) return true;
                    }
                    else
                    {
                        //Пользуясь уравнением прямой проходящей через две точки на плоскости,
                        //определим попадание по линии
                        float leftEquation = (xTouch - lineX[0]) / (lineX[1] - lineX[0]); //Правая часть уравнения
                        float rightEquation = (yTouch - lineY[0]) / (lineY[1] - lineY[0]); //Левая часть уравнения
                        float pogresh = 0.3f; //Погрешность попадания

                        if (leftEquation >= rightEquation - pogresh && leftEquation <= rightEquation + pogresh
                                && xTouch > xMin && xTouch < xMax && yTouch > yMin && yTouch < yMax) {
                            //Log.d(TAG, "-------- Line Touch detected --------");
                            return true;
                        }
                    }
                break;
            //Для полигона
            case POLYGON:
                float vx[] = new float[mPolygon.numVertices];
                float vy[] = new float[mPolygon.numVertices];
                //Получим координаты вершин треугольника
                for(int i=0; i<vx.length; i++)
                {
                    vx[i] = mPolygon.getX(i);
                    vy[i] = mPolygon.getY(i);
                }

                boolean b1, b2, b3;

                b1 = Sign(xTouch, yTouch, vx[0], vy[0], vx[1], vy[1]) < 0.0f;
                b2 = Sign(xTouch, yTouch, vx[1], vy[1], vx[2], vy[2]) < 0.0f;
                b3 = Sign(xTouch, yTouch, vx[2], vy[2], vx[0], vy[0]) < 0.0f;

                //Если полигон - треугольник
                if(shapeType==TRIANGLE)
                {
                    return ((b1 == b2) && (b2 == b3));
                }
                //Если полигон - квадрат
                else if(shapeType==SQUARE)
                {
                    boolean b4, b5, b6;
                    b4 = Sign(xTouch, yTouch, vx[2], vy[2], vx[3], vy[3]) < 0.0f;
                    b5 = Sign(xTouch, yTouch, vx[3], vy[3], vx[0], vy[0]) < 0.0f;
                    b6 = Sign(xTouch, yTouch, vx[0], vy[0], vx[2], vy[2]) < 0.0f;

                    return ((b1 == b2) && (b2 == b3)) || ((b4 == b5) && (b5 == b6));

                }

                //break;
        }

        return false;
    }

    float Sign(float xTouch, float yTouch, float x1, float y1, float x2, float y2) {
        return (xTouch - x2) * (y1 - y2) - (x1 - x2) * (yTouch - y2);
    }
    //-------------------------------------------------



    // ----------------- Методы для трансформации 2D объекта ---------------

    //Переместить
    public void Translate(float x_Trans, float y_Trans) {
            switch (shapeClass) {
                case LINE:
                    mLine.Translate(x_Trans, y_Trans);
                    break;
                case POLYGON:
                    mPolygon.Translate(x_Trans, y_Trans);
                    break;
            }
    }
    //Установить смещение в пикселях
    public void setTranslatePx (int x_Trans, int y_Trans) {
        switch (shapeClass) {
            case LINE:
                mLine.setTranslatePx(x_Trans, y_Trans);
                break;
            case POLYGON:
                mPolygon.setTranslatePx(x_Trans, y_Trans);
                break;
        }
    }
    //Установить смещение
    public void setTranslate(float x_Trans, float y_Trans) {
        switch (shapeClass) {
            case LINE:
                mLine.setTranslate(x_Trans, y_Trans);
                break;
            case POLYGON:
                mPolygon.setTranslate(x_Trans, y_Trans);
                break;
        }
    }
    //Установить угол
    public void setAngle(float angle) {
        mAngle = angle;
        switch (shapeClass) {
            case LINE:
                mLine.setAngle(mAngle);
                break;
            case POLYGON:
                mPolygon.setAngle(mAngle);
                break;
        }
    }
    //Поворот
    public void Rotate(float angle, float x_CenterRot, float y_CenterRot) {
        mAngle += angle;
        switch (shapeClass) {
            case LINE:
                mLine.Rotate(angle, x_CenterRot, y_CenterRot);
                break;
            case POLYGON:
                mPolygon.Rotate(angle, x_CenterRot, y_CenterRot);
                break;
        }
    }
    //Установить размер
    public void setScale(float _scale) {
        scale = _scale;
        switch (shapeClass) {
            case LINE:
                mLine.setScale(scale);
                break;
            case POLYGON:
                mPolygon.setScale(scale);
                break;
        }
    }
    //Масштабирование
    public void Scale(float scale_x, float scale_y, float xCenterScale, float yCenterScale) {
        scale += scale_x;
        switch (shapeClass) {
            case LINE:
                mLine.Scale(scale_x, xCenterScale, yCenterScale);
                break;
            case POLYGON:
                mPolygon.Scale(scale_x, xCenterScale, yCenterScale);
                break;
        }
    }
    //Установить слой
    public void setLayer(int layer) {
        switch (shapeClass) {
            case LINE:
                mLine.setLayer(layer);
                break;
            case POLYGON:
                mPolygon.setLayer(layer);
                break;
        }
    }

    public void setColor(float[] sColor) {
        for(int i=0; i<sColor.length; i++) { color[i] = sColor[i]; }
    }
    // ----------------------------------------------------------------


    // ------------- * Геттеры * -------------
    public float getX(int vertexNum ) {
        switch (shapeClass) {
            case LINE:
                return mLine.getX(vertexNum);
            case POLYGON:
                return mPolygon.getX(vertexNum);
        }
        return 0;
    }

    public float getY(int vertexNum ) {
        switch (shapeClass) {
            case LINE:
                return mLine.getY(vertexNum);
            case POLYGON:
                return mPolygon.getY(vertexNum);
        }
        return 0;
    }

    public float getXclean(int vertexNum) {
        switch (shapeClass) {
            case LINE:
                return mLine.getX(vertexNum);
            case POLYGON:
                return mPolygon.getX(vertexNum);
        }
        return 0;
    }

    public float getYclean(int vertexNum) {
        switch (shapeClass) {
            case LINE:
                return mLine.getY(vertexNum);
            case POLYGON:
                return mPolygon.getY(vertexNum);
        }
        return 0;
    }

    public float getxTrans() {
        switch (shapeClass) {
            case LINE:
                return mLine.getxLocalTrans();
            case POLYGON:
                return mPolygon.getxLocalTrans();
        }
        return 0;
    }

    public float getyTrans() {
        switch (shapeClass) {
            case LINE:
                return mLine.getyLocalTrans();
            case POLYGON:
                return mPolygon.getyLocalTrans();
        }
        return 0;
    }

    public float getAngle() { return mAngle; }

    public float[] getColor() { return color; }
    //-----------------------------------------------------------------


    // -------------- * Остальные методы * --------------------

    //Генерируем рандомный объект
    public static Shape2D genRandShape(int shape_Type)
    {

        final Random random = new Random();
        float rScale = random.nextFloat();
        int rxTrans = random.nextInt(250)-100;
        int ryTrans = random.nextInt(250)-100;
        int rAngle = random.nextInt(360);
        float[] rColor = new float[] { random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f};
        Shape2D tShape = new Shape2D(shape_Type, rScale, rColor);
        tShape.setTranslatePx(rxTrans, ryTrans);
        tShape.setAngle(rAngle);

        return tShape;
    }

}
