package com.example.proba;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.HealthServicesClient;
import androidx.health.services.client.PassiveMonitoringClient;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.PassiveMonitoringCapabilities;
import androidx.health.services.client.data.PassiveMonitoringConfig;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;


import com.example.proba.data.Sleep;
import com.example.proba.data.SleepDatabase;
import com.example.proba.data.SleepRepository;
import com.example.proba.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;


public class MainActivity extends Activity implements View.OnTouchListener{

    private ActivityMainBinding binding;
    public static final String APP_TAG = "MIKA";

    private HealthServicesClient healthServicesClient = null;
    private PassiveMonitoringClient passiveClient = null;
    private boolean permissionGranted = false;
    private boolean supportsHeartRate = false;


    private GestureDetector gestureDetector;

    private SleepDatabase sleepDatabase;
    private SleepRepository sleepRepository;
    private Sleep sleepShown;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.");


    private final int MAX_SHOWN_SLEEPS = 8;
    private int currentNumber = 0;

    private boolean msgShown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.out.println("--------------- ON CREATE -------------------------");
        System.out.println("#############################################################");

        permissionGranted = checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED
                && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED;

        if (!permissionGranted)
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION}, 0);


        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        healthServicesClient = HealthServices.getClient(this);
        passiveClient = healthServicesClient.getPassiveMonitoringClient();

        sleepDatabase = SleepDatabase.getInstance(this);
        sleepRepository = new SleepRepository(sleepDatabase.sleepDao());


        fillData();


        gestureDetector = new GestureDetector(this, new OnSwipeListener() {

            @Override
            public boolean onSwipe(Direction direction) {
                if (direction == Direction.left) {
                    //do your stuff
                    Log.d("MIKA", "onSwipe: left");
                    onLeftSwipe();

                }
                if (direction == Direction.right) {
                    //do your stuff
                    Log.d("MIKA", "onSwipe: right");
                    onRightSwipe(true);
                }
                return true;
            }


        });

        binding.getRoot().setOnTouchListener(this);


        binding.getRoot().setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_SCROLL &&
                        ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
                ) {
                    Log.d("MIKA", "Rotira se");
                    // Don't forget the negation here
                    float delta = -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                            ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                    ViewConfiguration.get(getApplicationContext()), getApplicationContext()
                            );

                    System.out.println("<< Delta is " + delta);
                    if (delta < 0){
                        onRightSwipe(false);
                    }
                    else{
                        onLeftSwipe();
                    }
                    return true;
                }
                return false;
            }
        });
        //binding.getRoot().requestFocus();

    }

    public void checkCapabilities(){
        ListenableFuture<PassiveMonitoringCapabilities> capabilitiesFuture = passiveClient.getCapabilities();
        Futures.addCallback(capabilitiesFuture,
                new FutureCallback<PassiveMonitoringCapabilities>() {
                    @Override
                    public void onSuccess(@Nullable PassiveMonitoringCapabilities result) {

                        if (result == null) return;
                        Set<DataType> supportedDataTypes = result.getSupportedDataTypesPassiveMonitoring();


                        supportsHeartRate = supportedDataTypes
                                .contains(DataType.HEART_RATE_BPM);


                        Log.d(APP_TAG, "Supports HeartRate " + supportsHeartRate);
                        if (supportsHeartRate && permissionGranted){
                            startPassiveReceiver();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // display an error
                        Log.d(APP_TAG, "Error with receiving capabilitites");
                    }
                },
                ContextCompat.getMainExecutor(this /*context*/));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            permissionGranted = true;
            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionGranted = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Could not get activity permissions", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
            }

            if (permissionGranted){
                Log.d(APP_TAG, "All permissions granted");
                checkCapabilities();
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startPassiveReceiver(){

        Log.d(APP_TAG, "STARTING PASSIVE RECEIVER");
        PassiveMonitoringConfig config = PassiveMonitoringConfig.builder()
                .setDataTypes(Collections.singleton(DataType.HEART_RATE_BPM))
                .setComponentName(new ComponentName(getPackageName(), PassiveDataReceiver.class.getName()))
                .setShouldIncludeUserActivityState(true)
                .build();

        ListenableFuture<Void> connectedFuture = passiveClient.registerDataCallback(config);

        Futures.addCallback(connectedFuture,
                new FutureCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        Log.d(APP_TAG, "Connected receiver");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(APP_TAG, "Failed connecting to receiver");
                        try {
                            throw (t);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                },
                ContextCompat.getMainExecutor(this /*context*/));
    }

    private void fillData(){

        sleepShown = sleepRepository.getLastSleep();
        if (sleepShown != null){
            showInfoAboutSleep();
        }
    }

    private void showInfoAboutSleep(){
        binding.sleepDuration.setText(getDurationString(sleepShown.getDuration()));
        binding.startTime.setText(sleepShown.getStart().format(timeFormatter));
        binding.endTime.setText(sleepShown.getEnd().format(timeFormatter));
        binding.startDate.setText(sleepShown.getStart().format(dateFormatter));
        binding.endDate.setText(sleepShown.getEnd().format(dateFormatter));
    }

    private String getDurationString(long seconds){
        return (seconds/3600) + getString(R.string.hours_label) + " " + ((seconds / 60) % 60) + getString(R.string.minutes_label);
    }

    private void changeFont(int increment){
        float px = binding.sleepDuration.getTextSize();
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float sp = px / scaledDensity;
        float newSP = sp + increment;
        binding.sleepDuration.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSP);


        float timeSize = getResources().getDimension(R.dimen.time_text_size) / scaledDensity;
        float dateSize = getResources().getDimension(R.dimen.date_text_size) / scaledDensity;
        float timeSizeSmaller = getResources().getDimension(R.dimen.time_text_size_smaller) / scaledDensity;
        float dateSizeSmaller = getResources().getDimension(R.dimen.date_text_size_smaller) / scaledDensity;


        if (newSP < 25){
            binding.startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeSizeSmaller);
            binding.endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeSizeSmaller);
            binding.startDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSizeSmaller);
            binding.endDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSizeSmaller);
        }
        else{
            binding.startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeSize);
            binding.endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeSize);
            binding.startDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSize);
            binding.endDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSize);
        }
        Log.d("MIKA", "NEW SP:" + newSP);


    }


    private void onRightSwipe(boolean swipe){
        if (msgShown){
            if (swipe) finish();
            return;
        }
        if (currentNumber == MAX_SHOWN_SLEEPS || sleepShown == null){
            showMsgToLeave();
            return;
        }
        if (sleepShown != null){
            if (currentNumber < MAX_SHOWN_SLEEPS) {
                Sleep sleep = sleepRepository.getPreviousSleep(sleepShown.getId());
                if (sleep != null) {

                    currentNumber++;
                    Log.d("MIKA", "cur num" + currentNumber);
                    changeFont(-2);
                    sleepShown = sleep;
                    showInfoAboutSleep();

                }
                else showMsgToLeave();
            }

        }

    }

    private void onLeftSwipe(){
        if (msgShown){
            disappearMsgToLeave();
            if (sleepShown != null){
                showInfoAboutSleep();
            }
            return;
        }
        if (sleepShown != null){
            Sleep sleep = sleepRepository.getNextSleep(sleepShown.getId());
            if (sleep != null){
                currentNumber--;
                Log.d("MIKA", "cur num"+ currentNumber);
                sleepShown = sleep;
                changeFont(2);
                showInfoAboutSleep();
            }

        }
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Log.d("MIKA", "onTouch: ");
        gestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private void showMsgToLeave(){
        msgShown = true;

        binding.messageNoMore.setVisibility(View.VISIBLE);
        binding.startDate.setVisibility(View.INVISIBLE);
        binding.startTime.setVisibility(View.INVISIBLE);
        binding.endDate.setVisibility(View.INVISIBLE);
        binding.endTime.setVisibility(View.INVISIBLE);
        binding.sleepDuration.setVisibility(View.INVISIBLE);
        binding.separator.setVisibility(View.INVISIBLE);
        binding.moonImage.setVisibility(View.INVISIBLE);

    }

    private void disappearMsgToLeave(){
        msgShown = false;

        binding.messageNoMore.setVisibility(View.INVISIBLE);
        binding.startDate.setVisibility(View.VISIBLE);
        binding.startTime.setVisibility(View.VISIBLE);
        binding.endDate.setVisibility(View.VISIBLE);
        binding.endTime.setVisibility(View.VISIBLE);
        binding.sleepDuration.setVisibility(View.VISIBLE);
        binding.separator.setVisibility(View.VISIBLE);
        binding.moonImage.setVisibility(View.VISIBLE);

    }
}