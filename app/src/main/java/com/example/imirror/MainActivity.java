package com.example.imirror;


import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private ImageButton Btn1 ,Btn5;
    private long firstPressedTime;
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


    }

    private void init(){
        Btn1 = (ImageButton) findViewById(R.id.btn1_Face);
        Btn5 = (ImageButton) findViewById(R.id.btn5);

        /*監聽按鈕被觸發*/
        Btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(MainActivity.this, FaceMenu.class);
                startActivity(intent);
            }
        });
        Btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewCamera();
            }
        });

    }

    private void previewCamera() {
    }


    // 設定Back鍵要按兩次避免誤觸
    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (System.currentTimeMillis() - firstPressedTime < 2000) {
            super.onBackPressed();
        } else {
            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            firstPressedTime = System.currentTimeMillis();
        }
    }

}