package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class ClientClass extends Thread {

    private final BluetoothSocket mmSocket;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConnection btConnect;
    private Context context;

    public ClientClass(BluetoothDevice device, Context context) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.context = context;
        btConnect = new BluetoothConnection(context);

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(BluetoothConnection.MY_UUID);
        } catch (IOException e) {
            //Toast.makeText(context,"[1/X][CLIENTE] Ligação Falhou",Toast.LENGTH_LONG).show();
        }
        mmSocket = tmp;
        //SocketHandler.setSocket(tmp);
    }

    public void run()
    {
        // Cancel discovery because it will slow down the connection
        //mBluetoothAdapter.cancelDiscovery();
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
//                Message message;
//                message = Message.obtain();
//                message.what = STATE_CONNECTING;
//                handler.sendMessage(message);
            //handler.obtainMessage(STATE_CONNECTING,mmSocket).sendToTarget();
            btConnect.handler.obtainMessage(BluetoothConnection.STATE_CLIENT_CONNECTING, mmSocket).sendToTarget();
            //SocketHandler.getSocket().connect();

            mmSocket.connect();

        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            btConnect.handler.obtainMessage(BluetoothConnection.STATE_CLIENT_CONNECTION_FAILED, mmSocket).sendToTarget();
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                //Log.d("socket","close");
            }
            return;
        }

        if(mmSocket!=null)
        {
            // Do work to manage the connection (in a separate thread)
            btConnect.handler.obtainMessage(BluetoothConnection.STATE_CLIENT_CONNECTED, mmSocket).sendToTarget();
            SocketHandler.setSocket(mmSocket);

            //Close context Activity
            ((Activity) context).finish();
        }
    }
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
            //((Activity)context).finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}