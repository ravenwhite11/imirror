package com.example.imirror.faceActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imirror.R;
import com.example.imirror.other.LoadingDialog;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FaceReport extends AppCompatActivity {
    private List<String> labels;
    private Bitmap bitmap;

    protected Interpreter tflite;
    private TensorImage inputImageBuffer;
    private int imageSizeX, imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    //In ClassifierQuantizedMobileNet, normalization is not required.
    private static final float IMAGE_MEAN = 0.0f, IMAGE_STD = 1.0f;
    // In ClassifierQuantizedMobileNet, the normalized parameters are defined as:
    private static final float PROBABILITY_MEAN = 0.0f, PROBABILITY_STD = 255.0f;

    private static final String MODEL_PATH = "face_model.tflite";
    private static final String LABEL_PATH = "face_dict.txt";

    LoadingDialog loadingDialog = new LoadingDialog(FaceReport.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_report);

        loadingDialog.startLoadingDialog();//????????????
        backClickListen();
        getFilepath();
        try{
            tflite = new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }
        classification();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadingDialog.dismissDialog();
    }

    /** ??????Lable????????? ??? ???????????? **/
    private void show_result(){
        TextView mTextView = findViewById(R.id.textView1);

        try{
            labels = FileUtil.loadLabels(this, LABEL_PATH ); //??????label.txt
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer)).getMapWithFloatValue();
        // ?????????????????????
        setRadarImage(
                labeledProbability.get("heart")*100,
                labeledProbability.get("kidney")*100,
                labeledProbability.get("liver")*100,
                labeledProbability.get("lung")*100,
                labeledProbability.get("normal")*100,
                labeledProbability.get("spleen")*100
        );

        float maxValueInMap = (Collections.max(labeledProbability.values())); // ??????????????????

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            //Log.d("cindy", "entry.getValue: "+entry.getValue()); // ?????????
            //Log.d("cindy", "entry.getKey: "+entry.getKey()); // ?????????
            if ( entry.getValue() == maxValueInMap ) {
                mTextView.setText( entry.getKey()+" "+entry.getValue() );//?????????????????????
            }
        }
    }
    /* ????????????== */
    private void classification() {
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, 4, 4, 512}
        imageSizeY = imageShape[1]; // height
        imageSizeX = imageShape[2]; // width
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        /* Output probability TensorBuffer. */
        //??? TensorFlow Lite ???????????????????????????????????????????????????
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES=1}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType); // ????????????TensorFlow Lite interpreter???????????????

        //--?????????????????????????????????(the output tensor & processor)--//
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build(); //?????? the post processor for the output probability

        inputImageBuffer = loadImage(bitmap); //???????????????TensorImage??????

        // Run inference
        tflite.run(inputImageBuffer.getBuffer(),
                outputProbabilityBuffer.getBuffer().rewind());
        show_result(); //??????????????????
    }
    /** ????????? TensorImage ?????????????????????????????? applys preprocessing. **/
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
    /** ??????tflite?????? **/
    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
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

    /* ???????????????layout */
    private void setPic(String mCurrentPhotoPath) {
        ImageView mShowImage = findViewById(R.id.pic1);
        int targetW = 200;
        int targetH = 200;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth; //??????????????????&??????
        int photoH = bmOptions.outHeight;
        //Log.d("cindy", "targetW "+targetW+" targetH "+targetH+" photoW "+photoW+" photoH "+photoH);
        // ??????????????????
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor; //????????????
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions); //??????????????????
        mShowImage.setImageBitmap(bitmap);
    }

    /* ????????? */
    private void setRadarImage(float heart, float kidney, float liver, float lung, float normal, float spleen) {
        RadarChart radarChart = findViewById(R.id.radarChart);
        ArrayList<RadarEntry> radarArray = new ArrayList<>();
        radarArray .add(new RadarEntry(heart));
        radarArray .add(new RadarEntry(kidney));
        radarArray .add(new RadarEntry(liver));
        radarArray .add(new RadarEntry(lung));
        radarArray .add(new RadarEntry(spleen));

        RadarDataSet radarDataSet = new RadarDataSet(radarArray, "????????????");
        radarDataSet.setColor(Color.rgb(168,218,175)); //????????????
        radarDataSet.setDrawFilled(true);  //??????
        radarDataSet.setFillColor(Color.argb(127,168,218,175)); //????????????
        radarDataSet.setLineWidth(2f);     //????????????
        radarDataSet.setDrawValues(true);  //??????????????????(?????????yAxis.setDrawLabels??????)
        radarDataSet.setValueTextSize(14); //??????????????????Size

        RadarData radarData = new RadarData();
        radarData .addDataSet(radarDataSet);

        String[] labels = {"???","???","???","???","???"};
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(20);
        YAxis yAxis = radarChart.getYAxis();
        yAxis.setDrawLabels(false);//y????????????(0,20,40,60,80,100)
        yAxis.setAxisMinimum(0);   //?????????
        yAxis.setAxisMaximum(80);  //?????????

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setData(radarData);
    }
    /* ??????????????? */
    private void backClickListen() {
        ImageButton Back = findViewById(R.id.back_facereport);
        Back.setOnClickListener(view -> onBackPressed());
    }
    /* ?????????????????? */
    private void getFilepath() {
        Bundle bundle = getIntent().getExtras();
        String filePath = bundle.getString("url");
        if( filePath != null ){
            setPic(filePath);
            Uri photoUri = Uri.fromFile(new File(filePath));
            bitmap = getBitmapFromURL(photoUri);
        }
    }
    /* Uri??????BitMap??????(????????????) */
    public Bitmap getBitmapFromURL(Uri uri) {
        InputStream Stream = null;
        InputStream inputStream = null;
        try {
            //??????uri??????????????????
            inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true; //options???in?????????????????????injustdecodebouond??????????????????????????????????????????????????????
            BitmapFactory.decodeStream(inputStream,null,options);
            /*1.????????????options.outHeight???????????????????????????????????????decodestream?????????options??????
                ??????options.outheight??????????????????
              2.?????? btm.getHeight()?????????????????????????????????1??????????????????????????????????????????
                Bitmap btm = BitmapFactory.decodeStream(inputStream);*/

            //??????????????????????????????
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            //?????????????????????
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            //heightPixels???????????????????????????????????????????????????
            int a = (int) Math.ceil((outHeight/(float)heightPixels));
            int b = (int) Math.ceil(outWidth/(float)widthPixels);
            //????????????,????????????????????????????????????????????????
            int max = Math.max(a, b);
            if(max > 1){
                options.inSampleSize = max;
            }
            //?????????????????????
            options.inJustDecodeBounds = false;
            //??????uri??????????????????inputstream???????????????????????????
            Stream = getContentResolver().openInputStream(uri);
            //Bitmap bitmap = BitmapFactory.decodeStream(Stream, null, options);
            //return bitmap;
            return BitmapFactory.decodeStream(Stream, null, options);
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


}