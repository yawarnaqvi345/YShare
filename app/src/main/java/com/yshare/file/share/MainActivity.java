package com.yshare.file.share;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorSpace;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.InterstitialAd;
import com.yshare.file.share.Fragments.Call;
import com.yshare.file.share.Fragments.Messaging;
import com.yshare.file.share.Fragments.Transfer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends BaseActivity {

    private TextView mTextMessage;
    Permissions runtimePermissionHelper;
    AdView adView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFrag = null;
            switch (item.getItemId()) {

                case R.id.navigation_transfer:
                    selectedFrag = new Transfer();
                    item.getIcon().setTint(Color.rgb(150, 15, 20));
                    break;
                case R.id.navigation_messaging:
                    selectedFrag = new Messaging();
                   /* item.getIcon().setColorFilter(new
                            PorterDuffColorFilter(0xfff000, PorterDuff.Mode.MULTIPLY));*/
                    break;
                case R.id.navigation_call:
                    selectedFrag = new Call();
                    item.getIcon().setTint(Color.rgb(150, 15, 20));
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_section, selectedFrag).commit();
            return true;
        }
    };
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        themeColorHeader(R.color.colorPrimary);



        if (Build.VERSION.SDK_INT >= 23) {
            runtimePermissionHelper = Permissions.getInstance(this);
            if (runtimePermissionHelper.isAllPermissionAvailable()) {
            } else {
                runtimePermissionHelper.setActivity(this);
                runtimePermissionHelper.requestPermissionsIfDenied();
            }
        }
        adView();
        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_section, new Transfer()).commit();
    }

    //TODO: Requesting Ads method

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

    private void adView() {
        adView = findViewById(R.id.banner);
        final AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
