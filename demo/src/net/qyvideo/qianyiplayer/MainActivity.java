package net.qyvideo.qianyiplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.qyvideo.qianyiplayer.R;

import net.qyvideo.qianyiplayer.util.Extensions;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private Context mContext;

    private ListView mListView;
    private EditText mEditText;
    private ListAdapter mAdapter;

    AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String path = (String) mAdapter.getItem(position);
            Log.d("XBC", "get the file path:" + path);
            Intent intent = new Intent(mContext, VideoPlayerActivity.class);
            intent.putExtra("path", path);
            startActivity(intent);
        }
    };

    View.OnKeyListener mKeyListerner = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                    keyCode == EditorInfo.IME_ACTION_GO ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String uri = mEditText.getText().toString();
                Log.d("XBC", "get the file path:" + uri);
            	Intent intent = new Intent(mContext, VideoPlayerActivity.class);
            	intent.putExtra("path", uri);
            	startActivity(intent);
            }
            return false;
        }
    };

    TextView.OnEditorActionListener mOnEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return false;
        }
    };

    private void initList() {
        String path = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            path = Environment.getExternalStorageDirectory().getPath();
        else
        {
            Toast.makeText(this, "Fail to get the external storage, so quit.", Toast.LENGTH_LONG).show();
            finish();
        }

        mAdapter.setFileList(getVideoFiles(path));
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemListener);
        mEditText.setOnKeyListener(mKeyListerner);
        mEditText.setOnEditorActionListener(mOnEditorListener);
    }

    private ArrayList<String> getVideoFiles(String root) {
        if(root == null || root == "")
            return null;

        ArrayList<String> list = new ArrayList<String>();
        File file = new File(root);
        File[] fileList = file.listFiles();

        for(File f : fileList)
        {
            if(f.getName().contains(".")) {
                int i = f.getName().lastIndexOf(".");
                if (i > 0) {
                    if (Extensions.VIDEO.contains(f.getName().substring(i))
                            || Extensions.AUDIO.contains(f.getName().substring(i))) {
                        list.add(f.getPath());
                    }
                }
            }
        }

        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this.getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ListAdapter(mContext);

        mListView = (ListView)findViewById(R.id.list);
        mEditText = (EditText)findViewById(R.id.network_url);

        initList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
