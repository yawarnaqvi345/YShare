package com.example.yshare.Fragments;


import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.List;

public class Application extends Fragment {

    RecyclerView mRecyclerView;
RelativeLayout anim;
ProgressBar progressBar;
AsyncTask asy;

    public Application() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_apps, container, false);
        mRecyclerView = rootView.findViewById(R.id.apps_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        anim=rootView.findViewById(R.id.loadingPanel);
        progressBar=rootView.findViewById(R.id.progressBar);
        asy = new AsyncTaskRunner().execute();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private ArrayList<AppIication> getInstalledApps(boolean getSysPackages) {
        ArrayList<AppIication> appsList = new ArrayList<AppIication>();
        //  List<ApplicationInfo> apps = getContext().getPackageManager().getInstalledApplications(0);
        List<PackageInfo> packs = getContext().getPackageManager().getInstalledPackages(0);
        for(int i = 0; i<packs.size(); i++) {

            PackageInfo p = packs.get(i);

            ApplicationInfo a=p.applicationInfo;
            File apk = new File(a.publicSourceDir);

            String path=apk.getAbsolutePath();
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            AppIication newApp = new AppIication();
            newApp.setAppname(p.applicationInfo.loadLabel(getContext().getPackageManager()).toString());
            newApp.setPname(p.packageName);
            newApp.setPath(path);

            newApp.setIcon(p.applicationInfo.loadIcon(getContext().getPackageManager()));
            appsList.add(newApp);
        }
        return appsList;
    }

    private class AppsAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private Activity context;
        private ArrayList<AppIication> appList;

        public AppsAdapter(Activity localContext, ArrayList<AppIication> applicationList) {
            context = localContext;
            appList = applicationList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View listItem = layoutInflater.inflate(R.layout.recycler_apps_layout, viewGroup, false);
            MyViewHolder viewHolder = new MyViewHolder(listItem);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
            myViewHolder.appCheckBox.setChecked(false);
            myViewHolder.appCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FileToSendPath path = new FileToSendPath();
                    path.setPath(appList.get(i).getPath());
                    path.setType("Application");
                    if (isChecked) {
                        FileSelectActivity.mPathsList.add(path);
                        buttonView.setChecked(true);
                    } else {
                        FileSelectActivity.mPathsList.remove(FileSelectActivity.getRefference(path));
                    }
                    FileSelectActivity.UpdateView();
                }
            });
            myViewHolder.appIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);


            Glide.with(context).load(appList.get(i).getIcon())
                    .placeholder(R.drawable.ic_launcher_foreground).centerCrop()
                    .into(myViewHolder.appIcon);
            myViewHolder.appName.setText(appList.get(i).getAppname());
        }

        @Override
        public int getItemCount() {
            return appList.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox appCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageview_app_icon);
            appName = itemView.findViewById(R.id.textview_app_name);
            appCheckBox = itemView.findViewById(R.id.apps_checkbox);

        }
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        ArrayList<AppIication> appList;

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
            try {
                if(appList.size()> 0) {
                    mRecyclerView.setAdapter(new AppsAdapter(getActivity(), appList));
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                anim.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                mRecyclerView.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }
}
