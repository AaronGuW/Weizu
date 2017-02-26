package com.demo.aaronapplication.weizu;

import android.content.Context;

import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * Created by Aaron on 2016/9/12.
 */
public class RongPushMessageReceiver extends PushMessageReceiver {
    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage pushNotificationMessage) {
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage pushNotificationMessage) {
        //TODO 如果是多条消息则需先开启mainActivity 再转到conversationList Fragment
        return false;
    }
}
