package com.burfdevelopment.android3d.Objects_3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.burfdevelopment.android3d.R;
import com.burfdevelopment.android3d.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import Utils.Constants;

public class Cube extends Object3d {

    /**
     * Store our model data in a float buffer.
     */
    private FloatBuffer mCubePositions;
    private FloatBuffer mCubeNormals;
    private FloatBuffer mCubeTextureCoordinates;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;

    /**
     * Size of the normal data in elements.
     */
    private final int mNormalDataSize = 3;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * This will be used to pass in model texture coordinate information.
     */
    private int mTextureCoordinateHandle;

    /**
     * Size of the texture coordinate data in elements.
     */
    private final int mTextureCoordinateDataSize = 2;

    /**
     * This will be used to pass in the texture.
     */
    private int mTextureUniformHandle;

    // X, Y, Z
    // Define points for a cube.
    final float[] cubePositionData =
            {
                    // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                    // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                    // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                    // usually represent the backside of an object and aren't visible anyways.

                    // Front face
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,

                    // Right face
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Back face
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,

                    // Left face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,

                    // Top face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Bottom face
                    1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
            };
    // X, Y, Z
    // The normal is used in light calculations and is a vector which points
    // orthogonal to the plane of the surface. For a cube model, the normals
    // should be orthogonal to the points of each face.
    final float[] cubeNormalData =
            {
                    // Front face
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,

                    // Right face
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    // Left face
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    // Top face
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    // Bottom face
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f
            };


    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.

    public void create(final Context activityContext, Renderer render) {
        this.mActivityContext = activityContext;
        this.mRenderer = render;

        // Initialize the buffers.
        this.mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mCubePositions.put(cubePositionData).position(0);

        this.mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mCubeNormals.put(cubeNormalData).position(0);

        //Log.i("SIZE", ""+scale.x);
        //Log.i("ARRAY SIZE", "ARRAY "+ textureCoordinateData.length);
        updateTextureSizeXY(10);

        this.mCubeTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        //updateTextureSizeXY();
        this.mCubeTextureCoordinates.put(textureCoordinateData).position(0);
    }

    public void draw(float[] mVMatrix, float[] mProjMatrix) {
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mRenderer.mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mRenderer.mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Draw a cube.
        // Translate the cube into the screen.
        Matrix.setIdentityM(mRenderer.mModelMatrix, 0);
        Matrix.translateM(mRenderer.mModelMatrix, 0, position.x, position.y, position.z);
        Matrix.scaleM(mRenderer.mModelMatrix, 0, scale.x, scale.y, scale.z);

        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.x, 1, 0, 0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.y, 0, 1, 0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.z, 0, 0, 1);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
//
        // Set color for drawing the triangle
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "v_Color");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, colour, 0);


        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");

        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mRenderer.mMVPMatrix, 0, mVMatrix, 0, mRenderer.mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mRenderer.mMVMatrixHandle, 1, false, mRenderer.mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjMatrix, 0, mRenderer.mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mRenderer.mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mRenderer.mMVPMatrixHandle, 1, false, mRenderer.mMVPMatrix, 0);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
//
        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);
    }

    public void loadPlacemarkerTextures() {
        this.loadShaders(R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);
        //cube.loadTexture(R.drawable.stone_wall_public_domain);
        this.setMinFilter(GLES20.GL_LINEAR);
        this.setMagFilter(GLES20.GL_LINEAR);
        this.colour = new float[]{0.0f, 1.0f, 1.0f, 1.0f};
        loadedTexture = true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cube))
            return false;
        Cube cube = (Cube) o;
        return position.x == (int) (cube.position.x / Constants.PlaceMarkerSize) && position.z == (int) (cube.position.z / Constants.PlaceMarkerSize);
    }


    public int hashCode() {
        return (int) (position.x + position.z);
    }

    ;

}
