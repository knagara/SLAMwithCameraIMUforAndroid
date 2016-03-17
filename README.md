# SLAM with Camera and IMU for Android
画像センサとIMUを用いたSLAMのためのAndroidアプリ（日本語説明は後半）

>SLAM = Simultaneous Localization and Mapping

## Overview

[![](http://img.youtube.com/vi/IDZ5fxp_XdY/0.jpg)](https://www.youtube.com/watch?v=IDZ5fxp_XdY)

![](https://github.com/knagara/miscellaneous/blob/master/Overview.png)

- Android App (This page)
- SLAM program (See -> [SLAMwithCameraIMUforPython](https://github.com/knagara/SLAMwithCameraIMUforPython))
- MQTT Broker (See -> [MQTT](http://mqtt.org/), [mosquitto](http://mosquitto.org/), [Apollo](https://activemq.apache.org/apollo/))

## How to use
1. Install OpenCV Manager from [Google Play](https://play.google.com/store/apps/details?id=org.opencv.engine)
2. Download the app from [here](https://github.com/knagara/miscellaneous/blob/master/SLAMwithCameraIMU.apk?raw=true)
3. Install and launch the app
4. Press the login button
5. Fill in the form
<br />
[Recommended setting]
<br />
![](https://github.com/knagara/miscellaneous/blob/master/Login_.png)
<br />
6. Press the start button
7. Now your smartphone is publishing sensor data to your MQTT server!
~~~
[Example of published sensor data (IMU)]
Topic: SLAM/input/all
Payload: 1452657360343&-0.039294902&-0.09044182&-0.06832063&-2.0395765&0.068644375&-3.0333138&-26.178856&-19.393106&-6.809244&0.026928991&0.018066406&-0.012341589
~~~
~~~
[Explanation of payload (IMU)]
Payload: timestamp&acceleration[x]&acceleration[y]&acceleration[z]&orientation[x]&orientation[y]&orientation[z]&magnet[x]&magnet[y]&magnet[z]&gyroscope[x]&gyroscope[y]&gyroscope[z]
~~~
~~~
[Example of published sensor data (Camera)]
Topic: SLAM/input/camera
Payload: 1452657480185$5:9:69.0:726.0:87.0:734.0&29:52:237.0:1177.0:249.0:1199.0&13:25:473.0:957.0:490.0:980.0&20:37:113.0:1013.0:126.0:1028.0&16:29:159.0:988.0:172.0:1002.0&
~~~
~~~
[Explanation of payload (Camera)]
Payload: timestamp$index_of_keypoint_on_previous_frame:index_of_keypoint_on_current_frame:image_coordinate[x]_on_previous_frame:image_coordinate[y]_on_previous_frame:image_coordinate[x]_on_current_frame:image_coordinate[y]_on_current_frame&...
~~~
8.Press anywhere on the display to stop publishing
 
## Files

|File name|Explanation|
|:--|:--|
|Conf.java|not important|
|DMatchComparator.java|not important|
|LoginActivity.java|Login page|
|MainActivity.java|Top page|
|MqttClientService.java|not important|
|MqttClientServiceEx.java|not important|
|MqttTraceCallback.java|not important|
|Preview.java|Capture camera image<br />Extract keypoints<br />Publish sensor data (Camera)|
|ProcessingActivity.java|Processing page|
|PublishSensorData.java|Get sensor data<br />Calculate orientation<br />Publish sensor data (IMU)|
|QuickToastTask.java|not important|
|Utils.java|Calculation functions|

<br /><br />

## 概要

[![](http://img.youtube.com/vi/IDZ5fxp_XdY/0.jpg)](https://www.youtube.com/watch?v=IDZ5fxp_XdY)

![](https://github.com/knagara/miscellaneous/blob/master/Overview.png)

- Androidアプリ（このページ）
- SLAMプログラム（ここを参照 -> [SLAMwithCameraIMUforPython](https://github.com/knagara/SLAMwithCameraIMUforPython)）
- MQTTブローカー（ここを参照 -> [MQTTについて詳しく知る](https://sango.shiguredo.jp/mqtt), [MQTTについてのまとめ](http://tdoc.info/blog/2014/01/27/mqtt.html), [MQTT Broker比較](http://acro-engineer.hatenablog.com/entry/2015/12/09/120500)）

## 使い方
1. OpenCV Managerをインストール → [Google Play](https://play.google.com/store/apps/details?id=org.opencv.engine)
2. アプリをダウンロード → [こちら](https://github.com/knagara/miscellaneous/blob/master/SLAMwithCameraIMU.apk?raw=true)
3. アプリをインストールして起動
4. Loginボタンを押下
5. フォームに記入
<br />
[推奨設定]
<br />
![](https://github.com/knagara/miscellaneous/blob/master/Login_.png)
<br />
6. Startボタンを押下
7. MQTTブローカーに対して、センサデータの送信が始まります。
~~~
[送信されるセンサデータの例（IMU）]
Topic: SLAM/input/all
Payload: 1452657360343&-0.039294902&-0.09044182&-0.06832063&-2.0395765&0.068644375&-3.0333138&-26.178856&-19.393106&-6.809244&0.026928991&0.018066406&-0.012341589
~~~
~~~
[Payloadの解説（IMU）]
Payload: タイムスタンプ&加速度[x]&加速度[y]&加速度[z]&角度[x]&角度[y]&角度[z]&地磁気[x]&地磁気[y]&地磁気[z]&角速度[x]&角速度[y]&角速度[z]
~~~
~~~
[送信されるセンサデータの例（画像センサ）]
Topic: SLAM/input/camera
Payload: 1452657480185$5:9:69.0:726.0:87.0:734.0&29:52:237.0:1177.0:249.0:1199.0&13:25:473.0:957.0:490.0:980.0&20:37:113.0:1013.0:126.0:1028.0&16:29:159.0:988.0:172.0:1002.0&
~~~
~~~
[Payloadの解説（画像センサ）]
Payload: タイムスタンプ$前フレームの特徴点のインデックス:現在フレームの特徴点のインデックス:前フレームの特徴点の画像座標[x]:前フレームの特徴点の画像座標[y]:現在フレームの特徴点の画像座標[x]:現在フレームの特徴点の画像座標[y]&...
~~~
8.画面をタップすると終了
 
## ファイル

|ファイル名|解説|
|:--|:--|
|Conf.java|not important|
|DMatchComparator.java|not important|
|LoginActivity.java|Login画面|
|MainActivity.java|TOP画面|
|MqttClientService.java|not important|
|MqttClientServiceEx.java|not important|
|MqttTraceCallback.java|not important|
|Preview.java|カメラ画像のキャプチャ<br />特徴点の抽出<br />センサデータの送信（画像）|
|ProcessingActivity.java|処理中画面|
|PublishSensorData.java|センサデータ取得<br />角度の算出<br />センサデータの送信（IMU）|
|QuickToastTask.java|not important|
|Utils.java|計算に使う関数|


