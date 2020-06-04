package com.andy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN =101 ;
    TextView btn;
    EditText inputEmail, inputPassword;
    Button btnLogin;
    private String googleUserName, googleEmail,googleProfilePicture;
    private ArrayList<String> Documents;
    private ArrayList<String> Tags;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    private Button btnGoogle;
    private TextView txtViewForgotPassword;
    GoogleSignInClient mGoogleSignInClient;
    DatabaseReference current_userdb;
    String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn=findViewById(R.id.textView_SignUp);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);

        btnLogin=findViewById(R.id.btnLogin);
        txtViewForgotPassword=findViewById(R.id.txtForgotPassword);
        mAuth=FirebaseAuth.getInstance();
        mLoadingBar=new ProgressDialog(LoginActivity.this,R.style.MyAlertDialogStyle);
        btnGoogle=findViewById(R.id.btnGoogle);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"Google login", Toast.LENGTH_SHORT).show();
                signIn();
            }
        });

        txtViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"Redirecting",Toast.LENGTH_SHORT).show();
                Intent it=new Intent(LoginActivity.this,forgotPasswordActivity.class);
                startActivity(it);
            }
        });
    }
    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLoadingBar.setTitle("Login");
        mLoadingBar.setMessage("Please wait while we check your credentials");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
     //   mLoadingBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000,android.graphics.PorterDuff.Mode.MULTIPLY);
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    //Log.w(TAG, "Google sign in failed", e);
                    Toast.makeText(this, "Sign in unsuccessful!", Toast.LENGTH_SHORT).show();
                    firebaseAuthWithGoogle(null);
                    // ...
                }
            }
        }
        catch (Exception e){
            Toast.makeText(LoginActivity.this,"Please enter credentials!",Toast.LENGTH_SHORT).show();
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, user.getEmail()+" logged in!",Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                           // Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().toString(),Toast.LENGTH_SHORT).show();
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {


        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (acct != null) {
            //String personName = acct.getDisplayName();
            googleUserName = acct.getDisplayName();
            //String personGivenName = acct.getGivenName();
            //String personFamilyName = acct.getFamilyName();
            googleEmail = acct.getEmail();
            //String personId = acct.getId();
            googleProfilePicture = acct.getPhotoUrl().toString();


            uID = acct.getId();
            current_userdb = FirebaseDatabase.getInstance().getReference().child("Profile");
            current_userdb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(uID)){
                       // Toast.makeText(LoginActivity.this, "Account exists!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{

                        current_userdb = FirebaseDatabase.getInstance().getReference().child("Profile").child(uID);
                        Profile pf = new Profile();
                        pf.setName(googleUserName);
                        pf.setEmail(googleEmail);
                        pf.setProfilePicture(googleProfilePicture);
                        pf.setDocuments(new ArrayList<String>());
                        pf.setTags(new ArrayList<String>(Arrays.asList("ConstraintLayout")));
                        current_userdb.setValue(pf);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }

        //Intent intent=new Intent(LoginActivity.this,MainPageActivity.class);

        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }


    private void checkCredentials() {

        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();
        if(email.isEmpty() || !email.contains("@")){
            showError(inputEmail,"Email is not valid!");
        }
        else if(password.isEmpty() || password.length()<7){
            showError(inputPassword,"Password must be atleast 7 characters long");
        }
        else{
            mLoadingBar.setTitle("Login");
            mLoadingBar.setMessage("Please wait while we check your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mLoadingBar.dismiss();
                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                        mLoadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();
    }
}
