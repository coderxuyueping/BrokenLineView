package com.tg.test.brokenlineview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private List<String> xText = new ArrayList<>();
    private List<String> yText = new ArrayList<>();
    private List<Float> income = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 6; i++) {
            xText.add(i + 1 + "月");
        }
        yText.add("0.1");
        yText.add("0.2");
        yText.add("0.3");
        yText.add("0.4");
        yText.add("0.5");
        yText.add("0.6");

        income.add(0.2f);
        income.add(0.12f);
        income.add(0.4f);
        income.add(0.3f);
        income.add(0.6f);
        income.add(0.1f);
        ((BrokenLineView) findViewById(R.id.broken_line)).drawLine(xText, yText, income, "%");
    }
}
