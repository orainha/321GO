package com.example.userasus.a321go;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.UUID;

import static com.example.userasus.a321go.BluetoothConnectionListActivity.btClientDevice;

public class BluetoothConnection{


    static final int STATE_LISTENING = 1;
    static final int STATE_CLIENT_CONNECTING = 2;
    static final int STATE_CLIENT_CONNECTED = 3;
    static final int STATE_CLIENT_ALTERNATIVE_CONNECTED = 20;
    static final int STATE_CLIENT_ALTERNATIVE_CONNECTION_FAILED = 21;
    static final int STATE_SERVER_CONNECTED = 4;
    static final int STATE_ALTERNATIVE_SERVER_CONNECTED = 22;
    static final int STATE_CLIENT_CONNECTION_FAILED = 5;
    static final int MESSAGE_READ=6;
    static final int MESSAGE_READ_SECONDS=7;
    static final int STATE_SERVER_CONNECTION_FAILED = 8;
    static final int STATE_ALTERNATIVE_SERVER_CONNECTION_FAILED = 23;
    static final int STATE_SERVER_LISTENING = 15;
    static final int STATE_CONNECTION_FAILED = 16;
    static final int BT_NOT_SUPPORTED = 17;
    static final int BT_PERMISSION_DENIED = 18;

    static final int MESSAGE_WRITE=9;

    static final int TYPE_ADAPTER = 10;
    static final int TYPE_SECONDS= 11;
    static final int TYPE_COUNTDOWN = 12;
    static final int TYPE_LATENCY = 13;
    static final int TYPE_LATENCY_CHECK = 14;
    static final int TYPE_HELLO = 19;

    static final int REQUEST_BT_ENABLE = 24;


    static final int TRY_COUNT_LIMIT = 8;


    static final UUID MY_UUID =  UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    static final String APP_NAME = "321GO";


    static Context context;
    static long startTime;

    static long startFirstStartClickTime;

    static ProgressDialog dialog;

    static int tryCount = 0;

