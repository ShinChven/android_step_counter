package net.atlassc.shinchven.nativestepcounter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import net.atlassc.shinchven.stepcounter.StepCounterDB;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private TextView count;
    boolean activityRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = (TextView) findViewById(R.id.count);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int steps = StepCounterDB.getStepsByDate(MainActivity.this, new Date(), "ShinChven");
                final String msg = "今日计步：" + steps;
                count.post(new Runnable() {
                    @Override
                    public void run() {
                        count.setText(msg);
                    }
                });
            }
        }, 1000, 15 * 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
