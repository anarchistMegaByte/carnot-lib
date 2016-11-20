package com.carnot.libclasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.carnot.global.ConstantCode;

public class IncomingSms extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    try {
//                        if (senderNum.equals("TA-DOCOMO")) {
//                            Otp Sms = new Otp();
//                            Sms.recivedSms(message);
//                        }

//                        message = "Your OTP is JG39Y";
                        if (message.contains("Carnot login OTP:") || message.contains("Carnot password reset OTP:")) {
                            message = message.replace("Carnot login OTP:", "");
                            message = message.replace("Carnot password reset OTP:", "");
                            if (message != null) {
                                message = message.trim();
                                try {
                                    message = message.substring(0, message.indexOf('.'));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (message.length() <= 6) {
                                    //Sending broadcast
                                    Intent broadcast = new Intent(ConstantCode.BROADCAST_MESSAGE_RECEIVE);
                                    broadcast.putExtra(ConstantCode.OTP, message);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {

        }
    }

}