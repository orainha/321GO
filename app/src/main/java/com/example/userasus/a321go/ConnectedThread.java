package com.example.userasus.a321go;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    //private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    BluetoothConnection btConnect;
    Context context;

    public ConnectedThread(BluetoothSocket socket, Context context) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        btConnect = new BluetoothConnection(context);
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        SocketHandler.setSocket(socket);
        this.context = context;
    }

    public void run() {
        byte[] buffer = new byte[10];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                if(bytes > 0)
                {
                    btConnect.handler.obtainMessage(BluetoothConnection.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    //Clear stream for read single messages only, not concatenate
                    //mmInStream.reset();
                }

            } catch (IOException e) {
                //TODO: quando o outro device falha, vem para aqui
                btConnect.handler.obtainMessage(BluetoothConnection.STATE_CONNECTION_FAILED)
                        .sendToTarget();
                e.printStackTrace();
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes, int type) {
        try {
            mmOutStream.write(bytes);
//            btConnect.handler.obtainMessage(BluetoothConnection.MESSAGE_WRITE, -1, -1, bytes)
//                    .sendToTarget();
            btConnect.handler.obtainMessage(BluetoothConnection.MESSAGE_WRITE,type,-1, bytes).sendToTarget();
            //Clear stream for read single messages only, not concatenate
            mmOutStream.flush();
        } catch (IOException e) { }
    }


    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            SocketHandler.getSocket().close();
        } catch (IOException e) { }
    }
}
