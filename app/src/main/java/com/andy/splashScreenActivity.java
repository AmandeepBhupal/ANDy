package com.andy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Intent homeIntent=new Intent(splashScreenActivity.this,introActivity.class);
//        startActivity(homeIntent);
//        finish();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        if(mUser!=null){
            Intent homeIntent=new Intent(splashScreenActivity.this,MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
        else{
            Intent homeIntent=new Intent(splashScreenActivity.this,introActivity.class);
            startActivity(homeIntent);
            finish();
        }


    }
}
