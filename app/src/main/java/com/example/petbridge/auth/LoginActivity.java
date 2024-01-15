package com.example.petbridge.auth;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import com.example.petbridge.MainActivity;
import com.example.petbridge.R;
import com.example.petbridge.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Finito
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    protected ActivityLoginBinding binding ;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

       binding.registerLoginActivity.setOnClickListener(v -> {
           Intent intent = new Intent( this , RegisterFirstActivity.class);
           startActivity(intent);
       });
       binding.forgetpsLogin.setOnClickListener(v -> {
           Intent intent = new Intent (this , ForgetPSActivity.class) ;
           startActivity(intent);
       });
       binding.passwordToggle.setOnClickListener(v -> {
           isPasswordVisible = !isPasswordVisible;
           if (isPasswordVisible) {
               binding.inPassword.setTransformationMethod(null);
               binding.passwordToggle.setImageResource(R.drawable.hidepass);
           } else {
               binding.inPassword.setTransformationMethod(new PasswordTransformationMethod());
               binding.passwordToggle.setImageResource(R.drawable.showpass);
           }
           // Posiziona il cursore fine testo ogni volta che cambia
           binding.inPassword.setSelection(binding.inPassword.getText().length());

       });


       binding.loginAct.setOnClickListener(v -> {
           if (!validateEmail())
           {
               Toast.makeText(this, "Inserisci un Email valida", Toast.LENGTH_SHORT).show();
           } else if (!validatePassword()) {
               Toast.makeText(this, "La password deve contenere almeno 8 caratteri", Toast.LENGTH_SHORT).show();
           } else {

               String email = binding.inEmail.getText().toString().trim();
               String password = binding.inPassword.getText().toString().trim();
               mAuth.signInWithEmailAndPassword(email, password)
                       .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if (task.isSuccessful()) {
                                   Log.d("Login Email and Password", "signInWithEmail:success");
                                   FirebaseUser user = mAuth.getCurrentUser();
                                   Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                                   startActivity(intent);
                               } else {

                                   Log.w("Login Email and Password", "signInWithEmail:failure", task.getException());
                                   Toast.makeText(LoginActivity.this, "Wrong Email Or Password",
                                           Toast.LENGTH_LONG).show();
                               }
                           }
                       });
       }});


}
    private Boolean validateEmail() {
        String email = binding.inEmail.getText().toString().trim();
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean validatePassword() {
        String password = binding.inPassword.getText().toString().trim();
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
        return password.matches(".*[@#$%^&+=].*");
    }

}