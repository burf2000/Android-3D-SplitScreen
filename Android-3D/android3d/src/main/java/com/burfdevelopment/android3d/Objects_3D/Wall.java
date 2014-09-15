package com.burfdevelopment.android3d.Objects_3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.burfdevelopment.android3d.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import Utils.Vector3;

/**
 * Created by snrb on 12/09/2014.
 */
public  class Wall extends Object3d {

    /** Store our model data in a float buffer. */
    private FloatBuffer mFloorPositions;
    private FloatBuffer mFloorNormals;
    private FloatBuffer mFloorTextureCoordinates;

    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** Size of the normal data in elements. */
    private final int mNormalDataSize = 3;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** This will be used to pass in model normal information. */
    private int mNormalHandle;

    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;

    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;

    //	/** This is a handle to our floor shading program. */
    //	private int mProgramHandle;

    // X, Y, Z
    // Define points for a floor.
    final float[] floorPositionData =
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
    // orthogonal to the plane of the surface. For a floor model, the normals
    // should be orthogonal to the points of each face.
    final float[] floorNormalData =
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




    public Wall(final Context activityContext, Renderer render, float x, float y, float z, float scaleX, float scaleY, float scaleZ)
    {
        super();

        this.mActivityContext = activityContext;
        this.mRenderer = render;
        this.position = new Vector3(x, y, z);
        this.scale = new Vector3(scaleX, scaleY, scaleZ);
        //this.rotation = new Vector3(rotX, rotY, rotZ);

        // Initialize the buffers.
        this.mFloorPositions = ByteBuffer.allocateDirect(floorPositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mFloorPositions.put(floorPositionData).position(0);

        this.mFloorNormals = ByteBuffer.allocateDirect(floorNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mFloorNormals.put(floorNormalData).position(0);

        if	(scale.x > scale.z)
        {
            updateTextureSizeXY(1);
        }
        else
        {
            updateTextureSizeYZ(1);
        }


        this.mFloorTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mFloorTextureCoordinates.put(textureCoordinateData).position(0);

        //this.colour = new float[]{0.0f,1.0f,1.0f,1.0f};
    }

    public void draw(float[] mVMatrix, float[] mProjMatrix)
    {
        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for floor drawing.
        mRenderer.mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mRenderer.mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Draw a floor.
        // Translate the floor into the screen.
        Matrix.setIdentityM(mRenderer.mModelMatrix, 0);

        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.x, 1, 0,0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.y, 0, 1,0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.z, 0, 0,1);

        Matrix.translateM(mRenderer.mModelMatrix, 0, position.x, position.y, position.z);
        Matrix.scaleM(mRenderer.mModelMatrix, 0, scale.x, scale.y, scale.z);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the texture coordinate information
        mFloorTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mFloorTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Set color for drawing the triangle
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "v_Color");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, colour, 0);

        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");

        // Pass in the position information
        mFloorPositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mFloorPositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mFloorNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mFloorNormals);

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

        // Draw the floor.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        // Pass in the texture coordinate information
        mFloorTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mFloorTextureCoordinates);
    }

}