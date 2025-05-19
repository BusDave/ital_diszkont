package com.example.teszt;

import static com.example.teszt.R.id.*;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.Manifest;

import java.util.ArrayList;

public class Shop extends AppCompatActivity {

    private static final String LOG_TAG = Shop.class.getName();
    private FirebaseUser user;

    private FirebaseAuth auth;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;

    private int gridNumber = 1;

    private boolean viewRow = true;
    private FrameLayout redCircle;
    private TextView contentTextView;
    private int cartItems = 0;

    private FirebaseFirestore mFirestore;

    private int queryLimit = 10;

    private CollectionReference mItems;

    private JobScheduler mJobScheduler;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(recyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(LOG_TAG, "Authenticated user");
        }else{
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();

        }

        mRecyclerView = findViewById(recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Italok");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotificationHandler = new NotificationHandler(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        mAlarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();
        mJobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        setJobScheduler();



    }
    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action==null){
                return;
            }
            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            queryData();
        }
    };
    private void queryData(){
      mItemList.clear();

      //mItems.whereEqualTo()..
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }

            if(mItemList.isEmpty()) {
                initializeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();

        });

    };

    public void deleteItem(ShoppingItem item){
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(succes ->{
            Log.d(LOG_TAG, "Item is succesfully deleted!" + item._getId());
        })
                .addOnFailureListener(failure ->{
                    Toast.makeText(this,"Item " + item._getId() + "Can not be deleted.",Toast.LENGTH_LONG).show();
                });
        queryData();
        mNotificationHandler.cancel();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_button:
                startProfileActivity();
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                // Visszatérés a main activity-re
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            case R.id.view_selector:
                if (viewRow) {
                    changeSpanCount(item, R.drawable.baseline_calendar_view_month_24, 1);
                } else {
                    changeSpanCount(item, R.drawable.baseline_calendar_view_day_24, 2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        final MenuItem alertMenuItem = menu.findItem(cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle= (FrameLayout) rootView.findViewById(view_alert_red_circle);
        contentTextView= (TextView) rootView.findViewById(view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount){
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);

    }

    private void initializeData(){
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price) ;

        TypedArray itemsImageResource= getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate= getResources().obtainTypedArray(R.array.shopping_item_rates);

        //mItemList.clear();

        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShoppingItem(itemsImageResource.getResourceId(i,0),itemsList[i],itemsInfo[i],itemsPrice[i],itemsRate.getFloat(i,0),0));
        }

        itemsImageResource.recycle();

        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuitem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuitem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return false;}

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG,newText);
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;

    }

    public void signout(View view) {
        auth.signOut();
        finish();

    }

    public void updateAlertIcon(ShoppingItem item){
        cartItems = (cartItems + 1);
        if (cartItems > 0) {
            contentTextView.setText(String.valueOf(cartItems));
            redCircle.setVisibility(View.VISIBLE);
        } else {
            contentTextView.setText("");
            redCircle.setVisibility(View.GONE);
        }

        mItems.document(item._getId()).update("cartedCount",item.getCartedCount() + 1)
                .addOnFailureListener(failure ->{
                    Toast.makeText(this,"Item " + item._getId() + " cannot be changed.",Toast.LENGTH_LONG).show();
                });
        mNotificationHandler.send(item.getName());
        queryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }
    private void setAlarmManager(){
        long repeatInterval = 2 * 1000;
        long triggerTime  = SystemClock.elapsedRealtime() + repeatInterval;
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent
        );
        //mAlarmManager.cancel(pendingIntent);

    }
    private void setJobScheduler(){

        int networkType = JobInfo.NETWORK_TYPE_UNMETERED;
        int hardDeadLine = 5000;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0,name)
                .setRequiredNetworkType(networkType)
                .setRequiresCharging(true)
                .setOverrideDeadline(hardDeadLine)
                ;

        mJobScheduler.schedule(builder.build());
        //mJobScheduler.cancel(0);
    }
}