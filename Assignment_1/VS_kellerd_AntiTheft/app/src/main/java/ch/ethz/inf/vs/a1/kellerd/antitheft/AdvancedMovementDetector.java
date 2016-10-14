package ch.ethz.inf.vs.a1.kellerd.antitheft;

import static java.lang.Math.abs;

/**
 * Created by simon on 04.10.16.
 * */



public class AdvancedMovementDetector extends AbstractMovementDetector{
    private float res=0;

    /**
     * AdvancedMovementDetector detects if the device is slowly moving by adding up the acceleration
     * values.
     * @param callback Callback function that's called
     * @param sensitivity Sensitivity to detect movement and classify it as a critical one
     */
    public AdvancedMovementDetector(AlarmCallback callback, int sensitivity){
        super(callback,sensitivity);
    }

    /**
     *
     * @param values Sensor values to classify movement
     * @return Boolean to indicate a critical movement. True => device stolen, false => no critical
     *         movement
     */
    @Override
    public boolean doAlarmLogic(float[] values){
        float mov = movement(values);
        //Adjust sensitivity to a reasonable range
        boolean phoneMoved = mov >=sensitivity/10;
        return phoneMoved;

    }
    /**
     *
     * @param actualvals Sensor values to compute movement
     * @return float; Processed sensor data to classify movement
     */
    private float movement(float[] actualvals){
        for (int i = 0; i<3; i++){
            res += abs(actualvals[i]);
        }
        return res;
    }
}

