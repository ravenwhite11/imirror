package com.example.imirror.faceActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.example.imirror.R;
import com.example.imirror.ShoppingHomeActivity;
import com.example.imirror.other.LoadingDialog;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_report);

        //setFragment(); //有關Fragment
        clickListen();
        getFilepath();
        try{
            tflite = new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }
        classification();
    }

    /** 讀取Lable文字檔 並 顯示結果 **/
    private void show_result(){
        try{
            labels = FileUtil.loadLabels(this, LABEL_PATH ); //讀取label.txt
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer)).getMapWithFloatValue();
        // 設置雷達圖數值
        setRadarImage(
                labeledProbability.get("heart")*100,
                labeledProbability.get("kidney")*100,
                labeledProbability.get("liver")*100,
                labeledProbability.get("lung")*100,
                labeledProbability.get("normal")*100,
                labeledProbability.get("spleen")*100
        );

        float maxValueInMap = (Collections.max(labeledProbability.values())); // 取得最大機率
        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if ( entry.getValue() == maxValueInMap ) {
                //TextView1.setText( entry.getKey()+" "+entry.getValue() );//設定出現的文字
                int id;
                switch (entry.getKey()) {
                    case "heart":
                        id = 0;
                        break;
                    case "kidney":
                        id = 1;
                        break;
                    case "liver":
                        id = 2;
                        break;
                    case "lung":
                        id = 3;
                        break;
                    case "normal":
                        id = 4;
                        break;
                    default:
                        id = 5;
                        break;
                }
                showText(id); //放置文字
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showText(int id) {
        TextView textView1 = findViewById(R.id.textView1);
        TextView textView2 = findViewById(R.id.textView2);
        TextView textView3 = findViewById(R.id.textView3);
        switch (id){
            case 0:{
                textView1.setText("您的心臟可能有潛在風險！\n" +
                        "症狀：面部浮腫\n" +
                        "診斷：有心臟病問題，此時臉部的肌肉過於鬆弛且按壓後會留下壓痕，皮膚回彈性很差。");
                textView2.setText("\u279C\t日常食用少量堅果，如15g花生、7.5g榛子、7.5g杏仁等，可使心血管疾病（心肌梗塞、中風等）之發病率降低。\n" +
                        "\u279C\t蘋果果膠含量豐富，一個中等大小的蘋果所含果膠量達3.5g。蘋果含有豐富的鉀，是高血壓患者的好選擇。此外，鉀含量較高的食物還有香蕉、扁豆、西蘭花，都是好選擇。");
                textView3.setText("\u279C\t在增加心血管病風險的生活方式和危險因素中，體力活動明顯不足成為其中一個重要因素。可選擇快步走、（30分鍾內走3公里等）簡單又便捷的運動方法來降低風險。\n" +
                        "\u279C\t充分睡眠可減緩心臟疾病風險，不過睡覺時間太長或太短會皆不洽當，建議合適的睡眠時間為：6-7小時");
                break;
            }
            case 1:{
                textView1.setText("您的肝臟可能有潛在風險！"+"\n"+
                        "症狀：臉色發黑\n" +
                        "診斷：肝臟受損，排毒與儲存鐵質的功能無法正常發揮，鐵質流入血液造成臉色發黑。");
                textView2.setText("\u279C\t蘆筍、韭菜、蒜頭、芝麻，能有效幫助代謝、排毒，幫助護肝。\n" +
                        "\u279C\t花茶能舒緩緊繃的情緒，大多有清熱、利尿、解毒之功效，與肝有益之花茶：玫瑰花茶、菊花茶、洛神花茶、茉莉花茶。");
                textView3.setText("\u279C\t坐推肝經，有利於梳肝理氣，去肝火、改氣色，肝經位置可坐在床上，右腿向前伸直，左腿彎曲平放腿內側朝上，彎曲腿正中間位置就是肝經，可兩腿交換推拿，從大腿根部推向膝蓋。");
                break;
            }
            case 2:{
                textView1.setText("您的脾臟可能有潛在風險！"+"\n"+
                        "症狀：鼻翼發紅\n" +
                        "診斷：鼻腔乾燥、嗅覺失靈、流清鼻涕、鼻子出血，大多是脾胃虛弱所導致。");
                textView2.setText("\u279C\t脾胃有四怕，怕生、怕冷、怕撐、怕生氣。生冷的食物，如各種冷飲、生的蔬菜水果等，會帶著寒氣進入身體，最容易傷及脾胃。此外，脾胃怕撐，飢一頓、飽一頓對其傷害最大。\n" +
                        "\u279C\t春天少吃酸，多吃甜味食物，如山藥、香蕉、大棗等，以養脾臟之氣。");
                textView3.setText("\u279C\t多動動腳趾，相當於按摩脾胃二經。平常上班時可以邊工作邊用腳趾抓地或鞋底，活動腳趾養脾胃。");
                break;
            }
            case 3:{
                textView1.setText("您的肺臟可能有潛在風險！");
                textView1.setText("症狀：兩眉之間長痘、或微發黑\n" +
                        "診斷：肺部健康時，印堂呈現白裡透紅，若印堂發紅，則說明肝火旺盛，印堂發白，可能囗氣虛或血虛，印堂發青或發黑，則是血瘀的一種情況。");
                textView2.setText("\u279C\t多吃利於化痰潤肺的白色食物，如：枇杷、蜂蜜、蓮子、百合、杏仁、蓮藕、白芝麻等。\n" +
                        "\u279C\t番茄、香蕉、蘋果，都能有效抑制肺部機能退化，同時修復受損的肺臟機能。");
                textView3.setText("\u279C\t養成運動習慣是鍛鍊肺最好的辦法。透過運動促進肺部血液循環、加強肺活量，增加肺部氣泡之彈性。\n" +
                        "\u279C\t搓揉手掌心的位置，有利肺氣、止咳喘、化痰、健肺，對於治療感冒也有很好的效果");
                break;
            }
            case 4:{
                textView1.setText("您的潛在風險皆偏低！");
                textView1.setText("症狀：無明顯症狀\n" +
                        "診斷：您的面容無明顯身體健康之疑慮，恭喜您！！");
                textView2.setText("\u279C\t繼續保持六大類食物份量攝取，使營養素種類齊全。三餐以全榖為主食提供身體適當的熱量，協助維持血糖，並保護肌肉與內臟器官的組織蛋白質。多選用高纖維食物，能促進腸道的生理健康，還可幫助血糖與血脂的控制。");
                textView3.setText("\u279C\t繼續維持多活動的生活習慣，每週累積至少150分鐘中等費力身體活動，或是75分鐘的較費力之身體活動。");
                break;
            }
            case 5:{
                textView1.setText("您的腎臟可能有潛在風險！\n" +
                        "症狀：鼻翼發紅\n" +
                        "診斷：鼻頭髮暗、枯燥脫皮，身體已經消耗過多津液和正氣，應養腎。");
                textView2.setText("\u279C\t多吃芝麻、核桃可以使皮膚白皙、豐潤，有助毛髮生長。\n" +
                        "\u279C\t紅辣椒中關鍵成分辣椒素有助於分解體內（特別是血液）之垃圾。\n" +
                        "\u279C\t高麗菜中含有豐富的維生素C、維生素E、胡蘿蔔素等，總計維生素含量比番茄多出3倍。高麗菜有助於清除體內自由基。可多吃點涼拌高麗菜。");
                textView3.setText("\u279C\t「春夏養陽，秋冬養陰。」春季是氣候由寒轉熱的時節，適合培養「腎陽」，喝大黃蓮藕茶可以降低累積在體內的尿毒指數。將大黃2片及藕粉10克放入杯中，以250毫升熱開水沖泡即可。\n" +
                        "\u279C\t按五行學說，黑色入腎。許多黑色的食物，如黑芝麻、黑木耳、黑米、黑豆等，也都有補腎的功效，可斟酌食用。");
                break;
            }
        }
    }

    /* 分析大師== */
    private void classification() {
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

        //--創建用於儲存结果的容器(the output tensor & processor)--//
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
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
    /** 讀取tflite檔案 **/
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

    /* 雷達圖 */
    public void setRadarImage(float heart, float kidney, float liver, float lung, float normal, float spleen) {
        RadarChart radarChart = findViewById(R.id.radarChart);
        ArrayList<RadarEntry> radarArray = new ArrayList<>();
        radarArray .add(new RadarEntry(heart));
        radarArray .add(new RadarEntry(kidney));
        radarArray .add(new RadarEntry(liver));
        radarArray .add(new RadarEntry(lung));
        radarArray .add(new RadarEntry(spleen));

        RadarDataSet radarDataSet = new RadarDataSet(radarArray, "分析結果");
        radarDataSet.setColor(Color.rgb(168,218,175)); //外框顏色
        radarDataSet.setDrawFilled(true);  //填充
        radarDataSet.setFillColor(Color.argb(127,168,218,175)); //內部填充
        radarDataSet.setLineWidth(2f);     //框線粗細
        radarDataSet.setDrawValues(true);  //每個點的數字(通常跟yAxis.setDrawLabels擇一)
        radarDataSet.setValueTextSize(14); //每個點的數字Size

        RadarData radarData = new RadarData();
        radarData .addDataSet(radarDataSet);

        String[] labels = {"心","肝","腎","肺","脾"};
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(20);
        YAxis yAxis = radarChart.getYAxis();
        yAxis.setDrawLabels(false);//y軸的數字(0,20,40,60,80,100)
        yAxis.setAxisMinimum(0);   //最小值
        yAxis.setAxisMaximum(80);  //最大值

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setData(radarData);
    }
    /* 放置圖片於layout */
    public void setPic(String mCurrentPhotoPath) {
        //Log.d("cindy", "mCurrentPhotoPath: "+mCurrentPhotoPath);
        ImageView mShowImage = findViewById(R.id.pic1);
        int targetW = 200;
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
        //bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions); //重新讀取圖片
        mShowImage.setImageBitmap(bitmap);
    }
    /* 返回鍵設置 */
    private void clickListen() {
        Intent intent = new Intent();
        ImageView Back = findViewById(R.id.imageBack);
        Back.setOnClickListener(view -> onBackPressed());

        MaterialButton btn1 = findViewById(R.id.buttonConnectReserve);
        btn1.setOnClickListener(view -> {
            intent.setClass(FaceReport.this, MedicalReserve.class);
            startActivity(intent);
        });

        ImageView imageView2 = findViewById(R.id.pic2);
        imageView2.setOnClickListener(view -> {
            intent.setClass(FaceReport.this, ShoppingHomeActivity.class);
            startActivity(intent);
        });
        ImageView imageView3 = findViewById(R.id.pic3);
        imageView3.setOnClickListener(view -> {
            intent.setClass(FaceReport.this, ShoppingHomeActivity.class);
            startActivity(intent);
        });

    }
    /* 取得圖片連結 */
    private void getFilepath() {
        Bundle bundle = getIntent().getExtras();
        String filePath = bundle.getString("url");
        if( filePath != null ){
            setPic(filePath);
            Uri photoUri = Uri.fromFile(new File(filePath));
            bitmap = getBitmapFromURL(photoUri);
        }
    }
    /* Uri轉成BitMap格式(超級麻煩) */
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
            //根据uri重新获取流，inputstream在解析中发生改变了
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

/*    private void setFragment() {
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setAdapter(new FramentPagerAdapter(this));


        //Fragment3Suggestion.newInstance("第一個","第二個");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment3Suggestion fragment3 = Fragment3Suggestion.newInstance("第一個","第二個");
        fragmentTransaction.add(R.id.container_view,fragment3).commit();


        TabLayout tabLayout = findViewById(R.id.tabLayout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("Overview");
                        tab.setIcon(R.drawable.ic_overview);
                        break;
                    }
                    case 1:{
                        tab.setText("Diagnostic");
                        tab.setIcon(R.drawable.ic_inquiry);
                        break;
                    }
                    case 2:{
                        tab.setText("Suggestion");
                        tab.setIcon(R.drawable.ic_suggestion);
                        break;
                    }
                }
            }
        }
        );
        tabLayoutMediator.attach(); //將之前detach的Fragment重新顯示在App的執行畫面，重新建立Fragment的畫面
    }
*/

}