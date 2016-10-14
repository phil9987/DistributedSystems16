package ch.ethz.inf.vs.a1.kellerd.antitheft;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.R.drawable;
import android.util.Log;

/**
 * Created by simon on 04.10.16.
 * Service that does starts to initialize the AntiTheft functionality and register the listener
 * to the sensor. If theft detected, waits for delay defined by the user and starts the alarm sound.
 */

public class AntiTheftService extends Service implements AlarmCallback {

    public static final String ACTIVITY_TAG = "### Anti-T Service ###";

    private MediaPlayer player;
    private NotificationManager notificationManager;
    private AbstractMovementDetector listener;


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    /**
     * Gets invoked after detection of critical movement of the device, waits for the predefined
     * delay and starts ringing the alarm sound
     */
    @Override
    public void onDelayStarted() {
        //Unregister listener to save battery, no more sensor data needed.
        SensorManager sMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sMgr.unregisterListener(listener);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(drawable.alert_light_frame)
                .setContentTitle("AntiTheft")
                .setContentText("Theft alarm activated. Alarm rings after delay expired!");
        mBuilder.setOngoing(true);
        notificationManager.notify(2,mBuilder.build());
        notificationManager.cancel(1);
        //Get delay from user preferences or use the default if user preferences not available
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int defDelay = R.string.delayDefault;

        int delay = Integer.parseInt(sharedPreferences.getString("delay", Integer.toString(defDelay)));
        int waitTime = delay*1000;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!player.isPlaying()){

                    player.start();
                }
            }
        },waitTime);

    }

    /**
     * Initialize AntiTheftService and register the selected Listener to the linear acceleration
     * sensor.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(drawable.alert_light_frame)
                        .setContentTitle("AntiTheft")
                        .setContentText("Device protected by VS AntiTheft");

        mBuilder.setOngoing(true);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,mBuilder.build());


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        //Prepare media player to play beep sound
        int sound = R.raw.beep;
        player = MediaPlayer.create(getApplicationContext(), sound);
        player.setVolume(1.0f, 1.0f);
        player.setLooping(true);


        int sensitivity = Integer.parseInt(prefs.getString("sensitivity", getString(R.string.sensDefault)))/10;

        //Start movement detector according to preferences
        boolean advanced = prefs.getBoolean("advanced", false);

        listener = new SpikeMovementDetector(this,sensitivity);
        if (advanced) {
            listener = new AdvancedMovementDetector(this,sensitivity);
        }

        SensorManager sMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sMgr.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    /**
     * Cleanup on stop of the AntiTheftService. Stop alarm sound, clear notifications
     */
    @Override
    public void onDestroy(){
        //Stop alarm sound
        player.stop();
        //Delete all notifications
        notificationManager.cancelAll();
    }
}







