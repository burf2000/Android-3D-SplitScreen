package com.burfdevelopment.android3d.Objects_3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.burfdevelopment.android3d.Objects_3D.Object3d;
import com.burfdevelopment.android3d.R;
import com.burfdevelopment.android3d.Renderer;

public class Sphere extends Object3d
{
    //Buffers
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;

    //Buffer sizes in aantal bytes
    private int vertexBufferSize;
    private int colorBufferSize;
    private int indexBufferSize;

    private int vertexCount;

    static final int FLOATS_PER_VERTEX = 3; // Het aantal floats in een vertex (x, y, z)
    static final int FLOATS_PER_COLOR = 4;  // Het aantal floats in een kleur (r, g, b, a)
    static final int SHORTS_PER_INDEX = 2;  
    static final int BYTES_PER_FLOAT = 4;   
    static final int BYTES_PER_SHORT = 2;   

    static final int BYTES_PER_VERTEX = FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
    static final int BYTES_PER_COLOR = FLOATS_PER_COLOR * BYTES_PER_FLOAT;
    static final int BYTES_PER_INDEX_ENTRY = SHORTS_PER_INDEX * BYTES_PER_SHORT;
    
    public boolean isSelected;

    public Sphere(float radius, int stacks, int slices, final Context activityContext, Renderer render)
    {
    	this.mActivityContext = activityContext;
		this.mRenderer = render;

        vertexCount         = (stacks+1) * (slices+1);
        vertexBufferSize    = vertexCount * BYTES_PER_VERTEX;
        colorBufferSize     = vertexCount * BYTES_PER_COLOR;
        indexBufferSize     = vertexCount * BYTES_PER_INDEX_ENTRY;

        // Setup vertex-array buffer. Vertices in float. A float has 4 bytes.
        vertexBuffer = ByteBuffer.allocateDirect(vertexBufferSize).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer = ByteBuffer.allocateDirect(colorBufferSize).order(ByteOrder.nativeOrder()).asFloatBuffer();
        indexBuffer = ByteBuffer.allocateDirect(indexBufferSize).order(ByteOrder.nativeOrder()).asShortBuffer();    

        generateSphereCoords(radius, stacks, slices);

        vertexBuffer.position(0);
        colorBuffer.position(0);
        indexBuffer.position(0);
    }
    
    public void draw(float[] mVMatrix, float[] mProjMatrix)
    {
    	
    	 // No culling of back faces so we can see through things
      	GLES20.glDisable(GLES20.GL_CULL_FACE);
      	// No depth testing
      	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
      	
      	GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
    	
    	// Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);
      
        // Set program handles for drawing.
        mRenderer.mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mRenderer.mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
		mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
		mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "v_Color");
		
        // Translate the  into the screen.
        Matrix.setIdentityM(mRenderer.mModelMatrix, 0); 
        Matrix.translateM(mRenderer.mModelMatrix, 0, position.x, position.y, position.z);
        Matrix.scaleM(mRenderer.mModelMatrix, 0, scale.x, scale.y, scale.z);
       
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.x, 1, 0,0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.y, 0, 1,0);
        Matrix.rotateM(mRenderer.mModelMatrix, 0, rotation.z, 0, 0,1);
        
        float color[] = { 0.0f, 0.0f, 1.0f, 1.0f };
        
        if (isSelected)
		{
        	color = new float[] { 0.0f, 1.0f, 1.0f, 1.0f };
		}
        
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
     
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, BYTES_PER_VERTEX, vertexBuffer);
        
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
      
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);    
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, vertexCount *2,GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        
        
        //public native void glDrawElements (int mode, int count, int type, int indices);
    }

    private void generateSphereCoords(float radius, int stacks, int slices)
    {
        for (int stackNumber = 0; stackNumber <= stacks; ++stackNumber)
        {
            for (int sliceNumber = 0; sliceNumber < slices; ++sliceNumber)
            {
                float theta = (float) (stackNumber * Math.PI / stacks);
                float phi = (float) (sliceNumber * 2 * Math.PI / slices);
                float sinTheta = (float) java.lang.Math.sin(theta); 
                float sinPhi = (float) java.lang.Math.sin(phi);
                float cosTheta = (float) java.lang.Math.cos(theta);
                float cosPhi = (float)java.lang.Math.cos(phi);
                vertexBuffer.put(new float[]{radius * cosPhi * sinTheta, radius * sinPhi * sinTheta, radius * cosTheta});
            }
        }

        for (int stackNumber = 0; stackNumber < stacks; ++stackNumber)
        {
            for (int sliceNumber = 0; sliceNumber <= slices; ++sliceNumber)
            {
                indexBuffer.put((short) ((stackNumber * slices) + (sliceNumber % slices)));
                indexBuffer.put((short) (((stackNumber + 1) * slices) + (sliceNumber % slices)));
            }
        }
    }
}