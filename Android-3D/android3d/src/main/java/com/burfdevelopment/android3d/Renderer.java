package com.burfdevelopment.android3d;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.bda.controller.Controller;
import com.bda.controller.MotionEvent;
import com.bda.controller.StateEvent;
import com.burfdevelopment.android3d.Objects_3D.Cube;
import com.burfdevelopment.android3d.skybox.Skybox;
import com.burfdevelopment.android3d.skybox.SkyboxShaderProgram;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import Utils.Constants;
import com.burfdevelopment.android3d.Objects_3D.Floor;
import Utils.Quaternion;
import Utils.TextureHelper;
import Utils.Vector3;

import static android.opengl.Matrix.multiplyMM;

//import Utils.AndroidRotationSensor;

public class Renderer implements android.opengl.GLSurfaceView.Renderer {
    private static final String TAG = Renderer.class.getSimpleName();
    private static final boolean D = true;

    private static final float SIZE_WORLD = 40f; // in meter
    private static final float PLAYER_WIDTH = 1f; // distance to wall where movement should stop

    private Shapes mFloor;
    private Shapes mCube;
    private Shapes mCube2;
    private Shapes mCube3;
    private Light mLight;

    private Screen mScreen;
    private float angleInDegrees;
    private Camera mCamera;

    private int mHalfWidth, mHeight;
    private float mRatio;

    // Declare as volatile because we are updating it from another thread
    public volatile float mdFOV = 0f;
    public volatile float mdAngleX = 0f;
    public volatile float mdAngleY = 0f;
    public volatile float mdPosX = 0;
    public volatile float mdPosY = 0;
    public volatile float mIPD = 1.0f;

    private volatile Quaternion mQuaternion = new Quaternion();
    //private AndroidRotationSensor androidSensor;

    private int frameCounter = 0;
    private long frameCheckTime = 0;

    private Context mContext;

    private List<Shapes> mShapes = new ArrayList<Shapes>();

    // cardboard
    protected HeadTracker mHeadTracker;
    protected HeadTransform mHeadTransform;
    protected float[] mHeadViewMatrix;
    //protected Matrix4 mHeadViewMatrix4;
    //private Quaternion mCameraOrientation;

    public final float[] mModelMatrix = new float[16];
    public final float[] mViewMatrix = new float[16];
    public final float[] mProjectionMatrix = new float[16];
    public final float[] mMVPMatrix = new float[16];

    private int mTextureWidth, mTextureHeight;
    private int mScreenWidth, mScreenHeight;
    int[] fb, depthRb, renderTex;
    IntBuffer texBuffer;
    private boolean aa = true;    // anti-aliasing

    // new merge
    /**
     * This will be used to pass in the transformation matrix.
     */
    public int mMVPMatrixHandle;
    /**
     * This will be used to pass in the modelview matrix.
     */
    public int mMVMatrixHandle;


    private Cube cube;
    private Floor plane;

    //SKybox
    private SkyboxShaderProgram skyboxProgram;
    private Skybox skybox;
    private int skyboxTexture;

    // MOGA
    static final int ACTION_CONNECTED = Controller.ACTION_CONNECTED;
    static final int ACTION_DISCONNECTED = Controller.ACTION_DISCONNECTED;
    static final int ACTION_VERSION_MOGA = Controller.ACTION_VERSION_MOGA;
    static final int ACTION_VERSION_MOGAPRO = Controller.ACTION_VERSION_MOGAPRO;

    Controller mController = null;

    final TreeMap<Integer, ExampleInteger> mStates = new TreeMap<Integer, ExampleInteger>();
    final TreeMap<Integer, ExampleInteger> mKeys = new TreeMap<Integer, ExampleInteger>();
    final TreeMap<Integer, ExampleFloat> mMotions = new TreeMap<Integer, ExampleFloat>();

    public Renderer(Context context) {
        mContext = context;

        mHeadTransform = new HeadTransform();
        mHeadViewMatrix = new float[16];
        //mHeadViewMatrix4 = new Matrix4();
        //mCameraOrientation = new Quaternion();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        //androidSensor = new AndroidRotationSensor(mContext);

        mScreen = new Screen(mContext);
        mFloor = Shapes.Floor(mContext, 1.0f).scale(SIZE_WORLD, 0.1f, SIZE_WORLD).translate(0f, -1f, 0f);

        mCube = Shapes.ColorCube(mContext, 1.0f).rotate(-40, 1, -1, 0).translate(0, 1.0f, 0);
        mCube2 = Shapes.ColorCube(mContext, 2.0f).translate(-3f, 1.0f, 0.0f);
        mCube3 = Shapes.ColorCube(mContext, 2.0f).translate(3f, 1.0f, 0.0f);

        mShapes.add(mCube);
        mShapes.add(mCube2);
        mShapes.add(mCube3);

        mLight = new Light(mContext);

        // add light to scene
        mFloor.addLight(mLight);
        mCube.addLight(mLight);
        mCube2.addLight(mLight);
        mCube3.addLight(mLight);

        mCamera = new Camera(Camera.PLAYER_IPD, Camera.PLAYER_EYE_HEIGHT, Camera.CAMERA_FOV, 1);
        mCamera.mPosZ = 10;
        mIPD = mCamera.getIPD();

        createCube();
        createFloor();

        skybox = new Skybox();
        skyboxProgram = new SkyboxShaderProgram(mContext);
        skyboxTexture = TextureHelper.loadCubeMap(mContext,
                new int[]{R.drawable.night_left, R.drawable.night_right,
                        R.drawable.night_bottom, R.drawable.night_top,
                        R.drawable.night_front, R.drawable.night_back});

        setupMogo();


    }

