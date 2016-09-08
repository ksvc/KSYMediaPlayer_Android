# 金山云Android播放SDK

## 1.产品概述

金山云Android播放SDK特有的直播优化策略能提供一流的直播体验，为Android开发者提供简单、快捷的接口，帮助开发者实现Android平台上的多媒体播放应用。

## 2.KSYMediaPlayer SDK 功能说明

* 特有的快速开播策略，为用户带来更快捷优质的播放体验；
* 特有的直播低延迟技术，提供更实时直播体验；
* 与系统播放器MediaPlayer接口一致，可以无缝快速切换至KSYMediaPlayer；
* 支持广泛的流式视频格式 HLS, RTMP, HTTP-FLV等；
* 业内一流的H.265解码；
* 支持画面旋转、截图
* 支持的文件封装格式和音视频编码标准如下所示：   


|      信息   |          直播         |             点播            |
|:----------:|:---------------------:|:--------------------------:|
|   流协议    | HLS, RTMP, HTTP, FILE |   HLS, RTMP, RTSP, HTTP, FILE |
|   封装格式  | FLV, TS, MPEG, MOV, M4V, MP3, AAC, GIF, ASF, RM | FLV, TS, MPEG, MOV, M4V, MP3, AAC, GIF, ASF, RM, MKV, AVI, WEBM |
| 视频编码格式 | H263, H264, H265, MPEG1, MPEG2, MPEG4, AVS, MJPEG, JPEG2000, GIF | H263, H264, H265, MPEG1, MPEG2, MPEG4, MJPEG, VC-1, WMV, RV40, PNG, JPEG, YUV, WEBP, TIFF, VP* |
| 音频编码格式 | AAC, MP3, NELLYMOSER, AMRNB, AMRWB, WMV1, WMV2, WMV3| AAC, MP3, NELLYMOSER, AMRNB, AMRWB, WMV1, WMV2, WMV3, OGG, FLAC, DTS, COOK |

### 文档

[详情请见wiki](https://github.com/ksvc/KSYMediaPlayer_Android/wiki)

## 3.下载并使用SDK

### 3.1 下载SDK
KSYMediaPlayer下载方式：

#### github下载
* 从github下载：[https://github.com/ksvc/KSYMediaPlayer_Android.git](https://github.com/ksvc/KSYMediaPlayer_Android.git)

解压缩后包含 demo、doc、README.md 四个部分, 解压后的目录结构如下所示:
* demo/ 目录存放KSYPlayerDemo，用于帮助开发都快速了解如何使用SDK。
* doc/ 目录存放接口参考文档。
* libs_live/ 目录包括**直播**so库，现提供了Android全部体系结构的支持。
* libs_vod/ 目录包括**点播**so库，现提供了Android全部体系结构的支持。
* README.md 即本文档。

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
[重置播放器&重新播放](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/reconnectAndRestart)   
[画面拉伸](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/scaleVideo)

## 6.反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues：<https://github.com/ksvc/KSYMediaPlayer_Android/issues>
