package com.ksyun.player.old.ui.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mzule.activityrouter.annotation.Module;
import com.github.mzule.activityrouter.annotation.Router;
import com.ksyun.player.old.R;
import com.ksyun.player.old.ui.fragment.LocalFragment;

@Module("oldplayerdemo")
@Router("oldMain")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private LocalFragment localFragment;
    private Button media_net;
    private Button media_setting;
    private Button media_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBarLayout(R.layout.media_actionbar,this);

        setDefaultFragment();

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
            media_net = (Button)findViewById(R.id.media_network);
            media_history = (Button)findViewById(R.id.media_history);
            media_setting = (Button)findViewById(R.id.media_setting);
            media_net.setOnClickListener(this);
            media_setting.setOnClickListener(this);
            media_history.setOnClickListener(this);
        }else{
            Toast.makeText(MainActivity.this, "ActionBar不存在", Toast.LENGTH_SHORT).show();
        }

    }

    private void setDefaultFragment() {

        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        localFragment = new LocalFragment();
        transaction.replace(R.id.contentFrame,localFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        int  id =view.getId();
        if(id==R.id.media_network){
            Toast.makeText(MainActivity.this, "media_net", Toast.LENGTH_SHORT).show();
            intent = new Intent(MainActivity.this,NetMediaActivty.class);
            startActivity(intent);
        }else if(id==R.id.media_history){
            Intent intent2 = new Intent(this,HistoryActivity.class);
            startActivity(intent2);
            Toast.makeText(MainActivity.this, "media_history", Toast.LENGTH_SHORT).show();
        }else if(id==R.id.media_setting){
            intent = new Intent(this,SettingActivity.class);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "media_setting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        localFragment.onBackPressed();
    }
}
