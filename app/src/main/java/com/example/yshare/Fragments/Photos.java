package com.example.yshare.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yshare.FileSelectActivity;
import com.example.yshare.R;
import com.example.yshare.strucmodels.AppIication;
import com.example.yshare.strucmodels.FileToSendPath;
import com.example.yshare.strucmodels.Image;

import java.util.ArrayList;
import java.util.List;
public class Photos extends Fragment {
    RecyclerView mRecyclerView;
   LinearLayout photoLinearLayout;
   RelativeLayout progress;
    private SimpleArrayMap<Integer, Boolean> ifSelected = new SimpleArrayMap<>();
    private List<Image> imagesList=new ArrayList<>();
    public Photos() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photos, container, false);
        mRecyclerView = rootView.findViewById(R.id.photos_recycler);
        photoLinearLayout = rootView.findViewById(R.id.photo_linear_layout);
        progress = rootView.findViewById(R.id.photo_loadingPanel);
        new AsyncTaskRunner().execute();
        return rootView;
    }
    private List<Image> getAllShownImagesPath(Context activity) {
        Uri uri;
        Cursor cursor;
       List<Image> mList=new ArrayList<Image>();
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA/*,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME*/, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.TITLE};
        String sortOrder=MediaStore.MediaColumns.DATE_MODIFIED+" DESC";
        cursor = getContext().getContentResolver().query(
                uri, // Uri
                null,
                null,
                null,
                sortOrder);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            int title = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
            //  int dt=cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
            //absolutePathOfImage = cursor.getString(column_index_data);
            Image img=new Image();
            img.setPath(cursor.getString(column_index_data));
            img.setTitle(cursor.getString(title));
            //   img.setLastModified( new Date(Long.parseLong(cursor.getString(dt))));
            mList.add(img);
            //listOfAllImages.add(absolutePathOfImage);
        }
        return mList;
    }
    public class ImageAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private Activity context;
        public ImageAdapter(Activity localContext,List<Image> imags) {
            context = localContext;
            imagesList = imags;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View listItem= layoutInflater.inflate(R.layout.recycler_photo_layout, viewGroup, false);
            MyViewHolder viewHolder=new MyViewHolder(listItem);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
            myViewHolder.checkBox.setChecked(false);
            myViewHolder.info.setText(imagesList.get(i).getTitle());
            Glide.with(context).load(imagesList.get(i).getPath())
                    .placeholder(R.drawable.ic_launcher_foreground).centerCrop()
                    .into(myViewHolder.imgThumb);
            myViewHolder.imgThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(Intent.ACTION_VIEW);
                    in.setDataAndType(Uri.parse(imagesList.get(i).getPath()),"image/*");
                    startActivity(in);
                }
            });
            myViewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FileToSendPath path=new FileToSendPath();
                    path.setPath(imagesList.get(i).getPath());
                    path.setName(imagesList.get(i).getTitle());
                    path.setType("Photo");
                    if(isChecked) {
                        FileSelectActivity.mPathsList.add(path);
                        buttonView.setChecked(true);
                    }
                    else{
                        FileSelectActivity.mPathsList.remove(getRefference(path));
                    }
                    FileSelectActivity.UpdateView();
                }
            });
        }
        @Override
        public int getItemCount() {
            return imagesList.size();
        }
    }
    class MyViewHolder extends  RecyclerView.ViewHolder{
        ImageView imgThumb;
        CheckBox checkBox;
        TextView info;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb=itemView.findViewById(R.id.photo_thumb);
            checkBox=itemView.findViewById(R.id.photo_checkbox);
            info=itemView.findViewById(R.id.photo_info);
        }
    }
    private FileToSendPath getRefference(FileToSendPath mPath){
        for(FileToSendPath path:FileSelectActivity.mPathsList){
            if(path.getPath().equals(mPath.getPath()))
                return path;
        }
        return null;
   }
    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {
        ArrayList<AppIication> appList;
        @Override
        protected Void doInBackground(Void... params) {
            try {
                imagesList = getAllShownImagesPath(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            try {
                mRecyclerView.setAdapter(new ImageAdapter(getActivity(),imagesList));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE );
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
