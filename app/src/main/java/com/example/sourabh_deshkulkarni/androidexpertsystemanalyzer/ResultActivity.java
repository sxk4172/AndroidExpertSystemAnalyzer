package com.example.sourabh_deshkulkarni.androidexpertsystemanalyzer;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.graphics.Color.GREEN;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String message = getIntent().getStringExtra("Message");
        int weightage = getIntent().getIntExtra("Weightage",0);
        final String recommendations = getIntent().getStringExtra("recommendations");
        final TextView field_recomm = (TextView)findViewById(R.id.recomm_textView);
        field_recomm.setMovementMethod(new ScrollingMovementMethod());
        final TextView field_result = (TextView)findViewById(R.id.resultText);
        field_result.setText(message);
        if(weightage <50){
            field_result.setBackgroundColor(Color.GREEN);
        }else if(weightage <= 75 && weightage >= 50){
            field_result.setBackgroundColor(Color.YELLOW);
        }else{
            field_result.setBackgroundColor(Color.RED);
        }
        final Button button_recomm = (Button)findViewById(R.id.recom_button);
        assert button_recomm != null;
        button_recomm.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                assert field_recomm != null;
                field_recomm.setText(recommendations);

            }
        });
    }
}
