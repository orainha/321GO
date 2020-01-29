package com.example.userasus.a321go;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class CountDownActivity extends AppCompatActivity{

    //Client delay
    private static final String IS_CLIENT = "IS_CLIENT";
    private int isClient;
    private long isClientDiscount;

    private int seconds;
    private TextView textView_counter;
    private CountDownTimer countDown;
    private ProgressBar progressBar;
    private Button btn_sair;

    private AdView mAdView;

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        //inicia texto com valor dos segundos
        textView_counter = findViewById(R.id.textView_counter);
        seconds = MainActivity.getINSTANCE().getCountDownSeconds();
        textView_counter.setText("" + seconds);
        btn_sair = findViewById(R.id.btn_sair);

        progressBar = findViewById(R.id.progressBar_count);

        //Advertisement Banner
        MobileAds.initialize(this,
                "ca-app-pub-5486192964357036/4278419532");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //if it's client...
        isClient = getIntent().getIntExtra(IS_CLIENT,-1);
        if (isClient == 1)
        {
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.countdown_client_prog_bar));
            //Toast.makeText(CountDownActivity.this,"IS client",Toast.LENGTH_LONG).show();
            isClientDiscount = MainActivity.getINSTANCE().getClientSecsDelay();
//            if(CountDownClientActivity.clickCounter==1)
//            {
//                long endFirstClickTime = System.currentTimeMillis();
//                long lauchTime = endFirstClickTime - BluetoothConnection.startFirstStartClickTime;
//                //Toast.makeText(this,"[LAUNCH TIME] : "+lauchTime,Toast.LENGTH_SHORT).show();
//                isClientDiscount = isClientDiscount + lauchTime;
//            }
        }else
            {
                //Toast.makeText(CountDownActivity.this,"NOT client",Toast.LENGTH_LONG).show();
                isClientDiscount = 0;
            }

        //adding 1000 millis to totalMilisSeconds to start countdown on selected second
        final long totalMilisSeconds = 1000+seconds*1000-isClientDiscount;
        countDown = new CountDownTimer(totalMilisSeconds,1000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished/1000 != 0) {
                    textView_counter.setText("" + millisUntilFinished / 1000);
                }
                /*long milisTillEnd = millisUntilFinished/1000;
                long totalDecimalSecs = totalMilisSeconds/1000;
                if (milisTillEnd <= totalDecimalSecs/2)
                {
                    setActivityBackgroundColor(Color.YELLOW);
                    textView_counter.setTextColor(Color.BLACK);
                }else
                    {
                        setActivityBackgroundColor(Color.RED);
                        textView_counter.setTextColor(Color.WHITE);
                    }*/
            }

            @Override
            public void onFinish() {
                textView_counter.setText("GO");
                textView_counter.setTextSize(150);
                //setActivityBackgroundColor(Color.parseColor("#A4F5AC"));
                setActivityBackgroundColor(Color.parseColor("#A5EFC8"));
                textView_counter.setTextColor(Color.WHITE);
                textView_counter.setTypeface(null, Typeface.BOLD);
                progressBar.setVisibility(View.GONE);
                btn_sair.setVisibility(View.INVISIBLE);

                //delay (para nao sair logo do GO):
                new Handler().postDelayed(new Runnable() {

                                              @Override
                                              public void run() {
                                                  //do something
                                                  CountDownActivity.super.onBackPressed();
                                              }
                                          }, 5000 //time in milisecond
                );

            }
        };
/*
        progressBar.setProgress(0);
        CountDownTimer countDownTimer =
                new CountDownTimer(totalMilisSeconds, 20) {
                    public void onTick(long millisUntilFinished) {
                        progressBar.setProgress(Math.abs((int) millisUntilFinished / 100 - 100));

                    }

                    public void onFinish() {
                        // you can do your action
                    }
                };


*/

        ValueAnimator animator = ValueAnimator.ofInt(0, progressBar.getMax());
        animator.setDuration(totalMilisSeconds);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation){
                progressBar.setProgress((Integer)animation.getAnimatedValue());
            }
        });
        /*animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // start your activity here
            }
        });*/



        //Countdown start's automaticaly on this Activity(CountDownActivity)
        //countDownTimer.start();
        countDown.start();
        animator.start();
    }


    public static Intent createIntent(Context context,int isClient)
    {
        return new Intent(context, CountDownActivity.class).putExtra(IS_CLIENT,isClient);
    }

    public void setActivityBackgroundColor(int color) {
        View view = findViewById(R.id.rel_countdown);
        view.setBackgroundColor(color);
    }

    public void onClickSair(View view)
    {
        finish();
    }
}
