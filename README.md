#KSYMediaPlayerKit_Android
---
##LIBRARY更新日志
 **V 1.0.0** 版本发布（**2016-01-29** ）

  -  UI细化及功能完善。实现了手机直播及回看，游戏直播，在线视频点播及短视频五种场景详细的播放页面，进一步完善上层播放器状态控制逻辑。
  -  新增针对手机直播及直播回看场景专用的TextureView子类，支持横竖屏切换保持原画面效果。修复原TextureView画面预览比例不正确问题及软键盘弹出后旋转导致画面白边问题。
  -  代码重构。部分公共接口抽离，添加对应不同场景单独的接口。精简各场景对应PlayView、VideoView及Controller包中的冗余代码。包结构有改变。

**V 0.1.0** 初版本发布（**2016-01-04**  ）

  -   提供对应场景(手机直播及回看，游戏直播，在线视频点播及短视频)的基本UI以及播放器状态控制逻辑

##LIBRARY使用说明
###适用场景说明
本项目为基于KSY MediaPlayer Android SDK封装的Library库，以及包含一系列集成示例的工程，分别适用于以下不同场景：

* 手机直播观看及手机直播回看
* 游戏直播观看
* 在线视频点播观看
* 短视频观看

**Library库集成并封装了Ksy MediaPlayer Android SDK**，目的是方便开发者快速集成播放器。无需为播放相关的UI，直播与点播差异，以及各种事件和状态（Home键,电源键,弱网状态操作等）下播放器状态处理耗费太多精力。

