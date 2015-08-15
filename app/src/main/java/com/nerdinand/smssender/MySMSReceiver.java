package com.nerdinand.smssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import java.io.IOException;

public class MySMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        SmsMessage shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);

        SMSMessage message = new SMSMessage(shortMessage.getOriginatingAddress(), shortMessage.getDisplayMessageBody());
        try {
            String json = message.toJSON();
            MainActivity.getCommunicationThread().write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
