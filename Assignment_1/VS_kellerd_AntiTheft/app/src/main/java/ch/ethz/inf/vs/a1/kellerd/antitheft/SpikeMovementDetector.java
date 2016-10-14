package ch.ethz.inf.vs.a1.kellerd.antitheft;

import static java.lang.Math.abs;

public class SpikeMovementDetector extends AbstractMovementDetector {


    /**
     * @param callback Callback function that's called
     * @param sensitivity Sensitivity to detect movement and classify it as a critical one
     */
    public SpikeMovementDetector(AlarmCallback callback, int sensitivity) {
        super(callback, sensitivity);
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
        boolean phoneMoved = mov >=sensitivity;
        return phoneMoved;

    }

    /**
     *
     * @param vals Sensor values to compute movement
     * @return float; Processed sensor data to classify movement
     */
    private float movement(float[] vals){
        float res = 0;
        for (int i = 0; i<3; i++){
            res += abs(vals[i]);
        }
        return res;
    }
}