**不同场景的集成示例工程，对应的UI及状态处理逻辑均有差异**，请开发者结合自身APP类型，合理的参考集成示例。鉴于APP业务逻辑可能与SDK默认处理逻辑略有差异，SDK提供了对应的状态修改API，如果不够灵活，开发者也可自行修改源码逻辑。
> 
> 如果开发者不需要Library库提供的功能，仅需替换之前用的MediaPlayer即可，那么请移步这里：[KSY MediaPlayer Android SDK](https://github.com/ksvc/KSYMediaPlayer_Android)

###结构
LIBRARY包含两个工程，其中

* **app**- 集成示例工程
* **VideoWidgetLib**- 播放器UI及逻辑封装库，以AS Library Module形式提供

其中**VideoWidgetLib/src/jniLibs/**目录下的**libksyplayer.so**文件为播放器底层so包，目前版本仅支持armeabi-v7a指令集，后续会扩展。
**VideoWidgetLib/src/libs/**目录下的**libksyplayer.jar**为播放器Java层实现，其核心类 **KSYMediaPlayer**,基本与Android原生的**MediaPlayer**接口保持一致，在其之上扩展了一些新的功能，API详情请见这里：[KSYMediaPlayer接口说明](https://github.com/ksvc/KSYMediaPlayer_Android/tree/master/doc)


##LIBRARY结构说明
###app工程
   
- **PhoneLiveActivity** 手机直播观看页面集成示例
- **PhoneLiveReplayActivity** 手机直播回看集成示例
- **ShortVideoActivity** 短视频观看集成示例
- **StreamVideoActivity** 游戏直播观看集成示例
- **OnlieVideoActivity** 在线视频观看集成示例

###VideoWidgetLib工程
主要包结构

- .controller 对应各场景Demo特有的，Video控件之上，浮层显示信息及控制UI等自定义控件
- .model  封装的实体类
- .ui  对应各场景Demo通用及特有的数据及UI
- .util  对应各工具类
- .videoview  对应各场景Demo通用的，封装了KsyMediaPlayer对象的VideoView以及TextureView

###关键类说明
- **MediaPlayerVideoView** 封装了KsyMediaPlayer API的SurfaceView，也包含了播放器状态控制及部分特殊事件回调与处理
- **MediaPlayerTextureVideoView** 封装了KsyMediaPlayer API的TextureView，功能基本同上
- **MediaPlayerView** 不同场景的Demo有各自版本的MediaPlayerView，其本质为包括了VideoView以及控制层的一个容器，主要负责对外提供操作API，简化集成

##LIBRARY集成
**导入Library库**

- Project视图空白处右键
- New < Module
- 选择Import Gradle Project
- 选择Library目录下VideoWidgetLib工程
- Finish
- 右键工程，Open Module Setting
- 在Modules下选择自己的module
- 点击选项卡Dependencies
- 点击添加，选择Module Dependencies
- 选中列表中的VideoWidgetLib即可

**初始化**

以在线视频点播场景集成为例：

**1.XML布局中声明MediaPlayerView**

```

	<com.ksy.media.widget.ui.video.VideoMediaPlayerView
   	 	android:id="@+id/video_player_view"
    	android:layout_width="fill_parent"
   	 	android:layout_height="230dp"
    	player:playmode="window" />

```

**2.Activity中初始化MediaPlayerView**

```

	playerView = (VideoMediaPlayerView) findViewById(R.id.video_player_view);
	// setPlayConfig方法的第一个参数为设置是否为直播，
	// 第二个参数决定Activity转变为不可见时（如按下Home键，多任务键，电源键等），播放器的状态是暂停还是销毁（对应返回的时候状态为播放还是创建）
	playerView.setPlayConfig(false, PlayConfig.INTERRUPT_MODE_PAUSE_RESUME);


```

**3.设置MediaPlayerView回调接口**

```

	// PlayrView回调事件
	playerView.setPlayerViewCallback(this);

```

**4.Activity声明周期及按键事件传递给PlayerView**

```

	@Override
	protected void onResume() {
    	super.onResume();
    	playerView.onResume();
	}

	@Override
	protected void onPause() {
    	super.onPause();
    	playerView.onPause();
	}

	@Override
	protected void onDestroy() {
    	super.onDestroy();
    	playerView.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
    	playerView.dispatchKeyEvent(event);
		return super.dispatchKeyEvent(event);
	}

```

**5.Manifest权限申请**

```

	<uses-permission android:name="android.permission.GET_TASKS" />
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.WAKE_LOCK" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.WRITE_SETTINGS" />
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.READ_PHONE_STATE" />
	<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

```

> 对应其它同场景的详细集成示例，请参考Library库中，app工程里各场景集成Demo

##LIBRARY接口说明

###MediaPlayerView接口说明

**方法名：**

*设置MediaPlayerView播放配置*

public void setPlayConfig(boolean isStream, int interruptMode) {}

参数说明：

- isStream: 是否为直播。默认值为false
- interruptMode：设置中断模式。中断模式决定Activity离开时（Home键，多任务键，电源键等），Player的状态是暂停还是销毁，以及对应的，Activity返回时状态为播放还是创建。

补充说明：中断模式可选值及说明

```

	PlayConfig.INTERRUPT_MODE_RELEASE_CREATE，退出释放，返回创建，一般适用于直播
	PlayConfig.INTERRUPT_MODE_PAUSE_RESUME, 退出暂停，返回播放，一般适用于点播
	PlayConfig.INTERRUPT_MODE_STAY_PLAYING,退出保持无声继续播放，一般适用于手机直播。暂未实现，接下来的版本会补充

```

**方法名：**

*设置播放url，开始播放*

public void play(String url) {}

参数说明：

- url: 播放地址字符串

**方法名：**

*设置MediaPlayerView事件回调接口*

public void setPlayerViewCallback(PlayerViewCallback callback) {

参数说明：

- callback: 回调接口实现者

补充说明：回调事件方法及说明

```

	public interface PlayerViewCallback {
		// 小屏切换全屏时的事件通知
    	void hideViews();
		// 全屏切回小屏时的事件通知
    	void restoreViews();
		// 当播放器初始化完成，可以进入播放时的事件通知
    	void onPrepared();
		// 用户操作切换清晰度时的事件通知
    	void onQualityChanged();
		// PlayerView请求Activity销毁自身时事件通知
    	void onFinish(int playMode);
		// PlayerView收到错误消息时的事件通知
    	void onError(int errorCode, String errorMsg);
	}

```

**方法名：**

*设置是否允许循环播放*

public void setRecyclePlay(boolean mRecyclePlay) {}

参数说明：

- mRecyclePlay: 是否允许循环播放

###MediaPlayerVideoView接口说明

**方法名：**

设置视频的宽高比

public void setVideoLayout(int layout) {}

参数说明：

- layout：区分16：9和4：3

**方法名：**

SurfaceTexture是否为null

public boolean isValid() {}

**方法名：**

设置视频的播放地址

public void setVideoPath(String path) {}

参数说明：

- path：视频的播放地址

**方法名：**

设置视频地址URI

public void setVideoURI(Uri uri) {}

参数说明：

- uri：视频地址uri

**方法名：**

停止播放

public void stopPlayback() {}

**方法名：**

设置MediaPlayerController接口

public void setMediaPlayerController(MediaPlayerController       mediaPlayerController){}

参数说明：

- mediaPlayerController： MediaPlayerController接口实例

**方法名：**

播放

public void start() {}

**方法名：**

暂停

public void pause() {}

**方法名：**

获取总时间

public int getDuration() {}

**方法名：**

获取MediaInfo信息

public MediaInfo getMediaInfo() {}

**方法名：**

获取视频当前播放位置

public int getCurrentPosition() {}

**方法名：**

seek的位置

public void seekTo(long msec) {}

参数说明：

- msec：seek的位置

**方法名：**

视频是否正在播放

public boolean isPlaying() {}

**方法名：**

获取buffer的百分比

public int getBufferPercentage() {}

**方法名：**

获取视频的宽

public int getVideoWidth() {}

**方法名：**

获取视频的高

public int getVideoHeight() {}

**方法名：**

是否可以暂停

public boolean canPause() {}

**方法名：**

是否可以向后seek

public boolean canSeekBackward() {}

**方法名：**

是否可以向前seek

public boolean canSeekForward() {}

**方法名：**

是否可以播放

public boolean canStart() {}

**方法名：**

是否锁屏

public boolean isKeyGuard() {}
