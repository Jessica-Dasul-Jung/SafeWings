package com.travelous.socialcomponents.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.travelous.socialcomponents.R;
import com.travelous.socialcomponents.main.main.MainActivity;

public class CallPolice extends AppCompatActivity {

    private ImageView bluebutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_police);

        bluebutton = (ImageView) findViewById(R.id.bluebutton);

//        // add PhoneStateListener
//        PhoneCallListener phoneListener = new PhoneCallListener();
//        TelephonyManager telephonyManager = (TelephonyManager) this
//                .getSystemService(Context.TELEPHONY_SERVICE);
//        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);


        bluebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setData(Uri.parse("tel:01030974325"));
//                startActivity(callIntent);
                dialContactPhone("112");

            }
        });
    }


    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

//    //monitor phone call activities
//    private class PhoneCallListener extends PhoneStateListener {
//
//        private boolean isPhoneCalling = false;
//
//        String LOG_TAG = "LOGGING 123";
//
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//
//            if (TelephonyManager.CALL_STATE_RINGING == state) {
//                // phone ringing
//                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
//            }
//
//            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
//                // active
//                Log.i(LOG_TAG, "OFFHOOK");
//
//                isPhoneCalling = true;
//            }
//
//            if (TelephonyManager.CALL_STATE_IDLE == state) {
//                // run when class initial and phone call ended,
//                // need detect flag from CALL_STATE_OFFHOOK
//                Log.i(LOG_TAG, "IDLE");
//
//                if (isPhoneCalling) {
//
//                    Log.i(LOG_TAG, "restart app");
//
//                    // restart app
//                    Intent i = getBaseContext().getPackageManager()
//                            .getLaunchIntentForPackage(
//                                    getBaseContext().getPackageName());
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(i);
//
//                    isPhoneCalling = false;
//                }
//
//            }
//        }
//    }
}
