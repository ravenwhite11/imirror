package com.example.imirror;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imirror.faceActivity.FaceMenu;
import com.example.imirror.firebase.Constants;
import com.example.imirror.firebase.PreferenceManager;
import com.example.imirror.other.LoadingDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private long firstPressedTime;
    private PreferenceManager preferenceManager;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clickListener();
        setTextForWelcome();

        //獲取Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if ( task.isSuccessful() && task.getResult()!=null ) {
                sendFCMTokenDatabase(task.getResult());

            } else{
                Log.d("cindy", "驗證token:" + task.getResult());
            }
        });

    }

    /* 設置文字 */
    private void setTextForWelcome() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        TextView textName = findViewById(R.id.textTitle);
        textName.setText(String.format(
                "%s %s",
                "HI~",
                preferenceManager.getString(Constants.KEY_NAME)
        ));
    }
    /* 把使用者的Token放到資料庫裡 */
    private void sendFCMTokenDatabase(String token) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(aVoid -> {
                    //Log.d("cindy", "驗證OnSuccessListener():" );
                    Toast.makeText(MainActivity.this, "Token更新", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->{
                        //Log.d("cindy", "驗證FailureListener():" );
                        Toast.makeText(MainActivity.this, "無法給予Token"+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /* 設置登出 */
    private void signOut(){
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete()); //應該是刪除Token
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "無法給予Token"+e.getMessage(), Toast.LENGTH_SHORT).show()
                );

    }

    /*監聽按鈕被觸發*/
    private void clickListener(){
        ImageButton Btn1 = findViewById(R.id.btn1_Face);
        ImageButton Btn2 = findViewById(R.id.btn2);
        ImageButton Btn8 = findViewById(R.id.btn8);
        MaterialButton BtnSignOut = findViewById(R.id.buttonSignOut);

        intent = new Intent();
        Btn1.setOnClickListener(view -> {
            intent.setClass(MainActivity.this, FaceMenu.class);
            startActivity(intent);
        });

        // 設置Loading動畫
        LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
        Btn2.setOnClickListener(view -> {
                loadingDialog.startLoadingDialog();
                Handler handler = new Handler();
                handler.postDelayed(() -> loadingDialog.dismissDialog(),5000);
        });
        Btn8.setOnClickListener(view -> {
            intent.setClass(MainActivity.this, ShoppingHomeActivity.class);
            startActivity(intent);
        });

        BtnSignOut.setOnClickListener(view -> signOut());
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