package com.cloverexamples.stock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.clover.sdk.v1.Intents;
import com.cloverexamples.stock.service.StockAPIService;
import com.cloverexamples.stock.service.StockService;
import com.cloverexamples.stock.utils.Constant;

import java.util.List;

/**
 * Created by dewei.kung on 6/10/16.
 */
public class CloverBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = CloverBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (mPref.getBoolean(Constant.PREF_DO_TRACK, true)) {
            Intent it = new Intent(context, StockAPIService.class);
            it.putExtra(Constant.EXTRA_INTENT_ACTION_TYPE, intent.getAction());

            switch (intent.getAction()) {
                case Constant.INTENT_ACTION_PAYMENT:
                    it.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID));
                    break;

                case Constant.INTENT_ACTION_REFUND:
                    it.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID));
                    it.putStringArrayListExtra(Constant.EXTRA_REFUND_LINE_ITEM_IDS, intent.getStringArrayListExtra(Constant.EXTRA_REFUND_LINE_ITEM_IDS));
                    break;

                case Constant.INTENT_ACTION_EXCHANGED:
                    it.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID));
                    it.putExtra(Constant.EXTRA_OLD_LINE_ITEM_ID, intent.getStringExtra(Constant.EXTRA_OLD_LINE_ITEM_ID));
                    it.putExtra(Constant.EXTRA_NEW_LINE_ITEM_ID, intent.getStringExtra(Constant.EXTRA_NEW_LINE_ITEM_ID));
                    break;
                default:
                    return;
            }
            context.startService(it);
        }
    }
}
