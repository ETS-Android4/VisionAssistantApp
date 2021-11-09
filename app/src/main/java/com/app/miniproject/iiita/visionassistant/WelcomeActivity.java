package com.app.miniproject.iiita.visionassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import com.app.miniproject.iiita.visionassistant.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.yoloV3Mb.setOnClickListener(view -> startActivity(new Intent(WelcomeActivity.this, YoloV3Activity.class)));

        binding.fireMb.setOnClickListener(view -> {
        });
    }
}