package com.ksyun.player.now.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ksyun.player.now.R;
import com.ksyun.player.now.utils.Setting;

public class SettingNewActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {


    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private RadioButton radioSoft;
    private RadioButton radioHard;


    private RadioGroup mChooseCodec;

    private EditText mBufferTime;
    private EditText mBufferSize;

    private EditText mPrepareTimeout;
    private EditText mReadTimeout;

    private ImageView back, looping;
    private Button confirm;

    private Boolean isLooping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        editor = settings.edit();
        setContentView(R.layout.activity_setting_new);
        String chooseDecode = settings.getString("choose_decode", Setting.USESOFT);
        int bufferTime = settings.getInt("buffertime", 2);
        int bufferSize = settings.getInt("buffersize", 15);
        int prepareTimeout = settings.getInt("preparetimeout", 5);
        int readTimeout = settings.getInt("readtimeout", 30);
        isLooping = settings.getBoolean("isLooping", false);
        mChooseCodec = (RadioGroup) findViewById(R.id.choose_codec);

        mChooseCodec.setOnCheckedChangeListener(this);

        mBufferSize = (EditText) findViewById(R.id.bfsize_edit);

        mBufferTime = (EditText) findViewById(R.id.bftime_edit);

        mPrepareTimeout = (EditText) findViewById(R.id.bfpreparetimeout_edit);
        mReadTimeout = (EditText) findViewById(R.id.bfreadtimeout_edit);

        back = (ImageView) findViewById(R.id.setting_back);
        back.setOnClickListener(this);
        looping = (ImageView) findViewById(R.id.setting_looping);
        looping.setOnClickListener(this);
        confirm = (Button) findViewById(R.id.confirm_config);
        confirm.setOnClickListener(this);
        mBufferSize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String strBufferSize = mBufferSize.getText().toString();

                if (!TextUtils.isEmpty(strBufferSize)) {
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
                if (!TextUtils.isEmpty(strBufferTime)) {
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
                if (!TextUtils.isEmpty(strPrepareTimeout)) {
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
                if (!TextUtils.isEmpty(strReadTimeout)) {
                    editor.putInt("readtimeout", Integer.parseInt(strReadTimeout));
                    editor.commit();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        initSetting(chooseDecode, bufferSize, bufferTime, prepareTimeout, readTimeout);

    }


    private void initSetting(String chooseDecode, int bufferSize, int bufferTime, int prepareTimeout, int readTimeout) {
        radioSoft = (RadioButton) findViewById(R.id.use_sw);
        radioHard = (RadioButton) findViewById(R.id.use_hw);


        mBufferSize.setText(String.valueOf(bufferSize));
        mBufferTime.setText(String.valueOf(bufferTime));

        mPrepareTimeout.setText(String.valueOf(prepareTimeout));
        mReadTimeout.setText(String.valueOf(readTimeout));

        switch (chooseDecode) {
            case Setting.USEHARD:
                mChooseCodec.check(radioHard.getId());
                break;
            case Setting.USESOFT:
                mChooseCodec.check(radioSoft.getId());
                break;
            default:
                mChooseCodec.check(radioSoft.getId());
                editor.putString("choose_decode", Setting.USESOFT);
                break;
        }
        if(isLooping){
            looping.setImageResource(R.mipmap.open);
        }else{
            looping.setImageResource(R.mipmap.close);
        }
        editor.commit();
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (i == R.id.use_hw) {
            editor.putString("choose_decode", Setting.USEHARD);
        } else if (i == R.id.use_sw) {
            editor.putString("choose_decode", Setting.USESOFT);
        }
        editor.commit();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.setting_back) {
            finish();
        } else if (id == R.id.setting_looping) {
            if (isLooping) {
                looping.setImageResource(R.mipmap.close);
                isLooping = false;
            } else {
                looping.setImageResource(R.mipmap.open);
                isLooping = true;
            }
            editor.putBoolean("isLooping", isLooping);
            editor.commit();
        } else if (id == R.id.confirm_config) {
            finish();
        }
    }
}
