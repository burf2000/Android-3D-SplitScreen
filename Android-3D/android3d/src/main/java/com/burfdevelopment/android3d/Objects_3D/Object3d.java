package com.burfdevelopment.android3d.Objects_3D;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.burfdevelopment.android3d.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import Utils.RawResourceReader;
import Utils.ShaderHelper;
import Utils.TextureHelper;
import Utils.Vector3;


public class Object3d {

    public int id;

    /**
     * This is a handle to our cube shading program.
     */
    protected int mProgramHandle;

    /**
     * This will be used to pass in model normal information.
     */
    protected int mNormalHandle;

    /**
     * This will be used to pass in model position information.
     */
    protected int mPositionHandle;

    // Colours
    /**
     * This will be used to pass in model color information.
     */
    protected int mColorHandle;

    /**
     * These are handles to our texture data.
     */
    protected int mTextureDataHandle;

    public Vector3 position = new Vector3(0.0f, 0.0f, 0.0f);
    public Vector3 rotation = new Vector3(0.0f, 0.0f, 0.0f);
    public Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
    public float[] colour = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    public Renderer mRenderer;
    public Context mActivityContext;

    public boolean loadedTexture = false;

    public String owner;

    /**
     * A temporary matrix.
     */
    public float[] mTemporaryMatrix = new float[16];

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    public float[] textureCoordinateData =
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f,

                    // Right face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f,

                    // Back face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f,

                    // Left face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f,

                    // Top face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f,

                    // Bottom face
                    0.0f, 0.0f,
                    0.0f, 1,
                    1, 0.0f,
                    0.0f, 1,
                    1, 1,
                    1, 0.0f
            };

    public void setMinFilter(final int filter) {
        if (mTextureDataHandle != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);
        }
    }

    public void setMagFilter(final int filter) {
        if (mTextureDataHandle != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filter);
        }
    }

    public void loadTexture(int texture) {
        // Load the texture
        this.mTextureDataHandle = TextureHelper.loadTexture(this.mActivityContext, texture);
    }

    public void loadShaders(int vertex_shader, int fragment_shader) {
        // Define a simple shader program for our point.
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, vertex_shader);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, fragment_shader);

        final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[]{"a_Position"});
    }

    public void updateTextureSizeXZ(float num) {
        for (int i = 0; i < textureCoordinateData.length; i++) {
            if (textureCoordinateData[i] == 1) {
                if ((i & 1) == 0) {
                    //even
                    textureCoordinateData[i] = scale.x / num;
                } else {
                    textureCoordinateData[i] = scale.z / num;
                }
            }
        }
    }


    public void updateTextureSizeXY(float num) {
        for (int i = 0; i < textureCoordinateData.length; i++) {
            if (textureCoordinateData[i] == 1) {
                if ((i & 1) == 0) {
                    //even
                    textureCoordinateData[i] = scale.x / num;
                } else {
                    textureCoordinateData[i] = scale.y / num;
                }
            }
        }
    }

    public void updateTextureSizeYZ(float num) {
        for (int i = 0; i < textureCoordinateData.length; i++) {
            if (textureCoordinateData[i] == 1) {
                if ((i & 1) == 0) {
                    //even
                    textureCoordinateData[i] = scale.z / num;
                } else {
                    textureCoordinateData[i] = scale.y / num;
                }
            }
        }
    }

}
