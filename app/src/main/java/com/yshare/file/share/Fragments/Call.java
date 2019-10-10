package com.yshare.file.share.Fragments;

import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.circularreveal.cardview.CircularRevealCardView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yshare.file.share.R;
import com.yshare.file.share.strucmodels.TextMessage;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Call extends Fragment {
    private final SimpleArrayMap<String, String> devNametoPos = new SimpleArrayMap<>();
    LinearLayout devInfo;
    TextView devName;
    Button connectButton;
    List<TextMessage> textList;
    String id;
    LottieAnimationView searchAnim;
    RecyclerView devFoundRecycler;
    LinearLayout searchLayout;
    Button discoverButton;
    boolean isDiscoverer = false;
    LinearLayout writeMessageLayout;
    String connectedDevId;
    String connectedDevName;
    MyDevRecyclerAdapter devAdapter;
    List<String> discoveredDevices = new ArrayList<String>();
    AudioRecord recorder;
    int sampleRate = 16000; // 44100 for music
    int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private AudioManager m_amAudioManager;
    RelativeLayout callScreen;
    TextView callerInfo;
    CircularRevealCardView acceptCallButton, rejectCallButton;
    boolean isCaller = false;
    boolean answered = false;
    Chronometer chronometer;

    AudioTrack track;
    String id1;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    boolean status = true;
    Button buttonCall, buttonReceiveCall;
    int fDevIndex = 0;
    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull final String s, @NonNull final DiscoveredEndpointInfo discoveredEndpointInfo) {
            searchAnim.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            discoveredDevices.add(discoveredEndpointInfo.getEndpointName());
            if (fDevIndex == 0) {
                devAdapter = new MyDevRecyclerAdapter();
                devFoundRecycler.setAdapter(devAdapter);
            } else {
                devAdapter.notifyDataSetChanged();
            }
            devNametoPos.put(discoveredEndpointInfo.getEndpointName(), s);
            devName.setText(discoveredEndpointInfo.getEndpointName());

        }

        @Override
        public void onEndpointLost(@NonNull String s) {

        }
    };
    int index = 0;
    PayloadCallback mPayLoadCallback = new PayloadCallback() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            String payloadFilenameMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
            if (payloadFilenameMessage.equalsIgnoreCase("end")) {
                try {
                    recorder.stop();
                    track.stop();
                    chronometer.stop();
                    chronometer.setVisibility(View.GONE);
                    recorder.release();
                    track.release();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                try {
                    Nearby.getConnectionsClient(getContext()).disconnectFromEndpoint(devNametoPos.get(discoveredDevices.get(0)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Nearby.getConnectionsClient(getContext()).disconnectFromEndpoint(id);
                // Nearby.getConnectionsClient(getContext()).stopAllEndpoints();
                RelativeLayout.LayoutParams rlp1 = (RelativeLayout.LayoutParams) rejectCallButton.getLayoutParams();
                rlp1.setMargins(50, 0, 0, 0);
                rejectCallButton.setLayoutParams(rlp1);
                acceptCallButton.setVisibility(View.VISIBLE);
                buttonCall.setVisibility(View.INVISIBLE);
                // devInfo.setVisibility(View.VISIBLE);
                discoverButton.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.GONE);
                searchAnim.setVisibility(View.GONE);
                connectButton.setVisibility(View.GONE);
                callScreen.setVisibility(View.GONE);
            }
            if (isCaller && index == 0) {
                track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufSize * 10,
                        AudioTrack.MODE_STREAM);
                // index++;
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                track.play();

            }
            if (index == 0) {
                buttonCall.setVisibility(View.INVISIBLE);
                buttonReceiveCall.setVisibility(View.INVISIBLE);
                devInfo.setVisibility(View.INVISIBLE);
                callScreen.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams rlp1 = (RelativeLayout.LayoutParams) rejectCallButton.getLayoutParams();
                rlp1.setMargins(0, 0, 0, 0);
                rejectCallButton.setLayoutParams(rlp1);
                acceptCallButton.setVisibility(View.GONE);
                index++;
                Vibrator vi = (Vibrator) getActivity().getSystemService(getContext().VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vi.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vi.vibrate(1000);
                }
            } else {
                if (answered || isCaller) {
                    track.write(payload.asBytes(), 0, payload.asBytes().length);
                }
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
    ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull final String s, @NonNull ConnectionInfo connectionInfo) {
            connectedDevName = connectionInfo.getEndpointName();
            connectedDevId = s;
            if (!isDiscoverer) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage(s + " wants to connect with this device");
                alertDialogBuilder.setPositiveButton("Allow",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Nearby.getConnectionsClient(getContext()).acceptConnection(s, mPayLoadCallback);
                                // Toast.makeText(ReceiveActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                            }
                        });

                alertDialogBuilder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Nearby.getConnectionsClient(getContext()).rejectConnection(s);
                        //finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                Nearby.getConnectionsClient(getContext()).acceptConnection(s, mPayLoadCallback);
            }
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    devName.setText("Connected to " + s);
                    id = s;
                    //buttonCall.setVisibility(View.VISIBLE);
                    buttonCall.callOnClick();
                    // buttonReceiveCall.setVisibility(View.VISIBLE);
                    //devInfo.setVisibility(View.VISIBLE);
                    discoverButton.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.GONE);
                    searchAnim.setVisibility(View.GONE);
                    Nearby.getConnectionsClient(getContext()).stopDiscovery();
                    connectButton.setVisibility(View.GONE);
                    chronometer.setVisibility(View.VISIBLE);
                    callerInfo.setText("Call to " + s);
                    callScreen.setVisibility(View.VISIBLE);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDisconnected(@NonNull final String s) {
            if (!isDiscoverer) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("connection to " + s + "lost, Do you want to reconnect?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                Nearby.getConnectionsClient(getContext()).requestConnection(connectedDevId, connectedDevName, mConnectionLifecycleCallback);
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        devName.setText("Connection Lost");
                        //finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                devName.setText("Connection Lost");
            }
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mbutton_call:
                    callerInfo.setText("call from " + id);
                    startStreaming();
                    isCaller = true;
                    break;
                case R.id.mbutton_rec:
                    break;
                case R.id.accept_call_button:
                    track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, minBufSize * 10,
                            AudioTrack.MODE_STREAM);
                    index++;
                    track.play();
                    answered = true;
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rejectCallButton.getLayoutParams();
                    rlp.setMargins(0, 0, 0, 0);
                    rejectCallButton.setLayoutParams(rlp);
                    acceptCallButton.setVisibility(View.GONE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    startStreaming();
                    break;
                case R.id.reject_call_button:
                    if (track != null) {
                        chronometer.stop();
                        recorder.stop();
                        track.stop();
                    }
                    //  Nearby.getConnectionsClient(getContext()).stopAllEndpoints();
                    RelativeLayout.LayoutParams rlp1 = (RelativeLayout.LayoutParams) rejectCallButton.getLayoutParams();
                    rlp1.setMargins(50, 0, 0, 0);
                    rejectCallButton.setLayoutParams(rlp1);
                    acceptCallButton.setVisibility(View.VISIBLE);
                    String s = "end";
                    Payload p = Payload.fromBytes(s.getBytes(StandardCharsets.UTF_8));
                    Nearby.getConnectionsClient(getContext()).sendPayload(id, p);
                    buttonCall.setVisibility(View.INVISIBLE);
                    // buttonReceiveCall.setVisibility(View.VISIBLE);
                    //  devInfo.setVisibility(View.VISIBLE);
                    discoverButton.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
                    searchAnim.setVisibility(View.GONE);
                    connectButton.setVisibility(View.GONE);
                    callScreen.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textList = new ArrayList<>();
        m_amAudioManager = (AudioManager) this.getActivity().getSystemService(getContext().AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.MODE_IN_CALL);
        m_amAudioManager.setSpeakerphoneOn(false);
        Nearby.getConnectionsClient(getContext())
                .startAdvertising(
                        /* endpointName= */ android.os.Build.MODEL,
                        /* serviceId= */ getContext().getPackageName(),
                        mConnectionLifecycleCallback,
                        new AdvertisingOptions(Strategy.P2P_CLUSTER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_call, container, false);
        // Inflate the layout for this fragment
        callerInfo = rootView.findViewById(R.id.caller_info);
        acceptCallButton = rootView.findViewById(R.id.accept_call_button);
        acceptCallButton.setOnClickListener(onClickListener);
        rejectCallButton = rootView.findViewById(R.id.reject_call_button);
        rejectCallButton.setOnClickListener(onClickListener);
        callScreen = rootView.findViewById(R.id.incoming_call);
        devInfo = rootView.findViewById(R.id.dev_info_call);
        devName = rootView.findViewById(R.id.dev_name_call);
        chronometer = rootView.findViewById(R.id.chronometer);
        connectButton = rootView.findViewById(R.id.connect_button_call_call);
        searchLayout = rootView.findViewById(R.id.search_layout_call);
        devFoundRecycler = rootView.findViewById(R.id.dev_found_recycler_call);
        devFoundRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAnim = rootView.findViewById(R.id.search_animation_call);
        discoverButton = rootView.findViewById(R.id.mbutton_discover_call);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Nearby.getConnectionsClient(getContext())
                            .startDiscovery(getContext().getPackageName(), endpointDiscoveryCallback,
                                    new DiscoveryOptions(Strategy.P2P_CLUSTER));
                    discoverButton.setVisibility(View.GONE);
                    searchAnim.setVisibility(View.VISIBLE);
                    isDiscoverer = true;
            }
        });
        buttonCall = rootView.findViewById(R.id.mbutton_call);
        buttonCall.setOnClickListener(onClickListener);
        buttonReceiveCall = rootView.findViewById(R.id.mbutton_rec);
        buttonReceiveCall.setOnClickListener(onClickListener);



        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Nearby.getConnectionsClient(getContext()).stopDiscovery();
        Nearby.getConnectionsClient(getContext()).stopAdvertising();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Nearby.getConnectionsClient(getContext()).stopAllEndpoints();
        Nearby.getConnectionsClient(getContext()).stopDiscovery();
        Nearby.getConnectionsClient(getContext()).stopAdvertising();
    }

    public void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {

                //  Log.d("VS", "Socket Created");
                byte[] buffer = new byte[minBufSize];
                Log.d("VS", "Buffer created of size " + minBufSize);
                //  DatagramPacket packet;
                //final InetAddress destination = InetAddress.getByName("192.168.10.21");
                //    Log.d("VS", "Address retrieved");
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize * 10);
                Log.d("VS", "Recorder initialized");
                recorder.startRecording();
                //    FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory() + "/share/os.pcm");
                //   InputStream is = new FileInputStream(Environment.getExternalStorageDirectory() + "/share/os.pcm");
                while (status) {
                    //reading data from MIC into buffer
                    try {
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        Payload bytesPayload = Payload.fromBytes(buffer);
                        Nearby.getConnectionsClient(getContext()).sendPayload(id, bytesPayload);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            recorder.stop();
                            recorder.release();
                        } catch (IllegalStateException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }


            }


        });
        streamThread.start();
    }

    class MyDevRecyclerAdapter extends RecyclerView.Adapter<MyDevViewHolder> {
        @NonNull
        @Override
        public MyDevViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View recyclerFile = layoutInflater.inflate(R.layout.rec_devices_layout, viewGroup, false);
            MyDevViewHolder viewHolder = new MyDevViewHolder(recyclerFile);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyDevViewHolder myDevViewHolder, final int i) {
            myDevViewHolder.devName.setText(discoveredDevices.get(i));

            myDevViewHolder.conButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Nearby.getConnectionsClient(getContext()).disconnectFromEndpoint(devNametoPos.get(discoveredDevices.get(i)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callerInfo.setText("Connecting to " + devNametoPos.get(discoveredDevices.get(i)));
                    callScreen.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
                    devInfo.setVisibility(View.GONE);
                    //  Nearby.getConnectionsClient(getContext()).stopAllEndpoints();
                    Nearby.getConnectionsClient(getContext()).requestConnection(discoveredDevices.get(i), devNametoPos.get(discoveredDevices.get(i)), mConnectionLifecycleCallback)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Request success", Toast.LENGTH_SHORT);
                                    callerInfo.setText("Waiting for connection acceptance");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Nearby.getConnectionsClient(getContext()).disconnectFromEndpoint(discoveredDevices.get(i));
                            callScreen.setVisibility(View.GONE);
                            searchLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity().getApplicationContext(), "Request Failed with exception " + e.getMessage(), Toast.LENGTH_SHORT);

                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return discoveredDevices.size();
        }
    }

    class MyDevViewHolder extends RecyclerView.ViewHolder {
        TextView devName;
        Button conButton;

        public MyDevViewHolder(@NonNull View itemView) {
            super(itemView);
            devName = itemView.findViewById(R.id.rec_dev_name);
            conButton = itemView.findViewById(R.id.rec_connect_button);
        }
    }

}

