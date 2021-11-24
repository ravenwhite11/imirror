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
                        "診斷：臉部的肌肉過於鬆弛，且按壓後會留下壓痕，皮膚回彈性很差\n" +
                        "症狀：唇色偏紫青\n" +
                        "診斷：血液循環功能異常\n" +
                        "症狀：眼瞼黃色瘤\n" +
                        "診斷：體內脂質代謝異常，血脂過高會提高患心臟病風險。");
                textView2.setText("\u279C\t宜食少許堅果，如15g花生、7.5g榛果、7.5g杏仁等，可使心血管疾病（心肌梗塞、中風等）之發病率降低。\n" +
                        "\u279C\t宜食蘋果，內果膠含量豐富。\n" +
                        "\u279C\t宜食有較高鉀含量之食，如香蕉、扁豆、西蘭花等。\n" +
                        "\u279C\t宜喝麥片粥，多攝入全穀類食物，如糙米、穀物、膳食纖維、維生素B1，有助於控製膽固醇的升高，保護心臟。\n" +
                        "\u279C\t不宜飽和脂肪高之食物，如奶酪、酸奶、肥肉、奶油、豬油、糕點、餅乾以及椰油。\n" +
                        "\u279C\t不宜高鈉。成人一日鹽攝取量不超過6g。");
                textView3.setText("\u279C\t宜多進行增健體力之活動，活動踝關節有促進全身血液循環之效，增加回心血量。如健走、快步走、登山步行等。\n" +
                        "\u279C\t宜保持充足睡眠，可減緩心臟疾病風險；建議合適的睡眠時間為6-7小時，時間太長或太短會皆不洽當。");
                break;
            }
            case 1:{
                textView1.setText("您的腎臟可能有潛在風險！\n" +
                        "症狀：鼻翼發紅。\n" +
                        "診斷：鼻頭髮暗、枯燥脫皮，身體已經消耗過多津液和正氣，應養腎。\n" +
                        "症狀：眼之下微發黑。\n" +
                        "診斷：腎虛，水代謝功能異常。\n" +
                        "症狀：兩頰發黑。\n" +
                        "診斷：腰膝酸軟、性功能減退。");
                textView2.setText("\u279C\t宜多食黑芝麻、桑葚、黑豆、何首烏、黑木耳、黑米等。五色食療，黑色入腎，利於補腎。\n" +
                        "\u279C\t「春夏養陽，秋冬養陰。」春季是氣候由寒轉熱時節，適合培養「腎陽」。於春季喝大黃蓮藕茶使體內尿毒指數降低。\n" +
                        "\u279C\t宜食高麗菜，其含有豐富維生素C、維生素E、胡蘿蔔素，利於清理體內自由基\n。" +
                        "\u279C\t宜多沙苑子、腰果、豬腰子，從食物的「樣貌」中能得到食補之啟示，其外型像人體的腎，可食用養腎。\n" +
                        "\u279C\t忌過多攝取蛋白質及電解質，分解其產生之物質對腎之代謝功能造成負擔大\n。" +
                        "\u279C\t忌高油、油脂，勿使脂肪囤積於體內。");
                textView3.setText("\u279C\t「慾不可縱，縱則精竭，竭則真散」房勞過度，精氣外洩，易耗傷身體，宜量力而行，勞作有度，房事有節。\n" +
                        "\u279C\t宜補充水份，水於健康至關重要，亦能協助腎臟之運作。每日所需之水量因人而異，大方向指標為尿液色澤呈稻草色。\n" +
                        "\u279C\t宜維持規律運動，每週散步、游泳或騎自行車來保持體重。超重將致血壓升高，對腎造成額外負擔。\n" +
                        "\u279C\t宜少量多餐以利維持正常血糖，腎臟將血液中之營養物質過濾回體內、並清除廢物，保持血糖於正常範圍內有助於維持腎臟及整體健康。\n" +
                        "\u279C\t忌睡眠不足、過度疲累，充足的睡眠對於氣血的生化、腎精之保養起著重要作用。\n" +
                        "\u279C\t忌運動過身體最高承受之量，易發生呼吸性鹼中毒，致肌肉勞損。\n" +
                        "\u279C\t忌抽菸，尼古丁不利於血壓控制。");
                break;
            }
            case 2:{
                textView1.setText("您的肝臟可能有潛在風險！\n"+
                        "症狀：臉色發黑。\n" +
                        "診斷：排毒與儲存鐵質的功能無法正常發揮，鐵質流入血液造成臉色發黑。\n" +
                        "症狀：黃疸（眼白泛黃）。\n" +
                        "診斷：膽管塞住膽汁無法流到腸道，而進到血液循環中造成黃疸，皮膚受刺激而發癢。\n" +
                        "症狀：左臉頰發紅。\n" +
                        "診斷：肝火旺盛。");
                textView2.setText("\u279C\t宜食蘆筍、韭菜、蒜頭、芝麻，有助代謝、排毒。\n" +
                        "\u279C\t宜食花茶，其有清熱、利尿、解毒之功效，並舒緩緊繃之情緒；利肝之花茶如玫瑰花茶、菊花茶、洛神花茶、茉莉花茶。\n" +
                        "\u279C\t宜食較酸性之食，如檸檬、山楂、食醋、優酪乳。偏酸之食經肝成鹼性，於人體形成鹼性循環，有助免疫力之提升。\n" +
                        "\u279C\t宜食綠色食物，如黃瓜、苦瓜、芥菜、海藻。\n" +
                        "\u279C\t忌辛辣、亂食補品及抗生素。\n" +
                        "\u279C\t不宜食用過多蛋白之食。\n" +
                        "\u279C\t不宜酒類、含黃麴毒素之食。");
                textView3.setText("\u279C\t宜進行坐推肝經，有利於梳肝理氣，去肝火、改氣色；可坐於床上，右腿向前伸直、左腿彎曲平放腿內側朝上，彎曲腿正中間位置即是肝經，可兩腿交換推拿，從大腿根部推向膝蓋。\n" +
                        "\u279C\t宜按壓太衝穴，其以泄為主，解肝氣鬱結。腳背上拇指和第二趾向上推，可推及一凹陷處，按壓較於其餘地方有感，即為太衝穴之位。\n" +
                        "\u279C\t宜按壓膻中穴，亦有紓解肝氣鬱結。位置於兩乳頭間之敏感處。於情緒激憤之餘，膻中穴尤為敏感。\n" +
                        "\u279C\t宜按壓三陰交穴，此穴位為補穴，常按摩能達養肝之效。位於內腳踝上三吋(約四並指)，脛骨內側後方的凹陷處。");
                break;

            }
            case 3:{
                textView1.setText("您的肺臟可能有潛在風險！");
                textView1.setText("症狀：面呈憔悴、黯淡無光之狀。\n" +
                        "診斷：肺主皮毛，其有異狀，毒素隨著肺作用沉積於表皮，易皮膚枯燥、粗糙、暗瘡、粉刺及黑斑。\n" +
                        "症狀：紅鼻頭。\n" +
                        "診斷：肺火旺，熱氣往上抵鼻頭。亦有鼻涕、鼻塞、鼻炎等鼻不適症狀。\n" +
                        "症狀：臉色蒼白。\n" +
                        "診斷：肺功效異狀之徵兆，易致氣虛、血含氧量不足。\n" +
                        "症狀：印堂微發黑。\n" +
                        "診斷：印堂於一般呈白裡透紅之狀；若發紅，則說明肝火旺盛；發白，可能囗氣虛、血虛；發青或發黑，則有血瘀之慮。\n" +
                        "症狀：右臉頰發紅。\n" +
                        "診斷：肺火旺盛。");
                textView2.setText("\u279C\t宜食利於化痰潤肺之白色食物，如：蓮子、百合、杏仁、蓮藕、白芝麻、蜂蜜等。\n" +
                        "\u279C\t宜食番茄、香蕉、蘋果，抑制肺部機能退化，亦修復受損之肺臟機能。\n" +
                        "\u279C\t宜於早晚喝丁香葉桂花茶，起潤肺養肺之效。\n" +
                        "\u279C\t忌高磷、鈉、鉀之刺激性食物，易刺激氣管黏膜會加重咳嗽、氣喘、心悸等症狀，並誘發哮喘。\n" +
                        "\u279C\t忌食海腥、油膩之品，用油過大易引上火於身。\n" +
                        "\u279C\t忌食產氣之食，如紅薯、韭菜，對肺氣宣降不利。");
                textView3.setText("\u279C\t宜有培養運動健身之習慣，促進血液循環、加強肺活量，並增加肺部氣泡之彈性。\n" +
                        "\u279C\t搓揉手掌心，利肺氣、止咳喘、化痰、健肺。肺氣充足，氣色紅潤、光滑細緻，亦有養顏美容之功效。\n" +
                        "\u279C\t宜笑口常開，大笑使肺擴張，並使人無自覺地深呼吸，有利呼吸道之清理，亦使呼吸更於通暢。");
                break;
            }
            case 4:{
                textView1.setText("您的潛在風險皆偏低！");
                textView1.setText("症狀：無明顯症狀\n" +
                        "診斷：您的面容無明顯身體健康之疑慮，但還需定期檢視自身健康狀況。");
                textView2.setText("\u279C\t繼續保持六大類食物份量攝取，使營養素種類齊全。三餐以全榖為主食提供身體適當的熱量，協助維持血糖，並保護肌肉與內臟器官的組織蛋白質。多選用高纖維食物，促進腸道之生理健康，並有助血糖與血脂之控制。");
                textView3.setText("\u279C\t繼續維持多活動的生活習慣，每週累積至少150分鐘中等費力身體活動，或75分鐘的較費力之身體活動。");
                break;
            }
            case 5:{
                textView1.setText("您的脾臟可能有潛在風險！\n"+
                        "症狀：鼻翼發紅\n" +
                        "診斷：鼻腔乾燥、嗅覺失靈、流清鼻涕、鼻內出血，大多為脾胃虛弱之徵兆。\n" +
                        "症狀：皮膚黃斑\n" +
                        "診斷：肝火旺盛，易怒，手腳冰涼。");
                textView2.setText("\u279C\t宜食甜味之食，如山藥、香蕉、大棗等，以養脾臟之氣。少食酸味之食物。\n" +
                        "\u279C\t宜於夏食豆類，夏日溼氣重，豆類健脾利濕。\n" +
                        "\u279C\t脾胃有四怕：怕生、怕冷、怕撐、怕怒。不宜生冷食物，如冷飲、生食蔬菜，其內帶寒氣，如進於身易傷及脾胃。\n" +
                        "\u279C\t不宜極端飲食，飢一頓、飽一頓傷害最大。");
                textView3.setText("\u279C\t宜多動腳趾，功效相當於按摩脾胃二經。於久坐之時可邊工作邊用腳趾抓地或鞋底。\n" +
                        "\u279C\t宜笑口常開，情緒意影響肝之調節功能，進而影響脾胃；不良情緒也亦影響胃液分泌不足、活動力下降。\n" +
                        "\u279C\t宜於進食時反覆咀嚼，於胃細碎食物益於消化，亦利於腸道。");
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
        radarArray .add(new RadarEntry(spleen));
        radarArray .add(new RadarEntry(lung));
        radarArray .add(new RadarEntry(liver));
        radarArray .add(new RadarEntry(kidney));

        RadarDataSet radarDataSet = new RadarDataSet(radarArray, "分析結果");
        radarDataSet.setColor(Color.rgb(168,218,175)); //外框顏色
        radarDataSet.setDrawFilled(true);  //填充
        radarDataSet.setFillColor(Color.argb(127,168,218,175)); //內部填充
        radarDataSet.setLineWidth(2f);     //框線粗細
        radarDataSet.setDrawValues(true);  //每個點的數字(通常跟yAxis.setDrawLabels擇一)
        radarDataSet.setValueTextSize(12); //每個點的數字Size

        RadarData radarData = new RadarData();
        radarData .addDataSet(radarDataSet);

        String[] labels = {"心","脾","肺","肝","腎"};
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
            Uri uri = Uri.parse("https://www.amazon.com/Solaray-Healthy-Dandelion-Artichoke-Peppermint/dp/B00014D9VC");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
        });
        ImageView imageView3 = findViewById(R.id.pic3);
        imageView3.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://www.amazon.com/dp/B00MJ7VL1O/ref=cm_sw_em_r_mt_dp_P0V56CDWWNNF54ZY6C0X");
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
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