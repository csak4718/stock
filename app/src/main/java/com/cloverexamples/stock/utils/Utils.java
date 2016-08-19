package com.cloverexamples.stock.utils;

import android.app.Activity;
import android.content.Intent;

import com.cloverexamples.stock.activity.SettingActivity;


/**
 * Created by dewei.kung on 6/13/16.
 */
public class Utils {
    public static void gotoSettingActivity(Activity activity) {
        Intent it = new Intent(activity, SettingActivity.class);
        activity.startActivity(it);
    }
}
