package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ServerAlternativeClass extends Thread{

    final UUID MY_UUID =  UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");
    final String APP_NAME = "321GO";

    //Setting mBluetoothAdapter to get Server Device Name.
    BluetoothAdapter mBluetoothAdapter;
    BluetoothServerSocket mmServerSocket;
    Context context;

    private BluetoothConnection btConnect;


    public ServerAlternativeClass(Context context, BluetoothAdapter mBluetoothAdapter)  {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        this.context = context;
        BluetoothServerSocket tmp = null;
        btConnect = new BluetoothConnection(context);
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
        } catch (IOException e) {
            //Toast.makeText(context,"[5/X][SERVER] Erro ao criar socket",Toast.LENGTH_LONG).show();
        }
        mmServerSocket = tmp;
        this.mBluetoothAdapter = mBluetoothAdapter;
        //Toast.makeText(context,"[5/X][SERVER] socket criado",Toast.LENGTH_LONG).show();
            }

    @Override
    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                //usar handlers para os toasts
//                    Message message = Message.obtain();
//                    message.what = STATE_LISTENING;
//                    handler.sendMessage(message);
                //btConnect.handler.obtainMessage(BluetoothConnection.STATE_ALTERNATIVE_SERVER_LISTENING,mmServerSocket).sendToTarget();
                socket = mmServerSocket.accept();
                //SocketHandler.setSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                btConnect.handler.obtainMessage(BluetoothConnection.STATE_ALTERNATIVE_SERVER_CONNECTION_FAILED,mmServerSocket).sendToTarget();
                //((Activity)context).finish();
                break;
            }
            // If a connection was accepted
            if (socket != null) {

//                    Message message = Message.obtain();
//                    message.what = STATE_CONNECTED;
//                    handler.sendMessage(message);
                btConnect.handler.obtainMessage(BluetoothConnection.STATE_ALTERNATIVE_SERVER_CONNECTED,socket).sendToTarget();

                SocketHandler.setAlternativeSocket(socket);

                try {
                    mmServerSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
