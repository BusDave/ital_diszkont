package com.example.teszt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private TextView userNameText, userEmailText, userAgeText, registrationDateText;
    private TextView ageGroupStatsText;
    private RecyclerView oldestUsersRecyclerView, newestUsersRecyclerView;
    private Button backButton;

    private UserAdapter oldestUsersAdapter, newestUsersAdapter;
    private List<UserData> oldestUsers, newestUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerViews();

        loadUserProfile();
        performComplexQueries();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        userAgeText = findViewById(R.id.userAgeText);
        registrationDateText = findViewById(R.id.registrationDateText);
        ageGroupStatsText = findViewById(R.id.ageGroupStatsText);
        oldestUsersRecyclerView = findViewById(R.id.oldestUsersRecyclerView);
        newestUsersRecyclerView = findViewById(R.id.newestUsersRecyclerView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerViews() {
        oldestUsers = new ArrayList<>();
        newestUsers = new ArrayList<>();

        oldestUsersAdapter = new UserAdapter(oldestUsers);
        newestUsersAdapter = new UserAdapter(newestUsers);

        oldestUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        oldestUsersRecyclerView.setAdapter(oldestUsersAdapter);

        newestUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newestUsersRecyclerView.setAdapter(newestUsersAdapter);
    }

    private void loadUserProfile() {
        String userId = currentUser.getUid();

        firestore.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String userName = document.getString("userName");
                                String userEmail = document.getString("userEmail");
                                Long age = document.getLong("age");
                                Date registrationDate = document.getDate("registrationDate");

                                userNameText.setText("Név: " + (userName != null ? userName : "N/A"));
                                userEmailText.setText("E-mail: " + (userEmail != null ? userEmail : "N/A"));
                                userAgeText.setText("Életkor: " + (age != null ? age : "N/A"));

                                if (registrationDate != null) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
                                    registrationDateText.setText("Regisztráció: " + dateFormat.format(registrationDate));
                                }
                            } else {
                                Log.e(LOG_TAG, "Felhasználói dokumentum nem található!");
                                Toast.makeText(ProfileActivity.this, "Felhasználói adatok nem találhatók!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(LOG_TAG, "Hiba a felhasználói adatok betöltése során", task.getException());
                            Toast.makeText(ProfileActivity.this, "Hiba a profil betöltése során!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void performComplexQueries() {
        getOldestUsers();

        getNewestUsers();

        getAgeGroupStatistics();
    }

    private void getOldestUsers() {
        firestore.collection("Users")
                .orderBy("age", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            oldestUsers.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userName = document.getString("userName");
                                Long age = document.getLong("age");
                                String userEmail = document.getString("userEmail");

                                if (userName != null && age != null) {
                                    oldestUsers.add(new UserData(userName, userEmail, age.intValue()));
                                }
                            }
                            oldestUsersAdapter.notifyDataSetChanged();
                            Log.d(LOG_TAG, "Legidősebb felhasználók betöltve: " + oldestUsers.size());
                        } else {
                            Log.e(LOG_TAG, "Hiba a legidősebb felhasználók lekérdezése során", task.getException());
                        }
                    }
                });
    }

    private void getNewestUsers() {
        firestore.collection("Users")
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            newestUsers.clear();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userName = document.getString("userName");
                                String userEmail = document.getString("userEmail");
                                Date registrationDate = document.getDate("registrationDate");

                                if (userName != null && registrationDate != null) {
                                    String formattedDate = dateFormat.format(registrationDate);
                                    newestUsers.add(new UserData(userName + " (" + formattedDate + ")", userEmail, 0));
                                }
                            }
                            newestUsersAdapter.notifyDataSetChanged();
                            Log.d(LOG_TAG, "Legfrissebb felhasználók betöltve: " + newestUsers.size());
                        } else {
                            Log.e(LOG_TAG, "Hiba a legfrissebb felhasználók lekérdezése során", task.getException());
                        }
                    }
                });
    }

    private void getAgeGroupStatistics() {
        getAgeGroupCount(18, 25, "18-25 év", new AgeGroupCallback() {
            @Override
            public void onResult(String ageGroup, int count) {
                getAgeGroupCount(26, 35, "26-35 év", new AgeGroupCallback() {
                    @Override
                    public void onResult(String ageGroup2, int count2) {
                        getAgeGroupCount(36, 50, "36-50 év", new AgeGroupCallback() {
                            @Override
                            public void onResult(String ageGroup3, int count3) {
                                getAgeGroupCount(51, 150, "50+ év", new AgeGroupCallback() {
                                    @Override
                                    public void onResult(String ageGroup4, int count4) {
                                        String statsText = "18-25 év: " + count + " fő\n" +
                                                "26-35 év: " + count2 + " fő\n" +
                                                "36-50 év: " + count3 + " fő\n" +
                                                "50+ év: " + count4 + " fő";
                                        ageGroupStatsText.setText(statsText);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void getAgeGroupCount(int minAge, int maxAge, String groupName, AgeGroupCallback callback) {
        firestore.collection("Users")
                .whereGreaterThanOrEqualTo("age", minAge)
                .whereLessThanOrEqualTo("age", maxAge)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            Log.d(LOG_TAG, groupName + ": " + count + " felhasználó");
                            callback.onResult(groupName, count);
                        } else {
                            Log.e(LOG_TAG, "Hiba a " + groupName + " csoport lekérdezése során", task.getException());
                            callback.onResult(groupName, 0);
                        }
                    }
                });
    }

    private interface AgeGroupCallback {
        void onResult(String ageGroup, int count);
    }

    public static class UserData {
        private String userName;
        private String userEmail;
        private int age;

        public UserData(String userName, String userEmail, int age) {
            this.userName = userName;
            this.userEmail = userEmail;
            this.age = age;
        }

        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public int getAge() { return age; }
    }
}