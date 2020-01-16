package net.caustix.computergraphics;

import android.opengl.Matrix;
import android.util.Log;

import net.caustix.computergraphics.android.MainGLActivity;
import net.caustix.computergraphics.android.MainGLRenderer;
import net.caustix.computergraphics.shapes.Square;

import java.util.ArrayList;

/**
 * Created by Николай on 15.11.2015.
 */

public class KeyFrames {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    float xTrans, yTrans;
    float screenWidth, screenHeight;

    float yNow; //Указатель координат Y для добавления кнопок

    // ---------------- МАТРИЦЫ -----------------
    private float[] mTranslationMatrix = new float[16];
    float[] mvpModifiedMatrix = new float[16];
    // ------------------------------------------

    Button framesButt; //Кнопка разворачивания/сворачивания панели кадров
    float frames_buttW, frames_buttH;

    Button addFrameButt;
    float addFrame_buttW, addFrame_buttH;

    Button delFrameButt;

    Square framesAreaSquare;
    float[] framesAreaColor = new float[] {0.8f, 0.8f, 0.8f, 1.0f};

    boolean framesTool; // true - панель инструментов развернута, false - свернута

    float padding;
    boolean buttTouched = false;

    ArrayList<Button> keyFrameButts = new ArrayList<Button>();
    float keyFrame_buttH;

    boolean keyTouched = false;
    int selectedKeyFrame = 0;
    int[] numTextureId = new int[4];

    Button playButt;
    boolean animation = false;

    // ============ * КОНСТРУКТОР Ключевые Кадры * ============
    public KeyFrames(float half_width, float half_height) {

        frames_buttW = MainGLRenderer.convertXpix(90);
        frames_buttH = MainGLRenderer.convertYpix(74);

        screenWidth = half_width*2;
        screenHeight = half_height*2;

        xTrans = half_width-frames_buttW/2+0.01f;

        framesTool = true;
        padding = MainGLRenderer.convertYpix(10);
        initNumbersTexture();

        framesAreaSquare = new Square(frames_buttW, screenHeight, framesAreaColor, 1);

        framesButt = new Button(90, 74, R.drawable.frames, R.drawable.frames, 2);
        framesButt.setTranslate(0, -half_height + frames_buttH / 2);

        //Кнопка добавления нового кадра
        addFrame_buttW = MainGLRenderer.convertXpix(70);
        addFrame_buttH = MainGLRenderer.convertYpix(32);
        yNow = half_height - addFrame_buttH/2 - padding;
        addFrameButt = new Button(70, 32, R.drawable.addkeybutt, R.drawable.addkeybutt_pressed, 2);
        addFrameButt.setTranslate(0, yNow);

        //Кнопка удаления кадра
        delFrameButt = new Button(70, 32, R.drawable.delkeybutt, R.drawable.delkeybutt_pressed, 2);
        delFrameButt.setTranslate(0, -half_height + frames_buttH + addFrame_buttH/2 + padding);

        //Первый кадр
        keyFrame_buttH = MainGLRenderer.convertYpix(70);
        yNow = yNow - addFrame_buttH/2 - keyFrame_buttH/2 - padding;
        Button tempButt = new Button(70, 70, R.drawable.emptybutt, R.drawable.emptybutt_pressed, 2);
        tempButt.addUpperPicture(19, 26, numTextureId[selectedKeyFrame]);
        tempButt.setTranslate(0, yNow);
        tempButt.select();
        keyFrameButts.add(tempButt);

        //Кнопка плей/старт
        playButt = new Button(70, 30, R.drawable.play, R.drawable.pause, 2);
        playButt.setTranslate(0, -half_height + frames_buttH + addFrame_buttH + addFrame_buttH/2 +  3*padding);

    }

    // ----------- РИСОВАНИЕ -----------
    public void draw(float[] mvpMatrix) {

        //Перемещение
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, xTrans, yTrans, 0);
        Matrix.multiplyMM(mvpModifiedMatrix, 0, mvpMatrix, 0, mTranslationMatrix, 0);

        framesAreaSquare.draw(mvpModifiedMatrix);
        framesButt.draw(mvpModifiedMatrix);
        addFrameButt.draw(mvpModifiedMatrix);
        delFrameButt.draw(mvpModifiedMatrix);
        playButt.draw(mvpModifiedMatrix);

