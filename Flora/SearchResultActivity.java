package com.jagged.flora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SearchResultActivity extends AppCompatActivity{

    private static final String TAG = SearchResultActivity.class.getName();
    private static final String TARGET_DB_COLLECTION = "flowers";
    private static final String DELIMETER = "!";
    private static final String[] FIELDS = {"colors","recipients","season","symbols"};
    private static final int[] TEXTVIEWS ={R.id.resultColorText,R.id.resultRecipientsText,
            R.id.resultSeasonText,R.id.resultSymbolText};

    // Cloud Firestore database variable
    private FirebaseFirestore db;
    private int targetMatchCount;
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private LoadingTask loadingTask;
    private Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        // Access Cloud Firestore database instance
        db = FirebaseFirestore.getInstance();
        returnButton = (Button)findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(SearchResultActivity.this,FlowerPickerActivity.class);
                startActivity(intent);
                finish();
            }
        });
        progressBarHolder = (FrameLayout)findViewById(R.id.progressBarHolder);
        loadingTask = new LoadingTask();
        // Tokenize search parameters from Extras
        String[] params = tokenizeSearchParameters(getIntent().getStringExtra("search_parameters"));
        targetMatchCount = getSearchParametersLength(params);
        if(targetMatchCount != -1){
            loadingTask.execute();
            queryFirestore(params);
        }
        else {
            Log.d(TAG,"No search parameters were found. Database will not be queried.");
            Intent intent = new Intent(SearchResultActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private String[] tokenizeSearchParameters(String params){
        params = params.trim().toLowerCase();
        return params.split(DELIMETER);
    }

    private int getSearchParametersLength(String[] params){
        int count = 0;
        for(String s : params){
            if(!s.equalsIgnoreCase("not selected")) count++;
        }
        return count > 0 ? count : -1;
    }

    private int checkDocument(DocumentSnapshot ds,String field,String value){
        String result = (String)ds.get(field);
        if(result != null){
            String[] splitResult=result.split(",");
            for(String s : splitResult)
                if(s.equalsIgnoreCase(value)) return 1;
        }
        return 0;
    }

    private void queryFirestore(final String[] params){
        db.collection(TARGET_DB_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int fieldMatchCount = 0;
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                for(int i = 0; i < FIELDS.length; i++)
                                    fieldMatchCount += checkDocument(document,FIELDS[i],params[i]);
                                if(fieldMatchCount == targetMatchCount){
                                    displayResults((String)document.get("name"));
                                    return;
                                }
                                fieldMatchCount = 0;
                            }
                            Log.d(TAG, "No documents matched the provided parameters.");
                            Intent intent = new Intent(SearchResultActivity.this,NoResultActivity.class);
                            startActivity(intent);
                            finish();
                        } else
                            Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void displayResults(String name){
        db.collection(TARGET_DB_COLLECTION)
                .whereEqualTo("name",name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task){
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                TextView textView; String text;
                                for(int i = 0; i < TEXTVIEWS.length; i++){
                                    textView = (TextView)findViewById(TEXTVIEWS[i]);
                                    text = (String)document.get(FIELDS[i]);
                                    if(text.contains(",")) textView.setText(format(text,true));
                                    else textView.setText(format(text,false));
                                }
                                textView = (TextView)findViewById(R.id.resultNameText);
                                text = (String)document.get("name");
                                textView.setText(format(text,false));
                            }
                        } else
                            Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
        loadingTask.cancel(true);
    }

    private String format(String result, boolean addSpace){
        StringBuilder sb = new StringBuilder();
        String toFormat = result.trim();
        if(addSpace){
            String[] split = toFormat.split(",");
            for(String s : split){
                sb.append(s + ", ");
            }
            toFormat = sb.toString().trim().substring(0,sb.length() - 2);
            sb.setLength(0);
        }
        sb.append(toFormat.substring(0,1).toUpperCase());
        sb.append(toFormat.substring(1));
        return sb.toString();
    }

    // Add data to database
    public void addData(){
        // Create Flower with name
        Map<String, Object> flower = new HashMap<>();
        flower.put("name", "new_flower");

        // Add new Flower document with generated ID
        db.collection(TARGET_DB_COLLECTION).add(flower)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
                    @Override
                    public void onSuccess(DocumentReference documentReference){
                        Log.d(TAG,"DocumentSnapshot added with ID: "+documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e){
                        Log.w(TAG,"Error adding document", e);
                    }
                });
    }


    // Read data from database
    public void readData(){
        db.collection(TARGET_DB_COLLECTION).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task){
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult())
                                Log.d(TAG,document.getId()+" => "+document.getData());
                        }else Log.w(TAG,"Error while getting documents.",task.getException());
                    }
                });
    }

    // Test getting specific document from db collection
    public void getRoseData(){
        DocumentReference docRef = db.collection(TARGET_DB_COLLECTION).document("rose");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    else Log.d(TAG, "No such document");
                }else Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            returnButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }


        @Override
        protected Void doInBackground(Void... params) {
            try {
                // run for arbitrary number of iterations until search finishes
                for(int i = 0; i < 1000; i++) {
                    Log.d(TAG,"Waiting for Database Query Results: " + i + " seconds passed");
                    TimeUnit.SECONDS.sleep(1);
                    if(this.isCancelled()) break;
                }
            } catch (InterruptedException e) {
                Log.d(TAG,"LoadingTask - doInBackground() was canceled upon finishing search.");
            }catch(Exception e){
                Log.w(TAG,"Error occurred in LoadingTask - doInBackground().");
            }
            return null;
        }

        @Override
        protected void onCancelled(){
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            returnButton.setEnabled(true);
        }
    }
}