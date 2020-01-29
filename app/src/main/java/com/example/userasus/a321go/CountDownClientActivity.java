package com.example.userasus.a321go;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;

public class CountDownClientActivity extends AppCompatActivity{

    private TextView textView_counter, textView_device, textView_connected;
    private Button btnStart, btnTryAgain;
    private ProgressBar spinner;

    private ImageView status;

    BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConnection btConnect;

    public static ListenerConnection listenerConnection = new ListenerConnection();

    public static ListenerLatency listenerLatency = new ListenerLatency();

    public static ListenerHello listenerHello = new ListenerHello();

    //clickCounter for START button (1 sec delay on first start)
    static int clickCounter = 0;

    private int seconds;

    private AdView mAdView;

    ClientAlternativeClass clientAlternative;
    Thread myThreadAlternative;
    BluetoothSocket socketAlternative;

    static ProgressDialog dialog;
    static Context context;

    int tryCount;

    //Handler
    final int DISPLAY_DLG = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_client);
        context = this;

        tryCount = 0;

        textView_counter = findViewById(R.id.textView_counter);
        textView_device = findViewById(R.id.textView_device);
        //textView_connected = findViewById(R.id.textView_connected);


        btnStart = findViewById(R.id.btn_start);
        btnStart.setVisibility(View.INVISIBLE);
        btnTryAgain = findViewById(R.id.btn_try_again);
        btnTryAgain.setVisibility(View.INVISIBLE);

        status = (ImageView)findViewById(R.id.imageView);

        //Get connection socket
        mmSocket = SocketHandler.getSocket();
        //socketAlternative = SocketHandler.getAlternativeSocket();
        //btDevice = BluetoothDevice.

        textView_device.setText(mmSocket.getRemoteDevice().getName());
        status.setImageResource(R.drawable.status_connected);

        //Thread for Bluetooth connection
        ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownClientActivity.this);
        //start countdown
        connectedThread.start();

        //Spinner waits for socket connection
        spinner=(ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);
        spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#5FD9CB"), PorterDuff.Mode.SRC_IN);

        dialog = new ProgressDialog(CountDownClientActivity.this);

        // Register the BroadcastReceiver
        //IntentFilter filter = new IntentFilter(mmSocket.getRemoteDevice().ACTION_BOND_STATE_CHANGED);
        //IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
        //filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_DISCONNECTED);
        //filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_CONNECTED);
        //registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


        //Receive server seconds
        seconds = 0;
            //while loops are only possible in threads
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                do
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    seconds = MainActivity.getINSTANCE().getCountDownSeconds();
                }while (seconds == 0);

                textView_counter.post(new Runnable() {
                    @Override
                    public void run() {
                        textView_counter.setText("" + seconds);
                        spinner.setVisibility(View.INVISIBLE);
                        btnStart.setVisibility(View.VISIBLE);
                    }
                });


            }
        };

        Thread myThread = new Thread(myRunnable);
        myThread.start();


        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
        filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_DISCONNECTED);
        filter.addAction(mmSocket.getRemoteDevice().ACTION_ACL_CONNECTED);
        registerReceiver(mReceiver, filter);

        /*IntentFilter filter2 = new IntentFilter(socketAlternative.getRemoteDevice().ACTION_ACL_DISCONNECTED);
        filter2.addAction(socketAlternative.getRemoteDevice().ACTION_ACL_CONNECTED);
        registerReceiver(mReceiver2,filter2);*/

        //Advertisement Banner
        MobileAds.initialize(this,
                "ca-app-pub-5486192964357036/4278419532");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //final BluetoothSocket alternativeSocket = SocketHandler.getAlternativeSocket();
//        ConnectionListener connectionListener = new ConnectionListener() {
//            @Override
//            public void answer() {
//                if (BluetoothConnectionListActivity.btClientDevice!=null) {
//
//                    if(tryCount < BluetoothConnection.TRY_COUNT_LIMIT) {
//                        new Handler().postDelayed(new Runnable() {
//
//                                                      @Override
//                                                      public void run() {
//                                                          //Enable device if it's not enabled
//                                                          checkBluetoothEnabled();
//
//                                                          BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice,CountDownClientActivity.this);
//                                                          BluetoothConnectionListActivity.client.start();
//
//                                                          tryCount++;
//                                                      }
//                                                  }, 1000 //time in milisecond
//                        );
//                    }else
//                    {
//                        BluetoothConnectionListActivity.client.cancel();
//                        dialog.dismiss();
//                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CountDownClientActivity.this);
//                        builder.setMessage(getResources().getString(R.string.sockHandler_alert_notPossible_text))
//                                .setTitle(getResources().getString(R.string.sockHandler_alert_notPossible_title))
//                                .setIcon(R.drawable.term_lig);
//
//                        builder.setNegativeButton(getResources().getString(R.string.sockHandler_alert_notPossible_btn_stop), new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                            }
//                        });
//                        android.support.v7.app.AlertDialog alertDialog = builder.create();
//                        alertDialog.show();
//
//                        tryCount = 0;
//                        //myHandler.obtainMessage(DISPLAY_DLG).sendToTarget();
//                        //SocketHandler.erroNaLigacao(CountDownClientActivity.this);
//
//
//                    }
//                }
//            }
//        };
//
//        listenerConnection.addListener(connectionListener);

