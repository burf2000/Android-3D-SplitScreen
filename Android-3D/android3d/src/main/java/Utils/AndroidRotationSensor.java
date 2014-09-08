package Utils;

/**
 * TODO: get full accelerometer and gyroscope data
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;

import java.util.List;

public class AndroidRotationSensor implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    float pitch = 0.0f;
    int axisSwapper = -1;

    private final double RADIANS_TO_DEGREES = 180 / Math.PI;
    private final float[] sZVector = {0, 0, 1, 1};
    private float mRotationMatrix_inv[] = new float[16];
    private float orientationVector[] = new float[4];
    private float azimuthVector[] = new float[4];
    private float orientation;//up direction
    private float azimuth;//aim to north

    private float mRotationMatrix[] = new float[16];
    private float orientationVals[] = new float[3];

    //static final float ALPHA = 0.20f;

    public AndroidRotationSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        registerSensor();
    }

    private void registerSensor() {
        if (sensorManager != null) {
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                if (!sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME))//faster than SENSOR_DELAY_NORMAL
                {
                    return;
                }
            }

            sensors = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
            if (sensors.size() > 0) {
                Sensor sensor = sensors.get(0);
                if (!sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME))//faster than SENSOR_DELAY_NORMAL
                {
                    return;
                }
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();

        //Log.i("TAG", "Sensor " + type);
//TODO http://stackoverflow.com/questions/7598574/how-to-improve-accuracy-of-accelerometer-and-compass-sensors
        //todo http://stackoverflow.com/questions/6911900/android-remove-gravity-from-accelerometer-readings/6912977#6912977

        // It is good practice to check that we received the proper sensor event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //lowPass( event.values.clone(), MagneticFieldValues_last );

            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,
                    event.values);

            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, orientationVals);

            Matrix.multiplyMV(orientationVector, 0, mRotationMatrix, 0, sZVector, 0);
            orientation = (float) (-Math.atan2(orientationVector[0], orientationVector[1]) * RADIANS_TO_DEGREES);

            Matrix.invertM(mRotationMatrix_inv, 0, mRotationMatrix, 0);
            Matrix.multiplyMV(azimuthVector, 0, mRotationMatrix_inv, 0, sZVector, 0);
            azimuth = (float) (180 + Math.atan2(azimuthVector[0], azimuthVector[1]) * RADIANS_TO_DEGREES);

            // Optionally convert the result from radians to degrees
            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);
            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]);
            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]);

        }


        if (type == Sensor.TYPE_MAGNETIC_FIELD) {

            pitch = event.values[2] * axisSwapper;
        }
    }

    public boolean getNowOrientation(float[] retValues) {
        retValues[0] = pitch;
        retValues[1] = orientation;
        retValues[2] = azimuth;
        return true;
    }

//    protected float[] lowPass( float[] input, float[] output ) {
//        if ( output == null ) return input;
//
//        for ( int i=0; i<input.length; i++ ) {
//            output[i] = output[i] + ALPHA * (input[i] - output[i]);
//        }
//        return output;
//    }
//
//	public void swapAxes() {
//		axisSwapper*=-1;
//
//	}

    //TODO pause sensor reading
}
