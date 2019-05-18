package com.example.yshare.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yshare.FileSelectActivity;
import com.example.yshare.R;
import com.example.yshare.strucmodels.AppIication;
import com.example.yshare.strucmodels.FileToSendPath;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareApplication extends Fragment {

GridView appsGridView;
RelativeLayout anim;
ProgressBar progressBar;
AsyncTask asy;

    public ShareApplication() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_apps, container, false);
        appsGridView=rootView.findViewById(R.id.apps_gridview);
        anim=rootView.findViewById(R.id.loadingPanel);
        progressBar=rootView.findViewById(R.id.progressBar);
       // ArrayList<AppIication> appsList=getInstalledApps(false);


       // ArrayList<AppIication> appsList=getInstalledApps(false);
        // Inflate the layout for this fragment

       asy = new AsyncTaskRunner().execute();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
//        Constants.isBackPressed = true;
    }

    private class AppsAdapter extends BaseAdapter {
        /**
         * The context.
         */
        private Activity context;
        private  ArrayList<AppIication> appList;
        /**
         * Instantiates a new image adapter.
         *
         * @param localContext the local context
         */
        public AppsAdapter(Activity localContext,ArrayList<AppIication> applicationList) {
            context = localContext;
            appList=applicationList;
            //ArrayList<AppIication> appsList=getInstalledApps(false);
          //  images = getAllShownImagesPath(context);
        }
        public int getCount() {
            return appList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = inflater.inflate(R.layout.grid_apps_layout, null);
            ImageView appIcon=rootView.findViewById(R.id.imageview_app_icon);
            TextView appName =rootView.findViewById(R.id.textview_app_name);
            CheckBox appCheckBox =rootView.findViewById(R.id.apps_checkbox);
            appCheckBox.setChecked(false);
            appCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FileToSendPath path=new FileToSendPath();
                    path.setPath(appList.get(position).getPath());
                    path.setType("Application");
                    if(isChecked) {
                        FileSelectActivity.mPathsList.add(path);
                        buttonView.setChecked(true);
                    }
                    else{
                        FileSelectActivity.mPathsList.remove(FileSelectActivity.getRefference(path));
                        // SendActivity.UpdateView();
                    }
                    FileSelectActivity.UpdateView();
                }
            });

            if (convertView == null) {
                // picturesView = new ImageView(context);
                appIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                // picturesView
                //  .setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                //rootView = (View) convertView;
            }
            Glide.with(context).load(appList.get(position).getIcon())
                    .placeholder(R.drawable.ic_launcher_foreground).centerCrop()
                    .into(appIcon);
            appName.setText(appList.get(position).getAppname());

            return rootView;
        }
    }
    private ArrayList<AppIication> getInstalledApps(boolean getSysPackages) {
        ArrayList<AppIication> appsList = new ArrayList<AppIication>();
        List<ApplicationInfo> apps = getContext().getPackageManager().getInstalledApplications(0);
        List<PackageInfo> packs = getContext().getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {

            PackageInfo p = packs.get(i);

            ApplicationInfo a=p.applicationInfo;
            File apk = new File(a.publicSourceDir);
           // File apk = getApkFile(getContext(),p.packageName);
           // Uri uri=Uri.parse(apk.getAbsolutePath());
            String path=apk.getAbsolutePath();
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            AppIication newApp = new AppIication();
            newApp.setAppname(p.applicationInfo.loadLabel(getContext().getPackageManager()).toString());
            newApp.setPname(p.packageName);
            newApp.setPath(path);
            // newApp.setVersionName(p.versionName);
            // newApp.setVersionCode(p.versionCode);
            newApp.setIcon(p.applicationInfo.loadIcon(getContext().getPackageManager()));
            appsList.add(newApp);
        }
        return appsList;
    }
    private HashMap<String, String> getAllInstalledApkFiles(Context context) {
        HashMap<String, String> installedApkFilePaths = new HashMap<>();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.SIGNATURE_MATCH);

        if (isValid(packageInfoList)) {
            for (PackageInfo packageInfo : packageInfoList) {
                ApplicationInfo applicationInfo;
                applicationInfo = getApplicationInfoFrom(packageManager, packageInfo);
                String packageName = applicationInfo.packageName;
                String versionName = packageInfo.versionName;
                int versionCode = packageInfo.versionCode;
                File apkFile = new File(applicationInfo.publicSourceDir);
                if (apkFile.exists()) {
                    installedApkFilePaths.put(packageName, apkFile.getAbsolutePath());
                    Log.d("tag",packageName + " = " + apkFile.getName());
                }
            }
        }

        return installedApkFilePaths;
    }
    private ApplicationInfo getApplicationInfoFrom(PackageManager packageManager, PackageInfo packageInfo) {
        return packageInfo.applicationInfo;
    }
    private boolean isValid(List<PackageInfo> packageInfos) {
        return packageInfos != null && !packageInfos.isEmpty();
    }
    public File getApkFile(Context context, String packageName) {
        HashMap<String, String> installedApkFilePaths = getAllInstalledApkFiles(context);
        File apkFile = new File(installedApkFilePaths.get(packageName));
        if (apkFile.exists()) {
            return apkFile;
        }

        return null;
    }
    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        ArrayList<AppIication> appList;
      //  private String resp;
       // ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                appList=getInstalledApps(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            // execution of result of Long time consuming operation
           // progressDialog.dismiss();
            try {
                if(appList.size()> 0) {
                    appsGridView.setAdapter(new AppsAdapter(getActivity(), appList));
                }
                appsGridView.setVisibility(View.VISIBLE);
                anim.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onPreExecute() {
           // progressDialog = ProgressDialog.show(getContext(),
                   // "Please Wait",
                   // "Loading");
            try {
                appsGridView.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }



    }
}
