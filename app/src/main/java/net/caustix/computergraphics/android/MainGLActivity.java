package net.caustix.computergraphics.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Created by ������� on 17.09.2015.
 */

public class MainGLActivity extends Activity {

    private static final String TAG = MainGLActivity.class.getSimpleName();

    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new MainGLSurfaceView(this);

        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onResume()
    {
        // ���������� GL surface view's onResume() ����� �������� ��������� onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // ���������� GL surface view's onPause() ����� ���� �������� onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }

}