        for(int i=0; i<keyFrameButts.size(); i++) {
            keyFrameButts.get(i).draw(mvpModifiedMatrix);
        }
    }

    public int checkTouch(float xTouch, float yTouch) {

        float cnvrtX = fromGlobalToLocal_x(xTouch);
        float cnvrtY = fromGlobalToLocal_y(yTouch);

        //Добавить кадр
        if(addFrameButt.checkTouch(cnvrtX, cnvrtY)) {

            if (keyFrameButts.size() < 4) {
                yNow = yNow - keyFrame_buttH - padding;
                Button tempButt = new Button(70, 70, R.drawable.emptybutt, R.drawable.emptybutt_pressed, 2);
                tempButt.setTranslate(0, yNow);
                keyFrameButts.get(selectedKeyFrame).release();
                keyFrameButts.add(tempButt);
                selectedKeyFrame = keyFrameButts.size()-1;
                tempButt.addUpperPicture(19, 26, numTextureId[selectedKeyFrame]);
                keyFrameButts.get(selectedKeyFrame).select();

                //Log.d(TAG, "keyFrame added, selectedKeyFrame = " + selectedKeyFrame);
                return 0;
            } else {
                //Log.d(TAG, "cant add keyFrame !");
            }

            buttTouched = true;
            return -1;
        }

        //Нажатие на ключевой кадр
        for(int i=0; i<keyFrameButts.size(); i++) {
            if(keyFrameButts.get(i).checkTouch2(cnvrtX, cnvrtY)) {
                keyFrameButts.get(selectedKeyFrame).release();
                selectedKeyFrame = i;
                keyFrameButts.get(selectedKeyFrame).select();
                //Log.d(TAG, "selectedKeyFrame = " + selectedKeyFrame);
                //keyTouched = true;
                return 1;
            }
        }

        //Старт/стоп анимации
        if(playButt.checkTouch2(cnvrtX, cnvrtY)) {
            if(!animation) {
                animation = true;
                playButt.select();
            }
            else
            {
                animation = false;
                playButt.release();
            }
            return 2;
        }

        //Удалить кадр
        if(delFrameButt.checkTouch(cnvrtX, cnvrtY)) {

            if(selectedKeyFrame==keyFrameButts.size()-1 && keyFrameButts.size()>1) {
                keyFrameButts.remove(selectedKeyFrame);
                selectedKeyFrame--;
                keyFrameButts.get(selectedKeyFrame).select();
                yNow = keyFrameButts.get(selectedKeyFrame).getyTrans();
                buttTouched = true;
                return 3;
            }
            else if(keyFrameButts.size()>1) {
                yNow = keyFrameButts.get(selectedKeyFrame).getyTrans();
                keyFrameButts.remove(selectedKeyFrame);
                for(int i=selectedKeyFrame; i<keyFrameButts.size(); i++) {
                    keyFrameButts.get(i).setTranslate(0, yNow);
                    //keyFrameButts.get(i).addUpperPicture(19, 26, numTextureId[i]);

                    if(i<keyFrameButts.size()-1) {
                        yNow = yNow - keyFrame_buttH - padding;
                    }
                }
                keyFrameButts.get(selectedKeyFrame).select();
                buttTouched = true;
                return 3;
            }

            buttTouched = true;
            return -1;
        }

        //Скрыть/раскрыть панель инструментов
        if(framesButt.checkTouch(cnvrtX, cnvrtY)) {
            //Log.d(TAG, "YES !");
            if(!framesTool) { yTrans = 0;  framesTool=true; }
            else { yTrans = screenHeight-frames_buttH; framesTool=false; }
            return 4;
        }

        return -1;
    }

    public void touchFinished() {
        if(buttTouched) {
            addFrameButt.release();
            delFrameButt.release();
            buttTouched=false;
        }
    }

    public float fromGlobalToLocal_x(float gX) {
        return gX-xTrans;
    }

    public float fromGlobalToLocal_y(float gY) {
        return gY-yTrans;
    }

    void initNumbersTexture() {
        numTextureId[0] = R.drawable.num1;
        numTextureId[1] = R.drawable.num2;
        numTextureId[2] = R.drawable.num3;
        numTextureId[3] = R.drawable.num4;
    }

    public int getSelectedKeyFrame() {
        return selectedKeyFrame;
    }

    public void selectKeyFrame(int keyFrame) {
        keyFrameButts.get(selectedKeyFrame).release();
        keyFrameButts.get(keyFrame).select();
        selectedKeyFrame = keyFrame;
    }


}
