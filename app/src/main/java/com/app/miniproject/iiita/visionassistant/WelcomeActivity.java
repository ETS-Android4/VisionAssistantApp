package com.app.miniproject.iiita.visionassistant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

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