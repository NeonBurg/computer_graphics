package net.caustix.computergraphics.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Created by Николай on 17.09.2015.
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
        // Вызывается GL surface view's onResume() когда активити переходит onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Вызывается GL surface view's onPause() когда наше активити onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }

}
