# 金山云Android播放SDK

## 1.产品概述

金山云Android播放SDK特有的直播优化策略能提供一流的直播体验，为Android开发者提供简单、快捷的接口，帮助开发者实现Android平台上的多媒体播放应用。

### 1.1 版本信息
LICENSE和版本信息：[LICENSE](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/license)

### 1.2 关于费用
金山云SDK保证，提供的[KSYMediaPlayer Android播放SDK](https://github.com/ksvc/KSYMediaPlayer_Android)可以用于商业应用，不会收取任何SDK使用费用。但是基于[KSYMediaPlayer Android播放SDK](https://github.com/ksvc/KSYMediaPlayer_Android)的其他商业服务，会由特定供应商收取授权费用，大致包括：

1. 云存储
1. CDN分发

## 2.KSYMediaPlayer SDK 功能说明

- [x] 接口定义与Android系统播放器[MediaPlayer](https://developer.android.com/reference/android/media/MediaPlayer.html)保持一致
- [x] 提供[KSYTextureView](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYTextureView)控件
- [x] 支持首屏秒开
- [x] 可设置播放器直播场景下[最大缓存时长](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/bufferTimeMax)
- [x] 支持[直播追赶(RTMP和HTTP+FLV)](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/bufferTimeMax)，降低主播和观众的延迟
- [x] 支持**RTMP**和**HTTP+FLV**直播方式
- [x] [点播库](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/LiveAndVod)支持多种格式(RMVB、AVI、MKV)
- [x] 支持软解和[MediaCodec硬解](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/hardwareDecode)
- [x] 支持[视频旋转(0/90/180/270度)](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/rotate)
- [x] 支持[播放截图](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/screenShot)
- [x] 支持[视频画面缩放模式](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/scaleVideo)
- [x] 支持[镜像播放](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/PlayerMirror)
- [x] 支持获取本地[视频信息与视频缩略图](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/androidProbeMediaInfo)
- [x] 支持获取[视频原始数据(YUV/RGB)](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/rawVideoData)
- [x] 支持获取[音频原始数据(PCM)](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/rawAudioData)
- [x] KSYMediaPlayer支持多实例
- [x] 支持音量调节，可静音播放
- [x] 支持后台播放
- [x] 支持纯音频播放
- [x] 支持H.265/HEVC播放
- [x] [支持在线查看文档](https://ksvc.github.io/KSYMediaPlayer_Android/docs/reference/packages.html)
- [x] 支持[反交错功能](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/VodSurppotDeinterlace)
- [x] 支持[音量放大](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/PlayerVolume)，最大可以放大两倍

### 2.1 播放库版本说明
#### 2.1.1 单独播放SDK
当应用只不用播放功能时，需要单独引用libksyplayer.so。libksyplayer.so在不同目录下，标识为直播库与点播库。
* [libs_live][libs_live]目录下的libksyplayer.so为直播SDK
* [libs_vod][libs_vod]目录下的libksyplayer.so为点播SDK，点播库完整包含直播库所有功能。

支持的文件封装格式和音视频编码标准如下所示：   


|      信息   |          直播         |             点播(完整包含直播所有功能)            |
|:----------:|:---------------------:|:--------------------------:|
|   流协议    | HLS, RTMP, HTTP,HTTPS, FILE |   HLS, RTMP, RTSP, HTTP, HTTPS, FILE |
|   封装格式  | FLV, TS, MPEG, MOV, M4V, MP3, AAC, GIF, ASF, RM | FLV, TS, MPEG, MOV, M4V, MP3, AAC, GIF, ASF, RM, MKV, AVI, WEBM |
| 视频编码格式 | H263, H264, H265, MPEG1, MPEG2, MPEG4, AVS, MJPEG, JPEG2000, GIF | H263, H264, H265, MPEG1, MPEG2, MPEG4, MJPEG, VC-1, WMV, RV40, PNG, JPEG, YUV, WEBP, TIFF, VP* |
| 音频编码格式 | AAC, MP3, NELLYMOSER, AMRNB, AMRWB, WMV1, WMV2, WMV3| AAC, MP3, NELLYMOSER, AMRNB, AMRWB, WMV1, WMV2, WMV3, OGG, FLAC, DTS, COOK |

> 点播库包含直播库全部功能，并且额外支持了更多的vod格式。

#### 2.1.2 直播SDK
[KSYLive_Android][KSYLive_Android]集成了[libksyplayer][libs_live]，具有播放SDK直播的所有功能，并且集成了[KSYStreamer][ksystreamer]，具有推流SDK所有功能。

如果使用直播推流、播放功能，请使用[KSYLive_Android][KSYLive_Android]，无需单独集成[libksyplayer][libs_live]。

### 2.2 SDK文档

[详情请见wiki](https://github.com/ksvc/KSYMediaPlayer_Android/wiki)

## 3.下载并使用SDK

### 3.1 下载SDK
KSYMediaPlayer下载方式：

#### github下载
* 从github下载：[https://github.com/ksvc/KSYMediaPlayer_Android.git](https://github.com/ksvc/KSYMediaPlayer_Android.git)

解压缩后包含 demo、doc、README.md 四个部分, 解压后的目录结构如下所示:
* KsyunPlayerDemo/ 目录存放KSY Android Player Demo，用于帮助开发都快速了解如何使用SDK。
* doc/ 目录存放接口参考文档。
* libs_live/ 目录包括**直播**so库，现提供了Android全部体系结构的支持。
* libs_vod/ 目录包括**点播**so库，现提供了Android全部体系结构的支持。
* README.md 即本文档。

需要注意的是：KsyunPlayerDemo/playerlib/libs 目录下只有 **armeabi-v7a** 的直播库，便于用户快速使用KsyunPlayerDemo，用户可根据自身需要将对应平台的动态库放至该目录下即可

> 直播库与点播库的区别可见链接: [https://github.com/ksvc/KSYMediaPlayer_Android/wiki/LiveAndVod](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/LiveAndVod)

#### oschina下载
* 从oschina下载：[http://git.oschina.net/ksvc/KSYMediaPlayer_Android](http://git.oschina.net/ksvc/KSYMediaPlayer_Android)

对于部分地方访问github比较慢的情况，可以从oschina clone，获取的库内容和github一致。

```
$ git clone https://git.oschina.net/ksvc/KSYMediaPlayer_Android.git
```

### 3.2 注意事项
[集成SDK注意事项](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/SDKIntegration)

### 3.3 KSYMediaPlayer的使用方法
在上述步骤之后，SDK已经集成至开发工程中，下面将给出`KSYMediaPlayer`的基本调用示例   
[KSYMediaPlayer基本调用示例](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYMediaPlayerBasicExample)


## 4.接口说明
请见doc目录下的详细接口说明

## 5.其他文档
用户如果有疑问，可访问此链接   
[GitHub WiKi](https://github.com/ksvc/KSYMediaPlayer_Android/wiki)   
[直播延迟相关设置](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/bufferTimeMax)   
[本地视频缩略图](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/androidProbeMediaInfo)   
[重置播放器&重新播放](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/reconnectAndRestart)   
[画面拉伸](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/scaleVideo)

## 6.反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720 [视频云技术交流群] 
- Issues：<https://github.com/ksvc/KSYMediaPlayer_Android/issues>

<a href="http://www.ksyun.com/"><img src="https://raw.githubusercontent.com/wiki/ksvc/KSYLive_Android/images/logo.png" border="0" alt="金山云计算" /></a>

[libs_live]: https://github.com/ksvc/KSYMediaPlayer_Android/tree/master/libs_live
[libs_vod]: https://github.com/ksvc/KSYMediaPlayer_Android/tree/master/libs_vod
[ksystreamer]: https://github.com/ksvc/KSYStreamer_Android/tree/master/libs
[KSYLive_Android]: https://github.com/ksvc/KSYLive_Android/tree/master/libs
