package net.caustix.computergraphics.android;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import net.caustix.computergraphics.R;
import net.caustix.computergraphics.ScreenSurface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Николай on 17.09.2015.
 */

public class MainGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private final Context mActivityContext;
    private boolean createdObjects=false;

    // mMVPMatrix это аббревиатура для "Model View Projection Matrix"
    //"Матрица модельно-видовой проекции / Модельно видовая матрица"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    //Параметры для конвертирования координат экрана в игровые координаты
    //Инициализируются в методе onSurfaceChanged()
    public static float koef_x, koef_y;
    public static int half_width, half_height;

    public int navBarHeight;

    public static float ratio;
    public static boolean landscape;

    public ScreenSurface mScreenSurface;

    public MainGLRenderer(Context activityContext)
    {
        mActivityContext = activityContext;
    }

    // ------------ Поверхность создается ------------
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Установим цвет фона кадра (черный по умолчанию)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        //Загрузим текстуры
        GFXUtils.loadTexture(mActivityContext, R.drawable.arrow);
        GFXUtils.loadTexture(mActivityContext, R.drawable.arrow_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.delete);
        GFXUtils.loadTexture(mActivityContext, R.drawable.delete_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.dotted_line);
        GFXUtils.loadTexture(mActivityContext, R.drawable.dotted_line_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.line);
        GFXUtils.loadTexture(mActivityContext, R.drawable.line_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.rotate);
        GFXUtils.loadTexture(mActivityContext, R.drawable.rotate_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.scale);
        GFXUtils.loadTexture(mActivityContext, R.drawable.scale_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.shapes);
        GFXUtils.loadTexture(mActivityContext, R.drawable.shapes_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.square);
        GFXUtils.loadTexture(mActivityContext, R.drawable.square_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.triangle);
        GFXUtils.loadTexture(mActivityContext, R.drawable.triangle_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.frames);
        GFXUtils.loadTexture(mActivityContext, R.drawable.addkeybutt);
        GFXUtils.loadTexture(mActivityContext, R.drawable.addkeybutt_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.delkeybutt);
        GFXUtils.loadTexture(mActivityContext, R.drawable.delkeybutt_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.emptybutt);
        GFXUtils.loadTexture(mActivityContext, R.drawable.emptybutt_pressed);
        GFXUtils.loadTexture(mActivityContext, R.drawable.num1);
        GFXUtils.loadTexture(mActivityContext, R.drawable.num2);
        GFXUtils.loadTexture(mActivityContext, R.drawable.num3);
        GFXUtils.loadTexture(mActivityContext, R.drawable.num4);
        GFXUtils.loadTexture(mActivityContext, R.drawable.pause);
        GFXUtils.loadTexture(mActivityContext, R.drawable.play);


        GFXUtils.loadTexture(mActivityContext, R.drawable.global);
        GFXUtils.loadTexture(mActivityContext, R.drawable.local);



        GLES20.glEnable(GLES20.GL_DEPTH_TEST );
        GLES20.glDepthFunc( GLES20.GL_LEQUAL );
        GLES20.glDepthMask( true );

        GFXUtils.loadShaderProg();
    }

    //------------ Изменение ориентации экрана ------------
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        navBarHeight = getNavBarHeight();

        half_width = width / 2 ;
        half_height = height / 2;

        //Горизонтальное положение устройства (landscape)
        if(width>height) {
            //half_height += navBarHeight/2; //Чтобы сохранить пропорции экрана при перевороте,
            //с учетом размера нижнего меню (Navigation bar)
            ratio = (float) width/height;
            Matrix.frustumM(mProjectionMatrix, 0, ratio, -ratio, -ratio / ratio, ratio / ratio, 3, 7);
            koef_x = (float) ratio/half_width;
            koef_y = (float) 1/half_height;
            landscape = true;
        }
        //Вертикальное положение (portrait)
        else {
            //half_width -= navBarHeight/2;
            ratio = (float) height/width;
            Matrix.frustumM(mProjectionMatrix, 0, ratio/ratio, -ratio/ratio, -ratio, ratio, 3, 7);
            koef_x = (float) 1/half_width;
            koef_y = (float) ratio/half_height;
            landscape = false;
        }

        //Блок кода выполняется только один раз при старте активити
        if(!createdObjects) {
            initObjects(); //Проинициализируем объекты
            createdObjects = true;
        }
    }

    // -------------------- Инициализация объектов --------------------
    public void initObjects() {
        mScreenSurface = new ScreenSurface();
    }

    //----------------------------- Отрисовка графики ----------------------------------
    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Установим положение камеры (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3.01f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Вычислим проекционную и видовую трансформации, умножив матрицы
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        mScreenSurface.draw(mMVPMatrix);
    }


    // ============================ * ОСТАЛЬНЫЕ МЕТОДЫ * ================================

    //Преобразуем координаты экрана в игровые координаты
    public static float convertXpix(float x_pix) { return x_pix*koef_x; }
    public static float convertYpix(float y_pix) { return y_pix*koef_y; }

    //Получение размера нижнего меню (Navigation bar)
    public int getNavBarHeight() {
        Resources resources = mActivityContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static float setLayer(int layer) {
        return -0.001f*layer;
    }
}
