package com.jagged.flora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity{

    private static final String TAG = SelectLocationActivity.class.getName();
    private static final int[] TEXT_BOXES = {R.id.streetAddressPlainText,R.id.cityPlainText,R.id.zipcodePlainText};
    private static final int[] SPINNERS = {R.id.countrySpinner,R.id.stateSpinner};
    private static final String SELECT_LOCATION_EXTRA = "from_select_location";


    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        address = "";
        setActivitySpinners();
        setActivityButtons();
    }

    private void setActivityButtons(){
        Button finishButton = (Button)findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                compileAddress();
                if(address != null && address.length() > 0){
                    Intent intent = new Intent(SelectLocationActivity.this,MapsActivity.class);
                    intent.putExtra("address", address);
                    intent.putExtra("id",SELECT_LOCATION_EXTRA);
                    startActivity(intent);
                }else Log.d(TAG,"Variable address is empty.");
            }
        });
        Button refreshButton = (Button)findViewById(R.id.clearButton);
        refreshButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        FloatingActionButton backButton = (FloatingActionButton)findViewById(R.id.fab);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(SelectLocationActivity.this,ShopFinderActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void compileAddress(){
        EditText text;
        String value;
        for(int i : TEXT_BOXES){
            text = (EditText)findViewById(i);
            value = text.getText().toString();
            if(value != "" && value != null) address = address + value + ",";
        }
        getSpinnerData();
    }

    private void setActivitySpinners(){
        // country spinner
        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country))
                countries.add(country);
        }

        Collections.sort(countries);
        for (String country : countries) System.out.println(country);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countries);

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner countrySpinner = (Spinner)findViewById(R.id.countrySpinner);
        countrySpinner.setAdapter(countryAdapter);

        // state spinner
        Spinner stateSpinner = (Spinner)findViewById(R.id.stateSpinner);
        ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(this,
                R.array.states_array, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateAdapter);
    }

    private void getSpinnerData(){
        Spinner spinner;
        String value;
        for(int i : SPINNERS){
            spinner = (Spinner)findViewById(i);
            value = spinner.getSelectedItem().toString();
            if(value != "" && value != null) address = address + value;
            if(i == R.id.countrySpinner) address = address + ",";
        }
    }
}