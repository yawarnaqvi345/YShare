package com.yshare.file.share.Fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yshare.file.share.FileSelectActivity;
import com.yshare.file.share.R;
import com.yshare.file.share.strucmodels.FileToSendPath;
import com.yshare.file.share.strucmodels.Track;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Music extends Fragment {
    List<Track> tracks;
    RecyclerView recyclerView;
    RelativeLayout progress;
    MediaPlayer mediaPlayer = new MediaPlayer();

    public Music() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music, container, false);
        progress=rootView.findViewById(R.id.music_loadingPanel);
        recyclerView =  rootView.findViewById(R.id.music_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



       new AsyncTaskRunner().execute();
        return rootView;
    }



    List<Track> getAllMusicTracks(){
       // List<Track> mTracks= ArrayList<Track>();
        ArrayList<Track> mTracks = new ArrayList<Track>();
       Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
       //Uri uri = Uri.fromFile(Environment.getExternalStoragePublicDirectory("DIRECTORY_MUSIC"));
        String sortOrder=MediaStore.MediaColumns.DATE_MODIFIED+" DESC";
       Cursor cursor = getContext().getContentResolver().query(
               uri, // Uri
               null,
               null,
               null,
               sortOrder);
       if (cursor == null) {

           Toast.makeText(getContext(),"Something Went Wrong.", Toast.LENGTH_LONG);

       } else if (!cursor.moveToFirst()) {

           Toast.makeText(getContext(),"No Music Found on SD Card.", Toast.LENGTH_LONG);

       }
       else {

           int Title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
           int size = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
           int length = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
           int path = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
           int dateint=cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);


           //Getting Song ID From Cursor.
           //int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

           do {

               // You can also get the Song ID using cursor.getLong(id).
               //long SongID = cursor.getLong(id);

               String SongTitle = cursor.getString(Title);
              // String types=cursor.getString(type);
              // String songPath=uri+"/"+SongTitle+"."+types;
               String songPath=cursor.getString(path);
               String songSize=cursor.getString(size);
               String songLength=cursor.getString(length);
               Date date = new Date(Long.parseLong(cursor.getString(dateint)));
               songLength=setCorrectDuration(songLength);
               float sz=cursor.getInt(length);
               Track track=new Track();
               track.setTitle(SongTitle);
               track.setSize(getFileSize(songSize));
               track.setLength(songLength);
               track.setPath(songPath);
               track.setDate(date);
               mTracks.add(track);
           } while (cursor.moveToNext());
       }
       // Collections.sort(mTracks);
        return mTracks;
   }

   public class RecyclerViewAdapter extends  RecyclerView.Adapter<MyViewHolder>{
        List<Track> mTracks;
      public RecyclerViewAdapter(List<Track> trks){
          mTracks=trks;
      }

       @NonNull
       @Override
       public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
           LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
           View listItem= layoutInflater.inflate(R.layout.recycler_music_item, viewGroup, false);
          // RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(listItem);
          MyViewHolder viewHolder=new MyViewHolder(listItem);
          viewHolder.setIsRecyclable(false);
           return viewHolder;
       }

       @Override
       public void onBindViewHolder(@NonNull MyViewHolder myViewHolder,final int i) {
          final int pos=i;
           final String text = mTracks.get(i).getTitle();
           final String size = mTracks.get(i).getSize();
           final String length = mTracks.get(i).getLength();
           myViewHolder.title.setText(text);
           myViewHolder.size.setText(size);
           myViewHolder.length.setText(length);
           myViewHolder.musicCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   FileToSendPath path=new FileToSendPath();
                   path.setPath(mTracks.get(i).getPath());
                   path.setType("Music");
                   path.setName(text);
                   if(isChecked) {
                       FileSelectActivity.mPathsList.add(path);
                   }
                   else{
                       FileSelectActivity.mPathsList.remove(FileSelectActivity.getRefference(path));
                   }
                   FileSelectActivity.UpdateView();
               }
           });
           myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   mediaPlayer.stop();
                   mediaPlayer=new MediaPlayer();
                   final String path=mTracks.get(i).getPath();
                  String title= Uri.parse(path).getLastPathSegment();
                   final Dialog dialog = new Dialog(getContext());
                   dialog.setContentView(R.layout.dialogue_layout);
                   dialog.setTitle(title);
                  final ImageView buttonPlayPause = (ImageView) dialog.findViewById(R.id.play_button);
                   buttonPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                   seekBarProgress = (SeekBar) dialog.findViewById(R.id.seek_bar);
                   seekBarProgress.setProgress(0);
                   seekBarProgress.setMax(100);
                   seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                       @Override
                       public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                           if(fromUser){
                                float factor =(float) progress/100;
                                float mills=factor*mediaFileLengthInMilliseconds;
                                mediaPlayer.seekTo((int) mills);}
                       }

                       @Override
                       public void onStartTrackingTouch(SeekBar seekBar) {

                       }

                       @Override
                       public void onStopTrackingTouch(SeekBar seekBar) {

                       }
                   });
                   TextView titleText=dialog.findViewById(R.id.track_name);
                   titleText.setText(title);
                   dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                       @Override
                       public void onDismiss(DialogInterface dialog) {
                           mediaPlayer.stop();
                       }
                   });
                   buttonPlayPause.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           try {
                               if (mediaPlayer.isPlaying()) {
                                   mediaPlayer.pause();
                               }else{
                                   mediaPlayer.start();
                               }
                               try {
                                   mediaPlayer.setDataSource(path);
                                   mediaPlayer.prepare();
                                   mediaPlayer.start();
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                               primarySeekBarProgressUpdater();

                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                           mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
                           if(mediaPlayer.isPlaying()){
                               buttonPlayPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                           }else{
                               buttonPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);

                           }
                       }
                   });