    public BluetoothConnection(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);

    }

    static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_CLIENT_CONNECTING:
                    //status.setText("Listening");
                    //Toast.makeText(context,"[CLIENTE] A tentar ligacao...",Toast.LENGTH_SHORT).show();
                    break;

                case STATE_CLIENT_CONNECTED:
                    //Toast.makeText(context,"[CLIENTE] LIGADO",Toast.LENGTH_LONG).show();
                    //FLAG_ACTIVITY_CLEAR_TOP clears all previous Activities on top of home. Returning to homescreen finishing all other activities
                    if (context == BluetoothConnectionListActivity.context)
                    {
                        BluetoothConnectionListActivity.dialog.dismiss();
                        if(BluetoothConnectionListActivity.dialog.isShowing())
                        {
                            BluetoothConnectionListActivity.dialog.dismiss();
                        }
                    }
                    if (context == CountDownClientActivity.context)
                    {
                        if(CountDownClientActivity.dialog.isShowing())
                        {
                            CountDownClientActivity.dialog.dismiss();
                        }
                    }
                    context.startActivity(CountDownClientActivity.createIntent(context).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                    break;
                case STATE_CLIENT_ALTERNATIVE_CONNECTED:
                    //Toast.makeText(context,"[CLIENT_ALT] Ligado",Toast.LENGTH_SHORT).show();
                    //CountDownClientActivity.listenerConnection.notifyListener();
                    //alternativeConnected = true;
                    break;

                case STATE_CLIENT_CONNECTION_FAILED:
                    if(context == BluetoothConnectionListActivity.context)
                    {
                        if (BluetoothConnectionListActivity.btClientDevice.getBondState()==BluetoothDevice.BOND_NONE)
                        {
                            if(BluetoothConnectionListActivity.dialog.isShowing())
                            {
                                BluetoothConnectionListActivity.dialog.dismiss();
                            }
                            break;
                        }
                    }
                    if(tryCount<TRY_COUNT_LIMIT)
                    {
                        new Handler().postDelayed(new Runnable() {

                              @Override
                              public void run() {
                                  if(context == BluetoothConnectionListActivity.context)
                                  {
                                      //BluetoothConnectionListActivity.dialog.dismiss();
                                      BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice, BluetoothConnectionListActivity.context);
                                      BluetoothConnectionListActivity.client.start();
                                      if(!BluetoothConnectionListActivity.dialog.isShowing())
                                      {
                                          BluetoothConnectionListActivity.dialog.show();
                                      }
                                  }
                                  if(context == CountDownClientActivity.context)
                                  {
                                      //CountDownClientActivity.dialog.dismiss();
                                      BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice, CountDownClientActivity.context);
                                      BluetoothConnectionListActivity.client.start();
                                      if(!CountDownClientActivity.dialog.isShowing())
                                      {
                                          CountDownClientActivity.dialog.show();
                                      }
                                  }
                              }
                          }, 1500 //time in milisecond
                        );
                        tryCount++;
                    }else
                        {
                            //Display Alert Dialog with Error message
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                            builder.setMessage(context.getResources().getString(R.string.sockHandler_alert_notPossible_text))
                                    .setTitle(context.getResources().getString(R.string.sockHandler_alert_notPossible_title))
                                    .setIcon(R.drawable.term_lig);

                            builder.setNegativeButton(context.getResources().getString(R.string.sockHandler_alert_notPossible_btn_try_again), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    tryCount = 0;

                                    new Handler().postDelayed(new Runnable() {

                                                                  @Override
                                                                  public void run() {
                                                                      if(context == BluetoothConnectionListActivity.context)
                                                                      {
                                                                          BluetoothConnectionListActivity.dialog.dismiss();
                                                                          BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice, BluetoothConnectionListActivity.context);
                                                                          BluetoothConnectionListActivity.client.start();
                                                                          BluetoothConnectionListActivity.dialog.show();
                                                                      }
                                                                      if(context == CountDownClientActivity.context)
                                                                      {
                                                                          CountDownClientActivity.dialog.dismiss();
                                                                          BluetoothConnectionListActivity.client = new ClientClass(BluetoothConnectionListActivity.btClientDevice, CountDownClientActivity.context);
                                                                          BluetoothConnectionListActivity.client.start();
                                                                          CountDownClientActivity.dialog.show();
                                                                      }
                                                                  }
                                                              }, 1000 //time in milisecond
                                    );


                                    dialog.dismiss();
                                }

                            });

                            builder.setPositiveButton(context.getResources().getString(R.string.sockHandler_alert_notPossible_btn_exit), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    tryCount = 0;
                                    if(context == BluetoothConnectionListActivity.context)
                                    {
                                        BluetoothConnectionListActivity.dialog.dismiss();
                                    }else if(context == CountDownClientActivity.context)
                                    {
                                        CountDownClientActivity.dialog.dismiss();
                                    }
                                }

                            });
                            android.support.v7.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }



                    break;
                case STATE_CLIENT_ALTERNATIVE_CONNECTION_FAILED:
                      //Toast.makeText(context,"[CLIENT_ALT] Ligacao Falhou",Toast.LENGTH_SHORT).show();
                    //CountDownClientActivity.listenerConnection.notifyListener();
                    //alternativeConnected = false;
                    break;

                case STATE_CONNECTION_FAILED:
                    //TODO: ACTIVAR/DESACTIVAR LISTENER
                    //Este STATE vem de ConnectedThread
                    //CountDownClientActivity.listenerConnection.notifyListener();
                    //CountDownServerActivity.listenerConnection.notifyListener();
                    break;

                case STATE_SERVER_CONNECTED:
                    //Toast.makeText(context,"[SERVER] LIGADO, espera mensagem",Toast.LENGTH_LONG).show();
                    break;
                case STATE_ALTERNATIVE_SERVER_CONNECTED:
                    //Toast.makeText(context,"[SERVER_ALT] LIGADO",Toast.LENGTH_LONG).show();
                    break;

                case STATE_SERVER_LISTENING:
                    //Toast.makeText(context,"[SERVER] Aguardando Ligacao",Toast.LENGTH_LONG).show();
                    break;

                case STATE_SERVER_CONNECTION_FAILED:
                    //Toast.makeText(context,"[SERVER] Ligacao Falhou",Toast.LENGTH_LONG).show();
                    //context.startActivity(ServerActivity.createIntent(context));
                    //CountDownServerActivity.action = BluetoothDevice.ACTION_ACL_DISCONNECTED;
                    //CountDownServerActivity.listenerConnection.notifyListener();
                    break;
                case STATE_ALTERNATIVE_SERVER_CONNECTION_FAILED:
                    //Toast.makeText(context,"[SERVER_ALT] FALHOU",Toast.LENGTH_LONG).show();
                    break;

                case MESSAGE_READ:
                    //Toast.makeText(context,"[SERVER] A RECEBER...",Toast.LENGTH_LONG).show();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    //Toast.makeText(context,"[MESSAGE] : "+readMessage,Toast.LENGTH_LONG).show();
                    /*//Toast.makeText(context,"[SERVER]: "+readMessage,Toast.LENGTH_LONG).show();
                    if (readMessage.compareTo("YE BABY!")==0)
                    {
                        //isClient parameter for applying time delay to CountDownClient, which is faster than Server
                        //context.startActivity(CountDownActivity.createIntent(context,0));
                        break;
                    }*/

                    //"Latency" and "Latcheck" for setting time delay to Client Countdown
                    //Latency is the time that Server takes to response, we need to add it to Client Countdown

                    //"Latency" for CountDownServerActivity read
                    if (readMessage.compareTo("latency")==0)
                    {
                        //listener response with "latcheck" message
                        //which message is handled by TYPE_LATENCY_CHECK
                        //it will start CountDownActivity
                        CountDownServerActivity.listenerLatency.notifyListener();
                        break;
                    }
                    //"Latcheck" for CountDownClientActivity read
                    if (readMessage.compareTo("latcheck")==0) {
                        long endTime = System.currentTimeMillis();
                        long latency = (endTime-startTime)/2;
                        MainActivity.getINSTANCE().setClientSecsDelay(latency);
                        //Toast.makeText(context,"[LATCHECK] : "+latency,Toast.LENGTH_LONG).show();

                        //isClient parameter for applying time delay to CountDownClient, which is faster than Server
                        context.startActivity(CountDownActivity.createIntent(context,1));
                        break;
                    }

                    //seconds for CountDownClientActivity and CountDownActivity read
                    if (isInteger(readMessage))
                    {
                        int seconds = Integer.valueOf(readMessage);
                        MainActivity.getINSTANCE().setCountDownSeconds(seconds);
                        //Toast.makeText(context,"[SECONDS CLIENT] : "+seconds,Toast.LENGTH_LONG).show();
                        break;
                    }


                    //"uthere" for CountDownClientActivity read
                    if (readMessage.compareTo("hello")==0) {

                        break;
                    }

                    //"uthere" for CountDownServerActivity read
                    if (readMessage.compareTo("uthereYes")==0) {

                        break;
                    }

                    break;

                case MESSAGE_WRITE:
                    //Toast.makeText(context,"[CLIENTE] A ENVIAR...",Toast.LENGTH_LONG).show();

                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    int msgType = msg.arg1;

                    switch (msgType)
                    {
                        case TYPE_ADAPTER:
                            //Toast.makeText(context,"[CLIENT DEVICE] : "+writeMessage,Toast.LENGTH_LONG).show();
                            break;
                        case TYPE_SECONDS:
                            //Toast.makeText(context,"[SECONDS SERVER] : "+writeMessage,Toast.LENGTH_LONG).show();
                            break;
                        case TYPE_COUNTDOWN:
                            /*if (writeMessage.compareTo("YE BABY!")==0)
                            {
                                //CountDownClientActivity.listenerLatency.notifyListener();
                                context.startActivity(CountDownActivity.createIntent(context,1));
                                break;
                            }*/
                            break;

                        case TYPE_LATENCY:
                            startTime = System.currentTimeMillis();
                            break;
                        case TYPE_LATENCY_CHECK:
                            context.startActivity(CountDownActivity.createIntent(context,0));
                            break;
                        case TYPE_HELLO:
                            startTime = System.currentTimeMillis();
                            break;
                    }
                    break;
                case BT_NOT_SUPPORTED:
                    //Toast.makeText(context, "Bluetooth access denied or not supported", Toast.LENGTH_LONG).show();
                    Toast.makeText(context, context.getResources().getString(R.string.main_toast_btNotSup), Toast.LENGTH_LONG).show();
                    break;
                case BT_PERMISSION_DENIED:
                    //Toast.makeText(context,"[SERVER] Bluetooth Detectavel Desactivado",Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, context.getResources().getString(R.string.main_toast_btPermDen), Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }
    });


    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }



}//Main_Class

