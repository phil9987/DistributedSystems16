package ch.ethz.inf.vs.kellerd.vs_kellerd_sensors;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by philipjunker on 05/10/16.
 */


public class GraphContainerImpl implements GraphContainer{
    private float[][] y_values;
    private double[] x_values;
    private int num_values;
    private int curr_idx;
    private double max_x;

    public GraphContainerImpl(int dimension){
        y_values = new float[100][];
        for (int i=0; i<100; i++){
            y_values[i] = new float[dimension];
        }
        x_values = new double[100];
        curr_idx = 0;
        num_values = dimension;
        max_x = 0;
    }

    @Override
    public void addValues(double xIndex, float[] values) {
        if(values.length != num_values || xIndex < max_x){
            //Log.d("DEBUG num_values = ", String.valueOf(num_values));
            //Log.d("DEBUG values length = ", String.valueOf(values.length));
            throw new ArithmeticException("Length of values incorrect!");
        }
        else {
            if (curr_idx == 100) {
                //shift all elements by one to the left, erasing the oldest one on the leftmost place
                for (int k = 1; k < curr_idx; k++) {
                    for (int i = 0; i < num_values; i++) {
                        y_values[k - 1][i] = y_values[k][i];
                    }
                    x_values[k - 1] = x_values[k];
                    //Log.d("idx: ", String.valueOf(k - 1));
                    //Log.d("value-write-check X: ", String.valueOf(x_values[k - 1]));
                }
                curr_idx -= 1;
            }
            x_values[curr_idx] = xIndex;
            max_x = xIndex;
            //Log.d("value-write-check X: ", String.valueOf(x_values[curr_idx]));
            for (int i = 0; i < num_values; i++) {
                y_values[curr_idx][i] = values[i];
                //Log.d("value-write-check Y: ", String.valueOf(values[i]));
            }

            //Log.d("value-write-check (x): ", String.valueOf(xIndex));
            curr_idx += 1;
        }
        return;
    }

    @Override
    public float[][] getValues() {
        float[][] ret = new float[curr_idx][];
        //Log.d("DEBUG curr_idx = ", String.valueOf(curr_idx));
        for(int i = 0; i < curr_idx; i++){
            ret[i] = y_values[i].clone();
        }
        //Log.d("DEBUG y_values size = ", String.valueOf(y_values.length));
        //Log.d("DEBUG y_values size2 = ", String.valueOf(y_values[0].length));
        return ret;
    }

    public DataPoint[] generateData(int val_dimension) {
        int count = curr_idx;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            DataPoint v = new DataPoint(x_values[i], y_values[i][val_dimension]);
            values[i] = v;
            //Log.d("value-check: ", String.valueOf(v));
        }
        return values;
    }
}
