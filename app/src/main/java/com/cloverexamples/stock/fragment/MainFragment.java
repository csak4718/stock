package com.cloverexamples.stock.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloverexamples.stock.R;
import com.cloverexamples.stock.activity.MainActivity;
import com.cloverexamples.stock.adapter.ItemListAdapter;
import com.cloverexamples.stock.entry.ItemEntry;
import com.cloverexamples.stock.loader.ItemListLoader;
import com.cloverexamples.stock.utils.Constant;
import com.cloverexamples.stock.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dewei.kung on 7/6/16.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ItemEntry>> {
    private static final String TAG = MainFragment.class.getSimpleName();
    private View mView;
    private ItemListAdapter mAdapter;
    private ListView mListView;
    private ActionBar mActionBar;
    private ImageButton btnSetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_main, container, false);
        setupAdapter();
        setupActionBar();

        return mView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (isNetworkAvailable()) {
            // Initialize a Loader with id 'Constant.MAIN_ACTIVITY_LOADER_ID'. If the Loader with this id already
            // exists, then the LoaderManager will reuse the existing Loader.
            getActivity().getSupportLoaderManager().initLoader(Constant.MAIN_FRAGMENT_LOADER_ID, null, this);
        } else {
            Toast.makeText(getActivity(), Constant.ERROR_NO_NETWORK, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAdapter() {
        mListView = (ListView) mView.findViewById(R.id.list_view);
        mAdapter = new ItemListAdapter((MainActivity) getActivity(), new ArrayList<ItemEntry>());
        mListView.setAdapter(mAdapter);
    }

    private void setupActionBar() {
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.main_action_bar);
        View actionBarView = mActionBar.getCustomView();
        TextView txvTitle = (TextView) actionBarView.findViewById(R.id.txv_action_bar_title);
        txvTitle.setText(Constant.TEXT_STOCK);

        btnSetting = (ImageButton) actionBarView.findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.gotoSettingActivity(getActivity());
            }
        });
    }

    @Override
    public Loader<List<ItemEntry>> onCreateLoader(int id, Bundle args) {
        return new ItemListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemEntry>> loader, List<ItemEntry> data) {
        mAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<ItemEntry>> loader) {
        mAdapter.setData(null);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
