package com.example.petbridge.auth;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.petbridge.R;
import com.example.petbridge.databinding.ActivityRegisterFinallyBinding;
import com.example.petbridge.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterFinallyActivity extends AppCompatActivity {
    protected ActivityRegisterFinallyBinding binding ;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Boolean isPasswordVisible1 =false;
    private Boolean isPasswordVisible2 =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_finally);
        Log.d("RegisterFirstActivity", "onCreate");

        binding = DataBindingUtil.setContentView(this , R.layout.activity_register_finally);
        String name = getIntent().getStringExtra("name");
        String lastName = getIntent().getStringExtra("lastName");
        String birthDate = getIntent().getStringExtra("birthDate");

        binding.backRegisterAc.setOnClickListener(v -> {
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
       // db = FirebaseFirestore.getInstance();
        db = FirebaseManager.getFirestoreInstance();

        binding.passwordToggleRegisterAc.setOnClickListener(v -> {
          PasswordToggle(binding.passwordToggleRegisterAc ,binding.inPasswordRegisterAc , isPasswordVisible1);
          isPasswordVisible1=!isPasswordVisible1;
        });
        binding.rpPasswordToggleRegisterAc.setOnClickListener(v -> {
            PasswordToggle(binding.rpPasswordToggleRegisterAc , binding.inRpPasswordRegisterAc , isPasswordVisible2);
            isPasswordVisible2=!isPasswordVisible2;
        });



        binding.btnRegisterAc.setOnClickListener(v -> {
            String rpPassword = binding.inRpPasswordRegisterAc.getText().toString().trim();
            String email = binding.inEmailRegisterAc.getText().toString().trim() ;
            String password = binding.inPasswordRegisterAc.getText().toString().trim();
            if (!validateEmail()){
                Toast.makeText(this, "Inserisci un Email valida", Toast.LENGTH_SHORT).show();
            } else if (!validatePassword()) {
                Toast.makeText(this, "Inserisci un password valida ", Toast.LENGTH_SHORT).show();
            } else if (!(password.equals(rpPassword))){
                Toast.makeText(this, "password must match ", Toast.LENGTH_SHORT).show();
            }else {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                sendEmailVerification(user);
                                saveUserDataToFirestore(user, name, lastName, birthDate, email , "null");
                                updateFCMToken();
                                Log.d("RegisterFireBase", "andato a buon fine");

                                new AlertDialog.Builder(this)
                                        .setTitle("Conferm your email")
                                        .setMessage("Conferm your email then login")
                                        .setPositiveButton("Conferm", (dialog, which) -> {

                                            dialog.dismiss();
                                            Intent intent = new Intent(this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                        })
                                        .show();



                            } else {
                                Toast.makeText(this, "Error creating user", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

        });



    }
    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM Token", token);

                        // Salva il token nel tuo database (nell'oggetto utente o in un'altra posizione appropriata)
                        saveFCMTokenToFirestore(token);
                    }
                });
    }

    private void saveFCMTokenToFirestore(String token) {
        // Ottieni l'ID dell'utente corrente (puoi ottenerlo da mAuth)
        String userId = mAuth.getCurrentUser().getUid();

        // Salva il token FCM nel documento dell'utente nel tuo database
        // Sostituisci "users" con la tua raccolta utenti nel Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Save FCM Token", "Success");
                })
                .addOnFailureListener(e -> {
                    Log.d("Save FCM Token", "Failed");
                });
    }
    public void PasswordToggle(ImageView toggleButton, EditText passwordField , Boolean isPasswordVisible) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setTransformationMethod(null);
            toggleButton.setImageResource(R.drawable.hidepass);
        }  else {
            passwordField.setTransformationMethod(new PasswordTransformationMethod());
            toggleButton.setImageResource(R.drawable.showpass);
        }
        passwordField.setSelection(binding.inPasswordRegisterAc.getText().length());

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("RegisterFirstActivity", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("RegisterFirstActivity", "onResume");
    }
    private void saveUserDataToFirestore(FirebaseUser user, String name , String lastName , String birthDate,String email,String image) {
        String userId = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("Name", name);
        userData.put("LastName" , lastName);
        userData.put("Email", email);
        userData.put("birthDate" , birthDate);
        userData.put("Image", image);
        db.collection("Users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Save data in FireStore", "Success");
                })
                .addOnFailureListener(e -> {
                    Log.d("Save data in FireStore", "failed");
                });
    }



    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("auth firebase", "Success");

                    } else {
                        Log.d("auth firebase", "failed");
                    }
                });
    }


        private Boolean validateEmail() {
        String email = binding.inEmailRegisterAc.getText().toString().trim();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false;
        return true ;
    }
    private boolean validatePassword() {
        String password = binding.inPasswordRegisterAc.getText().toString().trim();
        String rpPassword = binding.inRpPasswordRegisterAc.getText().toString().trim();
        int minLength = 8;
        if (password.length() < minLength) {
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        if (!password.matches(".*[@#$%^&+=].*")) {
            return false;
        }

        return true;
    }


}