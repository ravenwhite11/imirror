package com.example.imirror.makeupActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.example.imirror.R;

public class MakeupActivity extends AppCompatActivity implements View.OnClickListener{

    private final int PERMISSION_CONSTANT = 1000;
    Camera mCamera;
    HorizontalScrollView horizontalScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makeup);
        initial();
        checkPermissionANDGive();
    }

    private void initial() {
        ImageView ivCapure = findViewById(R.id.ivCapture);
        ImageView ivFilter = findViewById(R.id.ivFilter);
        horizontalScrollView = findViewById(R.id.filterLayout);
    }

    private void checkPermissionANDGive() {

    }

    @Override
    public void onClick(View v) {

    }
}