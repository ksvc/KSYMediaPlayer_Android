# KSY MediaPlayer Android SDK使用手册
## 阅读对象 

本文档面向所有使用该SDK的开发人员, 测试人员等, 要求读者具有一定的Android编程开发经验。

## 1.产品概述

金山云播放内核涵盖Android、iOS、Flash和浏览器插件四个平台，基于FFmpeg自主研发音视频媒体播放内核，作为一款全平台兼容的软件播放方案，金山云播放内核提供了跨终端平台的播放器SDK，以及开放的音视频播放、控制接口和完整的开源调用示例，不仅极大降低开发门槛，同时支持客户快速在多个平台发布产品。

KSY MediaPlayer Android SDK是金山云播放内核官方推出的Android平台上使用的软件开发工具包(SDK)，为Android开发者提供简单、快捷的接口，帮助开发者实现Android平台上的多媒体播放应用。

## 2.KSYMediaPlayer SDK 功能说明

* 与系统播放器MediaPlayer接口一致，可以无缝快速切换至KSYMediaPlayer；
* 本地全媒体格式支持, 并对主流的媒体格式(mp4, avi, wmv, flv, mkv, mov, rmvb 等 )进行优化；
* 支持广泛的流式视频格式, HLS, RTMP, HTTP Rseudo-Streaming 等；
* 低延时直播体验，配合金山云推流sdk，可以达到全程直播稳定的4秒内延时；
* 实现快速满屏播放，为用户带来更快捷优质的播放体验；
* 版本适配支持Android 2.1以上版本；
* 业内一流的H.265解码；
* 小于2M大小的超轻量级直播sdk；

## 3.运行环境

KSY MediaPlayer Android SDK可运行于手机移动端、平板电脑、电视以及其他设备 ，支持 Android 2.1 及以上版本; 支持 armv5/armv7a/arm64/x86以及虚拟机运行。

## 4.下载并使用SDK

### 4.1 Step1 下载SDK
KSYMediaPlayer下载方式：

* 请使用金山云账户邮件向 taochuntang@kingsoft.com索取;
* 从github下载：https://github.com/ksvc/KSYMediaPlayer_Android.git;

解压缩后包含 demo、doc、README.md 四个部分, 解压后的目录结构如下所示:
* domo/ 目录存放KSYPlayerDemo，用于帮助开发都快速了解如何使用SDK。
* doc/ 目录存放接口参考文档。
* domo/libs 目录存放了包括一个Jar包和一个so库，该库支持armv5/armv7a/arm64/x86四种体系结构。其中，Jar包主要包含了一个基于Android系统播放器 MediaPlayer实现的播放器KSYMediaPlayer，供外界调用。而so库则包含了底层网络协议，文件格式解析及相应的解码库的实现。
* README.md 即本文档。

### 4.2 Step2 申请auth认证 
需要从金山云获取的认证信息如下：
* appid
* ak
* sk 
请使用金山云账户邮件向 taochuntang@kingsoft.com索取 。

### 4.3 配置方法
#### 4.3.1 配置
使用金山云Android SDK需引入相应资源，这里其中so库以armv7a汇编指令集版本为例：
* libs/armeabi-v7a/libksyplayer.so
* libs/libksyplayer.jar 
其中，jar包包名是: 
* com.ksyun.media.player
如果开发者需要混淆代码，则必须在混淆的配置文件中添加如下配置，防止jar包被混淆，导致播放出错 :
*  -libraryjars libs/libksyplayer.jar

#### 4.3.2 系统权限
在您开始开发前，需要在您AndroidManifest.xml里添加如下权限，如若没有添加相应的权限，则会出现播放错误
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

### 4.4 KSYMediaPlayer的使用方法
以Android的系统播放器MediaPlayer为蓝本，实现了在接口命名、功能和播放状态都与 MediaPlayer保持一致的播放类KSYMediaPlayer，只需创建KSYMediaPlayer的对象，设置 播放地址即可开始播放视频。下面将简要介类KSYMediaPlayer。
#### 4.4.1 构造函数：
KSYMediaPlayer对象的创建采用了Builder模式，需要设置更多的参数用于SDK认证，如下所示：
```java
public class VideoPlayerActivity extends Activity{
	private KSYMediaPlayer ksyMediaPlayer;
  protected void onCreate(Bundle savedInstanceState) {
   	String timeSec = String.valueOf(System.currentTimeMillis() / 1000);
   	String skSign = md5("sb56661c74aabc0df83d723a8d3eba69" + timeSec);
   	ksyMediaPlayer = new    KSYMediaPlayer.Builder(this.getApplicationContext()).setAppId("QYA0788DA337D2E0EC45").setAccessKey("a8b4dff4665f6e69ba6cbeb8ebadc9a3").setSecretKeySign(skSign).setTimeSec(timeSec).build();
	}     
}
```

