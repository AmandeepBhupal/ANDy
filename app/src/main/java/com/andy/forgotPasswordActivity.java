package com.andy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPasswordActivity extends AppCompatActivity {
    Toolbar toolbar;
    ProgressBar progressBar;
    EditText userEmail;
    Button btnSendUserPass;

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

       // toolbar=findViewById(R.id.toolbar);
       // progressBar=findViewById(R.id.progressBar);
        userEmail=findViewById(R.id.userEmail);
        btnSendUserPass=findViewById(R.id.btnForgotPass);

        //toolbar.setTitle("Forgot Password");

        mAuth=FirebaseAuth.getInstance();

        btnSendUserPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(userEmail.getText().toString()).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(forgotPasswordActivity.this,"Password reset email sent!",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(forgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(forgotPasswordActivity.this,"User not found! Please register!",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(forgotPasswordActivity.this,RegisterActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }

        });

    }
}
