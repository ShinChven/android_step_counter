package net.atlassc.shinchven.nativestepcounter;

import android.app.Application;
import android.content.Intent;
import net.atlassc.shinchven.stepcounter.HardwareStepCounterService;

/**
 * Created by ShinChven on 16/4/9.
 */
public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent service = new Intent(this, HardwareStepCounterService.class);
        startService(service);

    }
}
