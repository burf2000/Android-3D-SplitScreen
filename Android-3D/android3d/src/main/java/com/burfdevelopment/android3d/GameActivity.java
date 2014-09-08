package com.burfdevelopment.android3d;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    private SurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGLView == null)
        {
            mGLView = new SurfaceView(this);
            setContentView(mGLView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
