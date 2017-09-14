package com.ksyun.player.old.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.ksyun.player.old.*;
import com.ksyun.player.old.util.Settings;

public class SettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    public static final int PLAY_VIDEO = 0x1002;


    private SharedPreferences settings ;
    private SharedPreferences.Editor editor;
    private RadioButton radioSoft;
    private RadioButton radioHard;
    private RadioButton radioLive;
    private RadioButton radioVod;
    private RadioButton radioFloating;
    private RadioButton radioMediaPlayer;
    private Switch debugSwitch;


    private RadioGroup mChooseCodec;
    private RadioGroup mChooseType;

    private EditText mBufferTime;
    private EditText mBufferSize;

    private EditText mPrepareTimeout;
    private EditText mReadTimeout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("SETTINGS",Context.MODE_PRIVATE);
        editor = settings.edit();
        setContentView(R.layout.activity_setting);
        String chooseDecode = settings.getString("choose_decode", Settings.USESOFT);
        String chooseDebug = settings.getString("choose_debug", Settings.DEBUGON);
        String chooseType = settings.getString("choose_type", Settings.LIVE);
        int bufferTime = settings.getInt("buffertime", 2);
        int bufferSize = settings.getInt("buffersize", 15);
        int prepareTimeout = settings.getInt("preparetimeout", 5);
        int readTimeout = settings.getInt("readtimeout", 30);

        mChooseCodec = (RadioGroup) findViewById(R.id.choose_codec);
        mChooseType = (RadioGroup) findViewById(R.id.choose_type);

        mChooseCodec.setOnCheckedChangeListener(this);
        mChooseType.setOnCheckedChangeListener(this);

        debugSwitch = (Switch) findViewById(R.id.switch_set);

        mBufferSize = (EditText) findViewById(R.id.bfsize_edit);

        mBufferTime = (EditText) findViewById(R.id.bftime_edit);

        mPrepareTimeout = (EditText) findViewById(R.id.bfpreparetimeout_edit);
        mReadTimeout = (EditText) findViewById(R.id.bfreadtimeout_edit);

        mBufferSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String strBufferSize = mBufferSize.getText().toString();

                if (!TextUtils.isEmpty(strBufferSize)){
                    editor.putInt("buffersize", Integer.parseInt(strBufferSize));
                    editor.commit();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBufferTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String strBufferTime = mBufferTime.getText().toString();
                if (!TextUtils.isEmpty(strBufferTime)){
                    editor.putInt("buffertime", Integer.parseInt(strBufferTime));
                    editor.commit();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mPrepareTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strPrepareTimeout = mPrepareTimeout.getText().toString();
                if (!TextUtils.isEmpty(strPrepareTimeout)){
                    editor.putInt("preparetimeout", Integer.parseInt(strPrepareTimeout));
                    editor.commit();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mReadTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strReadTimeout = mReadTimeout.getText().toString();
                if (!TextUtils.isEmpty(strReadTimeout)){
                    editor.putInt("readtimeout", Integer.parseInt(strReadTimeout));
                    editor.commit();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        initSetting(chooseDecode, chooseDebug, bufferSize, bufferTime, chooseType, prepareTimeout, readTimeout);

        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    editor.putString("choose_debug", Settings.DEBUGON);
                    Toast.makeText(SettingActivity.this, "Debug被打开", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("choose_debug", Settings.DEBUGOFF);
                    Toast.makeText(SettingActivity.this, "Debug被关闭", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
            }
        });
    }


    private void initSetting(String chooseDecode, String chooseDebug, int bufferSize, int bufferTime, String chooseType, int prepareTimeout, int readTimeout) {
        radioSoft = (RadioButton) findViewById(R.id.use_sw);
        radioHard = (RadioButton) findViewById(R.id.use_hw);

        radioLive = (RadioButton) findViewById(R.id.type_live);
        radioVod = (RadioButton) findViewById(R.id.type_vod);
        radioFloating = (RadioButton) findViewById(R.id.type_floating);
        radioMediaPlayer = (RadioButton) findViewById(R.id.type_media_player);

        mBufferSize.setText(String.valueOf(bufferSize));
        mBufferTime.setText(String.valueOf(bufferTime));

        mPrepareTimeout.setText(String.valueOf(prepareTimeout));
        mReadTimeout.setText(String.valueOf(readTimeout));

        switch (chooseDecode) {
            case Settings.USEHARD:
                mChooseCodec.check(radioHard.getId());
                break;
            case Settings.USESOFT:
                mChooseCodec.check(radioSoft.getId());
                break;
            default:
                mChooseCodec.check(radioSoft.getId());
                editor.putString("choose_decode", Settings.USESOFT);
                break;
        }
        switch (chooseDebug) {
            case Settings.DEBUGOFF:
                debugSwitch.setChecked(false);
                break;
            case Settings.DEBUGON:
                debugSwitch.setChecked(true);
                break;
            default:
                debugSwitch.setChecked(true);
                editor.putString("choose_debug", Settings.DEBUGON);
                break;
        }

        switch (chooseType) {
            case Settings.VOD:
                mChooseType.check(radioVod.getId());
                break;
            case Settings.LIVE:
                mChooseType.check(radioLive.getId());
                break;
            case Settings.FLOATING:
                mChooseType.check(radioFloating.getId());
                break;
            case Settings.MEDIA_PLAYER:
                mChooseType.check(radioMediaPlayer.getId());
                break;
            default:
                mChooseType.check(radioLive.getId());
                editor.putString("choose_type", Settings.LIVE);
                break;
        }
        editor.commit();

    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if(i==R.id.use_hw){
            editor.putString("choose_decode", Settings.USEHARD);
        }else if(i==R.id.use_sw){
            editor.putString("choose_decode", Settings.USESOFT);
        }else if(i==R.id.type_vod){
            editor.putString("choose_type", Settings.VOD);
        }else if(i==R.id.type_live){
            editor.putString("choose_type", Settings.LIVE);
        }else if(i==R.id.type_floating){
            editor.putString("choose_type", Settings.FLOATING);
        }else if(i==R.id.type_media_player){
            editor.putString("choose_type", Settings.MEDIA_PLAYER);
        }
        editor.commit();
    }
}
