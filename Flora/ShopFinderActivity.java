package com.jagged.flora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ShopFinderActivity extends AppCompatActivity{

    private static final String CURRENT_LOCATION_EXTRA = "from_current_location";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_finder);
        setActivityButtons();
    }

    private void setActivityButtons(){
        // TODO: Implement Snackbar for Floating Action Button -- shows current address and other stats
        FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ShopFinderActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button currentLocationButton = (Button)findViewById(R.id.currentLocationButton);
        currentLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ShopFinderActivity.this,MapsActivity.class);
                intent.putExtra("id",CURRENT_LOCATION_EXTRA);
                startActivity(intent);
                finish();
            }
        });

        Button selectLocationButton = (Button)findViewById(R.id.selectLocationButton);
        selectLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ShopFinderActivity.this,SelectLocationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}