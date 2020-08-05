package com.jagged.flora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FlowerPickerActivity extends AppCompatActivity{

    private static final String TAG = FlowerPickerActivity.class.getName();
    private static final String DELIMETER = "!";
    private static final int[] SPINNERS = {R.id.colorSpinner,R.id.recipientSpinner,
            R.id.seasonSpinner,R.id.symbolSpinner};

    private String searchParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_picker);
        searchParameters = "";
        setActivitySpinners();
        Button searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getSpinnerData();
                if(searchParameters != null && searchParameters.length() > 0){
                    Intent intent = new Intent(FlowerPickerActivity.this,SearchResultActivity.class);
                    intent.putExtra("search_parameters", searchParameters);
                    startActivity(intent);
                    finish();
                }else Log.d(TAG,"Variable searchParameters is empty.");
            }
        });
        FloatingActionButton backButton = (FloatingActionButton)findViewById(R.id.fab);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(FlowerPickerActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setActivitySpinners(){
        Spinner spinner;
        int id;

        for(int i : SPINNERS){
            spinner = (Spinner)findViewById(i);
            switch(i){
                case R.id.colorSpinner:
                    id = R.array.colors_array;
                    break;
                case R.id.recipientSpinner:
                    id = R.array.recipients_array;
                    break;
                case R.id.seasonSpinner:
                    id = R.array.seasons_array;
                    break;
                case R.id.symbolSpinner:
                    id = R.array.symbols_array;
                    break;
                default:
                    id = -1;
                    Log.w(TAG,"Error while initializing spinners.");
            }
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    id, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    private void getSpinnerData(){
        Spinner spinner;
        String value;

        for(int i : SPINNERS){
            spinner = (Spinner)findViewById(i);
            value = spinner.getSelectedItem().toString();

            if(value != "" && value != null && value != "not selected")
                searchParameters = searchParameters + value + DELIMETER;
        }
    }
}