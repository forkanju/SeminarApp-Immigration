package com.gicbd.officevisit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.gicbd.officevisit.R;

public class LoadingActivity extends AppCompatActivity {
    public static int Activity_Timeout = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //Auto timeout and go to scanner activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, ResultActivity.class);
                startActivity(intent);
                finish();
            }
        }, Activity_Timeout);
    }
}