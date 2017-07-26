package com.superpowered.frequencydomain;


import android.os.Build;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;

import android.view.View.OnClickListener;
import android.view.View;
import android.widget.RadioButton;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Button cutFButton = (Button) findViewById(R.id.SetCutF);
        final EditText inputFreq = (EditText) findViewById(R.id.inputCutF);
        cutFButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNewCutFrequency(Integer.parseInt(inputFreq.getText().toString()));
            }
        });

        Button detectionButton = (Button) findViewById(R.id.SetDetectionWindow);
        final EditText inputDetection = (EditText) findViewById(R.id.inputSetDetectionWindow);
        detectionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNewDetectionTime(Integer.parseInt(inputDetection.getText().toString()));
            }
        });

        Button rmsButton = (Button) findViewById(R.id.SetRMSWindow);
        final EditText inputrms = (EditText) findViewById(R.id.inputSetRMSWindow);
        rmsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNewRmsBuffer(Integer.parseInt(inputrms.getText().toString()));
            }
        });

        Button rmsDButton = (Button) findViewById(R.id.SetRMSStdDev);
        final EditText inputrmsDev = (EditText) findViewById(R.id.inputSetRMSStdDevN);
        rmsDButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetRmsStdDevN(Integer.parseInt(inputrmsDev.getText().toString()));
            }
        });

        Button buffersizeButton = (Button) findViewById(R.id.SetBufferSize);
        final EditText inputbuffersize = (EditText) findViewById(R.id.inputSetBufferSize);
        buffersizeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNewBufferSize(Integer.parseInt(inputbuffersize.getText().toString()));
            }
        });


        Button extremeBaseButton = (Button) findViewById(R.id.SetNewExtremeBase);
        final EditText inputExtremeBase = (EditText) findViewById(R.id.inputSetNewExtremeBase);
        extremeBaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SetNewExtremeBase(Float.parseFloat(inputExtremeBase.getText().toString()));
            }
        });

        Button avgButton = (Button) findViewById(R.id.SetNewAVGType);
        final EditText inputTimes = (EditText) findViewById(R.id.inputMeanNtimes);
        final EditText inputAvgDev = (EditText) findViewById(R.id.inputAvgStdDevNtimes);
        final RadioButton avg0 = (RadioButton) findViewById(R.id.radioButtonMean);
        avg0.setChecked(true);
        final RadioButton avg1 = (RadioButton) findViewById(R.id.radioButtonVariance);
        final RadioButton avg2 = (RadioButton) findViewById(R.id.radioButtonTimes);
        final RadioButton avg3 = (RadioButton) findViewById(R.id.radioButtonX);
        avgButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (avg0.isChecked()) {
                    SetNewAverageRmsType(0);
                } else if (avg1.isChecked()) {
                    SetAvgStdDevN(Integer.parseInt(inputAvgDev.getText().toString()));
                    SetNewAverageRmsType(1);
                } else if (avg2.isChecked()) {
                    SetNewRmsCustomVar(Integer.parseInt(inputTimes.getText().toString()));
                    SetNewAverageRmsType(2);
                } else if (avg3.isChecked()) {
                    SetNewRmsCustomVar(Integer.parseInt(inputTimes.getText().toString()));
                    SetNewAverageRmsType(3);
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();


        EditText inputFreq = (EditText) findViewById(R.id.inputCutF);
        EditText inputDetection = (EditText) findViewById(R.id.inputSetDetectionWindow);
        EditText inputrms = (EditText) findViewById(R.id.inputSetRMSWindow);
        EditText inputrmsDev = (EditText) findViewById(R.id.inputSetRMSStdDevN);
        EditText inputbuffersize = (EditText) findViewById(R.id.inputSetBufferSize);
        EditText inputExtremeBase = (EditText) findViewById(R.id.inputSetNewExtremeBase);
        EditText inputTimes = (EditText) findViewById(R.id.inputMeanNtimes);
        EditText inputAvgDev = (EditText) findViewById(R.id.inputAvgStdDevNtimes);
        RadioButton avg0 = (RadioButton) findViewById(R.id.radioButtonMean);
        RadioButton avg1 = (RadioButton) findViewById(R.id.radioButtonVariance);
        RadioButton avg2 = (RadioButton) findViewById(R.id.radioButtonTimes);
        RadioButton avg3 = (RadioButton) findViewById(R.id.radioButtonX);

        inputFreq.setText(GetNewCutFrequency()+"");
        inputDetection.setText(GetNewDetectionTime() + "");
        inputrms.setText(GetNewRmsBuffer()+ "");
        inputrmsDev.setText(GetRmsStdDevN() + "");
        inputExtremeBase.setText(GetNewExtremeBase() + "");
        inputTimes.setText(GetNewRmsCustomVar() + "");
        inputAvgDev.setText(GetAvgStdDevN()+"");

        int type = GetNewAverageRmsType();

        if(type == 0)
        {
            avg0.setChecked(true);
        } else if(type == 1)
        {
            avg1.setChecked(true);
        } else if(type == 2)
        {
            avg2.setChecked(true);
        } else if(type == 3)
        {
            avg3.setChecked(true);
        }

    }


    private native void SetNewCutFrequency(int f);

    private native void SetNewBufferSize(int buffer);

    private native void SetNewExtremeBase(float base);

    private native void SetNewAverageRmsType(int type);

    private native void SetNewRmsCustomVar(int var);

    private native void SetNewRmsBuffer(int lastSeconds);

    private native void SetNewDetectionTime(int lastMilliseconds);

    private native void SetAvgStdDevN(int n);

    private native void SetRmsStdDevN(int n);

    private native int GetNewCutFrequency();

    private native float GetNewExtremeBase();

    private native int GetNewAverageRmsType();

    private native int GetNewRmsCustomVar();

    private native int GetNewRmsBuffer();

    private native int GetNewDetectionTime();

    private native int GetAvgStdDevN();

    private native int GetRmsStdDevN();
}