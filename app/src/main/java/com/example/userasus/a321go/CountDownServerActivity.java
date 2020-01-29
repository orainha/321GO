package com.example.userasus.a321go;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;

public class CountDownServerActivity extends AppCompatActivity{

    private TextView textView_counter, textView_info, textView_device, textView_connected;
    private ProgressBar spinner;
    private ImageView status;

    public static ListenerLatency listenerLatency = new ListenerLatency();

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket mmSocket, socketAlternative;

    public static ListenerConnection listenerConnection = new ListenerConnection();
    public static String action;

    ProgressDialog dialog;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_server);


        textView_counter = findViewById(R.id.textView_counter);
        textView_counter.setText(""+MainActivity.getINSTANCE().getCountDownSeconds());

        textView_info = findViewById(R.id.textView_info);
        //textView_info.setText("Tens que esperar pelo gajo...");
        textView_device = findViewById(R.id.textView_device);

        mmSocket = SocketHandler.getSocket();
        //socketAlternative = SocketHandler.getAlternativeSocket();

        //Send Server Seconds to Client
        ConnectedThread connectedThread = new ConnectedThread(mmSocket, CountDownServerActivity.this);
        connectedThread.write(String.valueOf(MainActivity.getINSTANCE().getCountDownSeconds()).getBytes(),BluetoothConnection.TYPE_SECONDS);
        connectedThread.start();

        textView_device.setText(mmSocket.getRemoteDevice().getName());

        spinner=(ProgressBar)findViewById(R.id.progressBar_serv);
        spinner.setVisibility(View.VISIBLE);
        spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FBD29A"), PorterDuff.Mode.SRC_IN);

        status = (ImageView)findViewById(R.id.imageView);
        status.setImageResource(R.drawable.status_connected);

        //Listener to answer client request, latency calculation
        LatencyListener latencyListener = new LatencyListener() {
            @Override
            public void answer() {
                String msg_latency = "latcheck";
                ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownServerActivity.this);
                connectedThread.write(msg_latency.getBytes(),BluetoothConnection.TYPE_LATENCY_CHECK);
            }
        };

        listenerLatency.addListener(latencyListener);


//        ConnectionListener connectionListener = new ConnectionListener() {
//            @Override
//            public void answer() {
//
//                if(BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT) == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED)
//                {
//                    //FLAG_ACTIVITY_CLEAR_TOP clears all previous Activities on top of home. Returning to homescreen finishing all other activities
//                    startActivity(ServerActivity.createIntent(CountDownServerActivity.this).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                    finish();
//                    //CountDownServerActivity.super.finish();
//                }
//                /*if(mmSocket.getRemoteDevice().ACTION_ACL_DISCONNECTED.equals(action))
//                {
//                    //CountDownServerActivity.super.onBackPressed();
//                    //status.setImageResource(R.drawable.status_disconnected);
//                    startActivity(ServerActivity.createIntent(CountDownServerActivity.this));
//                    CountDownServerActivity.super.finish();
//
////                    spinner.setVisibility(View.VISIBLE);
//                    //ServerClass server = new ServerClass(CountDownServerActivity.this,mBluetoothAdapter);
//                    //server.start();
//                    //status.setImageResource(R.drawable.status_connected);
//                    //                      spinner.setVisibility(View.GONE);
//                    //action = BluetoothDevice.ACTION_ACL_CONNECTED;
//                    /*try {
//                        mmSocket.connect();
//                        status.setImageResource(R.drawable.status_connected);
//  //                      spinner.setVisibility(View.GONE);
//                        action = BluetoothDevice.ACTION_ACL_CONNECTED;
//
//                    } catch (IOException e) {
//
//                        e.printStackTrace();
//                        System.out.println("Nao foi possivel reconnectar");
//                    }
//                }*/
//                if(BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED)
//                {
//                    status.setImageResource(R.drawable.status_connected);
//                }
//
//            }
//        };
//
//        listenerConnection.addListener(connectionListener);

        // Register the BroadcastReceiver
        //IntentFilter filter = new IntentFilter(mmSocket.getRemoteDevice().ACTION_BOND_STATE_CHANGED);
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
        //filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_DISCONNECTED);
        //filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_CONNECTED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        dialog = new ProgressDialog(CountDownServerActivity.this);
        dialog.setMessage("Aguardando inicio da contagem");
        dialog.setCancelable(false);
        dialog.setButton("PARAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mmSocket.isConnected())
                {
                    try {
                        mmSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }
        });
        //dialog.show();


        //TODO: id da app : ca-app-pub-5486192964357036~3152437482
        // a inserir: ca-app-pub-5486192964357036/4278419532
        //Advertisement Banner
//        MobileAds.initialize(this,
//                "ca-app-pub-3940256099942544/6300978111");
        MobileAds.initialize(this,
                "ca-app-pub-5486192964357036/4278419532");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        //check if CoundDownClientActivity is running
//        new Handler().postDelayed(new Runnable() {
//
//                                      @Override
//                                      public void run() {
//                                          String bluetooth_message="hello";
//                                          ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownServerActivity.this);
//                                          connectedThread.write(bluetooth_message.getBytes(),BluetoothConnection.TYPE_HELLO);
//                                      }
//                                  }, 3000 //time in milisecond
//        );


    }//onCreate


    public static Intent createIntent(Context context)
    {
        return new Intent(context, CountDownServerActivity.class);
    }

    // Create a BroadcastReceiver for ACTION_ACL_DISCONNECTED
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            // When discovery finds a device
            //if (mmSocket.getRemoteDevice().ACTION_BOND_STATE_CHANGED.equals(action)) {
            //if(BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT) == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED)
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                startActivity(ServerActivity.createIntent(CountDownServerActivity.this).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
            //if(BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED)
            //{
            //        status.setImageResource(R.drawable.status_connected);
            //    }
            //}

        }//onReceive

    };//BroadcastReceiver

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //required to unregister the receiver
        unregisterReceiver(mReceiver);
    }

    public void onClickSair(View view)
    {
        SocketHandler.terminarLigacao(CountDownServerActivity.this);
    }

    @Override
    public void onBackPressed() {

        View view = findViewById(R.id.rel_serv_count);
        onClickSair(view);
    }
/*
    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case BluetoothConnection.STATE_CLIENT_CONNECTED:
                    status.setImageResource(R.drawable.status_connected);
                    break;
                case BluetoothConnection.STATE_CLIENT_CONNECTION_FAILED:
                    spinner.setVisibility(View.VISIBLE);
                    status.setImageResource(R.drawable.status_disconnected);
                    try {
                        mmSocket.connect();
                        status.setImageResource(R.drawable.status_connected);
                        spinner.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    });
    */
}
