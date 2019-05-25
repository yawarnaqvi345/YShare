package com.yshare.file.share;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.yshare.file.share.strucmodels.FileToSendPath;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReceiveActivity extends BaseActivity {
    WifiP2pManager manager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver receiver;
    LottieAnimationView animationView;
    ListView receiveListView;
    Collection<WifiP2pDevice> list;
    TextView textView;
    String connectedDeviceId;
    IntentFilter intentFilter;
    List<FileToSendPath> mPathsList;
    RecyclerView myRecyclerView;
    MyAdapter myAdapter;
    LinearLayout elseLinear;
    boolean requestFound=false;
    private SimpleArrayMap<String, String> deviceName = new SimpleArrayMap<>();
    AsyncTaskRunner asyn;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Nearby.getConnectionsClient(ReceiveActivity.this).stopAdvertising();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        themeColorHeader(R.color.colorPrimary);
        setTitle("Receive");
        mPathsList = new ArrayList<>();
        //  FileToSendPath d=new FileToSendPath();
        // d.setName(" ");
        // mPathsList.add(d);
        asyn = new AsyncTaskRunner();
        asyn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
       // asyn.execute();



        textView=findViewById(R.id.receive_activity_text);
        animationView=findViewById(R.id.receive_animation_view);
        elseLinear=findViewById(R.id.receive_linear_else);
        myRecyclerView=findViewById(R.id.receive_recycler);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

//        myAdapter=new MyAdapter(mPathsList);
//        myRecyclerView.setAdapter(myAdapter);


        Nearby.getConnectionsClient(ReceiveActivity.this)
                .startAdvertising(
                        /* endpointName= */ android.os.Build.MODEL,
                        /* serviceId= */ getPackageName(),
                        mConnectionLifecycleCallback, new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build());
    }

    @Override
    protected void onDestroy() {
        Nearby.getConnectionsClient(ReceiveActivity.this).stopAdvertising();
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
//        if(connectedDeviceId!=null)
//        Nearby.getConnectionsClient(ReceiveActivity.this).disconnectFromEndpoint(connectedDeviceId);
//        Nearby.getConnectionsClient(ReceiveActivity.this).stopAdvertising();
    }
    ConnectionLifecycleCallback mConnectionLifecycleCallback=new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(final String s, ConnectionInfo connectionInfo) {
            requestFound=true;
            deviceName.put(s,connectionInfo.getEndpointName());
            Toast.makeText(getApplicationContext(),"onConnectionInitiated",Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReceiveActivity.this);
            alertDialogBuilder.setMessage(s+" wants to connect with this device");
            alertDialogBuilder.setPositiveButton("Allow",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Nearby.getConnectionsClient(ReceiveActivity.this).acceptConnection(s, mPayLoadCallback);
                            //Toast.makeText(ReceiveActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        }
                    });

            alertDialogBuilder.setNegativeButton("Decline",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Nearby.getConnectionsClient(ReceiveActivity.this).rejectConnection(s);
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
//            Nearby.getConnectionsClient(ReceiveActivity.this)
//                    .stopAdvertising();
            //  Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(s, mPayLoadCallback);
        }

        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            Toast.makeText(getApplicationContext(),"onConnectionResult",Toast.LENGTH_SHORT).show();
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Toast.makeText(getApplicationContext(), "Connection Accepted", Toast.LENGTH_SHORT).show();
                    textView.setText("Connected to  " +  deviceName.get(s));
                    textView.setVisibility(View.VISIBLE);
                    animationView.setVisibility(View.GONE);
                    myRecyclerView.setVisibility(View.VISIBLE);
                    elseLinear.setVisibility(View.GONE);
                    connectedDeviceId=s;
                    Nearby.getConnectionsClient(ReceiveActivity.this).stopAdvertising();
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    textView.setText("Could not connect to  "+s);
                    textView.setVisibility(View.VISIBLE);
                    animationView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Rejected Connection", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    // The connection broke before it was able to be accepted.
                    textView.setText("there was an error in connection to   "+s);
                    textView.setVisibility(View.VISIBLE);
                    animationView.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Rejected Connection", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
                // Unknown status code
            }
        }

        @Override
        public void onDisconnected(String s) {
            textView.setText("Disconnected");
            Nearby.getConnectionsClient(ReceiveActivity.this)
                    .startAdvertising(
                            /* endpointName= */ android.os.Build.MODEL,
                            /* serviceId= */ getPackageName(),
                            mConnectionLifecycleCallback, new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build());
            // new AdvertisingOptions(Strategy.P2P_POINT_TO_POINT));
        }
    };
    PayloadCallback mPayLoadCallback= new ReceiveFilePayloadCallback();

    public class ReceiveFilePayloadCallback extends PayloadCallback {
        private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, Payload> completedFilePayloads = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, Integer> fileIdPosition = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, Integer> fileIdType = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, String> recfileIdType = new SimpleArrayMap<>();
        int index=0;


        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            //  incomingFilePayloads.put(payload.getId(), payload);
            if (payload.getType() == Payload.Type.BYTES) {
                String payloadFilenameMessage = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    payloadFilenameMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
                }
                long payloadId = addPayloadFilename(payloadFilenameMessage);
                processFilePayload(payloadId);
            } else if (payload.getType() == Payload.Type.FILE) {
                // Add this to our tracking map, so that we can retrieve the payload later.
                incomingFilePayloads.put(payload.getId(), payload);
                FileToSendPath file=new FileToSendPath();
                file.setName(filePayloadFilenames.get(payload.getId()));
                mPathsList.add(file);
                fileIdPosition.put(payload.getId(),index);
                if(index==0){
                    myAdapter=new MyAdapter(mPathsList);
                    myRecyclerView.setAdapter(myAdapter);
                }
                index++;
                myAdapter.notifyDataSetChanged();
            }
            fileIdType.put(payload.getId(),payload.getType());

        }

        private long addPayloadFilename(String payloadFilenameMessage) {
            String[] parts = payloadFilenameMessage.split(":");
            long payloadId = Long.parseLong(parts[0]);

            String filename = parts[1];
            String type=parts[2];
            recfileIdType.put(payloadId,type);
            filePayloadFilenames.put(payloadId, filename);
            recfileIdType.put(payloadId,type);
            return payloadId;
        }
        private void processFilePayload(long payloadId) {
            Payload filePayload = completedFilePayloads.get(payloadId);
            String filename = filePayloadFilenames.get(payloadId);
            if (filePayload != null && filename != null) {
                completedFilePayloads.remove(payloadId);
                filePayloadFilenames.remove(payloadId);
                // Get the received file (which will be in the Downloads folder)
                File payloadFile = filePayload.asFile().asJavaFile();
                File root = Environment.getExternalStorageDirectory();
                File f = new File(root.getAbsolutePath(), "YShare");
                if(!f.isDirectory())
                    f.mkdir();
                // Rename the file.
                //payloadFile.renameTo(new File(payloadFile.getParentFile(), filename));
                payloadFile.renameTo(new File(f, filename));
                // payloadFile.getAbsolutePath();
                mPathsList.get( fileIdPosition.get(payloadId)).setPath((new File(f, filename).getPath()));
                mPathsList.get(fileIdPosition.get(payloadId)).setType(recfileIdType.get(payloadId));
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                long payloadId = update.getPayloadId();
                Payload payload= incomingFilePayloads.remove(payloadId);
                completedFilePayloads.put(payloadId, payload);
//                if (payload.getType() == Payload.Type.FILE) {
                processFilePayload(payloadId);
                //              }
            }
            else if ((update.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS) &&  (fileIdType.get(update.getPayloadId())==Payload.Type.FILE) ) {
                float btrans=update.getBytesTransferred();
                float btotal=update.getTotalBytes();
                float factor=btrans/btotal;
                float progpercentage=factor*100;
                mPathsList.get( fileIdPosition.get(update.getPayloadId())).setProgress((int)progpercentage);
                myAdapter.notifyItemChanged(fileIdPosition.get(update.getPayloadId()));
            }
        }
    }








    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        //  List<FileToSendPath> mPathsList;
        MediaPlayer mediaPlayer = new MediaPlayer();
        private final Handler handler = new Handler();
        SeekBar seekBarProgress;
        private int mediaFileLengthInMilliseconds;
        public MyAdapter(List<FileToSendPath> mPathsListR) {
            //  mPathsList=mPathsListR;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View recyclerFile = layoutInflater.inflate(R.layout.receive_recycler_layout, viewGroup, false);
            MyViewHolder viewHolder = new MyViewHolder(recyclerFile);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
            myViewHolder.fileName.setText(mPathsList.get(i).getName());
            myViewHolder.progressBar.setProgress(mPathsList.get(i).getProgress());
            if (String.valueOf(mPathsList.get(i).getProgress()).equalsIgnoreCase("100")){
                myViewHolder.percentageText.setText("Completed!");
                myViewHolder.openButton.setVisibility(View.VISIBLE);
                myViewHolder.openButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = mPathsList.get(i).getPath();

                        if(mPathsList.get(i).getType().equalsIgnoreCase("Photo")){
                            Intent in = new Intent(Intent.ACTION_VIEW);
                            in.setDataAndType(Uri.parse(path),"image/*");
                            try {
                                startActivity(in);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(ReceiveActivity.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                            }
                        }
                        else if(mPathsList.get(i).getType().equalsIgnoreCase("Video")){
                            Intent in = new Intent(Intent.ACTION_VIEW);
                            in.setDataAndType(Uri.parse(path),"video/*");
                            try {
                                startActivity(in);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(ReceiveActivity.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                            }
                        }
                        else if(mPathsList.get(i).getType().equalsIgnoreCase("File")){
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri.parse(path); //  directory path
                            File f=new File(path);
                            String pa=f.getParent();
                            intent.setDataAndType(Uri.parse(pa), "*/*");
                            startActivity(Intent.createChooser(intent, "Open folder"));
                        }
                        else if(mPathsList.get(i).getType().equalsIgnoreCase("Music")){
                            mediaPlayer.stop();
                            mediaPlayer=new MediaPlayer();
                            final String pathy=path;
                            String title= Uri.parse(pathy).getLastPathSegment();
                            final Dialog dialog = new Dialog(ReceiveActivity.this);
                            dialog.setContentView(R.layout.dialogue_layout);
                            dialog.setTitle(title);
                            final ImageView buttonPlayPause = dialog.findViewById(R.id.play_button);
                            buttonPlayPause.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                            seekBarProgress = dialog.findViewById(R.id.seek_bar);
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
                                            mediaPlayer.setDataSource(pathy);
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
                                        buttonPlayPause.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                    }
                                }
                            });
                            dialog.show();
                        }
                        else if(mPathsList.get(i).getType().equalsIgnoreCase("Application")){
                            try {
                                File file= new File(path);
                                File toInstall = new File(file.getAbsolutePath());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Uri apkUri = FileProvider.getUriForFile(ReceiveActivity.this, "com.fast.file.share.and.data.transfer.free.apps.fileprovider", toInstall);
                                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                    intent.setData(apkUri);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivity(intent);
                                } else {
                                    Uri apkUri = Uri.fromFile(toInstall);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            } catch (Exception e) {
                                Toast.makeText(ReceiveActivity.this,"Couldn't find activity install this application",Toast.LENGTH_LONG);
                                e.printStackTrace();
                            }

                        }
                    }
                });

            }else{
                myViewHolder.percentageText.setText(String.valueOf(mPathsList.get(i).getProgress())+" %");
            }
        }
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
            return mPathsList.size();
        }
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;
        ProgressBar progressBar;
        TextView percentageText;
        Button openButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.rec_file_name);
            progressBar=itemView.findViewById(R.id.rec_recycler_progressbar);
            percentageText=itemView.findViewById(R.id.rec_percentage_text);
            openButton=itemView.findViewById(R.id.rec_open_button);
            openButton.setVisibility(View.GONE);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
        }
    }
    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if(!requestFound){
                Toast.makeText(ReceiveActivity.this, "Timeout", Toast.LENGTH_SHORT).show();
                finish();}
        }
        @Override
        protected void onPreExecute() {

        }



    }

}
