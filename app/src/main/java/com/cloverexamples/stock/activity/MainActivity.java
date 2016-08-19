package com.cloverexamples.stock.activity;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.cloverexamples.stock.R;
import com.cloverexamples.stock.adapter.ItemListAdapter;
import com.cloverexamples.stock.entry.ItemEntry;
import com.cloverexamples.stock.fragment.MainFragment;
import com.cloverexamples.stock.loader.ItemListLoader;
import com.cloverexamples.stock.service.OrderListenService;
import com.cloverexamples.stock.utils.Constant;
import com.cloverexamples.stock.utils.Utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = new MainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_content, mainFragment);
        transaction.commit();

        //TODO do experiments
//        Intent it = new Intent(this, OrderListenService.class);
//        startService(it);
//
//        String a = Intents.EXTRA_REFUND;
//        String b = Intents.ACTION_REFUND;
        
    }
}
