package com.jagged.flora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.jagged.flora.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SplashActivity.class.getName();
    private static final int SPLASH_SCREEN_TIME_OUT = 2000;
    private static final int ANIMATION_DURATION = 1250;
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;
    private GoogleSignInClient mSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;
    private LoadingTask loadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // used so splash screen will cover the entire screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        loadingTask = new LoadingTask();
        // Get device screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        final int height = size.y;
        final float deltaY = height / 1.25f;

        // Display Splash (Logo)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Move Logo image view
                ImageView logo = (ImageView) findViewById(R.id.logoImage);
                ObjectAnimator logoAnim = ObjectAnimator.ofFloat(logo, "translationY",
                        0.0f, (-height) + deltaY);
                logoAnim.setDuration(ANIMATION_DURATION);   // animation duration
                //animation.setRepeatCount(5);              // animation repeat count
                //animation.setRepeatMode(0);               // repeat animation
                logoAnim.start();
                // Move Sign In button
                SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
                int[] location = new int[2];
                signInButton.getLocationOnScreen(location); // button location (x, y)
                signInButton.setVisibility(View.VISIBLE);
                ObjectAnimator buttonAnim = ObjectAnimator.ofFloat(signInButton, "translationY",
                        (float)location[0], (-height / 2.5f));
                buttonAnim.setDuration(ANIMATION_DURATION);
                buttonAnim.start();
                // Reveal Sign In prompt
                TextView prompt = (TextView)findViewById(R.id.loginPromptText);
                inAnimation = new AlphaAnimation(0.0f,1.0f);
                inAnimation.setDuration(ANIMATION_DURATION * 2);
                prompt.setAnimation(inAnimation);
                prompt.setVisibility(View.VISIBLE);
                prompt.startAnimation(inAnimation);

            }
        }, SPLASH_SCREEN_TIME_OUT);
        int logoutStatus = getIntent().getIntExtra("logout_status",-1);
        if(logoutStatus == 1) signOut();
        mSignInButton=(SignInButton)findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        // Configure Google Sign In
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient=GoogleSignIn.getClient(this,gso);
        // Initialize FirebaseAuth
        mFirebaseAuth=FirebaseAuth.getInstance();
        progressBarHolder = (FrameLayout)findViewById(R.id.progressBarHolder);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut(){
        GoogleSignInOptions gso = new
                GoogleSignInOptions.
                        Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mSignInClient = GoogleSignIn.getClient(this, gso);
        // Sign Out of Firebase
        FirebaseAuth.getInstance().signOut();
        // Sign Out of Google
        mSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task){
                Toast.makeText(SplashActivity.this,"You have been successfully signed out.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Start loading task while authorization completes
        loadingTask.execute();
        // Result returned from launching the Intent in signIn()
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SplashActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SplashActivity.this, "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }
                        loadingTask.cancel(true);
                    }
                });
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSignInButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            mSignInButton.setEnabled(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // run for arbitrary number of iterations until authentication finishes
                for (int i = 0; i < 1000; i++) {
                    Log.d(TAG,"Waiting for Google Firebase Authentication: " + i + " seconds passed");
                    TimeUnit.SECONDS.sleep(1);
                    if(loadingTask.isCancelled()){
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG,"LoadingTask - doInBackground() was canceled upon finishing Firebase authentication.");
            }catch(Exception e){
                Log.w(TAG,"Error occurred in LoadingTask - doInBackground().");
            }
            return null;
        }
    }
}