package com.yshare.file.share.Fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.yshare.file.share.R;
import com.yshare.file.share.strucmodels.TextMessage;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Messaging extends Fragment {
    LinearLayout devInfo;
    TextView devName;
    Button connectButton;
    RecyclerView textRecycler;
    List<TextMessage> textList;
    MyRecyclerAdapter myAdapter;
    EditText messageTextView;
    Button sendButton;
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
    private final SimpleArrayMap<String, String> devNametoPos = new SimpleArrayMap<>();
    InterstitialAd mInterstitialAd;

    public Messaging() {
        // Required empty public constructor
    }

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

//            connectButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Nearby.getConnectionsClient(getContext()).requestConnection(discoveredEndpointInfo.getEndpointName(),s,mConnectionLifecycleCallback);
//                }
//            });
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
            if (index == 0) {
                TextMessage message = new TextMessage();
                String mess = new String(payload.asBytes(), StandardCharsets.UTF_8);
                message.setText(mess);
                message.setSender("other");
                textList.add(message);
                myAdapter = new MyRecyclerAdapter(textList);
                textRecycler.setAdapter(myAdapter);
                textRecycler.setVisibility(View.VISIBLE);
                index++;
            } else {
                TextMessage message = new TextMessage();
                String mess = new String(payload.asBytes(), StandardCharsets.UTF_8);
                message.setText(mess);
                message.setSender("other");
                textList.add(message);
                myAdapter.notifyDataSetChanged();
                textRecycler.scrollToPosition(textList.size() - 1);
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

                    // Toast.makeText(getApplicationContext(), "Connection Accepted", Toast.LENGTH_SHORT).show();
                    devName.setText("Connected to " + s);
                    id = s;
                    discoverButton.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.GONE);
                    devInfo.setVisibility(View.VISIBLE);
                    searchAnim.setVisibility(View.GONE);
                    Nearby.getConnectionsClient(getContext()).stopDiscovery();
                    textRecycler.setVisibility(View.VISIBLE);
                    writeMessageLayout.setVisibility(View.VISIBLE);
                    // Nearby.getConnectionsClient(getContext()).stopAdvertising();
                    connectButton.setVisibility(View.GONE);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    break;
                default:
                    break;
                // Unknown status code
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
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

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textList = new ArrayList<>();
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
        View rootView = inflater.inflate(R.layout.fragment_messaging, container, false);
        // Inflate the layout for this fragment

        //TODO: Ads intialization
        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstial));
        try {
            if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
                AdRequest adRequest = new AdRequest.Builder().build();
                mInterstitialAd.loadAd(adRequest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        requestNewInterstitial();

        devInfo = rootView.findViewById(R.id.dev_info);
        devName = rootView.findViewById(R.id.dev_name);
        connectButton = rootView.findViewById(R.id.connect_button);
        textRecycler = rootView.findViewById(R.id.text_recycler);
        textRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        messageTextView = rootView.findViewById(R.id.message_text);
        sendButton = rootView.findViewById(R.id.send_message_button);
        writeMessageLayout = rootView.findViewById(R.id.write_message_layout);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (messageTextView.getText().length() > 1) {
                    String messa = String.valueOf(messageTextView.getText());
                    messageTextView.setText("");
                    TextMessage tex = new TextMessage();
                    tex.setSender("me");
                    tex.setText(messa);
                    textList.add(tex);
                    if (index == 0) {
                        myAdapter = new MyRecyclerAdapter(textList);
                        textRecycler.setAdapter(myAdapter);
                        textRecycler.setVisibility(View.VISIBLE);
                        index++;
                    } else {
                        myAdapter.notifyDataSetChanged();
                        textRecycler.scrollToPosition(textList.size() - 1);
                    }

                    Payload filenameBytesPayload =
                            Payload.fromBytes(messa.getBytes(StandardCharsets.UTF_8));
                    Nearby.getConnectionsClient(getContext()).sendPayload(id, filenameBytesPayload);
                }

            }
        });


        searchLayout = rootView.findViewById(R.id.search_layout);
        devFoundRecycler = rootView.findViewById(R.id.dev_found_recycler);
        devFoundRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAnim = rootView.findViewById(R.id.search_animation);
        discoverButton = rootView.findViewById(R.id.mbutton_discover);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Nearby.getConnectionsClient(getContext())
                            .startDiscovery(getContext().getPackageName(), endpointDiscoveryCallback,
                                    new DiscoveryOptions(Strategy.P2P_CLUSTER));
                    discoverButton.setVisibility(View.GONE);
                    searchAnim.setVisibility(View.VISIBLE);
                    isDiscoverer = true;
                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        Nearby.getConnectionsClient(getContext())
                                .startDiscovery(getContext().getPackageName(), endpointDiscoveryCallback,
                                        new DiscoveryOptions(Strategy.P2P_CLUSTER));
                        discoverButton.setVisibility(View.GONE);
                        searchAnim.setVisibility(View.VISIBLE);
                        isDiscoverer = true;
                    }
                });
            }
        });
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
    }

    //TODO: Requesting Ads method
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
        List<TextMessage> mText;

        public MyRecyclerAdapter(List<TextMessage> mText) {
            this.mText = mText;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View recyclerFile = layoutInflater.inflate(R.layout.text_recycler_layout, viewGroup, false);
            // RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(listItem);
            MyViewHolder viewHolder = new MyViewHolder(recyclerFile);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

            myViewHolder.messageText.setText(mText.get(i).getText());
            if (mText.get(i).getSender().equalsIgnoreCase("other")) {
                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) myViewHolder.txtCardView.getLayoutParams();
                param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                myViewHolder.txtCardView.setLayoutParams(param);
                myViewHolder.txtCardView.setCardBackgroundColor(Color.rgb(10, 240, 50));
            } else {
                // myViewHolder.txtCardView.setCardBackgroundColor(Color.rgb(10,240,50));
                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) myViewHolder.txtCardView.getLayoutParams();
                param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                myViewHolder.txtCardView.setLayoutParams(param);
            }
        }

        @Override
        public int getItemCount() {
            return mText.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        CardView txtCardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            txtCardView = itemView.findViewById(R.id.text_rec_cardview);
        }
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
                    Nearby.getConnectionsClient(getContext()).requestConnection(discoveredDevices.get(i), devNametoPos.get(discoveredDevices.get(i)), mConnectionLifecycleCallback);
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
