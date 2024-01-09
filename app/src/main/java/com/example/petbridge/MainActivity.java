package com.example.petbridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;


import com.example.petbridge.navigation.HomeFragment;
import com.example.petbridge.navigation.ProfileFragment;
import com.example.petbridge.navigation.SOSFragment;
import com.example.petbridge.navigation.MessageFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navbtn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navbtn = findViewById(R.id.tabMain);

        navbtn.setOnItemSelectedListener(item -> {
            String selected = Objects.requireNonNull(item.getTitle().toString());
            ApplicaFragement(selected);
            return true ;
        });


    }
    protected  void ApplicaFragement ( String name ) {
        FragmentManager fragmentManager1 = getSupportFragmentManager() ;
        switch (name){
            case "Home" : {
                fragmentManager1.beginTransaction().replace(R.id.fragment_container , HomeFragment.class  , null)
                        .setReorderingAllowed(true)
                        .commit();
                break;
            }
            case "Message":
                fragmentManager1.beginTransaction().replace(R.id.fragment_container , MessageFragment.class  , null)
                        .setReorderingAllowed(true)
                        .commit();
                break;
            case "SOS" :
                fragmentManager1.beginTransaction().replace(R.id.fragment_container , SOSFragment.class  , null)
                        .setReorderingAllowed(true)
                        .commit();
                break;
            case "Profile":
                fragmentManager1.beginTransaction().replace(R.id.fragment_container , ProfileFragment.class  , null)
                        .setReorderingAllowed(true)
                        .commit();
            break ;
        }
    }


}