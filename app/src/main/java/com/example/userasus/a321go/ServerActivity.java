package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.userasus.a321go.algoritmos.IteradorIteravel;
import com.example.userasus.a321go.algoritmos.ListaSimplesCircularBaseNaoOrdenada;

public class ServerActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE_BT = 3;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothConnection btConnect;
    ServerClass server;
    ServerAlternativeClass serverAlternative;

    BluetoothSocket mmSocket, socketAlternative;


//    private String msg1 = "Aguardando ligação";
//    private String msg2 = "Diz ao gajo para carregar no Ligar...";
//    private String msg3 = "Ele que experimente voltar atrás e escolher o "+mBluetoothAdapter.getName();
//    private String msg4 = "Um de vocês tem que Criar Ligação, e outro Ligar...";

    private ProgressBar spinner;

    TextView textInfo;

    Thread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        //tornar este dispositivo BT detectavel
        //make current bluetooth device discoverable
        server = null;

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

        startActivityForResult(discoverableIntent,REQUEST_DISCOVERABLE_BT);

        //startActivity(discoverableIntent);

        spinner=(ProgressBar)findViewById(R.id.progressBar);
        //spinner.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        //spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#f7a22d"), PorterDuff.Mode.SRC_IN);


        //TODO: PODE-SE POR MENSAGENS RANDOM ENQUANTO SERVER ESPERA POR CLIENT

        //Random messages while waiting for client
        textInfo = findViewById(R.id.textView_info);

        ListaSimplesCircularBaseNaoOrdenada<String> lista = new ListaSimplesCircularBaseNaoOrdenada<>();
        lista.inserir(getResources().getString(R.string.server_msg1));
        lista.inserir(getResources().getString(R.string.server_msg2));
        lista.inserir(getResources().getString(R.string.server_msg3));
        lista.inserir(getResources().getString(R.string.server_msg4));

        IteradorIteravel<String> iteradorListaMsgs = lista.iterador();

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    //waits 8 seconds for next msg
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!iteradorListaMsgs.podeAvancar())
                    {
                        iteradorListaMsgs.reiniciar();
                    }

                    iteradorListaMsgs.avancar();
                    String msg = iteradorListaMsgs.corrente();

                    //textInfo has to run on main thread
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            if(msg == getResources().getString(R.string.server_msg3))
                            {
                                textInfo.setText(msg + " " + mBluetoothAdapter.getName());
                            }else
                                {
                                    textInfo.setText(msg);
                                }
                        }
                    });


                }
            }
        };

        myThread = new Thread(myRunnable);
        //End of Random messages while waiting for client

        //Tentativa de criar socket alternativo para que o cliente tivesse a indicação que depois da ligação falhar
        //já se poderia ligar ao servidor (se ligação estabelecida com socketAlternativo, aparecia botão Ligar Novamente)

        //Tentativa de eliminar concorrencia, só depois de alternative iniciar, é que iniciava server
/*
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                do{
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while (socketAlternative==null);

                server = new ServerClass(ServerActivity.this, mBluetoothAdapter);
                server.start();
            }
        };

        myThread = new Thread(myRunnable);
*/

        /*
        mmSocket = SocketHandler.getSocket();

        HelloListener helloListener = new HelloListener() {
            @Override
            public void hello() {
                String msg_latency = "helloClient";
                ConnectedThread connectedThread = new ConnectedThread(mmSocket,ServerActivity.this);
                connectedThread.write(msg_latency.getBytes(),BluetoothConnection.TYPE_HELLO);
            }
        };

        listenerHello.addListener(helloListener);*/

    }

    public static Intent createIntent(Context context)
    {
        return new Intent(context, ServerActivity.class);
    }

    public void onClickSair(View view)
    {
        //SocketHandler.terminarLigacao(ServerActivity.this);
        if(server!=null)
        {
            server.cancel();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if(server!=null)
        {
            server.cancel();
        }
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if(requestCode == REQUEST_DISCOVERABLE_BT)
        {
            //Toast.makeText(ServerActivity.this,"[3/X][REQUEST_DISCOVERABLE_BT] resultCode: "+resultCode,Toast.LENGTH_LONG).show();

            if (resultCode == 300)
            {

                //serverAlternative = new ServerAlternativeClass(ServerActivity.this, mBluetoothAdapter);
                //serverAlternative.start();

                //Generate random messages while waiting for client connection
                myThread.start();

                //Toast.makeText(this,"[4/X][SERVER] Iniciar Server",Toast.LENGTH_LONG).show();
                server = new ServerClass(ServerActivity.this, mBluetoothAdapter);
                server.start();

                //finish();
            }

            if (resultCode == RESULT_CANCELED) {
                btConnect.handler.obtainMessage(BluetoothConnection.BT_PERMISSION_DENIED);
                finish();
                //startActivity(MainActivity.createIntent(ServerActivity.this));
                //Toast.makeText(this,"CANCELED",Toast.LENGTH_LONG).show();
            }
        }
    }
}