    private void setupMogo() {
        // MOGO
        mController = Controller.getInstance(mContext);
        mController.init();

        if (mController.getState(StateEvent.STATE_CONNECTION) == ACTION_CONNECTED) {
            //mRenderActivity.application.mogaEnabled = true;
        }

        mStates.put(StateEvent.STATE_CONNECTION, new ExampleInteger("STATE_CONNECTION"));
        mStates.put(StateEvent.STATE_POWER_LOW, new ExampleInteger("STATE_POWER_LOW"));
        mStates.put(StateEvent.STATE_CURRENT_PRODUCT_VERSION, new ExampleInteger("STATE_CURRENT_PRODUCT_VERSION"));
        mStates.put(StateEvent.STATE_SUPPORTED_PRODUCT_VERSION, new ExampleInteger("STATE_SUPPORTED_PRODUCT_VERSION"));
    }

    public void setHeadTracker(HeadTracker headTracker) {
        mHeadTracker = headTracker;

    }

    @Override
    public void onDrawFrame(GL10 unused) {

        movePlayer();

        updateScene();

        //renderFrameToTexture();
        renderFrame();


        if (frameCheckTime < System.currentTimeMillis()) {
//    		if(D) Log.d(TAG, String.format("FPS: %d, angle: %.2f, x: %.2f, y: %.2f, IPD: %.4f, FOV: %.2f",
//    						frameCounter, mCamera.mYaw, mCamera.mPosZ, mCamera.mPosX, mCamera.getIPD(), mCamera.getFOV()) );
            frameCounter = 0;
            frameCheckTime += 1000;
        }
        frameCounter++;

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        mScreenWidth = width;
        mScreenHeight = height;

        mTextureWidth = width;
        mTextureHeight = height;

        mHalfWidth = mTextureWidth / 2;
        mHeight = mTextureHeight;

        mRatio = (float) mHalfWidth / height;

        mCamera.setFOV(mCamera.getFOV(), mRatio);

        frameCheckTime = System.currentTimeMillis() + 1000;

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1.0f, 10);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -1.0000001f, 0.0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Setup Render to texture
        //setupRenderToTexture();

