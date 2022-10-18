package com.example.proba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.health.services.client.data.DataPoint;
import androidx.health.services.client.data.PassiveMonitoringUpdate;
import androidx.health.services.client.data.UserActivityInfo;
import androidx.health.services.client.data.UserActivityState;


import com.example.proba.data.ActivityChange;
import com.example.proba.data.ActivityChangeRepository;
import com.example.proba.data.Bpm;
import com.example.proba.data.BpmRepository;
import com.example.proba.data.Sleep;
import com.example.proba.data.SleepDatabase;
import com.example.proba.data.SleepRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PassiveDataReceiver extends BroadcastReceiver {

    private static final String SECONDS_KEY = "com.example.deepsleep.seconds";
    private static final String START_KEY = "com.example.deepsleep.start";
    private static final String END_KEY = "com.example.deepsleep.end";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(PassiveMonitoringUpdate.ACTION_DATA)) return;
        PassiveMonitoringUpdate update = PassiveMonitoringUpdate.fromIntent(intent);
        if (update == null) return;

        System.out.println("------------ON RECEIVE---------------------");

        // Working directly with repositories, because broadcast receiver has no appropriate context
        SleepDatabase sleepDatabase = SleepDatabase.getInstance(context);
        SleepRepository sleepRepository = new SleepRepository(sleepDatabase.sleepDao());
        ActivityChangeRepository activityChangeRepository = new ActivityChangeRepository(sleepDatabase.activityChangeDao());
        BpmRepository bpmRepository = new BpmRepository(sleepDatabase.bpmDao());


        List<DataPoint> dataPoints = update.getDataPoints();
        List<UserActivityInfo> userActivityInfoUpdates = update.getUserActivityInfoUpdates();


        for (int i = 0; i < userActivityInfoUpdates.size(); i++){
            UserActivityInfo userActivityInfo = userActivityInfoUpdates.get(i);

            UserActivityState state = userActivityInfo.getUserActivityState();
            ZonedDateTime time = userActivityInfo.getStateChangeTime().atZone(ZoneId.of("Europe/Belgrade"));


            ActivityChange lastChange = activityChangeRepository.getLastChange();

            switch (state){
                case USER_ACTIVITY_ASLEEP:

                    if (lastChange == null || lastChange.getTime().toEpochSecond() != time.toEpochSecond()){
                        activityChangeRepository.insert(new ActivityChange(0, "ASLEEP", time));
                        System.out.println("SUCCESSFUL INSERTED ASLEEP TIME");
                    }
                    System.out.println("ASLEEP TIME: " + time);


                    break;
                case USER_ACTIVITY_PASSIVE:

                    if (lastChange == null || lastChange.getTime().toEpochSecond() != time.toEpochSecond()){
                        activityChangeRepository.insert(new ActivityChange(0, "AWAKE", time));
                        System.out.println("SUCCESSFUL INSERTED AWAKE TIME");
                    }

                    System.out.println("AWAKE TIME: " + time);

                    if (lastChange != null && lastChange.getNewState().equals("ASLEEP")){
                        long seconds = ChronoUnit.SECONDS.between(lastChange.getTime(), time);
                        sleepRepository.insert(new Sleep(0, seconds, lastChange.getTime(), time));
                        System.out.println("SLEEP INSERTED - duration: " + (seconds / 60) + ":" + (seconds % 60) );


                        DataClient dataClient = Wearable.getDataClient(context);

                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sleep").setUrgent();
                        putDataMapReq.getDataMap().putLong(SECONDS_KEY, seconds);
                        putDataMapReq.getDataMap().putLong(START_KEY, lastChange.getTime().toEpochSecond());
                        putDataMapReq.getDataMap().putLong(END_KEY, time.toEpochSecond());
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();

                        Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);
                        putDataTask.addOnSuccessListener(dataItem -> System.out.println("Successful transfer!"));
                        putDataTask.addOnFailureListener(exception -> System.out.println("Failure transfer!"));


                    }

                    break;
            }

        }



        // ?? kasnije dodati samo kad spava da se unose bpmovi, za sada neka ostane ovako

        for (int i = 0; i < dataPoints.size(); i++){
            DataPoint dataPoint = dataPoints.get(i);
            Double value = dataPoint.getValue().asDouble(); // zasto double kad je int??

            Instant bootInstant =
                    Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime());

            // start and end time are the same - data sampled at a single point in time
            ZonedDateTime time = dataPoint.getStartInstant(bootInstant).atZone(ZoneId.of("Europe/Belgrade"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String timeString = time.format(formatter);

            System.out.println("INSERTED BPM VALUE: " + value + "BPM TIME: " + time);
            Bpm bpm = new Bpm(0, value, time, timeString);
            bpmRepository.insert(bpm);


        }



        System.out.println("DATA POINTS");
        System.out.println(dataPoints);

        System.out.println("USER ACTIVITY INFO");
        System.out.println(userActivityInfoUpdates);
    }
}
