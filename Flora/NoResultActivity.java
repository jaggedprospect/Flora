package com.jagged.flora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoResultActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_result);
        Button newSearchButton = (Button)findViewById(R.id.newSearchButton);
        newSearchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(NoResultActivity.this,FlowerPickerActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}