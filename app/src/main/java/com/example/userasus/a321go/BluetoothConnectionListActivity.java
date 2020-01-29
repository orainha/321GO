package com.example.userasus.a321go;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class BluetoothConnectionListActivity extends AppCompatActivity {



    //Devices List's
    ListView listViewPairedDevices, listViewDevices;
    ArrayList<String> devicesList = new ArrayList<>();
    ArrayList<String> pairedDevicesList = new ArrayList<>();

    //Bluetooth Only
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ArrayList<BluetoothDevice> btDevicesArray, btPairedDevicesArray;

    //Adapters (to set on ListView's)
    private ArrayAdapter adapter, pairedAdapter;

    //onActivityResult
    final int REQUEST_BOND = 1;
    final int REQUEST_BT_ENABLE = 2;

    //Handler
    final int DISPLAY_DLG = 3;

    //Listener for connection state
    public static ListenerConnection listenerConnection = new ListenerConnection();

    //Bluetooth Client Device - usage on ClientClass
    public static BluetoothDevice btClientDevice;
    BluetoothManager bluetoothManager;

    public ProgressBar spinner;

    private Button btnStop;

    static ClientClass client;
    static ClientAlternativeClass clientAlternative;

    private BluetoothConnection btConnect;

    static ProgressDialog dialog;

    static Context context;

    int tryCount;


    private boolean mScanning;
    private Handler handler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        context = this;

        tryCount = 0;

        listViewDevices = findViewById(R.id.listViewBluetoothDevs);
        //listViewPairedDevices = findViewById(R.id.listViewBluetoothPairedDevs);

        btDevicesArray = new ArrayList<BluetoothDevice>();
        //btPairedDevicesArray = new ArrayList<BluetoothDevice>();

        //spinner=(ProgressBar)findViewById(R.id.progressBar);
        dialog = new ProgressDialog(BluetoothConnectionListActivity.this);
        dialog.setTitle(getResources().getString(R.string.btConList_dialog_search_title));
        dialog.setMessage(getResources().getString(R.string.btConList_dialog_search_text));
        dialog.setCancelable(false);
        dialog.setButton(getResources().getString(R.string.btConList_dialog_con_btn_stop), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mBluetoothAdapter.isDiscovering())
                        {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                        dialog.dismiss();
                    }
                });
        dialog.show();


        listenBTDevices();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        //BLE
//        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            // BLE is supported, we can use BLE API
//            // This callback is added to the start scan method as a parameter in this way
//            // REQUER API LEVEL 18 (ATUAL É O 15)
//
//            // Initializes Bluetooth adapter.
////            bluetoothManager =   (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
////            mBluetoothAdapter = bluetoothManager.getAdapter();
////
////            mBluetoothAdapter.startLeScan(leScanCallback);
//
//            //checkBluetoothEnabled();
//
//            //scanLeDevice(true);
//
//            //Toast.makeText(this, "BLE suportado", Toast.LENGTH_SHORT).show();
//
//
//
//        } else {
//            // BLE is not supported, Don’t use BLE capabilities here
//
//            Toast.makeText(this, "BLE nao suportado", Toast.LENGTH_SHORT).show();
//        }




        //Add adapters to Listview's
        adapter = new ArrayAdapter<>(this, R.layout.single_row_img, R.id.tv_dev_name, devicesList);
        //pairedAdapter = new ArrayAdapter<>(this,android.R.layout.simple_selectable_list_item,pairedDevicesList);

        listViewDevices.setAdapter(adapter);
        //listViewPairedDevices.setAdapter(pairedAdapter);

        //ListenerLatency's
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                //Get position id on ListView and match it with btDevicesArray
                connectToClientClass(position,btDevicesArray);
                //spinner.setVisibility(View.GONE);

                //btConnect.handler.obtainMessage(BluetoothConnection.STATE_CLIENT_CONNECTING);
                //Toast.makeText(BluetoothConnectionListActivity.this,"[CLIENTE] A tentar ligacao",Toast.LENGTH_SHORT).show();

                //listViewDevices.setEnabled(false);

            }

        });

        //spinner.setVisibility(View.VISIBLE);
        client = null;

        /*listViewPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //Get position id on ListView and match it with btPairedDevicesArray
                connectToClientClass(position,btPairedDevicesArray);
            }

        });*/

        //spinner.setVisibility(View.GONE);
        //spinner.setVisibility(View.VISIBLE);

        //TODO: DESCOMENTAR LISTENER, POR SO CLIENT

        ConnectionListener connectionListener = new ConnectionListener() {
            @Override
            public void answer() {
                    //Enable device if it's not enabled
                    checkBluetoothEnabled();

                    client = new ClientClass(btClientDevice, BluetoothConnectionListActivity.this);
                    client.start();
                }
            };

        listenerConnection.addListener(connectionListener);

