package net.atlassc.shinchven.stepcounter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HardwareStepCounterService extends Service implements SensorEventListener {
    private static final int NOTIFICATION_ID = 5092;

    private final IBinder mBinder = new LocalBinder();
    private SensorManager sensorManager;

    public HardwareStepCounterService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {

            float value = event.values[0];
            if (lastFromBoot==0) {
                lastFromBoot =value;
            }
            currentFromBoot = value;
        }
    }

    float currentFromBoot = 0;
    float lastFromBoot = 0;

    public class UpdateTask extends AsyncTask {

        int stepCount = 0;

        @Override
        protected Object doInBackground(Object[] params) {
            float stepsToAddUpTo = currentFromBoot - lastFromBoot;
                StepCounterDB.saveCounting(HardwareStepCounterService.this, "ShinChven", new Date(), (int)
                        stepsToAddUpTo);
                stepCount = StepCounterDB.getStepsByDate(HardwareStepCounterService.this, new Date(), "ShinChven");
                lastFromBoot = currentFromBoot;
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

                String msg = "今日计步：" + stepCount;
                showForegroundNotification(msg);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocalBinder extends Binder {

        public HardwareStepCounterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HardwareStepCounterService.this;
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerSensor();

        int steps = StepCounterDB.getStepsByDate(HardwareStepCounterService.this, new Date(), "ShinChven");
        String msg = "今日计步：" + steps;
        showForegroundNotification(msg);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new UpdateTask().execute();
            }
        }, 5 * 1000, 15 * 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    private void registerSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
    }


    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher

        Class<?> aClass = null;
        try {
            aClass = Class.forName("net.atlassc.shinchven.nativestepcounter.MainActivity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Intent showTaskIntent = new Intent();
        if (aClass != null) {
            showTaskIntent.setClass(getApplicationContext(), aClass);
            showTaskIntent.setAction(Intent.ACTION_MAIN);
            showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }


        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_directions_run)
                .setAutoCancel(false)
                .setContentTitle("我的计步器")
                .setContentText(contentText)
                .setContentIntent(contentIntent);


        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