其中，Builder：
* KSYMediaPlayer内部静态类，构建类；
* setAppId：设置开发者标识，标识由金山云分发给开发者；
* setAccessKey：设置AccessKey，与SecretKey对应，由金山云针对appid分配；
* setTimeSec：设置时间戳，单位为秒(s)；
* setSecretKeySign：设置加密后的SecretKey，加密方式如（SecretKeySign=md5( SecretKey + TimeSec )）； 
* build：创建KSYMediaPlayer对象并返回；


#### 4.4.2 设置事件监听：
```java
public class VideoPlayerActivity extends Activity{
    private KSYMediaPlayer ksyMediaPlayer;
     protected void onCreate(Bundle savedInstanceState) {
        ksyMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        ksyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        ksyMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        ksyMediaPlayer.setOnInfoListener(mOnInfoListener);
        ksyMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        ksyMediaPlayer.setOnErrorListener(mOnErrorListener);
        ksyMediaPlayer.setOnSeekCompleteListener(mOnSeekCompletedListener);
   }
}
```
监听实现，这里以mOnComletionListener为例：
```java
private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
    @Override
    public void onCompletion(IMediaPlayer mp) {
        Toast.makeText(mContext, "OnCompletionListener, play complete.", Toast.LENGTH_LONG).show();
        videoPlayEnd();
    }
};
```

#### 4.4.3 设置视频显示：
```java
public class VideoPlayerActivity extends Activity{
    private Surface mSurface = null;
   private SurfaceView mVideoSurfaceView = null;
   private SurfaceHolder mSurfaceHolder = null;
    protected void onCreate(Bundle savedInstanceState) {
       mSurfaceHolder = mVideoSurfaceView.getHolder();    
       mSurfaceHolder.addCallback(mSurfaceCallback);
    }
   private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            ksyMediaPlayer.setDisplay(holder);
        }
    }
}
```

#### 4.4.4 设置播放URL，并且进入prepare状态：
```java
ksyMediaPlayer.setDataSource(mrl);
ksyMediaPlayer.prepareAsync(); 
```
#### 4.4.5 开始播放：
```java
private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
  public void onPrepared(IMediaPlayer mp) {
    ksyMediaPlayer.start();
    scaleVideoView();
  }
}
```

#### 4.4.6 播放控制 pause 、stop、resume：
```java
ksyMediaPlayer.start();
ksyMediaPlayer.pause();
ksyMediaPlayer.stop(); 
```

#### 4.4.7 播放器销毁：
```java
ksyMediaPlayer.release(); 
```

#### 4.4.8 其他设置：
* Http点播协议支持Cache到本地存储：
```java
ksyMediaPlayer.setCachedDir("/mnt/sdcard/");
```

* 当前播放截图
```java
ksyMediaPlayer.getCurrentFrame(Bitmap bitmap)
```

* 设置缓冲区大小。单位MB
```java
ksyMediaPlayer.setBufferSize()
```

*设置网络超时时长。单位s
```java
ksyMediaPlayer.seTimeout()
```
*得到裸数据RGB32
```java
((KSYMediaPlayer)mMediaPlayer).setOption(KSYMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", KSYMediaPlayer.SDL_FCC_RV32);		//make sure it is called before "prepareAsync"

ByteBuffer rawBuffer[] = new ByteBuffer[5];		//5 buffers is just an example
for(int index=0; index<rawBuffer.length; index++)
{
	rawBuffer[index] = ByteBuffer.allocate(1280*720*4);     //make sure it is big enougth, not smaller than the maximum video resolution
       	((KSYMediaPlayer)mMediaPlayer).addVideoRawBuffer(rawBuffer[index].array());
}

((KSYMediaPlayer)mMediaPlayer).setVideoRawDataListener(new KSYMediaPlayer.OnVideoRawDataListener() {
	@Override
	public void onVideoRawDataAvailable(IMediaPlayer mp, byte[] buf, int size, int width, int height, int format) {
		//todo: handle rgb raw data. 
		//You can handle it in another thread, but never forget calling addVideoRawBuffer after it is done
		//....
		
		((KSYMediaPlayer)mMediaPlayer).addVideoRawBuffer(buf);		//MUST call it after the buf is handled
	}
}
```

## 特性说明
当前下载版本为轻量级播放sdk，该版本有如下特性:  
1. 支持h.264/h.265/aac/mp3编码格式;  
2. 支持rtmp/hls/http-flv直播;  
3. 支持hls和http点播，封装格式为mp4/flv/ts；  

如有其他编码和封装格式，请直接联系金山云客服获取其他版本。
## 接口说明
请见doc目录下的详细接口说明
