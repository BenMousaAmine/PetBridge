package com.example.petbridge.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.os.Bundle;

import com.example.petbridge.R;

import com.example.petbridge.databinding.ActivityRegisterFirstBinding;

import java.util.Calendar;

public class RegisterFirstActivity extends AppCompatActivity {
    protected ActivityRegisterFirstBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_first);
        binding = DataBindingUtil.setContentView(this , R.layout.activity_register_first);

        binding.inBirthRegisterAc.setOnClickListener(v -> {
            mostraDatePicker();
        });

        binding.loginPage.setOnClickListener(v -> {
            Intent intent = new Intent(this , LoginActivity.class );
            startActivity(intent);
        });

            binding.nextRegisterAc.setOnClickListener(v -> {
            String name = binding.inNameRegisterAc.getText().toString().trim();
            String lastName = binding.inLastNameRegisterAc.getText().toString().trim();
            String birthDate = binding.inBirthRegisterAc.getText().toString().trim();
            if (!verficaCampo(name) || !verficaCampo(lastName) || birthDate.length() == 0) {
                Toast.makeText(this, "Campo nome e cognome accettano solo caratteri alfabetici e la data di nascita è obbligatoria", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, RegisterFinallyActivity.class);
                intent.putExtra("name" , name) ;
                intent.putExtra("lastName" , lastName);
                intent.putExtra("birthDate" , birthDate);
                startActivity(intent);
            }
        });



    }

    private boolean verficaCampo(String name) {
        return !TextUtils.isEmpty(name) && name.matches("^[a-zA-Z]+( [a-zA-Z]+)*$");
    }


    private void mostraDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, yearSelected);
                    int age = calculateAge(yearSelected, monthOfYear, dayOfMonth);
                    if (age > 13) {
                        binding.inBirthRegisterAc.setText(selectedDate);
                    } else {
                        Toast.makeText(this, "Devi essere oltre i 13 anni per registrarti.", Toast.LENGTH_LONG).show();
                    }
                },
                year,
                month,
                day);

        datePickerDialog.show();
    }

    private int calculateAge(int birthYear, int birthMonth, int birthDay) {
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int age = todayYear - birthYear;
        if (todayMonth < birthMonth || (todayMonth == birthMonth && todayDay < birthDay)) {
            age--;
        }
        return age;
    }

}