//        HelloListener helloListener = new HelloListener() {
//            @Override
//            public void answer() {
//                String bluetooth_message="hello";
//                ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownClientActivity.this);
//                connectedThread.write(bluetooth_message.getBytes(),BluetoothConnection.TYPE_HELLO);
//            }
//        };
//
//        listenerHello.addListener(helloListener);

        //        BluetoothConnection.alternativeConnected = false;
//        Runnable myRunnable2 = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                do
//                {
//                    //socketAlternative = SocketHandler.getAlternativeSocket();
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    clientAlternative = new ClientAlternativeClass(BluetoothConnectionListActivity.btClientDevice, CountDownClientActivity.this);
//                    clientAlternative.start(); //Assim que tenta ligar a um socket, liga-se logo ao primeiro que aparecer, não é possivel quando se cria dois sockets
//                }while (!BluetoothConnection.alternativeConnected);
//
//                //status.setImageResource(R.drawable.status_connected);
//            }
//        };
//
//        myThreadAlternative = new Thread(myRunnable2);
        //myThreadAlternative.start();


    }

//    private Handler myHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case DISPLAY_DLG:
//                    if (!isFinishing()) {
//                        SocketHandler.erroNaLigacao(CountDownClientActivity.this);
//                    }
//                    break;
//            }
//        }
//    };

    public static Intent createIntent(Context context)
    {
        return new Intent(context, CountDownClientActivity.class);
    }

    public void onClickStartClient(View view)
    {
        //String bluetooth_message="YE BABY!";
        //ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownClientActivity.this);
        //connectedThread.write(bluetooth_message.getBytes(),BluetoothConnection.TYPE_COUNTDOWN);

        String bluetooth_message="latency";
        ConnectedThread connectedThread = new ConnectedThread(mmSocket,CountDownClientActivity.this);
        connectedThread.write(bluetooth_message.getBytes(),BluetoothConnection.TYPE_LATENCY);

        //Save START button click count, if == 1, add 1000 millseconds to isClientDiscount
//        clickCounter++;
//        if (clickCounter == 1)
//        {
//            BluetoothConnection.startFirstStartClickTime = System.currentTimeMillis();
//        }

        //btnStart.setEnabled(false);


        //startActivity(CountDownActivity.createIntent(CountDownClientActivity.this,1));
    }

    public void onClickTryAgain(View view) {

        if (BluetoothConnectionListActivity.client != null)
        {
            BluetoothConnectionListActivity.client.cancel();
        }
        spinner.setVisibility(View.VISIBLE);

        dialog.setTitle(getResources().getString(R.string.countDownClient_dialog_try_title));
        dialog.setMessage(getResources().getString(R.string.countDownClient_dialog_try_text));
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.a_ligar);
        dialog.setButton(getResources().getString(R.string.countDownClient_dialog_try_btn_stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (BluetoothConnectionListActivity.client != null)
                {
                    BluetoothConnectionListActivity.client.cancel();
                }
                spinner.setVisibility(View.INVISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();

        BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice,CountDownClientActivity.this);
        BluetoothConnectionListActivity.client.start();



    }


    public void onClickSair(View view) {

        SocketHandler.terminarLigacao(CountDownClientActivity.this);
    }

    @Override
    public void onBackPressed() {
        View view = findViewById(R.id.rel_client_count);
        onClickSair(view);
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
                status.setImageResource(R.drawable.status_disconnected);
                btnStart.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.VISIBLE);
                btnTryAgain.setVisibility(View.VISIBLE);

                //ClientClass close context activity when state is connected.
                if (BluetoothConnectionListActivity.client != null)
                {
                    BluetoothConnectionListActivity.client.cancel();
                }

                //myThreadAlternative.start();

            }

            //TODO:PERCEBER PORQUE VEM PARA AQUI DEPOIS DE CLICAR EM TENTAR NOVAMENTE
            /*if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
            {
                status.setImageResource(R.drawable.status_connected);
            }*/

            //acho que nunca chega a entrar aqui... é iniciada uma nova activity sempre que se estabelece ligacao
            if(BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED)
            {
                status.setImageResource(R.drawable.status_connected);
            }

        }//onReceive

    };//BroadcastReceiver




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //required to unregister the receiver
        unregisterReceiver(mReceiver);
    }

    private void checkBluetoothEnabled()
    {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,BluetoothConnection.REQUEST_BT_ENABLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (requestCode == BluetoothConnection.REQUEST_BT_ENABLE) {

            if (resultCode == RESULT_CANCELED) {
                //permanecer onde está
                btConnect.handler.obtainMessage(BluetoothConnection.BT_PERMISSION_DENIED);
                //super.onBackPressed();
                finish();
            }
        }
    }
}


