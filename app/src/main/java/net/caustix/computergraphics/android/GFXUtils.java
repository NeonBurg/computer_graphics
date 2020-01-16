package net.caustix.computergraphics.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.SparseIntArray;

/**
 * Created by Николай on 17.09.2015.
 */

public class GFXUtils {

    // ------------ ШЕЙДЕРЫ ------------
    //Шейдер вершин для ТЕКСТУРНЫХ объектов
    public static final String vertexTextureShaderCode =
                "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  TexCoordOut = TexCoordIn;" +
                    "}";

    //Фрагментарный шейдер для ТЕКСТУРНЫХ объектов (шейдер кадра)
    public static final String fragmentTextureShaderCode =
                "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +
                    "varying lowp vec2 TexCoordOut;" +
                    "void main() {" +
                    "  gl_FragColor = (vColor * texture2D(u_Texture, TexCoordOut));" +
                    "}";
    //для ЦВЕТНЫХ объектов
    public static final String vertexColorShaderCode =
                "uniform mat4 uMVPMatrix;" +
                    "uniform float uThickness;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  gl_PointSize = 2.0;" +
                    "}";
    public static final String fragmentColorShaderCode =
                "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    //----------------------------------



    public static int mTextureShaderProgram; //Программа для объектов с текстурами
    public static int mColorShaderProgram; //Программа для объектов с цветом

    public static final int COORDS_PER_VERTEX = 3;
    public static final int COORDS_PER_TEXTURE = 2;
    public static int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
    public static int textureStride = COORDS_PER_TEXTURE * 4; // bytes per vertex

    public static SparseIntArray textures = new SparseIntArray();


    // ------------ Загрузка (компиляция) ШЕЙДЕРА ------------
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderHandle = GLES20.glCreateShader(type);

        if(shaderHandle != 0) {
            // Передаем в наш шейдер программу.
            GLES20.glShaderSource(shaderHandle, shaderCode);
            // Компиляция шейреда
            GLES20.glCompileShader(shaderHandle);

            // Получаем результат процесса компиляции
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // Если компиляция не удалась, удаляем шейдер.
            if(compileStatus[0] == 0) {
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if(shaderHandle == 0)
        {
            throw new RuntimeException("Error compiling shader.");
        }

        return shaderHandle;
    }


    // ------------ Загружаем ТЕКСТУРУ ------------
    public static void loadTexture(final Context context, final int resourceId) {

        //Резезвируем id для текстуры, мы будем использовать его
        //для вызова одинаковых текстур
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        //Декодируем файл текстуры в объект Android Bitmap
        if(textureHandle[0] != 0) {

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // Включить предварительное маштабирование

            //Считываем ресурс
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            //Привязываем к текстуре в OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            //Установим фильтрацию
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            //Загружаем bitmap в связанную текстуру
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        textures.append(resourceId, textureHandle[0]);
    }

    public static void loadShaderProg() {
        //Загрузим (скомпилируем) код шейдеров
        //Для текстурных объектов
        int vertexTextureShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexTextureShaderCode);
        int fragmentTextureShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentTextureShaderCode);
        //Для цветных объектов
        int vertexColorShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexColorShaderCode);
        int fragmentColorShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentColorShaderCode);


        //Проинициализируем программы шейдеров
        //Для текстурных объектов
        mTextureShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mTextureShaderProgram, vertexTextureShader);
        GLES20.glAttachShader(mTextureShaderProgram, fragmentTextureShader);
        GLES20.glLinkProgram(mTextureShaderProgram);
        //Для цветных объектов
        mColorShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mColorShaderProgram, vertexColorShader);
        GLES20.glAttachShader(mColorShaderProgram, fragmentColorShader);
        GLES20.glLinkProgram(mColorShaderProgram);
    }
}
