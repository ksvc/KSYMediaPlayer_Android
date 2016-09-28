package com.ksyun.player.demo.activity;

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
import com.ksyun.player.demo.util.Settings;

import java.util.ArrayList;


public class NetMediaActivty extends AppCompatActivity implements View.OnClickListener{
    private Button net_setting;
    private Button net_history;
    private Button net_scan;
    private Button net_startvedio;
    private EditText texturl;
    private ListView netlist;

    private ArrayList<String> listurl;

    private Cursor cursor;
    private NetDbAdapter NetDb;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);

        texturl = (EditText)findViewById(R.id.search_net);
        net_startvedio = (Button)findViewById(R.id.btn_net_vedio);
        netlist = (ListView)findViewById(R.id.list_net);

        final String[] sampleUrl = {"rtmp://live.hkstv.hk.lxdns.com/live/hks",
                "http://playback.ks.zb.mi.com/record/live/107578_1467605748/hls/107578_1467605748.m3u8",
                "http://cxy.kssws.ks-cdn.com/h265_56c26b7a7dc5f6043.mp4"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,sampleUrl);
        netlist.setAdapter(adapter);

        netlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                texturl.setText(sampleUrl[i]);
            }
        });

        net_startvedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = texturl.getText().toString();
                NetDb = new NetDbAdapter(NetMediaActivty.this);
                NetDb.open();

                if(NetDb.getData(path)){
                    NetDb.updateData(path);
                }else{
                    NetDb.createDate(path);
                }
                NetDb.close();

                String chooseview;
                settings = getSharedPreferences("SETTINGS",Context.MODE_PRIVATE);
                chooseview = settings.getString("choose_view","undefind");

               if(chooseview.equals(Settings.USEKSYTEXTURE)){
                    Intent intent = new Intent(NetMediaActivty.this, TextureVideoActivity.class);
                    intent.putExtra("path",path);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(NetMediaActivty.this,SurfaceActivity.class);
                    intent.putExtra("path",path);
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
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);

            net_history = (Button)findViewById(R.id.net_history);
            net_setting = (Button)findViewById(R.id.net_setting);
            net_scan = (Button)findViewById(R.id.net_scan);
            net_scan.setOnClickListener(this);
            net_history.setOnClickListener(this);
            net_setting.setOnClickListener(this);

        }else{
            Toast.makeText(NetMediaActivty.this, "ActionBar不存在", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.net_history:
                listurl = new ArrayList<String>();
                NetDb = new NetDbAdapter(NetMediaActivty.this);
                NetDb.open();
                cursor = NetDb.getAllData();
                cursor.moveToFirst();
                if(cursor.getCount()>0){
                    listurl.add( cursor.getString(cursor.getColumnIndex(NetDbAdapter.KEY_PATH)));
                }
                while(cursor.moveToNext()){
                    listurl.add( cursor.getString(cursor.getColumnIndex(NetDbAdapter.KEY_PATH)));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,listurl);
                netlist.setAdapter(adapter);
                netlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        texturl.setText(listurl.get(i));
                    }
                });

                break;
            case R.id.net_setting:
                Intent intent = new Intent(this,SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.net_scan:
                Intent intent1 = new Intent(this,CaptureActivity.class);
                startActivityForResult(intent1,0);
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
            texturl.setText(scanResult);
        }
    }
}
