package com.yshare.file.share.Adapters;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yshare.file.share.Fragments.Application;
import com.yshare.file.share.Fragments.Files;
import com.yshare.file.share.Fragments.Music;
import com.yshare.file.share.Fragments.Photos;
import com.yshare.file.share.Fragments.Videos;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    Fragment apps, files, photos, videos, music;
    private Context mContext;

    TabLayout.Tab tabLayout;
    public ViewPagerAdapter(Context cntxt, FragmentManager fm) {
        super(fm);
        mContext = cntxt;
        apps = new Application();
        files = new Files();
        photos = new Photos();
        videos = new Videos();
        music = new Music();

    }


    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return photos;
            case 1:
                return videos;
            case 2:
                return music;
            case 3:
                return files;
            case 4:
                return apps;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position

       /* switch (position) {
            case 0:
                return mContext.getString(R.string.String_photos);
            case 1:
                return mContext.getString(R.string.String_videos);
            case 2:
                return mContext.getString(R.string.String_music);
            case 3:
                return mContext.getString(R.string.String_Files);
            case 4:
                return mContext.getString(R.string.String_Apps);
            default:
                return null;
        }*/
        return null;
    }
}
