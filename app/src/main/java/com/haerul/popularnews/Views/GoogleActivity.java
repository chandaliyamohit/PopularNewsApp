package com.haerul.popularnews.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.haerul.popularnews.MainActivity;
import com.haerul.popularnews.R;

public class GoogleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Google_Activity";
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String googleIDToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        googleSignInButton = findViewById(R.id.sign_in_button);
        progressBar = findViewById(R.id.cyclic_progress_bar);
        googleSignInButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null){
                // User is already registered with Google account
                // Exiting user
                googleSignInButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                navigateToMainActivity(account);
            }else {
                // User is not registered with Google account
                // New User
                googleSignInButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }catch (Exception e){
            Log.d(TAG, "User already exits check exception: "+e.toString());
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(GoogleActivity.this, "Sign out successful", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "onComplete: ");
                    })
                    .addOnFailureListener(e1 -> Log.d(TAG, "onFailure: "));
        }

    }


    @Override
    public void onClick(View v) {
        if (v == googleSignInButton){
            // Google sign in button clicked
            progressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            if (account != null){
                Log.d(TAG, "Google account not null: ");
                if (account.getIdToken() != null){
                    googleIDToken = account.getIdToken();
                    Log.d(TAG, "Google account token id not null: "+googleIDToken);
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),  null);
                    firebaseAuthWithGoogle(credential, account);
                }
                else {
                    Log.d(TAG, "Google account token id null: ");
                }
            }
            else {
                Log.d(TAG, "Google account null: ");
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }

    private void navigateToMainActivity(GoogleSignInAccount account) {
        try {
            progressBar.setVisibility(View.INVISIBLE);
            Intent navigateToMainAcitivity = new Intent(this, MainActivity.class);
            if (account.getDisplayName() != null){
                navigateToMainAcitivity.putExtra("user_name", account.getDisplayName());
            }
            if (account.getEmail() != null){
                navigateToMainAcitivity.putExtra("user_email", account.getEmail());
            }
            if (account.getId() != null){
                navigateToMainAcitivity.putExtra("user_id", account.getId());
            }
            if (account.getIdToken() != null){
                navigateToMainAcitivity.putExtra("user_id_token", account.getIdToken());
            }
            if (account.getPhotoUrl() != null){
                navigateToMainAcitivity.putExtra("user_image", account.getPhotoUrl());
            }
            startActivity(navigateToMainAcitivity);
        }catch (Exception e){
            Log.d(TAG, "navigateToMainActivity exception: "+e.toString());
        }
    }


    private void firebaseAuthWithGoogle(AuthCredential credential, GoogleSignInAccount googleSignInAccount){
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(GoogleActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity(googleSignInAccount);
                    }else {
                        Toast.makeText(GoogleActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}