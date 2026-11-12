package com.bobr.exam.net;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PingWorker extends Worker {

    public PingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            OkHttpClient c = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request r = new Request.Builder()
                    .url("https://bobrexam.onrender.com/")
                    .get()
                    .build();
            Response resp = c.newCall(r).execute();
            resp.close();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
