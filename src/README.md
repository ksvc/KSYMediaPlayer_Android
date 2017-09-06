## 概述
此文件夹包含了金山云Android播放SDK的两个核心播放控件[KSYTextureView](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYTextureView)和[KSYVideoView](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYVideoView)的Java源码   

这两个类的对比如下：   

|    功能    | KSYVideoView |  KSYTextureView |
|:----------:|:-----------:|:----------------:|
| 系统版本要求| 2.3 (API Level 9) | 4.1 (API Level 16) |
|  硬解旋转 |  NO  | YES |
|  硬解缩放 |  NO  | YES |
|  硬解截图 |  NO  | YES |
| 硬解播放镜像 |  NO  | YES |
| 硬解前后台切换是否黑屏|  YES  |  NO  |

### 1. KSYTextureView
[KSYTextureView](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYTextureView)封装了**TextureView**与**KSYMediaPlayer**，接口与**KSYMediaPlayer**保持一致   
**KSYTextureView**在软解和硬解情况下均可使用，并支持在硬解情况下前后台切换不黑屏或花屏、切后台音频播放、旋转、缩放、截图等功能

#### 1.1 基本代码结构
**KSYTextureView**继承了FrameLayout，内部类**RenderTextureView**继承了**TextureView**，在**KSYTextureView**被创建时会初始化播放器、创建**RenderTextureView**的对象并添加至**KSYTextureView**   

旋转、缩放、平移、镜像等功能均在类**RenderTextureView**中实现


### 2. KSYVideoView
[KSYVideoView](https://github.com/ksvc/KSYMediaPlayer_Android/wiki/KSYVideoView)封装了**SurfaceView**与**KSYMediaPlayer**，接口与**KSYMediaPlayer**保持一致


#### 2.2 基本代码结构
**KSYVideoView**继承了FrameLayout，内部类**KSYSurfaceView**继承了**SurfaceView**，在**KSYVideoView**被创建时会初始化播放器、创建**RenderTextureView**的对象并添加至**KSYVideoView**