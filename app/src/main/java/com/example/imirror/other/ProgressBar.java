    package com.example.imirror.other;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imirror.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;

public class ProgressBar extends AppCompatActivity {

    SpinKitView spinKitView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        //progressBar = (ProgressBar) findViewById(R.id.dialog_spin_kit);
        //Sprite doubleBounce = new DoubleBounce();
        //progressBar.setIndeterminateDrawable(doubleBounce);
    }


}