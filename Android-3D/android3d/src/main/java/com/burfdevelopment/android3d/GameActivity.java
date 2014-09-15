package com.burfdevelopment.android3d;

import android.app.Activity;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

public class GameActivity extends Activity {

    private SurfaceView mGLView;
    private HeadTracker mHeadTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGLView == null) {
            mGLView = new SurfaceView(this);
            setContentView(mGLView);
        }

        mHeadTracker = new HeadTracker(this);

        mGLView.getRenderer().setHeadTracker(mHeadTracker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
        mHeadTracker.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        mHeadTracker.startTracking();
    }
}

