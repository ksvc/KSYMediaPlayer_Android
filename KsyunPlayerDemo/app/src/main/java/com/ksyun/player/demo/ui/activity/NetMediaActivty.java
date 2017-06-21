package com.ksyun.player.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.model.NetDbAdapter;
import com.ksyun.player.demo.ui.activity.player.FloatingVideoActivity;
import com.ksyun.player.demo.ui.activity.player.MediaPlayerActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVideoActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVodActivity;
import com.ksyun.player.demo.util.Settings;

import java.util.ArrayList;


public class NetMediaActivty extends AppCompatActivity implements View.OnClickListener{
    private Button netHistory;
    private Button netScan;
    private Button netStartVideo;
    private Button netSetting;
    private EditText textUrl;
    private ListView netList;

    private ArrayList<String> listUrl;

    private Cursor cursor;
    private NetDbAdapter NetDb;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_net);

        textUrl = (EditText) findViewById(R.id.search_net);
        netStartVideo = (Button) findViewById(R.id.btn_net_vedio);
        netList = (ListView) findViewById(R.id.list_net);

        final String[] sampleUrl = { "rtmp://live.hkstv.hk.lxdns.com/live/hks",
                "http://playback.ks.zb.mi.com/record/live/107578_1467605748/hls/107578_1467605748.m3u8" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, sampleUrl);
        netList.setAdapter(adapter);

        netList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                textUrl.setText(sampleUrl[i]);
            }
        });

        netStartVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = textUrl.getText().toString();
                NetDb = new NetDbAdapter(NetMediaActivty.this);
                NetDb.open();

                if (NetDb.getData(path)) {
                    NetDb.updateData(path);
                } else {
                    NetDb.createDate(path);
                }
                NetDb.close();
                String playerType = settings.getString("choose_type", Settings.LIVE);
                if (playerType.equals(Settings.VOD)) {
                    Intent intent = new Intent(NetMediaActivty.this, TextureVodActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                } else if (playerType.equals(Settings.LIVE)){
                    Intent intent = new Intent(NetMediaActivty.this, TextureVideoActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);

                } else if (playerType.equals(Settings.MEDIA_PLAYER)) {
                    Intent intent = new Intent(NetMediaActivty.this, MediaPlayerActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(NetMediaActivty.this, FloatingVideoActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                }
            }
        });
        setActionBarLayout(R.layout.net_actionbar,this);
    }

    public void setActionBarLayout(int layoutId, Context mContext) {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(layoutId, new LinearLayout(mContext), false);
            ActionBar.LayoutParams layout = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);

            netHistory = (Button) findViewById(R.id.net_history);
            netScan = (Button) findViewById(R.id.net_scan);
            netSetting = (Button) findViewById(R.id.net_setting);
            netScan.setOnClickListener(this);
            netHistory.setOnClickListener(this);
            netSetting.setOnClickListener(this);

        }else{
            Toast.makeText(NetMediaActivty.this, "ActionBar不存在", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.net_history:
                listUrl = new ArrayList<String>();
                NetDb = new NetDbAdapter(NetMediaActivty.this);
                NetDb.open();
                cursor = NetDb.getAllData();
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    listUrl.add(cursor.getString(cursor.getColumnIndex(NetDbAdapter.KEY_PATH)));
                }
                while (cursor.moveToNext()) {
                    listUrl.add(cursor.getString(cursor.getColumnIndex(NetDbAdapter.KEY_PATH)));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, listUrl);
                netList.setAdapter(adapter);
                netList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        textUrl.setText(listUrl.get(i));
                    }
                });

                break;
            case R.id.net_scan:
                Intent intent1 = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent1, 0);
                break;
            case R.id.net_setting:
                Intent intent2 = new Intent(this, SettingActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            textUrl.setText(scanResult);
        }
    }
}
