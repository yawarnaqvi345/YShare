package com.yshare.file.share;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {


    public void themeColorHeader(int pdfColor) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(pdfColor)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(pdfColor, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(pdfColor));
        }
    }

}
