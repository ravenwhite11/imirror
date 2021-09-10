package com.example.imirror;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MedicalReserve extends AppCompatActivity {

    Intent intent = new Intent();
    Spinner hospital;
    Spinner division;
    Spinner doctor;
    TextView etDate;
    TextView tvTimer;
    int tHour,tMinute;
    Button reserve_check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reserve);

        sp1();
        sp2();
        sp3();
        sp4();
        sp5();

        init();
    }
    private void sp1(){
        hospital= findViewById(R.id.spinner1);
        final String[] hos ={"請選擇醫院","天晟醫療社團法人天晟醫院","長庚醫療財團法人林口長庚紀念醫院","衛生福利部桃園醫院","臺北榮民總醫院桃園分院"};
        ArrayAdapter<String> a1=new ArrayAdapter<String>(MedicalReserve.this,android.R.layout.simple_spinner_item,hos);
        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospital.setAdapter(a1);
        hospital.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        Toast.makeText(MedicalReserve.this, "醫院："+hos[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(MedicalReserve.this, "醫院："+hos[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MedicalReserve.this, "醫院："+hos[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(MedicalReserve.this, "醫院："+hos[position],Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    private void sp2(){
        division= findViewById(R.id.spinner2);
        final String[] div ={"請選擇科別","神經科","腎臟科","肝膽腸胃科","胸腔內科","一般外科","泌尿科","骨科","身心科"};
        ArrayAdapter<String> a1=new ArrayAdapter<String>(MedicalReserve.this,android.R.layout.simple_spinner_item,div);
        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        division.setAdapter(a1);
        division.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(MedicalReserve.this, "醫院："+div[position],Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    private void sp3(){
        doctor= findViewById(R.id.spinner3);
        final String[] doc ={"請選擇醫生","王醫師","蔡醫師","陳醫師","黃醫師"};
        ArrayAdapter<String> a1=new ArrayAdapter<String>(MedicalReserve.this,android.R.layout.simple_spinner_item,doc);
        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        doctor.setAdapter(a1);
        doctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        Toast.makeText(MedicalReserve.this, "醫院："+doc[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(MedicalReserve.this, "醫院："+doc[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MedicalReserve.this, "醫院："+doc[position],Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(MedicalReserve.this, "醫院："+doc[position],Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    private void sp4(){
        //tvDate = findViewById(R.id.textView4);
        etDate =findViewById(R.id.textView4_1);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        //月曆式日期
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MedicalReserve.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String date = year+"/"+month+"/"+day;
                        etDate.setText(date);
                    }
                },year,month,day
                );
                datePickerDialog.show();
            }
        });
    }
    private void sp5(){
        tvTimer = findViewById(R.id.textView6);

        tvTimer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MedicalReserve.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //initialize hour and minute
                        tHour = hourOfDay;
                        tMinute = minute;
                        //store hour and minute in string
                        String time = tHour+" ："+tMinute;
                        //initalize 24 hours time format
                        SimpleDateFormat f24Hours = new SimpleDateFormat(
                                "HH:mm"
                        );
                        tvTimer.setText(time);
                        /*try {
                            Date date = f24Hours.parse(time);
                            //initialize 12 hours time format
                            SimpleDateFormat f12hours = new SimpleDateFormat(
                                    "hh:mm aa"
                            );
                            //set selected time on text view
                            tvTimer.setText(f12hours.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }*/
                    }
                }, 12,0,false
                );
                //set transparent background
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //displayed previous selected time
                timePickerDialog.updateTime(tHour,tMinute);
                //show dialog
                timePickerDialog.show();
            }

        });

    }

    private void init(){
        reserve_check = (Button) findViewById(R.id.reserve_check);

        /*監聽按鈕被觸發*/
        reserve_check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getDialog();
            }

            private void getDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MedicalReserve.this);
                builder.setCancelable(false);
                //這邊是設定使用者可否點擊空白處返回
                //builder.setIcon();
                //setIcon可以在Title旁邊放一個小插圖
                //builder.setTitle("IT鐵人賽");
                builder.setMessage("預約成功");
                //alterdialog最多可以放三個按鈕，且位置是固定的，分別是
                //setPositiveButton()右邊按鈕
                //setNegativeButton()中間按鈕
                //setNeutralButton()左邊按鈕
                builder.setNegativeButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        intent.setClass(MedicalReserve.this, MedicalReserveQuery.class);
                        startActivity(intent);
                    }
                });
                builder.create().show();
            }
        });
    }

}