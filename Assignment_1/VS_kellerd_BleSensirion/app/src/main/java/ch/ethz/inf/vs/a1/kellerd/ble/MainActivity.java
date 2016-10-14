package ch.ethz.inf.vs.a1.kellerd.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

// Sources used to implement this class:
// - https://developer.android.com/guide/topics/connectivity/bluetooth-le.html
// - http://www.truiton.com/2015/04/android-bluetooth-low-energy-ble-example/
// - https://github.com/ranvijaySingh-Webonise/AndroidScanBeaconDevice/
// - https://android.googlesource.com/platform/development/+/cefd49aae65dc85161d08419494071d74ffb982f/samples/BluetoothLeGatt/src/com/example/bluetooth/le/DeviceScanActivity.java
//
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private List<ScanFilter> filters;
    private ScanSettings settings = new ScanSettings.Builder()
                                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                            .build();

    private ArrayAdapter<BluetoothDevice> devicesListAdapter;

    private ProgressBar progressBar;
    private Button restartButton;

    // Timeout for one round of scanning
    // 10s. Inspired by https://developer.android.com/guide/topics/connectivity/bluetooth-le.html#find
    private static final long MAX_SCAN_TIME = 10000;

    // Request constants
    private static final int REQUEST_ENABLE_BTLE = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_ENABLE_LOCATION = 3;

    // Check for location
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private PendingResult<LocationSettingsResult> mPendingLocationSettingsResult;
    private boolean mNonModalEnableLocationDialogue = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.main_activity_title);

        // Check if this device supports bluetooth le
        // Adapted from https://developer.android.com/guide/topics/connectivity/bluetooth-le.html#permissions
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.bluetooth_le_not_supported, Toast.LENGTH_LONG).show();
            finish();
        }

        // Set up ListView
        devicesListAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1);
        ListView devicesList = (ListView) findViewById(R.id.device_list);
        devicesList.setAdapter(devicesListAdapter);
        devicesList.setOnItemClickListener(listViewClickHanlder);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        restartButton = (Button) findViewById(R.id.restart_button);

        filters = new ArrayList<ScanFilter>();
        ScanFilter scanFilter;
        scanFilter = new ScanFilter.Builder().setDeviceName("Smart Humigadget").build();
        filters.add(scanFilter);

        // Set up api client to check for location availablity
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // No manager needed
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothLeScanner = null;
    }

    @Override
    protected void onPause() {
        scanForBleDevices(false);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BTLE:
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.ble_needed, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (hasLocationPermission(true)) {
                        checkEnabledLocation();
                    }
                    return;
                }
                break;

            case REQUEST_ENABLE_LOCATION:
                if (resultCode == Activity.RESULT_CANCELED) {
                    // If we open the settings to enable Location we always end up in this case
                    if (!mNonModalEnableLocationDialogue) {
                        Toast.makeText(this, R.string.location_needed, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        scanForBleDevices(true);
                        return;
                    }
                } else {
                    scanForBleDevices(true);
                    return;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /***********************************************************************************************
     * Check for permissions
     **********************************************************************************************/

    private void checkPermissions() {
        // Check if bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BTLE);
        } else {
            if (hasLocationPermission(true)) {
                checkEnabledLocation();
            }
        }
    }

    /***********************************************************************************************
     * Ask for `ACCESS_FINE_LOCATION` needed for blescanning
     * Adapted from https://developer.android.com/training/permissions/requesting.html
     **********************************************************************************************/

    private boolean hasLocationPermission(final boolean ask) {
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ask) {
                // Ask user for access
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean hasLocationPermission() {
        return hasLocationPermission(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkEnabledLocation();
            }
        }
    }

    /***********************************************************************************************
     * Check for location enabled
     **********************************************************************************************/

    private void checkEnabledLocation() {
        // http://stackoverflow.com/a/4239019/286611
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // Show the nicer dialogue if possible (that is when an internet connection exists)
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            mNonModalEnableLocationDialogue = false;
            if (mLocationSettingsRequest == null) {
                // From https://developer.android.com/training/location/change-location-settings.html#location-request
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(100000);
                locationRequest.setFastestInterval(50000);
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

                mLocationSettingsRequest = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .build();
            }

            mPendingLocationSettingsResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                    mLocationSettingsRequest);

            mPendingLocationSettingsResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();
                    final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // Success
                            scanForBleDevices(true);
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Ask for permission
                            try {
                                status.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_ENABLE_LOCATION
                                );
                            } catch (IntentSender.SendIntentException e) {
                                // nop
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // nop
                            break;
                    }
                }
            });
        } else {
            mNonModalEnableLocationDialogue = true;
            int locationSettingState = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, -1);

            if (locationSettingState == Settings.Secure.LOCATION_MODE_OFF) {
                Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Toast.makeText(this, R.string.enable_location, Toast.LENGTH_LONG).show();
                startActivityForResult(enableLocation, REQUEST_ENABLE_LOCATION);
            } else {
                scanForBleDevices(true);
            }
        }
    }

    /***********************************************************************************************
     * Scan for BLE devices
     **********************************************************************************************/

    private void scanForBleDevices(final boolean start) {
        if (isScanning()) {
            // We don't start a new scan if there is one running
            return;
        }

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner == null) {
                // Still null -> happens when BT is not enabled
                return;
            }
        }

        if (start) {
            // Timer to stop scan after `MAX_SCAN_TIME`
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    bluetoothLeScanner.stopScan(scanCallback);
                    setScanning(false);
                }
            }, MAX_SCAN_TIME);

            setScanning(true);
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        } else {
            handler.removeCallbacks(runnable);
            bluetoothLeScanner.stopScan(scanCallback);
            setScanning(false);
        }
    }

    protected ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int index = devicesListAdapter.getPosition(device);
            // http://stackoverflow.com/a/6715287/286611
            if (index == -1) {
                devicesListAdapter.add(device);
                devicesListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result: results) {
                BluetoothDevice device = result.getDevice();
                int index = devicesListAdapter.getPosition(device);
                // http://stackoverflow.com/a/6715287/286611
                if (index == -1) {
                    devicesListAdapter.add(device);
                }
            }
            devicesListAdapter.notifyDataSetChanged();
        }
    };

    /***********************************************************************************************
     * Handle taps in list view
     **********************************************************************************************/

    private AdapterView.OnItemClickListener listViewClickHanlder = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent deviceDetailIntent = new Intent(view.getContext(), DeviceDetailActivity.class);
            BluetoothDevice device = devicesListAdapter.getItem(position);
            deviceDetailIntent.putExtra(DeviceDetailActivity.BL_DEVICE_ADDRESS, device.getAddress());
            scanForBleDevices(false);
            startActivity(deviceDetailIntent);
        }
    };

    /***********************************************************************************************
     * Restart button
     **********************************************************************************************/

    public void onClickRestartButton(View v) {
        if (!isScanning()) {
            devicesListAdapter.clear();
            devicesListAdapter.notifyDataSetChanged();
            checkPermissions();
        }
    }

    /***********************************************************************************************
     * Getter and setter
     **********************************************************************************************/

    public boolean isScanning() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;

        if (scanning) {
            progressBar.setVisibility(View.VISIBLE);
            restartButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            restartButton.setEnabled(true);
        }
    }

    /***********************************************************************************************
     * GoogleApiClient.ConnectionCallbacks
     **********************************************************************************************/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // nop
        // We use the api client only for the nicer dialogue to enable Location
    }

    @Override
    public void onConnectionSuspended(int i) {
        // nop
        // We use the api client only for the nicer dialogue to enable Location
    }

    /***********************************************************************************************
     * GoogleApiClient.OnConnectionFailedListener
     **********************************************************************************************/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // nop
        // We use the api client only for the nicer dialogue to enable Location
    }
}