dialog.show();

               }
           });
       }
       private final Handler handler = new Handler();
       SeekBar seekBarProgress;
       private int mediaFileLengthInMilliseconds;
       private void primarySeekBarProgressUpdater() {
           seekBarProgress.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
           if (mediaPlayer.isPlaying()) {
               Runnable notification = new Runnable() {
                   public void run() {
                       primarySeekBarProgressUpdater();
                   }
               };
               handler.postDelayed(notification,1000);
           }
       }

       @Override
       public int getItemCount() {
           return mTracks.size();
       }
   }

   public class MyViewHolder extends  RecyclerView.ViewHolder{
      public TextView title;
      public  TextView size;
      public TextView length;
      public CheckBox musicCheckbox;
      public ImageView musicIcon;
       public MyViewHolder(@NonNull View itemView) {
           super(itemView);
           title=itemView.findViewById(R.id.track_title);
           size=itemView.findViewById(R.id.track_size);
           length=itemView.findViewById(R.id.track_length);
           musicCheckbox=itemView.findViewById(R.id.music_checkbox);
           musicIcon=itemView.findViewById(R.id.music_icon);
       }
   }
    private String setCorrectDuration(String songs_duration) {
        // TODO Auto-generated method stub

        if(Integer.valueOf(songs_duration) != null) {
            int time = Integer.valueOf(songs_duration);

            int seconds = time/1000;
            int minutes = seconds/60;
            seconds = seconds % 60;

            if(seconds<10) {
                songs_duration = String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {
                songs_duration = String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
            return songs_duration;
        }
        return null;
    }

    public String getFileSize(String size) {
        String modifiedFileSize = null;
        double fileSize = 0.0;
        if (size!=null) {
            //fileSize = Double.longBitsToDouble();//in Bytes
           fileSize= Double.parseDouble(size);

            if (fileSize < 1024) {
                modifiedFileSize = String.valueOf(fileSize).concat("B");
            } else if (fileSize > 1024 && fileSize < (1024 * 1024)) {
                modifiedFileSize = String.valueOf(Math.round((fileSize / 1024 * 100.0)) / 100.0).concat("KB");
            } else {
                modifiedFileSize = String.valueOf(Math.round((fileSize / (1024 * 1204) * 100.0)) / 100.0).concat("MB");
            }
        } else {
            modifiedFileSize = "Unknown";
        }

        return modifiedFileSize;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tracks = getAllMusicTracks();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            try {
                recyclerView.setAdapter(new RecyclerViewAdapter(tracks));
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {
            recyclerView.setVisibility(View.GONE);
        }



    }

}