        mScreen.setRatio(ratio);
    }

    private void movePlayer()
    {

        if (mController.getState(StateEvent.STATE_CONNECTION) == ACTION_CONNECTED)
        {
            float x =  mController.getAxisValue(MotionEvent.AXIS_Z) ;
            float y =  mController.getAxisValue(MotionEvent.AXIS_RZ) ;

            //TODO limit up down

            mdAngleX += x;
            mdAngleY += y;

            float forward =  mController.getAxisValue(MotionEvent.AXIS_Y ) / 2;
            float left  =  mController.getAxisValue(MotionEvent.AXIS_X) / 2;

            Log.i("TAG", "Forward " + forward + " Left " + left);

            mdPosX -= forward;
            mdPosY -= left;

        }

        mCamera.mYaw += mdAngleX;
        float cosAngle = (float) Math.cos(mCamera.mYaw / 180.0 * Math.PI);
        float singAngle = (float) Math.sin(mCamera.mYaw / 180.0 * Math.PI);

        mCamera.mPitch += mdAngleY;

        mCamera.mPosZ += cosAngle * mdPosX + singAngle * mdPosY;
        mCamera.mPosX += cosAngle * mdPosY - singAngle * mdPosX;
        mCamera.setIPD(mIPD);

        mCamera.setHeadOrientation(mQuaternion);

        //float orientationValues[] = new float[3];
        //androidSensor.getNowOrientation(orientationValues);
        //Matrix.rotateM(mCamera.mHMatrix, 0, orientationValues[0], 1, 0, 0);
        //Matrix.rotateM(mCamera.mHMatrix, 0, orientationValues[1], 0, 0, 1);
        //Matrix.rotateM(mCamera.mHMatrix, 0, orientationValues[2], 0, 1, 0);

        // Use Google Cardboard
        mHeadTracker.getLastHeadView(mHeadViewMatrix, 0);
        Matrix.multiplyMM(mCamera.mHMatrix, 0, mHeadViewMatrix, 0, mCamera.mHMatrix, 0);

        if (mdFOV != 0)
            mCamera.setFOV(mCamera.getFOV() + mdFOV, mRatio);

        mdAngleY = 0;
        mdAngleX = 0;
        mdPosX = 0;
        mdPosY = 0;
        mdFOV = 0;

        // collision with room walls?
//        float border = SIZE_WORLD / 2 - PLAYER_WIDTH;
//        mCamera.mPosZ = Math.min(border, Math.max(-border, mCamera.mPosZ));
//        mCamera.mPosX = Math.min(border, Math.max(-border, mCamera.mPosX));

        mCamera.update();
    }

    private void updateScene() {
        mCube.rotate(1.5f, 6f, 2.7f, 3.5f);
        mCube2.rotate(-0.3f, 3f, 2.1f, 0.5f);

        angleInDegrees = (360.0f / 10000.0f) * ((int) (SystemClock.uptimeMillis() % 10000L));
        mLight.reset().translate(-1.5f, 0, 0f).rotate(angleInDegrees, 0, 1, 0).translate(0f, 2.8f, -3f);

    }

    private void renderFrame() {
        GLES20.glClearColor(0.53f, 0.81f, 0.98f, 1f);

        // clear background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // view for left eye
        GLES20.glViewport(0, 0, mHalfWidth, mHeight);
        // draw scene
        drawScene(mCamera.mVMatrixLeft, mCamera.mProjMatrix);
        // flush
        GLES20.glFlush();


        // view for right eye
        GLES20.glViewport(mHalfWidth, 0, mHalfWidth, mHeight);
        // draw scene
        drawScene(mCamera.mVMatrixRight, mCamera.mProjMatrix);
        // flush
        GLES20.glFlush();
    }

    private void drawScene(float[] VMatrix, float[] PMatrix) {


        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        //mFloor.draw(VMatrix, PMatrix);
        drawSky(PMatrix);

        plane.draw(VMatrix, PMatrix);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

//        for (Shapes s : mShapes) {
//            s.draw(VMatrix, PMatrix);
//        }

        mLight.draw(VMatrix, PMatrix);

        cube.draw(VMatrix, PMatrix);
    }

    void createCube()
    {
        cube = new Cube();
        cube.position = new Vector3(1, 1, 1);
        cube.colour = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
        cube.scale = new Vector3(1, 1, 1);
        cube.create(mContext, this);

        cube.loadShaders(R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);
        cube.loadTexture(R.drawable.stone_wall_public_domain);
        cube.setMinFilter(GLES20.GL_LINEAR);
        cube.setMagFilter(GLES20.GL_LINEAR);
    }

    void createFloor()
    {
        plane = new Floor();
        plane.position = new Vector3(0.0f,-2.0f,0.0f);
        plane.scale = new Vector3(Constants.MapMaxSizeX, 1.0f, Constants.MapMaxSizeZ );
        plane.create(mContext, this,50);

        // ground
        plane.loadShaders(R.raw.per_pixel_vertex_shader_tex_and_light,R.raw.per_pixel_fragment_shader_tex_and_light);
        plane.loadTexture(R.drawable.concrete_floors0048_7_s);
        plane.setMinFilter(GLES20.GL_LINEAR);
        plane.setMagFilter(GLES20.GL_LINEAR);
    }

    // OLD

    private boolean renderFrameToTexture() {

        GLES20.glViewport(0, 0, mTextureWidth, mTextureHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);

        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
            return false;

        renderFrame();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        renderTexture();

        return true;
    }

    private void renderTexture() {

        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        mScreen.draw(mMVPMatrix, renderTex[0]);

        GLES20.glFlush();
    }

    void drawSky(float[] mProjMatrix)
    {
        // No depth testing
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        skyboxProgram.useProgram();
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.multiplyMM(mModelMatrix, 0, mHeadViewMatrix, 0, mModelMatrix, 0);
        multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mModelMatrix, 0);

        skyboxProgram.setUniforms(mMVPMatrix, skyboxTexture);
        skybox.bindData(skyboxProgram);
        skybox.draw();
    }

    private void setupRenderToTexture() {
        fb = new int[1];
        depthRb = new int[1];
        renderTex = new int[1];

        // generate
        GLES20.glGenFramebuffers(1, fb, 0);
        GLES20.glGenRenderbuffers(1, depthRb, 0);
        GLES20.glGenTextures(1, renderTex, 0);

        // generate color texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);

        // parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        // create it
        // create an empty intbuffer first?
        int[] buf = new int[mTextureWidth * mTextureHeight];
        texBuffer = ByteBuffer.allocateDirect(buf.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        ;
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, mTextureWidth, mTextureHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, texBuffer);

        // create render buffer and bind 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mTextureWidth, mTextureHeight);
    }
}