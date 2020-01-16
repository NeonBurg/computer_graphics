package net.caustix.computergraphics.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import static java.lang.Math.abs;

/**
 * Created by Николай on 17.09.2015.
 */

public class MainGLSurfaceView extends GLSurfaceView {

    private static final String TAG = MainGLActivity.class.getSimpleName();
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private final MainGLRenderer mRenderer;

    private float mPreviousX;
    private float mPreviousY;

    float half_width, half_height;

    public MainGLSurfaceView(Context context) {
        super(context);

        //Создаем OpenGL ES 2.0 контекст
        setEGLContextClientVersion(2);

        mRenderer = new MainGLRenderer(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        //Установим Renderer для отображения графики на GLSurfaceView
        setRenderer(mRenderer);

        //Рендерить вид только когда произошли изменения в данных отображения графики
        //(по запросу), другой мод: постоянный рендеринг
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // -------------- КАСАНИЯ (touches) ----------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Массивы с координатами касаний
        //(индекс 0 - первый палец попавший на экран)
        //(индекс 1 - второй палец попавший на экран)
        float[] x = new float[2];
        float[] y = new float[2];
        //Массивы со значением перемещения координат касаний
        float dx;
        float dy;

        boolean multitouch=false;
        final int pointerCount = event.getPointerCount(); //Touch counter
        if(pointerCount>1 && pointerCount<=2) { multitouch = true; }

        x[0] = event.getX(0);
        y[0] = event.getY(0);
        if(multitouch) {
            x[1] = event.getX(1);
            y[1] = event.getY(1);
        }

        half_width = mRenderer.half_width;
        half_height = mRenderer.half_height;

        //Считываем жест
        switch (event.getAction()) {
            //Движение пальца
            case MotionEvent.ACTION_MOVE:
                dx = (x[0] - mPreviousX);
                dy = (y[0] - mPreviousY) * (-1);
                mRenderer.mScreenSurface.checkTouch(x[0], y[0], dx, dy);
                requestRender();
                break;
            case MotionEvent.ACTION_DOWN:
                mRenderer.mScreenSurface.checkButtonTouch(x[0], y[0]);
                requestRender();
                break;
            case MotionEvent.ACTION_UP:
                mRenderer.mScreenSurface.touchFinished();
                requestRender();
                break;
        }

        mPreviousX = x[0];
        mPreviousY = y[0];

        return true;
    }

}
