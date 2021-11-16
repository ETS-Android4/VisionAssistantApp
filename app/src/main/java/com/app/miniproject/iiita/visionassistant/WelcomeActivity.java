package com.app.miniproject.iiita.visionassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.miniproject.iiita.visionassistant.databinding.ActivityWelcomeBinding;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    public TextToSpeech textToSpeech;
    private final Handler handler = new Handler();
    private Runnable myRunnable,myRunnable1,myRunnable2;
    public String s="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        if (getConnectivityStatusString(this)!=null){
            fun();
            Toast.makeText(WelcomeActivity.this, "Network connection is available", Toast.LENGTH_SHORT).show();

        } else{
            final AlertDialog dialog = new AlertDialog.Builder(WelcomeActivity.this)
                    .setTitle("Warning").setMessage("Check your data or wi-fi connectivity")
                    .setPositiveButton("Retry", null)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if(getConnectivityStatusString(WelcomeActivity.this)!=null){
                                dialog.dismiss();
                                fun();
                            }
                        }
                    });
                }
            });
            dialog.show();

        }




    }

    public static String getConnectivityStatusString(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
                return status;
            }
        }
        return status;
    }



    public void fun(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.setSpeechRate(0.8f);
                    String s = "Welcome to Vision Assistant, For YoloV3 Model Object Detection, press Left Button, and for" +
                            "Firebase On-Device Model Object Detection press Right Button";
                    int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
                    Toast.makeText(WelcomeActivity.this, "Speak Success!! Welcome", Toast.LENGTH_SHORT).show();
                }
            }
        }, "com.google.android.tts");

        binding.yoloV3Mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,YoloV3Activity.class);
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
                startActivity(intent);
            }
        });
        binding.fireMb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,OnDeviceActivity.class);
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}