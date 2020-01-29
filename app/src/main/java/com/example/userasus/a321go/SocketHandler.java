package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.io.IOException;

public class SocketHandler {
    private static BluetoothSocket socket;
    private static BluetoothServerSocket serverSocket;

    private static BluetoothSocket alternativeSocket;
    private static BluetoothServerSocket serverAlternativeSocket;

    public static synchronized BluetoothSocket getSocket(){
        return socket;
    }

    public static synchronized BluetoothServerSocket getServerSocket(){
        return serverSocket;
    }

    public static synchronized void setSocket(BluetoothSocket socket){
        SocketHandler.socket = socket;
    }
    public static synchronized void setServerSocket(BluetoothServerSocket socket){
        SocketHandler.serverSocket = socket;
    }


    public static synchronized BluetoothSocket getAlternativeSocket(){
        return alternativeSocket;
    }


    public static synchronized void setServerAlternativeSocket(BluetoothServerSocket socket){
        SocketHandler.serverAlternativeSocket = socket;
    }

    public static synchronized void setAlternativeSocket(BluetoothSocket socket){
        SocketHandler.alternativeSocket = socket;
    }

    public static void terminarLigacao(final Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.sockHandler_alert_text))
                .setTitle(context.getResources().getString(R.string.sockHandler_alert_title))
                .setIcon(R.drawable.term_lig);

        builder.setNegativeButton(context.getResources().getString(R.string.sockHandler_alert_btn_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((Activity) context).finish();
            }
        });

        builder.setPositiveButton(context.getResources().getString(R.string.sockHandler_alert_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

//    public static void erroNaLigacao(Context context)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(context.getResources().getString(R.string.sockHandler_alert_notPossible_text))
//                .setTitle(context.getResources().getString(R.string.sockHandler_alert_notPossible_title))
//                .setIcon(R.drawable.term_lig);
//
//        builder.setNegativeButton(context.getResources().getString(R.string.sockHandler_alert_notPossible_btn_stop), new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                if(getSocket()!=null)
//                {
//                    try {
//                        getSocket().close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                dialog.dismiss();
//            }
//        });
//
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//    }



}