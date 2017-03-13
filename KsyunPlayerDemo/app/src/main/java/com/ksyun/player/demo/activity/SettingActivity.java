package com.ksyun.player.demo.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.util.Settings;

public class SettingActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    public static final int PLAY_VIDEO = 0x1002;


    private SharedPreferences settings ;
    private SharedPreferences.Editor editor;
    private RadioButton radioSoft;
    private RadioButton radioHard;
    private RadioButton radioLive;
    private RadioButton radioVod;
    private Switch debugSwitch;


    private RadioGroup mChooseCodec;
    private RadioGroup mChooseType;

    private EditText mBufferTime;
    private EditText mBufferSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("SETTINGS",Context.MODE_PRIVATE);
        editor = settings.edit();
        setContentView(R.layout.activity_setting);
        String chooseDecode = settings.getString("choose_decode", Settings.USESOFT);
        String chooseDebug = settings.getString("choose_debug", Settings.DEBUGON);
        String chooseType = settings.getString("choose_type", Settings.LIVE);
        String bufferTime = settings.getString("buffertime", "2");
        String bufferSize = settings.getString("buffersize", "15");


        mChooseCodec = (RadioGroup) findViewById(R.id.choose_codec);
        mChooseType = (RadioGroup) findViewById(R.id.choose_type);

        mChooseCodec.setOnCheckedChangeListener(this);
        mChooseType.setOnCheckedChangeListener(this);

        debugSwitch = (Switch) findViewById(R.id.switch_set);

        mBufferSize = (EditText) findViewById(R.id.bfsize_edit);
        mBufferTime = (EditText) findViewById(R.id.bftime_edit);

        mBufferSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editor.putString("buffersize", mBufferSize.getText().toString());
                editor.commit();
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

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString("buffertime", mBufferTime.getText().toString());
                editor.commit();
            }
        });

        initSetting(chooseDecode, chooseDebug, bufferSize, bufferTime, chooseType);

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


    private void initSetting(String chooseDecode, String chooseDebug, String bufferSize, String bufferTime, String chooseType) {
        radioSoft = (RadioButton) findViewById(R.id.use_sw);
        radioHard = (RadioButton) findViewById(R.id.use_hw);

        radioLive = (RadioButton) findViewById(R.id.type_live);
        radioVod = (RadioButton) findViewById(R.id.type_vod);

        mBufferSize.setText(bufferSize);
        mBufferTime.setText(bufferTime);

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
            default:
                mChooseType.check(radioLive.getId());
                editor.putString("choose_type", Settings.LIVE);
                break;
        }
        editor.commit();

    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.use_hw:
                editor.putString("choose_decode", Settings.USEHARD);
                break;
            case R.id.use_sw:
                editor.putString("choose_decode", Settings.USESOFT);
                break;
            case R.id.type_vod:
                editor.putString("choose_type", Settings.VOD);
                break;
            case R.id.type_live:
                editor.putString("choose_type", Settings.LIVE);
                break;
            default:
                break;
        }
        editor.commit();
    }
}
