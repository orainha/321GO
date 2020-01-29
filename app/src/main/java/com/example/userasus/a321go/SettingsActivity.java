package com.example.userasus.a321go;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Field;

import static java.security.AccessController.getContext;

public class SettingsActivity extends AppCompatActivity  {

    //To use onValueChange function
    //implements NumberPicker.OnValueChangeListener

    TextView tvSeconds;
    TextView tvSecondsDescription;

    //EditText editTextSeconds;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    NumberPicker numberPicker;

    private int seconds;

    private final int DEFAULT_SECONDS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvSeconds = findViewById(R.id.textView_segundos);
        tvSecondsDescription = findViewById(R.id.textView_segundos_2);

        //editTextSeconds = findViewById(R.id.editText_seconds);
        numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(99);

        //Colors (NumberPicker)
        setDividerColor(numberPicker, Color.TRANSPARENT);
        setNumberPickerTextColor(numberPicker, Color.parseColor("#4f4f4f"));

        //Colors (tvSeconds - tvSecondsDesc)
        tvSeconds.setTextColor(Color.parseColor("#9c9c9c"));
        tvSecondsDescription.setTextColor(Color.parseColor("#696969"));

        /*numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d",value);
            }
        });*/

        //numberPicker.setOnValueChangedListener(this);

        seconds = MainActivity.getINSTANCE().getCountDownSeconds();

        if(seconds != 0)
        {
            numberPicker.setValue(seconds);
        }else
            {
                numberPicker.setValue(DEFAULT_SECONDS);
            }

        /*String strSeconds = String.valueOf(MainActivity.getINSTANCE().getCountDownSeconds());
        if(strSeconds!="") {
            editTextSeconds.setText(strSeconds);
        }else
            {
                editTextSeconds.setText("10");
            }*/

        //Get Seconds from SharedPreferences
        //mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        mEditor = mPreferences.edit();



    }



    public void onClickIniciar(View view)
    {
        //mEditor.putInt("seconds",Integer.valueOf(String.valueOf(editTextSeconds.getText())));
        //mEditor.commit();

        //MainActivity.getINSTANCE().setCountDownSeconds(Integer.valueOf(editTextSeconds.getText().toString()));

        seconds = numberPicker.getValue();

        mEditor.putInt("seconds",seconds);
        mEditor.commit();

        MainActivity.getINSTANCE().setCountDownSeconds(seconds);

        startActivity(ServerActivity.createIntent(SettingsActivity.this).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();




        //super.onBackPressed();

    }


    public void onClickSair(View view)
    {
        finish();
    }

    public static Intent createIntent(Context context)
    {
        return new Intent(context, SettingsActivity.class);
    }


    /*@Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        seconds = newVal;
        //TODO: aumentar texto do item seleccionado
        //numberPicker.text

        //seconds = numberPicker.getValue();

        mEditor.putInt("seconds",seconds);
        mEditor.commit();

        MainActivity.getINSTANCE().setCountDownSeconds(seconds);
    }*/

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }//setDividerPicker

    public static void setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {

        try{
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
        }
        catch(NoSuchFieldException e){
            e.printStackTrace();
        }
        catch(IllegalAccessException e){
            e.printStackTrace();
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
        }

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText)
                ((EditText)child).setTextColor(color);
        }
        numberPicker.invalidate();
    }
}
