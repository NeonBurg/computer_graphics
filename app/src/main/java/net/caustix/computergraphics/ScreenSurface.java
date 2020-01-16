package net.caustix.computergraphics;

import android.opengl.Matrix;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.SparseArray;

import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;
import net.caustix.computergraphics.shapes.Line;
import net.caustix.computergraphics.shapes.Square;

import java.util.ArrayList;

/**
 * Created by Николай on 20.09.2015.
 */

public class ScreenSurface {

    private static final String TAG = MainGLActivity.class.getSimpleName();
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

    final int   LINE = 0,
            TRIANGLE = 2,
            SQUARE = 3;
    final int shapeTypesNum = 3;

    //Размеры экрана
    float half_width, half_height;
    float screenWidth, screenHeight;

    // ----- Матрицы -----
    float[] translationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];

    //Рамка вокруг области видимости объектов
    Square rightArea;
    Square leftArea;
    float bLeftAreaWidth; //Ширина границы справа
    float[] bLeftAreaColor = new float[] {0.8f, 0.8f, 0.8f, 1.0f};
    float[] bRightAreaColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

    //Оси координат
    Line xAxis;
    Line yAxis;
    float[] xAxCoords;
    float[] yAxCoords;
    float[] axisColor = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    //Клеточное поле
    ArrayList<Line> cellList = new ArrayList<Line>();
    int cellSize = 25;

    //Граница области маленького экрана по оси X
    float xSmallScreen;

    //Кнопки
    ArrayList<Button> buttonsList = new ArrayList<Button>();
    ArrayList<Button> shapeButtList = new ArrayList<Button>();
    Square shapeButtArea;
    boolean showShapeButtons = false;
    float buttPadding;

    int touchButtIndex = 0;
    int touchShapesButtIndex = -1;

    //Квадраты для выделения вершин фигуры
    SparseArray<ArrayList<Square>> verticeList = new SparseArray<ArrayList<Square>>();
    final int selectSize = 7;
    float[] selectColor = new float[] {0, 0, 0, 1};

    //Массив с 2D фигурами
    ArrayList<Shape2D> shapesList = new ArrayList<Shape2D>();
    ArrayList<ArrayList <Shape2D>> keyFramesArr = new ArrayList<ArrayList <Shape2D>>();
    int selectedKeyFrame = 0;
    float[] lineColor = new float[] {0.0f, 1.0f, 1.0f, 1.0f};
    int touchShapeIndex = -1;
    public static int selectedShapeIndex = -1;

    boolean selectTool = true;
    boolean rotateShape = false;
    boolean scaleShape = false;
    boolean dottedSelect = false;

    DottedSquare mDottedSq;
    ArrayList<Integer> selectedIndexList = new ArrayList<Integer>();

    boolean touchStart = false;
    static boolean isGlobalCoord = true;

    Square centerPoint;
    public static int cpSize = 7;
    float[] cpGlobalColor = new float[] {1, 1, 0, 1};
    float[] cpLocalColor = new float[] {0, 1, 0, 1};

    KeyFrames mKeyFrames;
    boolean animation = false;
    CountDownTimer timerAnim;


    // ============== * КОНСТРУКТОР ГЛАВНОГО ЭКРАНА * ===============
    public ScreenSurface() {

        half_width = MainGLRenderer.convertXpix(MainGLRenderer.half_width);
        half_height = MainGLRenderer.convertYpix(MainGLRenderer.half_height);

        screenHeight = half_height*2;
        screenWidth = half_width*2;

        bLeftAreaWidth = MainGLRenderer.convertXpix(90);
        buttPadding = MainGLRenderer.convertYpix(10);

        xSmallScreen = -half_width+bLeftAreaWidth;

        xAxCoords = new float[] {-half_width, 0, 0,
                half_width, 0, 0};
        yAxCoords = new float[] {0, -half_height, 0,
                0, half_height, 0};
        xAxis = new Line(xAxCoords, new short[] {0,1}, axisColor);
        yAxis = new Line(yAxCoords, new short[] {0,1}, axisColor);

        initCellField();

        //Рамка вокруг области видимости объектов
        float leftCenter = -half_width + bLeftAreaWidth / 2;
        leftArea = new Square(bLeftAreaWidth, screenHeight, bLeftAreaColor, 1);
        rightArea = new Square(screenWidth, screenHeight, bRightAreaColor, 0);
        leftArea.setTranslate(leftCenter, 0);

        //Линии
        Shape2D tempShape = new Shape2D(LINE, 0.5f, lineColor);
        tempShape.setTranslatePx(0, 25);
        tempShape.setAngle(45);
        shapesList.add(tempShape);

        tempShape = new Shape2D(LINE, 1.0f, lineColor);
        tempShape.setTranslatePx(0, 200);
        //tempShape.setAngle(60);
        shapesList.add(tempShape);

        //Треугольник
        tempShape = new Shape2D(TRIANGLE, 0.5f, lineColor);
        tempShape.setTranslatePx(-50, -100);
        tempShape.setAngle(90);
        shapesList.add(tempShape);

        //Квадрат
        tempShape = new Shape2D(SQUARE, 0.4f, lineColor);
        tempShape.setTranslatePx(0, 0);
        tempShape.setAngle(0);
        shapesList.add(tempShape);

        keyFramesArr.add(shapesList);

        // --------- Кнопки ---------
        Button tempButt;
        float buttWidth = MainGLRenderer.convertXpix(70);
        float buttHeight = MainGLRenderer.convertYpix(70);
        float buttOffset = buttHeight/2+buttPadding;
        //Стрелка [0]
        tempButt = new Button(70, 70, R.drawable.arrow, R.drawable.arrow_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttOffset);
        tempButt.select();
        buttonsList.add(tempButt);

        //Выбор фигур [1]
        tempButt = new Button(70, 70, R.drawable.shapes, R.drawable.shapes_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight / 2 * 3 - buttPadding * 2);
        buttonsList.add(tempButt);

        //Изменение размера [2]
        tempButt = new Button(70, 70, R.drawable.scale, R.drawable.scale_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight / 2 * 5 - buttPadding * 3);
        buttonsList.add(tempButt);

        //Изменение размера [3]
        tempButt = new Button(70, 70, R.drawable.rotate, R.drawable.rotate_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight / 2 * 7 - buttPadding * 4);
        buttonsList.add(tempButt);

        //Пунктирная линия выделения[4]
        tempButt = new Button(70, 70, R.drawable.dotted_line, R.drawable.dotted_line_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight / 2 * 9 - buttPadding * 5);
        buttonsList.add(tempButt);

        //Удаление [5]
        tempButt = new Button(70, 70, R.drawable.delete, R.drawable.delete_pressed, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight/2 * 11 - buttPadding*6);
        buttonsList.add(tempButt);

        //Кнопки выбора фигуры
        float shapeAreaWidth = buttWidth*3 + buttPadding*4;
        float shapeAreaHeight = MainGLRenderer.convertYpix(90);
        shapeButtArea = new Square(shapeAreaWidth, shapeAreaHeight, bLeftAreaColor, 0);
        shapeButtArea.setTranslate(xSmallScreen + shapeAreaWidth / 2, half_height - buttHeight / 2 * 3 - buttPadding * 2);

        //Линия[6]
        tempButt = new Button(70, 70, R.drawable.line, R.drawable.line_pressed, 2);
        tempButt.setTranslate(xSmallScreen + buttWidth / 2 + buttPadding, half_height - buttHeight / 2 * 3 - buttPadding * 2);
        shapeButtList.add(tempButt);

        //Треугольник[7]
        tempButt = new Button(70, 70, R.drawable.triangle, R.drawable.triangle_pressed, 2);
        tempButt.setTranslate(xSmallScreen + buttWidth / 2 * 3 + buttPadding * 2, half_height - buttHeight / 2 * 3 - buttPadding * 2);
        shapeButtList.add(tempButt);

        //Квадрат[8]
        tempButt = new Button(70, 70, R.drawable.square, R.drawable.square_pressed, 2);
        tempButt.setTranslate(xSmallScreen + buttWidth / 2 * 5 + buttPadding * 3, half_height - buttHeight / 2 * 3 - buttPadding * 2);
        shapeButtList.add(tempButt);

        //Квадрат[8]
        tempButt = new Button(70, 70, R.drawable.global, R.drawable.local, 2);
        tempButt.setTranslate(leftCenter, half_height - buttHeight / 2 * 13 - buttPadding * 6);
        buttonsList.add(tempButt);

        mDottedSq = new DottedSquare();
        centerPoint = new Square(cpSize, cpSize, cpGlobalColor, 1);
        mKeyFrames = new KeyFrames(half_width, half_height);
    }
    // ------------------------------------------------------------------------------

    void initCellField() {
        float cellWidth = MainGLRenderer.convertXpix(cellSize);
        float cellHeight = MainGLRenderer.convertYpix(cellSize);
        Line tempLine;
        float start = 0;
        while(start<half_width) {
            start+=cellWidth;
            tempLine = new Line(yAxCoords, new short[] {0,1}, bLeftAreaColor);
            tempLine.setTranslate(start, 0);
            cellList.add(tempLine);
        }
        start = 0;
        while(start>-half_width) {
            start-=cellWidth;
            tempLine = new Line(yAxCoords, new short[] {0,1}, bLeftAreaColor);
            tempLine.setTranslate(start, 0);
            cellList.add(tempLine);
        }
        start = 0;
        while(start<half_height) {
            start+=cellHeight;
            tempLine = new Line(xAxCoords, new short[] {0,1}, bLeftAreaColor);
            tempLine.setTranslate(0, start);
            cellList.add(tempLine);
        }
        start = 0;
        while(start>-half_height) {
            start-=cellHeight;
            tempLine = new Line(xAxCoords, new short[] {0,1}, bLeftAreaColor);
            tempLine.setTranslate(0, start);
            cellList.add(tempLine);
        }
    }


    // =========== * Рисование объектов на экране * ===========
    public void draw(float[] mvpMatrix) {
        leftArea.draw(mvpMatrix);
        //Сместим центр оси X координат в центр малого экрана
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.translateM(translationMatrix, 0, bLeftAreaWidth / 2, 0, 0);
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, translationMatrix, 0);

        rightArea.draw(mvpModifiedMatrix);
        //Клеточное поле
        for(int i=0; i<cellList.size(); i++) {
            cellList.get(i).draw(mvpModifiedMatrix);
        }
        xAxis.draw(mvpModifiedMatrix);
        yAxis.draw(mvpModifiedMatrix);

        //2D фигуры
        for(int i=0; i<shapesList.size(); i++) {
            shapesList.get(i).draw(mvpModifiedMatrix);
        }
        //Основные кнопки
        for(int i=0; i<buttonsList.size(); i++) {
            buttonsList.get(i).draw(mvpMatrix);
        }
        //Выделенные вершины
        for(int i=0; i<verticeList.size(); i++) {
            int key = verticeList.keyAt(i);

            ArrayList<Square> tempList = (ArrayList<Square>) verticeList.get(key);

            for(int j=0; j<tempList.size(); j++) {
                tempList.get(j).draw(mvpModifiedMatrix);
            }
        }
        //Кнопки выбора фигуры
        if(showShapeButtons) {
            shapeButtArea.draw(mvpMatrix);
            for(int i=0; i<shapeButtList.size(); i++) {
                shapeButtList.get(i).draw(mvpMatrix);
            }
        }

        mDottedSq.draw(mvpModifiedMatrix);
        centerPoint.draw(mvpModifiedMatrix);
        mKeyFrames.draw(mvpMatrix);
    }
    // -------------------------------------------------------------



    // ============= * Проверка нажатия на экран * =============
    public void checkTouch(float x_px, float y_px, float dx, float dy) {

        float cnvrtX, cnvrtY;
        float cnvrtDx, cnvrtDy;

        if (x_px < MainGLRenderer.half_width) {
            cnvrtX = MainGLRenderer.convertXpix(MainGLRenderer.half_width - x_px) * (-1);
        } else {
            cnvrtX = MainGLRenderer.convertXpix(x_px - MainGLRenderer.half_width);
        }

        if (y_px < MainGLRenderer.half_height) {
            cnvrtY = MainGLRenderer.convertYpix(MainGLRenderer.half_height - y_px);
        } else {
            cnvrtY = MainGLRenderer.convertYpix(y_px - MainGLRenderer.half_height) * (-1);
        }

        cnvrtDx = MainGLRenderer.convertXpix(dx);
        cnvrtDy = MainGLRenderer.convertYpix(dy);

        if (cnvrtX >= xSmallScreen && !animation) {
            //Сместим считанную координату касания по оси Х в центр малого экрана
            cnvrtX = cnvrtX - bLeftAreaWidth;

            //Если выбрана одна или не одной фигуры
            //Проверка на касание по фигуре
            if(verticeList.size()<=1 && selectTool && touchShapeIndex==-1) {
                for(int i=shapesList.size()-1; i>=0; i--) {
                    Shape2D tempShape = shapesList.get(i);
                    if(tempShape.checkTouch(cnvrtX, cnvrtY))
                    {
                        touchShapeIndex = i;
                        if(selectedShapeIndex!=i) {
                            selectedShapeIndex = i;
                            selectObject(selectedShapeIndex);
                            dottedAroundShape();
                        }
                        break;
                    }
                    else  {
                        freeSelection();
                    }
                }
            }
            //Проверка касания по выделенной области
            else if(selectTool && touchShapeIndex==-1) {
                if(!mDottedSq.checkTouch(cnvrtX, cnvrtY)) {
                    freeSelection();
                }
            }

            //Трансформации
            if(verticeList.size()!=0 && !dottedSelect)
            {
                //Вращение
                if(rotateShape) {
                    for(int i=0; i<verticeList.size(); i++) {
                        int key = verticeList.keyAt(i);
                        Shape2D tempShape = shapesList.get(key);

                        if (isGlobalCoord) tempShape.Rotate(dx / 2, 0, 0);
                        else
                            tempShape.Rotate(dx / 2, mDottedSq.getXcenter(), mDottedSq.getYcenter());
                    }
                    mDottedSq.Rotate(dx / 2, isGlobalCoord);
                    updateVertices();
                    if (!isGlobalCoord)
                        centerPoint.setTranslate(mDottedSq.getXcenter(), mDottedSq.getYcenter());
                }
                //Масштабирование
                else if(scaleShape) {
                    for(int i=0; i<verticeList.size(); i++) {
                        int key = verticeList.keyAt(i);
                        Shape2D tempShape = shapesList.get(key);
                        if (isGlobalCoord) tempShape.Scale(cnvrtDx, cnvrtDy, 0, 0);
                        else
                            tempShape.Scale(cnvrtDx, cnvrtDy, mDottedSq.getXcenter(), mDottedSq.getYcenter());
                    }
                    updateVertices();
                    dottedAroundShape();
                }
                //Перемещение
                else if(selectTool) {
                    for(int i=0; i<verticeList.size(); i++) {
                        int key = verticeList.keyAt(i);
                        Shape2D tempShape = shapesList.get(key);
                        tempShape.Translate(cnvrtDx, cnvrtDy);
                    }
                    mDottedSq.Translate(cnvrtDx, cnvrtDy);
                    if (!isGlobalCoord)
                        centerPoint.setTranslate(mDottedSq.getXcenter(), mDottedSq.getYcenter());
                    updateVertices();
                }
            }

            if(dottedSelect) {
                if(!touchStart) { freeSelection(); touchStart=true; }
                mDottedSq.growSquare(cnvrtX, cnvrtY);

                for(int i=0; i<shapesList.size(); i++)
                {
                    Shape2D tempShape = shapesList.get(i);

                    float[] xv = new float[tempShape.verticeNum];
                    float[] yv = new float[tempShape.verticeNum];

                    for(int j=0; j<xv.length; j++)
                    {
                        xv[j] = tempShape.getX(j);
                        yv[j] = tempShape.getY(j);
                    }

                    if(mDottedSq.checkIntersect(xv, yv)) {
                        touchShapeIndex = i;
                        selectedShapeIndex = i;
                        selectObject(selectedShapeIndex);
                    }
                    else {
                        for(int j=0; j<verticeList.size(); j++) {
                            int key = verticeList.keyAt(j);
                            if(key==i) { verticeList.delete(key); break; }
                        }
                    }
                }
            }

        }
    }
    // -----------------------------------------------------------------------------------------------



    // ===================== * Проверка нажатия на кнопку * =====================
    public void checkButtonTouch(float x_px, float y_px) {
        float cnvrtX, cnvrtY;

        if(x_px<MainGLRenderer.half_width) {
            cnvrtX = MainGLRenderer.convertXpix(MainGLRenderer.half_width - x_px)*(-1);
        }
        else {
            cnvrtX = MainGLRenderer.convertXpix(x_px - MainGLRenderer.half_width);
        }

        if(y_px<MainGLRenderer.half_height) {
            cnvrtY = MainGLRenderer.convertYpix(MainGLRenderer.half_height - y_px);
        }
        else {
            cnvrtY = MainGLRenderer.convertYpix(y_px - MainGLRenderer.half_height)*(-1);
        }

        if(cnvrtX < xSmallScreen) {
            if (!buttonsList.isEmpty()) {
                //Стрелка
                if (buttonsList.get(0).checkTouch(cnvrtX, cnvrtY)) {
                    touchButtIndex = 0;
                    selectTool = true;
                }
                //Выбор фигур
                else if (buttonsList.get(1).checkTouch(cnvrtX, cnvrtY)) {
                    if(!showShapeButtons) { showShapeButtons = true; }
                    else {
                        showShapeButtons = false;
                        buttonsList.get(1).release();
                        buttonsList.get(0).select();
                    }
                    touchButtIndex = 1;
                }
                //Масштабирование
                else if (buttonsList.get(2).checkTouch(cnvrtX, cnvrtY)) {
                    scaleShape = true;
                    touchButtIndex = 2;
                }
                //Поворот
                else if (buttonsList.get(3).checkTouch(cnvrtX, cnvrtY)) {
                    rotateShape = true;
                    touchButtIndex = 3;
                }
                //Выделение области
                else if (buttonsList.get(4).checkTouch(cnvrtX, cnvrtY))
                {
                    dottedSelect = true;
                    if(selectedShapeIndex!=-1) { freeSelection(); }
                    touchButtIndex = 4;
                }
                //Удаление
                else if (buttonsList.get(5).checkTouch(cnvrtX, cnvrtY)) {
                    //if(selectedShapeIndex!=-1) { shapesList.remove(selectedShapeIndex); freeSelection(); }
                    if(verticeList.size()>0) {
                        Log.d(TAG, "verticeList.size()" + verticeList.size());
                        ArrayList<Shape2D> tempShapesList = new ArrayList<Shape2D>();
                        for(int i=0; i<verticeList.size(); i++) {
                            int index = verticeList.keyAt(i);
                            tempShapesList.add(shapesList.get(index));
                            //Log.d(TAG, "index = " + index);
                            //shapesList.remove(index);
                        }
                        shapesList.removeAll(tempShapesList);
                        freeSelection();
                    }
                    buttonsList.get(0).select();
                    touchButtIndex = 5;
                }
                else if (buttonsList.get(6).checkTouch2(cnvrtX, cnvrtY)) {
                    if(isGlobalCoord) {
                        isGlobalCoord = false;
                        if(selectedShapeIndex!=-1)
                        {
                            centerPoint.setColor(cpLocalColor);
                            centerPoint.setTranslate(mDottedSq.getXcenter(), mDottedSq.getYcenter());
                        }
                        buttonsList.get(6).select();
                        //Log.d(TAG, "LocalCoords");
                    }
                    else {
                        isGlobalCoord = true;
                        centerPoint.setColor(cpGlobalColor);
                        centerPoint.setTranslate(0, 0);
                        buttonsList.get(6).release();
                        //Log.d(TAG, "GlobalCoords");
                    }
                    if(touchButtIndex!=-1) buttonsList.get(touchButtIndex).select();
                }
                else
                {
                    touchButtIndex = -1;
                }

                if(touchButtIndex!=0 && selectTool) { selectTool = false; }
                if(touchButtIndex!=1 && showShapeButtons) { showShapeButtons = false; }
                if(touchButtIndex!=2 && scaleShape) { scaleShape = false; }
                if(touchButtIndex!=3 && rotateShape) { rotateShape = false; }
                if(touchButtIndex!=4 && dottedSelect) { dottedSelect = false; }
                if(touchButtIndex==-1) {
                    buttonsList.get(0).select();
                    touchButtIndex = 0;
                    selectTool = true;
                }
                //if(!isGlobalCoord) buttonsList.get(6).select();
                //else { buttonsList.get(6).release(); }
                //Глобальная/Локальная система координат
            }
        }
        else if(showShapeButtons) {
            //Линия[0]
            if (shapeButtList.get(0).checkTouch(cnvrtX, cnvrtY)) {
                if(!animation)
                {
                    //Генерируем случайную линию
                    shapesList.add(Shape2D.genRandShape(LINE));
                    freeSelection();
                    selectedShapeIndex = shapesList.size()-1;
                    selectObject(selectedShapeIndex);
                    dottedAroundShape();
                }
                touchShapesButtIndex = 0;
            }
            //Треугольник[1]
            else if (shapeButtList.get(1).checkTouch(cnvrtX, cnvrtY)) {
                if(!animation)
                {
                    //Генерируем случайный треугольник
                    shapesList.add(Shape2D.genRandShape(TRIANGLE));
                    freeSelection();
                    selectedShapeIndex = shapesList.size()-1;
                    selectObject(selectedShapeIndex);
                    dottedAroundShape();
                }
                touchShapesButtIndex = 1;
            }
            //Квадрат[2]
            else if (shapeButtList.get(2).checkTouch(cnvrtX, cnvrtY)) {
                if(!animation)
                {
                    //Генерируем случайный квадрат
                    shapesList.add(Shape2D.genRandShape(SQUARE));
                    freeSelection();
                    selectedShapeIndex = shapesList.size()-1;
                    selectObject(selectedShapeIndex);
                    dottedAroundShape();
                }
                touchShapesButtIndex = 2;
            }
        }

        //Нажатие на инструмент с ключевыми кадрами
        switch(mKeyFrames.checkTouch(cnvrtX, cnvrtY)) {
            //Добавление кадра
            case 0:
                ArrayList<Shape2D> copyShapesList = new ArrayList<Shape2D>();
                for(Shape2D shape : shapesList) {
                    copyShapesList.add(new Shape2D(shape));
                }
                keyFramesArr.remove(selectedKeyFrame);
                keyFramesArr.add(selectedKeyFrame, copyShapesList);

                keyFramesArr.add(shapesList);
                selectedKeyFrame = mKeyFrames.getSelectedKeyFrame();
                break;
            //Выбор кадра
            case 1:
                freeSelection();
                selectedKeyFrame = mKeyFrames.getSelectedKeyFrame();
                shapesList = keyFramesArr.get(selectedKeyFrame);
                break;
            //Старт/стоп анимации
            case 2:
                freeSelection();
                if(!animation) {
                    animation = true;
                    timerAnim = new CountDownTimer(20000, 100) {
                        int i=selectedKeyFrame;
                        public void onTick(long millisUntilFinished) {
                            shapesList = keyFramesArr.get(i);
                            mKeyFrames.selectKeyFrame(i);
                            if(i<keyFramesArr.size()-1)
                                i++;
                            else
                                i=0;
                            selectedKeyFrame = i;
                        }

                        public void onFinish() {
                            shapesList = keyFramesArr.get(i);
                            selectedKeyFrame = i;
                            Log.d(TAG, "timer finished !");
                        }
                    }.start();
                }
                else {
                    animation = false;
                    timerAnim.cancel();
                }

                break;
            //Удаление кадра
            case 3:
                freeSelection();
                keyFramesArr.remove(selectedKeyFrame);
                selectedKeyFrame = mKeyFrames.getSelectedKeyFrame();
                shapesList = keyFramesArr.get(selectedKeyFrame);
                break;
        }

    }
    // -----------------------------------------------------------------------------------------------



    // ------ * Завершение касания экрана * ------
    public void touchFinished() {
        if(touchShapeIndex!=-1) { touchShapeIndex = -1; }

        if(touchButtIndex!=-1)
        {
            for(int i=0; i<buttonsList.size(); i++) {
                if(i!=5 && i!=6)
                {
                    if(touchButtIndex!=i) {
                        buttonsList.get(i).release();
                    }
                }
            }

            if(touchButtIndex==5) { buttonsList.get(touchButtIndex).release(); buttonsList.get(0).select(); selectTool=true; }
        }

        if(touchShapesButtIndex!=-1) {
            shapeButtList.get(touchShapesButtIndex).release();
            touchShapesButtIndex = -1;
        }

        if(dottedSelect && !selectedIndexList.isEmpty()) {
            dottedAroundShape();
        }

        touchStart=false;

        mKeyFrames.touchFinished();

    }


    // ------ * Выделение объекта * ------
    void selectObject(int objectId) {
        Shape2D selectShape = shapesList.get(objectId); //Выделенный объект

        ArrayList<Square> tempVertList = new ArrayList<Square>();

        //verticeList.clear();
        float s2color[] = new float[] {1, 0, 0};
        float centerColor[] = new float[] {1, 1, 0};

        int vNum = selectShape.verticeNum;

        float[] xv = new float[vNum];
        float[] yv = new float[vNum];

        Square tSquare;

        for(int i=0; i<vNum; i++)
        {
            xv[i] = selectShape.getX(i);
            yv[i] = selectShape.getY(i);

            if(i==0) tSquare = new Square(selectSize, selectSize, s2color, 0);
            else tSquare = new Square(selectSize, selectSize, selectColor, 0);
            tSquare.setAngle(selectShape.getAngle());

            tSquare.setTranslate(xv[i], yv[i]);
            //verticeList.add(tSquare);
            tempVertList.add(tSquare);

        }

        if(!dottedSelect)
        {
            //Выбранный объект в конец списка
            shapesList.remove(objectId);
            shapesList.add(selectShape);
            selectedIndexList.add(shapesList.size()-1);
            selectedShapeIndex = shapesList.size()-1;
        }

        selectedIndexList.add(selectedShapeIndex);
        verticeList.append(selectedShapeIndex, tempVertList);

        touchShapeIndex = selectedShapeIndex;
    }

    // ------ Выделение фигур пунктирным прямоугольником ------
    void dottedAroundShape() {
        //Log.d(TAG, "x_min = " + x_min + " | x_max = " + x_max + "y_min = " + y_min + " | y_max = " + y_max);

        float x_min=10, y_min=10;
        float x_max=-10, y_max=-10;

        for(int i=0; i<verticeList.size(); i++) {
            int index = verticeList.keyAt(i);
            Shape2D tempShape = shapesList.get(index);

            int vNum = tempShape.verticeNum;

            float[] xv = new float[vNum];
            float[] yv = new float[vNum];

            for(int j=0; j<vNum; j++)
            {
                xv[j] = tempShape.getX(j);
                yv[j] = tempShape.getY(j);

                if(xv[j]<x_min) x_min = xv[j];
                if(xv[j]>x_max) x_max = xv[j];
                if(yv[j]<y_min) y_min = yv[j];
                if(yv[j]>y_max) y_max = yv[j];
            }
        }

        mDottedSq.free();
        mDottedSq.growSquare(x_min, y_min);
        mDottedSq.growSquare(x_max, y_max);

        if(!isGlobalCoord) {
            centerPoint.setColor(cpLocalColor);
            centerPoint.setTranslate(mDottedSq.getXcenter(), mDottedSq.getYcenter());
        }
    }


    // ------ Обновление положений выделенных вершин ------
    void updateVertices() {
        //Shape2D selectShape = shapesList.get(selectedShapeIndex);

        if(verticeList.size()!=0 && selectedShapeIndex!=-1) {

            for(int i=0; i < verticeList.size(); i++)
            {
                int key = verticeList.keyAt(i);

                ArrayList<Square> tempList = (ArrayList<Square>) verticeList.get(key);
                Shape2D selectShape = shapesList.get(key);
                int vNum = selectShape.verticeNum;

                for(int j=0; j<vNum; j++)
                {
                    float x1 = selectShape.getX(j);
                    float y1 = selectShape.getY(j);
                    tempList.get(j).setTranslate(x1, y1);
                    tempList.get(j).setAngle(selectShape.getAngle());
                }
            }
        }
    }

    // --------------- Снять выделение ---------------
    void freeSelection() {
        selectedShapeIndex= -1;
        verticeList.clear();
        verticeList.clear();
        selectedIndexList.clear();
        mDottedSq.free();

        if(!isGlobalCoord) {
            centerPoint.setColor(cpGlobalColor);
            centerPoint.setTranslate(0, 0);
        }
    }
}