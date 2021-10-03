package com.example.imirror.faceActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.imirror.R;
import com.example.imirror.cameraActivity.TakePicActivity;
import com.example.imirror.videoActivity.VideoList;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FaceMenu extends AppCompatActivity {

    public static final int PermissionCode = 1000; //權限代碼
    public static final int GetPhotoCode = 1001;   //照片代碼
    private Activity activity;
    String imageFilePath = "";
    private boolean isCameraPermission = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_menu);

        activity = this;
        clickListen();

    }

    private void clickListen() {
        ImageButton Btn1 = findViewById(R.id.btn1_1); //開啟相機(健檢)
        RelativeLayout Btn2 = findViewById(R.id.btn1_2); //預約
        RelativeLayout Btn3 = findViewById(R.id.btn1_3); //查詢
        RelativeLayout Btn4 = findViewById(R.id.btn1_4); //進入診間
        ImageButton Back = findViewById(R.id.back1);

        Intent intent = new Intent();
        Btn1.setOnClickListener(view -> openCamera());
        Btn2.setOnClickListener(view -> {
            intent.setClass(FaceMenu.this, MedicalReserve.class);
            startActivity(intent);
        });
        Btn3.setOnClickListener(view -> {
            intent.setClass(FaceMenu.this, MedicalReserveQuery.class);
            startActivity(intent);
        });
        Btn4.setOnClickListener(view -> {
            intent.setClass(FaceMenu.this, VideoList.class);
            startActivity(intent);
        });
        Back.setOnClickListener(view -> onBackPressed());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isCameraPermission = true;
                //Toast.makeText(this, "感謝賜予權限！", Toast.LENGTH_SHORT).show();
                //Log.d("cindy", "onRequestPermissionsResult的成功授權");
                openCamera(); //開啟相機
            }
            else { //拒絕
                isCameraPermission = false;
                Toast.makeText(this, "需要權限才能使用面徵健檢!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //創造檔案名稱、和存擋路徑
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,   /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void openCamera() {
        //已獲得權限
        if (isCameraPermission) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                //Log.d("checkpoint", "error for createImageFile 創建路徑失敗");
            }
            //成功創建路徑的話
            if (photoFile != null) {
                Intent intent = new Intent(FaceMenu.this, TakePicActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", photoFile.getAbsolutePath());

                //Uri photoUri = Uri.fromFile(photoFile);
                //intent.putExtra("photoUri", photoUri.toString());

                intent.putExtras(bundle);
                //Log.d("cindy", "Menu頁面的photoUri.toString():"+photoUri);
                //startActivityForResult(intent, GetPhotoCode);
                startActivity(intent); //開始切換
            }
        }
        //沒有獲得權限
        else {
            getPermission();
        }
    }

    private void getPermission() {
        //檢查是否取得權限
        final int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        //沒有權限時
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            isCameraPermission = false;
            ActivityCompat.requestPermissions(FaceMenu.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionCode);
        } else { //已獲得權限
            isCameraPermission = true;
            openCamera();
        }
    }
}