package com.yshare.file.share.Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yshare.file.share.FileSelectActivity;
import com.yshare.file.share.R;
import com.yshare.file.share.ReceiveActivity;
import com.yshare.file.share.strucmodels.FileToSendPath;
import com.yshare.file.share.strucmodels.Video;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 */
public class Videos extends Fragment {
    List<Video> mVideoList=new ArrayList<Video>();
    RecyclerView mRecyclerView;
    RelativeLayout progress;
    AsyncTaskRunner asyn;
    public Videos() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videos, container, false);
        progress=rootView.findViewById(R.id.video_loadingPanel);
        mRecyclerView = rootView.findViewById(R.id.videos_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        asyn = new AsyncTaskRunner();
        asyn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }
    private class VideoRecyclerAdapter extends  RecyclerView.Adapter<MyViewHolder>{
        private List<Video> videosList;
        private Activity context;
        public VideoRecyclerAdapter(Activity localContext, List<Video> vids) {
            context = localContext;
            videosList=vids;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View listItem= layoutInflater.inflate(R.layout.recycler_video_layout, viewGroup, false);
           MyViewHolder viewHolder=new MyViewHolder(listItem);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
            try {
                Glide.with(context).load(videosList.get(i).getPath())
                        .placeholder(R.drawable.ic_launcher_foreground).centerCrop()
                        .into(myViewHolder.vidThumb);
            } catch (Exception ignored) {

            }
            myViewHolder.vidCheckBox.setChecked(false);
            myViewHolder.vidInfo.setText(videosList.get(i).getTitle());
            myViewHolder.vidThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(Intent.ACTION_VIEW);
                    in.setDataAndType(Uri.parse(videosList.get(i).getPath()),"video/*");
                    try {
                        startActivity(in);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            myViewHolder.vidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FileToSendPath path=new FileToSendPath();
                    path.setPath(videosList.get(i).getPath());
                    path.setType("Video");
                    if(isChecked) {
                        FileSelectActivity.mPathsList.add(path);
                        buttonView.setChecked(true);
                    }
                    else{
                        FileSelectActivity.mPathsList.remove(FileSelectActivity.getRefference(path));
                    }
                    FileSelectActivity.UpdateView();
                }
            });
        }
        @Override
        public int getItemCount() {
            return videosList.size();
        }
    }
    class MyViewHolder extends  RecyclerView.ViewHolder{
        ImageView vidThumb;
        CheckBox vidCheckBox;
        TextView vidInfo;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            vidThumb=itemView.findViewById(R.id.video_thumb);
            vidCheckBox=itemView.findViewById(R.id.video_checkbox);
            vidInfo=itemView.findViewById(R.id.video_info);
        }
    }
    private List<Video> getAllShownVideoPath(Context activity) {
        Uri uri;
        Cursor cursor;
        List<Video> vidList=new ArrayList<>();
        int column_index_data;
        String absolutePathOfVideo = null;
       // Environment.getExternalStorageDirectory() + File.separator + "folder_name/";
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String sortOrder=MediaStore.MediaColumns.DATE_MODIFIED+" DESC";
        cursor = activity.getContentResolver().query(uri, null, null,
                null, sortOrder);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfVideo = cursor.getString(column_index_data);
            String title=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));
            Date date= new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))));
            Video vid=new Video();
            vid.setTitle(title);
            vid.setDateModified(date);
            vid.setPath(absolutePathOfVideo);
            vidList.add(vid);
        }
        return vidList;
    }
    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mVideoList = getAllShownVideoPath(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

            try {
                mRecyclerView.setAdapter(new VideoRecyclerAdapter(getActivity(),mVideoList));
                progress.setVisibility(View.GONE );
                mRecyclerView.setVisibility(View.VISIBLE);
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
