package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.Share;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERABLE_BT = 3;
    private static final int DEFAULT_SECONDS = 10;
    private static final int DEFAULT_CLNT_SECONDS_DELAY = 0;

    public static final MainActivity INSTANCE = new MainActivity();
    public static MainActivity getINSTANCE() {
        return INSTANCE;
    }

    static int countDownSeconds = DEFAULT_SECONDS;
    static long clientSecsDelay = DEFAULT_CLNT_SECONDS_DELAY;

    private BluetoothConnection btConnect;
    //Save SECONDS and LOCALE
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get previously saved locale (language)
        loadLocale();
        setContentView(R.layout.activity_main);

        //TODO: mudar fonte
        //Design images
        View img_or = findViewById(R.id.img_ou);
        img_or.bringToFront();
        img_or.invalidate();

        /*View img_criar = findViewById(R.id.img_criar);
        img_criar.bringToFront();
        img_criar.invalidate();

        View img_ligar = findViewById(R.id.img_ligar);
        img_ligar.bringToFront();
        img_ligar.invalidate();*/


        //Init FB
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Share app on facebook
        View img_fb_icon = findViewById(R.id.img_fb_icon);
        img_fb_icon.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {


                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setQuote("321GO - Bluetooth Countdown App")
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=project.a321go"))
                        .build();
                if (ShareDialog.canShow(ShareLinkContent.class))
                {
                    shareDialog.show(linkContent);
                }
            }
        });
        printKeyHash();


        //mPreferences = getSharedPreferences("com.example.userasus.a321go",Context.MODE_PRIVATE);

        //Get previously saved seconds
        countDownSeconds = Integer.valueOf(mPreferences.getInt("seconds",DEFAULT_SECONDS));

        if(SocketHandler.getServerSocket()!=null)
        {
            try {
                SocketHandler.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void printKeyHash() {
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.userasus.a321go", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void onClickPT(View view) {
        setLocale("");
        recreate();
    }

    public void onClickEN(View view) {
        setLocale("en");
        recreate();
    }

    //BUTTON "Ligar"
    public void onClickCliente(View view) {

        // Device does not support Bluetooth
        if (mBluetoothAdapter == null) {
            btConnect.handler.obtainMessage(BluetoothConnection.BT_NOT_SUPPORTED);
        }else {
            //Enable device if it's not enabled
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            //Start activity if it's Enabled
            if (mBluetoothAdapter.isEnabled()) {

                startActivity(BluetoothConnectionListActivity.createIntent(MainActivity.this));
            }
        }
    }

    //BUTTON "Criar Ligacao"
    public void onClickServidor(View view)
    {
        // Device does not support Bluetooth
        if (mBluetoothAdapter == null) {
            btConnect.handler.obtainMessage(BluetoothConnection.BT_NOT_SUPPORTED);
        }else
            {
                //Inicia ServerActivity
                //startActivity(ServerActivity.createIntent(MainActivity.this));
                startActivity(SettingsActivity.createIntent(MainActivity.this));
            }
    }

    public void setCountDownSeconds(int countDownSeconds) {

        this.countDownSeconds = countDownSeconds;
    }

    public int getCountDownSeconds() {

        return countDownSeconds;
    }

    public void setClientSecsDelay(long clientSecsDelay) {

        this.clientSecsDelay = clientSecsDelay;
    }

    public long getClientSecsDelay() {

        return clientSecsDelay;
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {


        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == RESULT_OK)
            {
                startActivity(BluetoothConnectionListActivity.createIntent(MainActivity.this));
            }
            if (resultCode == RESULT_CANCELED) {
                //permanecer onde está
                btConnect.handler.obtainMessage(BluetoothConnection.BT_PERMISSION_DENIED);
                super.onBackPressed();
                //finish();
            }
        }
    }


    public static Intent createIntent(Context context)
    {
        return new Intent(context, MainActivity.class);
    }

    //Local language to use
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        //Save data to Shared Preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("lang",lang);
        editor.apply(); //apply não devolve nada, nem bloqueia a execução da thread
    }

    public void loadLocale()
    {
        mPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String lang = mPreferences.getString("lang","");
        setLocale(lang);
    }
}


