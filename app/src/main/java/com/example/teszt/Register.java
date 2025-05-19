package com.example.teszt;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private static final String LOG_TAG = Register.class.getName();

    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    private static final int SECRET_KEY = 99;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userPasswordEditText;
    EditText userPasswordAgainEditText;
    EditText birthDateEditText;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int secret = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secret != 99) {
            finish();
        }

        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userPasswordEditText = findViewById(R.id.passwordEditText);
        userPasswordAgainEditText = findViewById(R.id.passwordAgainEditText);
        birthDateEditText = findViewById(R.id.birthDateEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(userName);
        userPasswordEditText.setText(password);
        userPasswordAgainEditText.setText(password);

        birthDateEditText.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(Register.this,
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        birthDateEditText.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Firebase inicializálás
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    public Register() {
        super();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString();
        String userEmail = userEmailEditText.getText().toString();
        String userPassword = userPasswordEditText.getText().toString();
        String userPasswordAgain = userPasswordAgainEditText.getText().toString();
        String birthDateString = birthDateEditText.getText().toString();

        if (!userPassword.equals(userPasswordAgain)) {
            Log.e(LOG_TAG, "Nem egyenlőek a jelszavak!");
            return;
        }
        if (userName.isEmpty()) {
            userNameEditText.setError("Név megadása kötelező!");
            return;
        }
        if (userEmail.isEmpty()) {
            userEmailEditText.setError("E-mail megadása kötelező!");
            return;
        }
        if (userPassword.isEmpty()) {
            userPasswordEditText.setError("Jelszó megadása kötelező!");
            return;
        }
        if (birthDateString.isEmpty()) {
            birthDateEditText.setError("Születési dátum megadása kötelező!");
            return;
        }

        String[] parts = birthDateString.split("/");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int year = Integer.parseInt(parts[2]);

        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 18) {
            birthDateEditText.setError("18 éven felüliek regisztrálhatnak!");
            Log.e(LOG_TAG, "Felhasználó túl fiatal: " + age + " éves");
            Toast.makeText(Register.this, "Túl fiatal!", Toast.LENGTH_LONG).show();
            return;
        }

        Log.i(LOG_TAG, "Regisztrált: " + userName + ", E-mail: " + userEmail + ", Életkor: " + age);

        // Firebase Authentication regisztráció
        int finalAge = age;
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Sikeres regisztrálás!");

                            // Regisztrált felhasználó adatok lekérése
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // Felhasználói adatok Firestore-ba mentése
                                saveUserToFirestore(user.getUid(), userName, userEmail, birthDateString, finalAge);
                            }
                        } else {
                            Log.d(LOG_TAG, "Sikertelen regisztrálás!");
                            Toast.makeText(Register.this, "Nem sikeres: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String userName, String userEmail, String birthDate, int age) {
        // Felhasználói adatok map-be rendezése
        Map<String, Object> user = new HashMap<>();
        user.put("userName", userName);
        user.put("userEmail", userEmail);
        user.put("birthDate", birthDate);
        user.put("age", age);
        user.put("registrationDate", Calendar.getInstance().getTime());

        // Dokumentum mentése a "Users" kollekcióba az UID-vel mint dokumentum ID
        firestore.collection("Users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "Felhasználói adatok sikeresen mentve a Firestore-ba!");
                        Toast.makeText(Register.this, "Regisztráció sikeres!", Toast.LENGTH_SHORT).show();
                        startShopping();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Hiba a felhasználói adatok mentése során", e);
                        Toast.makeText(Register.this, "Hiba történt az adatok mentése során: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        // Itt dönthetünk, hogy mégis továbblépjünk-e vagy maradjunk a regisztrációs oldalon
                        startShopping();
                    }
                });
    }

    public void cancel(View view) {
        finish();
    }

    private void startShopping() {
        Intent intent = new Intent(this, Shop.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }
}