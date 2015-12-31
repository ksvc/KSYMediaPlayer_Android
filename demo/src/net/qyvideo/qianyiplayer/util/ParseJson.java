package net.qyvideo.qianyiplayer.util;

import net.qyvideo.qianyiplayer.Strings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by QianYi-Xin on 2015/6/2.
 */
public class ParseJson {

    public static ArrayList<JObject> getPlayUrl(String data) {
        JSONObject jsonObject = null;
        JSONObject realUrl = null;
        JSONArray array = null;
        ArrayList<JObject> srcList = new ArrayList<>();

        try {
            jsonObject = new JSONObject(data);
            array = jsonObject.getJSONArray("demo_urls");
            for(int i = 0; i < array.length(); i++) {
                JObject obj = new JObject();

                obj.name = array.getJSONObject(i).getString("name");

                obj.url264 = array.getJSONObject(i).getString("src");
                obj.resolution264 = array.getJSONObject(i).getString("src_re");
                obj.bitrate264 = array.getJSONObject(i).getInt("src_br");
                obj.framerate264 = array.getJSONObject(i).getInt("src_fr");

                obj.url265 = array.getJSONObject(i).getString("dst");
                obj.resolution265 = array.getJSONObject(i).getString("dst_re");
                obj.bitrate265 = array.getJSONObject(i).getInt("dst_br");
                obj.framerate265 = array.getJSONObject(i).getInt("dst_fr");

                srcList.add(obj);
            }
        }catch (JSONException e){
            return null;
        }

        return srcList;
    }
}
