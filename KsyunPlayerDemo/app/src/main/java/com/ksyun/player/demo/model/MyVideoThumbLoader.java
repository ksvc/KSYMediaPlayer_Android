package com.ksyun.player.demo.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.ksyun.media.player.misc.KSYProbeMediaInfo;

/**
 * Created by liubohua on 16/7/15.
 */
public class MyVideoThumbLoader {

    //创建cache
    private LruCache<String, Bitmap> lruCache;

    @SuppressLint("NewApi")
    public MyVideoThumbLoader(){
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取最大的运行内存
        int maxSize = maxMemory /4;//拿到缓存的内存大小 35         lruCache = new LruCache<String, Bitmap>(maxSize){

        lruCache = new LruCache<String, Bitmap>(maxSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //这个方法会在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }
    public void addVideoThumbToCache(String path,Bitmap bitmap){
        if(getVideoThumbToCache(path) == null){
            //当前地址没有缓存时，就添加
            lruCache.put(path, bitmap);
        }
    }
    public Bitmap getVideoThumbToCache(String path){

        return lruCache.get(path);

    }
    public void showThumbByAsynctack(String path,ImageView imgview){

        if(getVideoThumbToCache(path) == null){
            //异步加载
            new MyBobAsyncTask(imgview, path).execute(path);
        }else{
            imgview.setImageBitmap(getVideoThumbToCache(path));
        }

    }

    class MyBobAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;

        public MyBobAsyncTask(ImageView imageView, String path) {
            this.imgView = imageView;
            this.path = path;
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = getKSYVideoThumbnail(strings[0]);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(imgView.getTag().equals(path)) {//通过 Tag可以绑定 图片地址和 imageView，这是解决Listview加载图片错位的解决办法之一
                imgView.setImageBitmap(bitmap);
            }
        }
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public Bitmap getKSYVideoThumbnail(String filePath)
    {
        Bitmap bitmap = null;
        KSYProbeMediaInfo mediaInfo = new KSYProbeMediaInfo();
        bitmap = mediaInfo.getVideoThumbnailAtTime(filePath, 5*1000, 352, 0);

        return bitmap;
    }

}
