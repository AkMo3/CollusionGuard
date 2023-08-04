package com.akmo.collusionguard;

import android.content.Intent;
import android.os.Bundle;

import com.akmo.collusionguard.databinding.ActivityDataViewingBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.widget.TextView;


public class DataViewingActivity extends AppCompatActivity {

    private ActivityDataViewingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDataViewingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String sender = intent.getStringExtra("Sender");
        String receiver = intent.getStringExtra("Receiver");
        String key = sender.concat(receiver);
        StringBuilder data = new StringBuilder();

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("Activity Between " + sender + " and " + receiver);

        Log.d("CollusionGuard", "Sender: " + sender + ", Receiver: " + receiver + ", key: " + key);

        MessageEntity entity = SystemIntentReceiver.map.get(key);
        if (entity != null) {
            entity.dataList.forEach(passedData -> data.append(passedData).append("\n"));
        }

        Log.d("CollusionGuard", "Setting data to " + data.toString());

        TextView view = findViewById(R.id.scrolling_text);
        view.setText(data.toString());
    }
}