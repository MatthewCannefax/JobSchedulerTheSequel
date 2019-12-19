package com.matthewcannefax.jobschedulerthesequel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 0;

    Button bSchedule;
    Button bCancel;
    Switch swDeviceIdle;
    Switch swDeviceCharging;
    SeekBar sbSeekBar;


    private JobScheduler mScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bSchedule = findViewById(R.id.schedule_button);
        bCancel = findViewById(R.id.cancel_button);

        swDeviceIdle = findViewById(R.id.idle_switch);
        swDeviceCharging = findViewById(R.id.charging_switch);

        sbSeekBar = findViewById(R.id.seekBar);

        final TextView seekBarProgress = findViewById(R.id.seekBarProgress);

        sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i > 0){
                    seekBarProgress.setText(Integer.toString(i) + "s");
                }else{
                    seekBarProgress.setText("Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleJob();
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelJob();
            }
        });
    }

    private void cancelJob() {
        if(mScheduler != null){
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, "Jobs Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void scheduleJob(){

        int seekBarInt = sbSeekBar.getProgress();
        boolean seekBarSet = seekBarInt > 0;

        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        RadioGroup networkOptions = findViewById(R.id.networkOptions);

        int selectedNetworkId = networkOptions.getCheckedRadioButtonId();

        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;

        switch (selectedNetworkId){
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName serviceName = new ComponentName(
                getPackageName(),
                NotificationJobService.class.getName());

        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName);
        builder.setRequiredNetworkType(selectedNetworkOption)
        .setRequiresDeviceIdle(swDeviceIdle.isChecked())
        .setRequiresCharging(swDeviceCharging.isChecked());

        if(seekBarSet){
            builder.setOverrideDeadline(seekBarInt * 1000);
        }

        boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE)
                || swDeviceCharging.isChecked() ||swDeviceIdle.isChecked() || seekBarSet;


        if (constraintSet) {
            JobInfo myJobInfo = builder.build();
            mScheduler.schedule(myJobInfo);
            Toast.makeText(this, "Job Scheduled, job will run when the constraints are met.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Job requires network connection", Toast.LENGTH_SHORT).show();
        }


    }
}
