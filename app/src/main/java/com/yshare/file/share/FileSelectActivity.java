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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.yshare.file.share.Adapters.ViewPagerAdapter;
import com.yshare.file.share.Fragments.Transfer;
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
    ViewPagerAdapter adapter;
   ViewPager viewPager;

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
            selectedDisplayLayout.setVisibility(View.GONE);
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
        UpdateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mPathsList.clear();
            clearList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_file_select);
       themeColorHeader(R.color.colorPrimary);



        setTitle("Files");
        viewPager = findViewById(R.id.send_activity_view_pager);
       adapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout = findViewById(R.id.send_activity_tab_layout);

        tabLayout.setupWithViewPager(viewPager);
        for(int i=0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(15, 15, 15, 15);
            tab.requestLayout();
        }
        tabLayout.addOnTabSelectedListener (new TabLayout.OnTabSelectedListener() {
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
            if  (!frag.isAdded()) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(frag);
                ft.attach(frag);
                ft.commit();
            }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent shareIntent = new Intent(getApplicationContext(), FinalSendActivity.class);
                    startActivity(shareIntent);


            }
        });



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


  void clearList(){
      mPathsList.clear();
      UpdateView();
      Fragment frag = adapter.getItem(viewPager.getCurrentItem());
      final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.detach(frag);
      ft.attach(frag);
      ft.commit();
  }
}
