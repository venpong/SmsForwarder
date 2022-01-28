package com.idormy.sms.forwarder.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.idormy.sms.forwarder.MainActivity;
import com.idormy.sms.forwarder.MyApplication;
import com.idormy.sms.forwarder.R;
import com.idormy.sms.forwarder.utils.CommonUtil;
import com.idormy.sms.forwarder.utils.OSUtil;
import com.idormy.sms.forwarder.utils.PhoneUtils;
import com.idormy.sms.forwarder.utils.SettingUtil;

public class FrontService extends Service {
    private static final String TAG = "FrontService";
    private static final String CHANNEL_ONE_ID = "com.idormy.sms.forwarder";
    private static final String CHANNEL_ONE_NAME = "com.idormy.sms.forwarderName";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    @SuppressLint("IconColors")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        //是否同意隐私协议
        if (!MyApplication.allowPrivacyPolicy) return;

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_forwarder);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        if (OSUtil.isMIUI()) {
            builder.setContentTitle(getString(R.string.app_name));
        }
        builder.setContentText(getString(R.string.notification_content));
        Intent intent = new Intent(this, MainActivity.class);
        int flags = Build.VERSION.SDK_INT >= 30 ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        builder.setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

        Notification notification = builder.build();
        startForeground(1, notification);

        //检查权限是否获取
        //PackageManager pm = getPackageManager();
        //PhoneUtils.CheckPermission(pm, this);

        //Android8.1以下尝试启动主界面，以便动态获取权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        // 手机重启，未打开app时，主动获取SIM卡信息
        if (MyApplication.SimInfoList.isEmpty()) {
            PhoneUtils.init(this);
            MyApplication.SimInfoList = PhoneUtils.getSimMultiInfo();
        }

        if (SettingUtil.getSwitchEnableAppNotify() && CommonUtil.isNotificationListenerServiceEnabled(this)) {
            CommonUtil.toggleNotificationListenerService(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //是否同意隐私协议
        if (!MyApplication.allowPrivacyPolicy) return;

        //进行自动重启
        Intent intent = new Intent(FrontService.this, FrontService.class);
        //重新开启服务
        startService(intent);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY; //保证service不被杀死
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //获取当前电量
    @SuppressLint("ObsoleteSdkInt")
    private int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

}
