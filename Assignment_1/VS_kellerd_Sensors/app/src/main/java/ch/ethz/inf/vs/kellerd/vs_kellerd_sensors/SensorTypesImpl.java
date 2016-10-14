package ch.ethz.inf.vs.kellerd.vs_kellerd_sensors;

import android.hardware.Sensor;

public class SensorTypesImpl implements SensorTypes {
    public int getNumberValues(int sensorType){
        int numberValues = 0;
        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                numberValues = 3;
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                numberValues = 1;
                break;
            case Sensor.TYPE_GYROSCOPE:
                numberValues = 3;
                break;
            case Sensor.TYPE_LIGHT:
                numberValues = 1;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                numberValues = 3;
                break;
            case Sensor.TYPE_ORIENTATION:
                numberValues = 3;
                break;
            case Sensor.TYPE_PRESSURE:
                numberValues = 1;
                break;
            case Sensor.TYPE_PROXIMITY:
                numberValues = 1;
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                numberValues = 1;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                numberValues = 3;
                break;
            default: numberValues = 1;
                break;
        }
		return numberValues;
    }

    public String getUnitString(int sensorType){
		String unit = "";
        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                unit = "m/s^2";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                unit = "°C";
                break;
            case Sensor.TYPE_GYROSCOPE:
                unit = "rad/s";
                break;
            case Sensor.TYPE_LIGHT:
                unit = "lx";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                unit = "microT";
                break;
            case Sensor.TYPE_ORIENTATION:
            case Sensor.TYPE_ROTATION_VECTOR:
                unit = "no unit";
                break;
            case Sensor.TYPE_PRESSURE:
                unit = "hPa";
                break;
            case Sensor.TYPE_PROXIMITY:
                unit = "cm";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                unit = "%";
                break;
            default: unit = "no unit";
                break;
        }
       return unit;
    }
}
