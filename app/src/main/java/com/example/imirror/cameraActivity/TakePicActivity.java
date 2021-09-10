package com.example.imirror.cameraActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.imirror.FaceReport;
import com.example.imirror.R;
import com.example.imirror.cameraActivity.CameraSurfaceView;

public class TakePicActivity extends AppCompatActivity {

    private Button button;
    private CameraSurfaceView mCameraSurfaceView;

    private Activity activity;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pic);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        activity = this;
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        button = (Button) findViewById(R.id.takePic);

        getBundleData();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraSurfaceView.takePicture(activity, filePath);

            }
        });

    }

    //取得從FaceMenu傳過來的資料url
    private void getBundleData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            filePath = bundle.getString("url");
        }
        //Log.d("cindy", "驗證 filePath " + filePath);
    }


}