//        int REQUEST_ACCESS_COARSE_LOCATION = 1;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
//            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                case PackageManager.PERMISSION_DENIED:
//                    ((TextView) new AlertDialog.Builder(this)
//                            .setTitle("Runtime Permissions up ahead")
//                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
//                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
//                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                        ActivityCompat.requestPermissions(BluetoothConnectionListActivity.this,
//                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}
//                                                , REQUEST_ACCESS_COARSE_LOCATION);
//                                    }
//                                }
//                            })
//                            .show()
//                            .findViewById(android.R.id.message))
//                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
//                    break;
//                case PackageManager.PERMISSION_GRANTED:
//                    break;
//            }
//        }

    }//onCreate






    private void connectToClientClass(int position, ArrayList<BluetoothDevice> typeDeviceListArray)
    {
        //Toast.makeText(BluetoothConnectionListActivity.this,""+btPairedDevicesArray.get(position).getName(),Toast.LENGTH_SHORT).show();
        //spinner.setVisibility(View.VISIBLE);
        dialog.setTitle(getResources().getString(R.string.btConList_dialog_con_title));
        dialog.setMessage(getResources().getString(R.string.btConList_dialog_con_text));
        dialog.setIcon(R.drawable.a_ligar);
        dialog.show();

        btClientDevice = typeDeviceListArray.get(position);

        //If it is not paired with selected device..
        if (typeDeviceListArray.get(position).getBondState() == BluetoothDevice.BOND_NONE)
        {
            //Toast.makeText(BluetoothConnectionListActivity.this,"Tem que pedir ligacao",Toast.LENGTH_LONG).show();

            //Enable device if it's not enabled
            checkBluetoothEnabled();

            //Intent bondRequest = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
            //startActivityForResult(bondRequest,REQUEST_BOND);

            //Required to get compatibility with API15+ SDK's
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)//TEM QUE TER ISTO PARA COMPATIBILIDADE API'S
            {
                //Toast.makeText(BluetoothConnectionListActivity.this,"Emparelhando com "+typeDeviceListArray.get(position).getName(),Toast.LENGTH_LONG).show();
//                Intent bondRequest = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
//                startActivityForResult(bondRequest);
                typeDeviceListArray.get(position).createBond();
                //Toast.makeText(BluetoothConnectionListActivity.this,"Emparelhado",Toast.LENGTH_LONG).show();

            }
        }

        //When it becomes paired, current device becomes the Client
        client = new ClientClass(btClientDevice, BluetoothConnectionListActivity.this);
        client.start();

        //clientAlternative = new ClientAlternativeClass(BluetoothConnectionListActivity.btClientDevice, BluetoothConnectionListActivity.this);
        //clientAlternative.start();
        //Close current Activity
        //finish();

        //Toast.makeText(BluetoothConnectionListActivity.this,"Cliente Falhou?",Toast.LENGTH_LONG).show();
        //contar tempo?



    }//connectToClientClass



    //BLE
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            // Get the BluetoothDevice object from the Intent
                            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            // Add the name and address to an array adapter to show in a ListView
                            //devicesList -> adapter (listViewDevices)
                            if(!devicesList.contains(device.getName()))
                            {
                                devicesList.add(device.getName()+" BLE");
                                //btDevicesArray -> Bluetooth Devices Array to create connection
                                btDevicesArray.add(device);
                                Toast.makeText(BluetoothConnectionListActivity.this, "" + device.getName() + "\n" + device.getAddress(), Toast.LENGTH_LONG).show();
                                //update listViewDevices list
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(leScanCallback);
//                }
//            }, SCAN_PERIOD);
//
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(leScanCallback);
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(leScanCallback);
//        }
//    }



    // Create a BroadcastReceiver for ACTION_FOUND
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                //devicesList -> adapter (listViewDevices)
                if (device.getName() != null && !devicesList.contains(device.getName())) {

                    devicesList.add(device.getName());
                    String name = device.getName();

                    //btDevicesArray -> Bluetooth Devices Array to create connection
                    btDevicesArray.add(device);
                    Toast.makeText(BluetoothConnectionListActivity.this, "" + device.getName() + "\n" + device.getAddress(), Toast.LENGTH_LONG).show();

                    //update listViewDevices list
                    adapter.notifyDataSetChanged();

                    //Dismiss spiner
                    dialog.dismiss();
                }

            }//BluetoothDevice.ACTION_FOUND

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //Stop Spinner
                //spinner.setVisibility(View.GONE);
                dialog.dismiss();

                if(btDevicesArray.isEmpty())
                {
                    //dialog = new ProgressDialog(BluetoothConnectionListActivity.this);
                    dialog.setTitle(getResources().getString(R.string.btConList_dialog_notFound_title));
                    dialog.setMessage(getResources().getString(R.string.btConList_dialog_notFound_text));
                    dialog.show();
                }
                //Toast.makeText(BluetoothConnectionListActivity.this,"ACABOU",Toast.LENGTH_LONG).show();

            }//BluetoothAdapter.ACTION_DISCOVERY_FINISHED

            //set space between the ListViews in this Activity
            listViewDevices.setPadding(0, 0, 0, 20);



        }//onReceive

    };//BroadcastReceiver


    //Create connection between activities
    public static Intent createIntent (Context context)
    {
        return new Intent(context,BluetoothConnectionListActivity.class);
    }


    public void onClickSearch(View view)
    {
        super.recreate();
    }//onClickSearch

    public void onClickSair(View view)
    {
        finish();
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    public void listenBTDevices()
    {



        //Cancel Bluetooth discover if already discovering
        if(mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Quick permission check
//        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//        if (permissionCheck != 0) {
//
//            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//        }

        //Discover Bluetooth devices
        mBluetoothAdapter.startDiscovery();

        //btnStop.setVisibility(View.VISIBLE);



      /*  //Get Bluetooth paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //pairedDevicesList -> pairedAdapter (listViewPairedDevices)
                pairedDevicesList.add(device.getName());
                //btPairedDevicesArray -> Bluetooth Devices Array to create connection
                btPairedDevicesArray.add(device);
            }
        }*/

    }//listenBTDevices

    private void checkBluetoothEnabled()
    {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_BT_ENABLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (requestCode == REQUEST_BT_ENABLE) {

            if (resultCode == RESULT_CANCELED) {
                //permanecer onde está
                btConnect.handler.obtainMessage(BluetoothConnection.BT_PERMISSION_DENIED);
                //super.onBackPressed();
                finish();
            }
        }

//        if (requestCode == REQUEST_BOND) {
//
//            if (resultCode == RESULT_OK)
//            {
//                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)//TEM QUE TER ISTO PARA COMPATIBILIDADE API'S
//                {
//                    btClientDevice.createBond();
//                }
//
//            }
//            if (resultCode == RESULT_CANCELED)
//            {
//                //permanecer onde está
//                btConnect.handler.obtainMessage(BluetoothConnection.BT_PERMISSION_DENIED);
//                dialog.dismiss();
//                return;
//                //finish();
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //required to unregister the receiver
        unregisterReceiver(mReceiver);
    }

}//Main_Class


