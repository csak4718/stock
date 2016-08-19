package com.cloverexamples.stock.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.cloverexamples.stock.R;
import com.cloverexamples.stock.utils.Constant;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    @Bind (R.id.switch_track) Switch switchTrack;
    @Bind (R.id.switch_notification) Switch switchNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        setupActionBar();

        switchTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();
                editor.putBoolean(Constant.PREF_DO_TRACK, isChecked);
                editor.commit();
            }
        });
        switchNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();
                editor.putBoolean(Constant.PREF_DO_NOTIF, isChecked);
                editor.commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        switchTrack.setChecked(mPref.getBoolean(Constant.PREF_DO_TRACK, true));
        switchNotif.setChecked(mPref.getBoolean(Constant.PREF_DO_NOTIF, true));
    }

    private void setupActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.setting_activity_action_bar);
        View actionBarView = mActionBar.getCustomView();
        TextView txvTitle = (TextView) actionBarView.findViewById(R.id.txv_action_bar_title);
        txvTitle.setText(Constant.TEXT_SETTING);
    }
}
