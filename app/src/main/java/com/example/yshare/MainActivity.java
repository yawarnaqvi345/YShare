package com.example.yshare;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.yshare.Fragments.Call;
import com.example.yshare.Fragments.Messaging;
import com.example.yshare.Fragments.Transfer;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    Permissions runtimePermissionHelper;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFrag=null;
            switch (item.getItemId()) {
                case R.id.navigation_transfer:
                    selectedFrag = new Transfer();
                    break;
                case R.id.navigation_messaging:
                    selectedFrag=new Messaging();
                case R.id.navigation_call:
                    selectedFrag = new Call();
            }
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                /*case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;*/
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_section, selectedFrag).commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            runtimePermissionHelper = Permissions.getInstance(this);
            if (runtimePermissionHelper.isAllPermissionAvailable()) {
            } else {
                runtimePermissionHelper.setActivity(this);
                runtimePermissionHelper.requestPermissionsIfDenied();
            }
        }

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_section, new Transfer()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            runtimePermissionHelper = Permissions.getInstance(this);
            if (runtimePermissionHelper.isAllPermissionAvailable()) {
            } else {
                runtimePermissionHelper.setActivity(this);
                runtimePermissionHelper.requestPermissionsIfDenied();
            }
        }
    }

}
