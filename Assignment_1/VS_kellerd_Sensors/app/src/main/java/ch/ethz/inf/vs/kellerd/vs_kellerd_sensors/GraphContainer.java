package ch.ethz.inf.vs.kellerd.vs_kellerd_sensors;

import com.jjoe64.graphview.series.DataPoint;

import java.io.IOException;

public interface GraphContainer {

    /**
     * Add values to the underlying graph with the corresponding index.
     *
     * @param xIndex The x index.
     * @param values The values. If there is more than one value there should be several series.
     */
    void addValues(double xIndex, float[] values);

    /**
     * Get all values currently displayed in the graph.
     *
     * @return A matrix containing the values in the right order (oldest values first).
     *         The rows are the series, the columns the values for each series.
     */
    float[][] getValues();

    DataPoint[] generateData(int val_dimension);

}
