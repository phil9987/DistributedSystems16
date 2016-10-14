package ch.ethz.inf.vs.a1.kellerd.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static android.util.JsonToken.NULL;
import static android.util.JsonToken.STRING;

public class DeviceDetailActivity extends AppCompatActivity {
    /*
     * Bluetooth stuff
     */
    protected static String BL_DEVICE_ADDRESS = "BL_DEVICE_ADDRESS";

    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService humidityService;
    private BluetoothGattCharacteristic humidityCharacteristic;
    private BluetoothGattService temperatureService;
    private BluetoothGattCharacteristic temperatureCharacteristic;

    /*
     * UI stuff
     */
    private double starttime;
    private double graph_upper_bound;
    private double graph_lower_bound;

    private GraphView graph;

    private TextView humidityTextView;
    private TextView temperatureTextView;

    LineGraphSeries<DataPoint> humiditySeries;
    LineGraphSeries<DataPoint> temperatureSeries;

    private RelativeLayout loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Intent intent = getIntent();
        String address = intent.getStringExtra(BL_DEVICE_ADDRESS);

        if (address.isEmpty()) {
            Toast.makeText(this, R.string.no_device_selected, Toast.LENGTH_LONG).show();
            finish();
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = bluetoothAdapter.getRemoteDevice(address);

        setTitle(device.getName() == null ? address : device.getName());

        // Configure UI
        humidityTextView = (TextView) findViewById(R.id.humidity);
        temperatureTextView = (TextView) findViewById(R.id.temperature);
        humidityTextView.setText("N/A");
        temperatureTextView.setText("N/A");

        loadingView = (RelativeLayout) findViewById(R.id.loading_view);

        starttime = SystemClock.elapsedRealtime() / 1000;

        // Get graph and add empty data point sets
        graph = (GraphView) findViewById(R.id.graph);
        humiditySeries = new LineGraphSeries<DataPoint>();
        humiditySeries.setColor(Color.RED);
        temperatureSeries = new LineGraphSeries<DataPoint>();
        temperatureSeries.setColor(Color.BLUE);
        graph.addSeries(humiditySeries);
        graph.addSeries(temperatureSeries);

        // Configure graph
        graph.getViewport().setXAxisBoundsManual(true);
        graph_upper_bound = 120;
        graph_lower_bound = 0;
        graph.getViewport().setMinX(graph_lower_bound);
        graph.getViewport().setMaxX(graph_upper_bound);
        GridLabelRenderer labelrenderer = graph.getGridLabelRenderer();
        labelrenderer.setHorizontalAxisTitle("s");
        labelrenderer.setLabelHorizontalHeight(70);
        labelrenderer.setVerticalAxisTitle("% / ˚C");
        labelrenderer.setLabelVerticalWidth(60);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothGatt.disconnect();
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        bluetoothGatt.close();
        super.onDestroy();
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        private BluetoothGattDescriptor humidityDescriptor;
        private BluetoothGattDescriptor temperatureDescriptor;

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            // Humidity notifications should now be enabled
            // Let's also enable temperature change notifications
            if (temperatureDescriptor == null) {
                temperatureDescriptor = new BluetoothGattDescriptor(SensirionSHT31UUIDS.NOTIFICATION_DESCRIPTOR_UUID,
                                                                    BluetoothGattDescriptor.PERMISSION_READ);
                temperatureDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                temperatureCharacteristic.addDescriptor(temperatureDescriptor);
                gatt.writeDescriptor(temperatureDescriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            final float convertedValue = convertRawValue(characteristic.getValue());
            final double timestamp = (SystemClock.elapsedRealtime() / 1000) - starttime;

            if (characteristic == humidityCharacteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        humiditySeries.appendData(new DataPoint(timestamp, convertedValue), true, 150);
                        humidityTextView.setText("Humidity: " + convertedValue + "%");
                    }
                });
            } else if (characteristic == temperatureCharacteristic) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        temperatureSeries.appendData(new DataPoint(timestamp, convertedValue), true, 150);
                        temperatureTextView.setText("Temperature: " + convertedValue + "˚C");
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            humidityService = gatt.getService(SensirionSHT31UUIDS.UUID_HUMIDITY_SERVICE);
            temperatureService = gatt.getService(SensirionSHT31UUIDS.UUID_TEMPERATURE_SERVICE);

            humidityCharacteristic = humidityService.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC);
            temperatureCharacteristic = temperatureService.getCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC);

            // Set up change notifications
            gatt.setCharacteristicNotification(humidityCharacteristic, true);
            gatt.setCharacteristicNotification(temperatureCharacteristic, true);

            humidityDescriptor = new BluetoothGattDescriptor(SensirionSHT31UUIDS.NOTIFICATION_DESCRIPTOR_UUID,
                                                             BluetoothGattDescriptor.PERMISSION_READ);
            humidityDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            humidityCharacteristic.addDescriptor(humidityDescriptor);
            gatt.writeDescriptor(humidityDescriptor);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                fadeOutConnectingView();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                humidityDescriptor = null;
                temperatureDescriptor = null;
            }
        }

        private float convertRawValue(byte[] raw) {
            ByteBuffer wrapper = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            return wrapper.getFloat();
        }
    };

    // http://stackoverflow.com/a/20782483/286611
    private void fadeOutConnectingView() {
        final Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // nop
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nop
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingView.startAnimation(fadeOut);
            }
        });
    }
}
