package com.yshare.file.share.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yshare.file.share.FileSelectActivity;
import com.yshare.file.share.R;
import com.yshare.file.share.ReceiveActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


public class Transfer extends Fragment implements View.OnClickListener {

    Button buttonSend;
    Button buttonReceive;
    InterstitialAd mInterstitialAd;

    public Transfer() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transfer, container, false);
        buttonSend = root.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        buttonReceive = root.findViewById(R.id.button_receive);
        buttonReceive.setOnClickListener(this);

        //TODO: Ads intialization
        mInterstitialAd = new InterstitialAd(getContext());
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
        return root;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_send:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent shareIntent = new Intent(getContext(), FileSelectActivity.class);
                    startActivity(shareIntent);
                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        Intent shareIntent = new Intent(getContext(), FileSelectActivity.class);
                        startActivity(shareIntent);
                    }
                });
                break;
            case R.id.button_receive:
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent intent = new Intent(getContext(), ReceiveActivity.class);
                    startActivity(intent);
                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        Intent intent = new Intent(getContext(), ReceiveActivity.class);
                        startActivity(intent);
                    }
                });
                break;
        }
    }

    //TODO: Requesting Ads method
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }
}
