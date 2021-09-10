package com.example.imirror;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imirror.cameraActivity.TakePicActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FaceReport extends AppCompatActivity {

    private ImageView mShowImage;
    private TextView mTextView;
    private ImageButton Back;
    private List<String> labels;
    private Bitmap bitmap;

    protected Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private TensorImage inputImageBuffer;
    private  int imageSizeX;
    private  int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    //In ClassifierQuantizedMobileNet, normalization is not required.
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    // In ClassifierQuantizedMobileNet, the normalized parameters are defined as:
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_report);

        mShowImage = (ImageView) findViewById(R.id.pic1);
        mTextView = (TextView) findViewById(R.id.textView2);
        back_clicklisten();

        Bundle bundle = getIntent().getExtras();
        String filePath = bundle.getString("url");
        //Log.d("cindy", "Report頁面的filePath^^:" + filePath);

        if(filePath != null){
            setPic(filePath);
            Uri photoUri = Uri.fromFile(new File(filePath));
            bitmap = getBitmapFromURL(photoUri);
            //Log.d("cindy", "Report頁面的bitmap: " + bitmap);
        }

        try{
            tflite = new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }
        classfication();
    }

    /* 分析大師== */
    private void classfication() {
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, 4, 4, 512}
        imageSizeY = imageShape[1]; // height
        imageSizeX = imageShape[2]; // width
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        /* Output probability TensorBuffer. */
        //從 TensorFlow Lite 模型文件中獲取輸出緩衝區的數組大小
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES=1}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType); // 後面設定TensorFlow Lite interpreter所需的型態

        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType); //創造 the output tensor & processor
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build(); //創造 the post processor for the output probability

        inputImageBuffer = loadImage(bitmap); //將圖片轉成TensorImage格式

        // Run inference
        tflite.run(inputImageBuffer.getBuffer(),
                outputProbabilityBuffer.getBuffer().rewind());
        show_result(); //顯示學習結果
    }


    /** 轉換為 TensorImage 格式以進行高效處理並 applys preprocessing. **/
    private TensorImage loadImage(final Bitmap bitmap) {
        inputImageBuffer.load(bitmap);
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    // 讀取tflite檔案
    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("plant_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }
    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    /** 讀取Lable文字檔 並 顯示結果 **/
    private void show_result(){
        try{
            labels = FileUtil.loadLabels(this,"plant_labels.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue()==maxValueInMap) {
                //設定出現的文字
                mTextView.setText(entry.getKey());
            }
        }
    }

    private void back_clicklisten() {
        Intent intent = new Intent();
        Back = (ImageButton) findViewById(R.id.back_facereport);
        //返回鍵設置
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(FaceReport.this, FaceMenu.class);
                startActivity(intent);
            }
        });
    }

    /* 放置圖片於layout */
    private void setPic(String mCurrentPhotoPath) {

        int targetW = 200; //得到ImageView的長寬
        int targetH = 200;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth; //圖片原始高度&寬度
        int photoH = bmOptions.outHeight;
        //Log.d("cindy", "targetW "+targetW+" targetH "+targetH+" photoW "+photoW+" photoH "+photoH);
        // 計算縮放比例
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor; //縮放倍數
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions); //重新讀取圖片
        mShowImage.setImageBitmap(bitmap);
    }

    // Uri轉成BitMap格式(超級麻煩)
    public Bitmap getBitmapFromURL(Uri uri) {
        InputStream Stream = null;
        InputStream inputStream = null;
        try {
            //根据uri获取图片的流
            inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true; //options的in系列的设置了，injustdecodebouond只解析图片的大小，而不加载到内存中去
            BitmapFactory.decodeStream(inputStream,null,options);
            /*1.如果通过options.outHeight获取图片的宽高，就必须通过decodestream解析同options赋值
                否则options.outheight获取不到宽高
              2.通过 btm.getHeight()获取图片的宽高就不需要1的解析，我这里采取第一张方式
                Bitmap btm = BitmapFactory.decodeStream(inputStream);*/

            //以屏幕的宽高进行压缩
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            //获取图片的宽高
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            //heightPixels就是要压缩后的图片高度，宽度也一样
            int a = (int) Math.ceil((outHeight/(float)heightPixels));
            int b = (int) Math.ceil(outWidth/(float)widthPixels);
            //比例计算,一般是图片比较大的情况下进行压缩
            int max = Math.max(a, b);
            if(max > 1){
                options.inSampleSize = max;
            }
            //解析到内存中去
            options.inJustDecodeBounds = false;
//            根据uri重新获取流，inputstream在解析中发生改变了
            Stream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(Stream, null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(Stream != null){
                    Stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  null;
    }


/*    private void setItem() {
        mTextView = (TextView) findViewById(R.id.item_txt);
        String str = "&honey melty\n蜂蜜澤亮柔順護理\n\n$1,199";

        SpannableString span = new SpannableString(str);
        span.setSpan(new TextAppearanceSpan(this, R.style.style0_15dp), 0, str.length() - 6,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new TextAppearanceSpan(this, R.style.style1_red_25dp), str.length() - 6, str.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/

/*        str += "\n\n\n更多資訊";
        span.setSpan(new TextAppearanceSpan(this, R.style.style2_link), 31, 35,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.d("cindy", Integer.toString(str.length()));
        span.setSpan(new URLSpan("http://www.google.com"), 0, str1.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
*/
/*        mTextView.setText(span, TextView.BufferType.SPANNABLE);
    }*/
}