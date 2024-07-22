package com.example.my_application_1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class LogcatService extends Service {

    private Thread logcatThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logcatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                captureLogcat();
            }
        });
        logcatThread.start();
        return START_STICKY;
    }

    private void captureLogcat() {
        try {
            // Execute the logcat command to get the logs
            Process process = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            FileOutputStream fileOutputStream = openFileOutput("app_logs.txt", MODE_PRIVATE);

            // Read the logs line by line and write them to the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileOutputStream.write((line + "\n").getBytes());
            }

            // Close the streams
            fileOutputStream.close();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (logcatThread != null && logcatThread.isAlive()) {
            logcatThread.interrupt();
        }
    }
}
