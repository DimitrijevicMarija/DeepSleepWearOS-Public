package com.example.proba;

import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.PassiveMonitoringClient;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.PassiveMonitoringConfig;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class RegisterForPassiveDataWorker extends Worker {

    private final Context context;
    public RegisterForPassiveDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        PassiveMonitoringClient passiveClient = HealthServices.getClient(context).getPassiveMonitoringClient();
        PassiveMonitoringConfig config = PassiveMonitoringConfig.builder()
                .setDataTypes(Collections.singleton(DataType.HEART_RATE_BPM))
                .setComponentName(new ComponentName("com.example.proba", PassiveDataReceiver.class.getName()))
                .setShouldIncludeUserActivityState(true)
                .build();

        ListenableFuture<Void> connectedFuture = passiveClient.registerDataCallback(config);
        try {
            connectedFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }
}
