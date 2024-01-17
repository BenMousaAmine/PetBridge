package com.example.petbridge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.petbridge.auth.LoginActivity;
import com.example.petbridge.auth.RegisterFirstActivity;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;

public class RunActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button login ;
    Button register ;
    private final Boolean auth = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        Places.initialize(getApplicationContext(), getString(R.string.places_api_key));

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(RunActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        login = findViewById(R.id.loginMain);
        register = findViewById(R.id.registerMain);



        login.setOnClickListener(v ->{
            Intent intent = new Intent(this , LoginActivity.class);
            startActivity(intent);
        });
        register.setOnClickListener(v -> {
            Intent intent = new Intent(this , RegisterFirstActivity.class);
            startActivity(intent);
        });
    }
}