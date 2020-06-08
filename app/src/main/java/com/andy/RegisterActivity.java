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

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    private EditText inputEmail,inputUserName,inputPassword,inputConfirmPassword;
    Button btnRegister;
    TextView btn,btnLoginDirectly;
    private String googleUserName,googleEmail,googleProfilePicture;
    private ArrayList<String> Documents;
    private ArrayList<String> Tags;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    DatabaseReference current_userdb;
    String uID;
    GoogleSignInClient mGoogleSignInClient;
    Button btnGoogleReg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btnGoogleReg=findViewById(R.id.btnGoogleReg);
        btn=findViewById(R.id.alreadyHaveAnAccount);
        inputUserName=findViewById(R.id.inputUserName);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPassword=findViewById(R.id.inputConfirmPassword);
        btnRegister=findViewById(R.id.btnRegister);
        mAuth=FirebaseAuth.getInstance();
        mLoadingBar=new ProgressDialog(RegisterActivity.this,R.style.MyAlertDialogStyle);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
            }
        });
        btnLoginDirectly=findViewById(R.id.loginDirectly);




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLoginDirectly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        btnGoogleReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this,"Google login", Toast.LENGTH_SHORT).show();
                signIn();
            }
        });
    }

    private void signIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        catch(Exception e){
            Toast.makeText(RegisterActivity.this,"Login Unsuccessful!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLoadingBar.setTitle("Login");
        mLoadingBar.setMessage("Please wait while we check your credentials");
        mLoadingBar.setCanceledOnTouchOutside(false);
        mLoadingBar.show();
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        try {
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
            Toast.makeText(RegisterActivity.this,"Please enter credentials!",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RegisterActivity.this, user.getEmail()+" logged in!",Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            // Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().toString(),Toast.LENGTH_SHORT).show();
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
            current_userdb = FirebaseDatabase.getInstance().getReference().child("Profile").child(uID);
            Profile pf = new Profile();
            pf.setName(googleUserName);
            pf.setEmail(googleEmail);
            pf.setProfilePicture(googleProfilePicture);
            pf.setDocuments(new ArrayList<String>());
            pf.setTags(new ArrayList<String>(Arrays.asList("ConstraintLayout")));
            current_userdb.setValue(pf);
            current_userdb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(uID)){
                        //Toast.makeText(RegisterActivity.this, "Account exists!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }




    private void checkCredentials() {
        String userName=inputUserName.getText().toString();
        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();
        String confirmPassword=inputConfirmPassword.getText().toString();

        if(userName.isEmpty() || userName.length()<7){
            showError(inputUserName,"Please enter a username of atleast 7 characters");
        }
        else if(email.isEmpty() || !email.contains("@")){
            showError(inputEmail,"Email is not valid!");
        }
        else if(password.isEmpty() || password.length()<7){
            showError(inputPassword,"Password must be atleast 7 characters long");
        }
        else if(confirmPassword.isEmpty() || !confirmPassword.equals(password)){
            showError(inputConfirmPassword,"Passwords do not match!");
        }
        else{
            mLoadingBar.setTitle("Registration");
            mLoadingBar.setMessage("Please wait while we upload your credentials");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT);
                        mLoadingBar.dismiss();
                        uID=mAuth.getCurrentUser().getUid();
                        current_userdb= FirebaseDatabase.getInstance().getReference().child("Profile");
                        current_userdb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(uID)){
                                    Toast.makeText(RegisterActivity.this, "Account exists!",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else{
                                    current_userdb = FirebaseDatabase.getInstance().getReference().child("Profile").child(uID);
                                    Profile pf = new Profile();
                                    pf.setName(inputUserName.getText().toString());
                                    pf.setEmail(inputEmail.getText().toString());
                                    pf.setProfilePicture("https://drive.google.com/file/d/1S-FhCulXC_vbiYBwspqZ6yrhALvVqYGh/view?usp=sharing");
                                    pf.setDocuments(new ArrayList<String>());
                                    pf.setTags(new ArrayList<String>());
                                    current_userdb.setValue(pf);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    } else {
                        Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
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
