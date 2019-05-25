package com.yshare.file.share;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yshare.file.share.Adapters.ViewPagerAdapter;
import com.yshare.file.share.strucmodels.FileToSendPath;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.WHITE;

public class FileSelectActivity extends BaseActivity {
    final String TAG = "FileSelectActivity";
    public static List<FileToSendPath> mPathsList = new ArrayList<>();
    public static LinearLayout selectedDisplayLayout;
    public static TextView numOfFilesSelected;
    public static ImageView crossButton;
    public static ImageView sendButton;
    TabLayout tabLayout;
    // AdView adView;
    InterstitialAd mInterstitialAd;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.photos);
        tabLayout.getTabAt(1).setIcon(R.drawable.vidos);
        tabLayout.getTabAt(2).setIcon(R.drawable.music);
        tabLayout.getTabAt(3).setIcon(R.drawable.files);
        tabLayout.getTabAt(4).setIcon(R.drawable.apps);
    }

    public static void UpdateView() {
        numOfFilesSelected.setText(String.valueOf(mPathsList.size()));
        if (mPathsList.size() < 1) {
            selectedDisplayLayout.setVisibility(View.INVISIBLE);
        } else {
            selectedDisplayLayout.setVisibility(View.VISIBLE);
        }
    }

    public static FileToSendPath getRefference(FileToSendPath mPath) {
        for (FileToSendPath path : FileSelectActivity.mPathsList) {
            if (path.getPath().equals(mPath.getPath()))
                return path;
        }
        return null;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mPathsList.clear();
        UpdateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPathsList.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_file_select);
        themeColorHeader(R.color.colorPrimary);

        //TODO: Ads intialization
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstial));
        try {
            if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
                AdRequest adRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(adRequest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        requestNewInterstitial();

        setTitle("Files");
        final ViewPager viewPager = findViewById(R.id.send_activity_view_pager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout = findViewById(R.id.send_activity_tab_layout);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Drawable ic = tab.getIcon();
                ic.setTint(WHITE);
            }

            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Drawable ic = tab.getIcon();
                ic.setTint(R.color.colorGrey);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setupTabIcons();
        selectedDisplayLayout = findViewById(R.id.selected_display_layout);
        numOfFilesSelected = findViewById(R.id.num_of_files_selected);
        crossButton = findViewById(R.id.cross_button);
        sendButton = findViewById(R.id.send_button);
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPathsList.clear();
                UpdateView();
                Fragment frag = adapter.getItem(viewPager.getCurrentItem());
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(frag);
                ft.attach(frag);
                ft.commit();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent shareIntent = new Intent(getApplicationContext(), FinalSendActivity.class);
                    startActivity(shareIntent);
                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        Intent shareIntent = new Intent(getApplicationContext(), FinalSendActivity.class);
                        startActivity(shareIntent);
                    }
                });
            }
        });

//        //TODO: Ads intialization
//        // adView();
//        mInterstitialAd = new InterstitialAd(getApplicationContext());
//        mInterstitialAd.setAdUnitId(getString(R.string.interstial));
//        try {
//            if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
//                AdRequest adRequest = new AdRequest.Builder().build();
//                mInterstitialAd.loadAd(adRequest);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        requestNewInterstitial();
        //setupTabIcons();
    }

    //TODO: Requesting Ads method
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

  /*  private void adView() {
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
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
    }*/
}
