package com.bobr.exam;

import android.app.Application;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.bobr.exam.net.PingWorker;
import com.bobr.exam.ui.util.ThemeHelper;
import java.util.concurrent.TimeUnit;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class BobrExamApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applySaved(this);
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(new OneTimeWorkRequest.Builder(PingWorker.class).build());
        PeriodicWorkRequest ping = new PeriodicWorkRequest.Builder(
                PingWorker.class, 15, TimeUnit.MINUTES).build();
        wm.enqueueUniquePeriodicWork("render-ping", ExistingPeriodicWorkPolicy.KEEP, ping);
    }
}
