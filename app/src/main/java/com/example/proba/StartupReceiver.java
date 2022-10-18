package com.example.proba;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) return;

        boolean permissionGranted = context.checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED
                && context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED;

        if (!permissionGranted) return;

        WorkRequest request = new OneTimeWorkRequest.Builder(RegisterForPassiveDataWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);


    }
}
