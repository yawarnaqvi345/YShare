package com.example.yshare;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yshare.strucmodels.FileToSendPath;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FinalSendActivity extends AppCompatActivity {
    ConstraintLayout view;
    Activity mActivity=this;
    TextView finalshareTextView;
    File fileToSend;
    List<FileToSendPath> mPathsList;
    Payload filePayload;
    String connectedDeviceId;
    Uri uri;
    RecyclerView recyclerView;
    private SimpleArrayMap<Long, Integer> recyclerIdPosition = new SimpleArrayMap<>();
    private SimpleArrayMap<String, String> deviceName = new SimpleArrayMap<>();
    MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_send);
        setTitle("Send");
        mPathsList=FileSelectActivity.mPathsList;
        recyclerView=findViewById(R.id.finalshare_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        myAdapter=new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        finalshareTextView=findViewById(R.id.finalshare_activity_text);

        EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(final String s, final DiscoveredEndpointInfo discoveredEndpointInfo) {
                deviceName.put(s,discoveredEndpointInfo.getEndpointName());
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FinalSendActivity.this);
                alertDialogBuilder.setMessage("Found a device named "+discoveredEndpointInfo.getEndpointName()+": Do you want to sent to this device?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Nearby.getConnectionsClient(FinalSendActivity.this)
                                        .requestConnection(
                                                /* endpointName= */ discoveredEndpointInfo.getEndpointName(),
                                                s,
                                                mConnectionLifecycleCallback).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "Connection Requested", Toast.LENGTH_SHORT).show();
                                        Nearby.getConnectionsClient(FinalSendActivity.this)
                                                .stopDiscovery();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "on faliure connection", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                OnFailureListener onFailureListener=new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                };
                            }
                        });

                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                Toast.makeText(getApplicationContext(), "onEndpointFound", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onEndpointLost(String s) {
//                int b = 54;
                Toast.makeText(getApplicationContext(), "onEndpointLost", Toast.LENGTH_SHORT).show();
                Snackbar.make(view, "onEndpointLost", Snackbar.LENGTH_SHORT).show();

            }
        };
        Nearby.getConnectionsClient(FinalSendActivity.this)
                .startDiscovery(
                        /* serviceId= */ getPackageName(),
                        mEndpointDiscoveryCallback,new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build());
        // new DiscoveryOptions(Strategy.P2P_POINT_TO_POINT));
    }

    @Override
    protected void onDestroy() {
        Nearby.getConnectionsClient(FinalSendActivity.this).stopDiscovery();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        //mPathsList.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(connectedDeviceId!=null)
//            Nearby.getConnectionsClient(FinalShareActivity.this).disconnectFromEndpoint(connectedDeviceId);
//        Nearby.getConnectionsClient(FinalShareActivity.this).stopDiscovery();
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String s, ConnectionInfo connectionInfo) {
            // Toast.makeText(getApplicationContext(), "Connection init", Toast.LENGTH_SHORT).show();
            Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(s, mPayLoadCallback);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            // Toast.makeText(getApplicationContext(), "onConnectionResult", Toast.LENGTH_SHORT).show();
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    finalshareTextView.setText("connected to "+deviceName.get(s));
                    connectedDeviceId=s;
                    int index=0;
                    for(FileToSendPath path:mPathsList){
                        uri= Uri.fromFile(new File(mPathsList.get(index).getPath()));
                        String title=mPathsList.get(index).getName();
                        try {
                            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                            filePayload = Payload.fromFile(pfd);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String filenameMessage = filePayload.getId() + ":" +uri.getLastPathSegment()+":"+path.getType();
                        Payload filenameBytesPayload =
                                Payload.fromBytes(filenameMessage.getBytes(StandardCharsets.UTF_8));
                        Nearby.getConnectionsClient(FinalSendActivity.this).sendPayload(s, filenameBytesPayload);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Nearby.getConnectionsClient(FinalSendActivity.this).sendPayload(s, filePayload);
                        recyclerIdPosition.put(filePayload.getId(),index);
                        index++;
                    }
                    Toast.makeText(getApplicationContext(), "Connection Accepted", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    finalshareTextView.setText("connection to "+s+" rejected");
                    // The connection was rejected by one or both sides.
                    Toast.makeText(getApplicationContext(), "Rejected Connection", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    finalshareTextView.setText("There was an error connecting to "+s);
                    // The connection broke before it was able to be accepted.
                    Toast.makeText(getApplicationContext(), "Rejected Connection", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // Unknown status code
            }
        }

        @Override
        public void onDisconnected(String s) {
            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            Nearby.getConnectionsClient(FinalSendActivity.this)
                    .requestConnection(
                            /* endpointName= */ deviceName.get(s),
                            s,
                            mConnectionLifecycleCallback).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Connection Requested", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "on faliure connection", Toast.LENGTH_SHORT).show();
                }
            });

        }
    };
    PayloadCallback mPayLoadCallback=new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Toast.makeText(getApplicationContext(),"Payload Received",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            // Toast.makeText(getApplicationContext(),"Payload Updated",Toast.LENGTH_SHORT).show();
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                long payloadId = payloadTransferUpdate.getPayloadId();
                //  int pos=recyclerIdPosition.get(payloadId);
                //  mPathsList.get(pos).setProgress((int) ((payloadTransferUpdate.getBytesTransferred()/payloadTransferUpdate.getTotalBytes())*100));
                // Nearby.getConnectionsClient(getApplicationContext()).sendPayload(s, filePayload)// }
            }
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.IN_PROGRESS){
                if(recyclerIdPosition.get(payloadTransferUpdate.getPayloadId())!=null) {
                    int pos = recyclerIdPosition.get(payloadTransferUpdate.getPayloadId());
                    float btrans=payloadTransferUpdate.getBytesTransferred();
                    float btotal=payloadTransferUpdate.getTotalBytes();
                    float factor=btrans/btotal;
                    float progpercentage=factor*100;
                    mPathsList.get(pos).setProgress((int)progpercentage );
                    myAdapter.notifyItemChanged(pos);
                }
            }
        }
    };





    class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View recyclerFile = layoutInflater.inflate(R.layout.filetoshare_recycler, viewGroup, false);
            // RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(listItem);
            MyViewHolder viewHolder = new MyViewHolder(recyclerFile);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            myViewHolder.fileName.setText( Uri.parse(mPathsList.get(i).getPath()).getLastPathSegment());
            myViewHolder.progressBar.setProgress(mPathsList.get(i).getProgress());
            if (String.valueOf(mPathsList.get(i).getProgress()).equalsIgnoreCase("100")){
                myViewHolder.percentageText.setText("Completed!");

            }else{
                myViewHolder.percentageText.setText(String.valueOf(mPathsList.get(i).getProgress())+" %");

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
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.finalshare_file_name);
            progressBar=itemView.findViewById(R.id.finalshare_recycler_progressbar);
            percentageText=itemView.findViewById(R.id.percentage_text);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);

        }
    }


}
