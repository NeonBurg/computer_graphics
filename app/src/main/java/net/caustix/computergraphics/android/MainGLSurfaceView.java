package net.caustix.computergraphics.android;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import static java.lang.Math.abs;

/**
 * Created by ������� on 17.09.2015.
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

        //������� OpenGL ES 2.0 ��������
        setEGLContextClientVersion(2);

        mRenderer = new MainGLRenderer(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        //��������� Renderer ��� ����������� ������� �� GLSurfaceView
        setRenderer(mRenderer);

        //��������� ��� ������ ����� ��������� ��������� � ������ ����������� �������
        //(�� �������), ������ ���: ���������� ���������
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // -------------- ������� (touches) ----------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //������� � ������������ �������
        //(������ 0 - ������ ����� �������� �� �����)
        //(������ 1 - ������ ����� �������� �� �����)
        float[] x = new float[2];
        float[] y = new float[2];
        //������� �� ��������� ����������� ��������� �������
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

        //��������� ����
        switch (event.getAction()) {
            //�������� ������
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
