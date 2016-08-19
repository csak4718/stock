package com.cloverexamples.stock.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloverexamples.stock.R;
import com.cloverexamples.stock.activity.MainActivity;
import com.cloverexamples.stock.entry.ItemEntry;
import com.cloverexamples.stock.loader.ItemListLoader;
import com.cloverexamples.stock.utils.Constant;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by dewei.kung on 6/28/16.
 */
public class ItemListAdapter extends BaseAdapter {
    private static final String TAG = ItemListAdapter.class.getSimpleName();
    private MainActivity mMainActivity;
    private List<ItemEntry> mItemEntries;
    private String baseUrl = "https://sandbox.dev.clover.com/v3/merchants/";
    private String itemStockUrl = "/item_stocks/";

    public ItemListAdapter(MainActivity activity, List<ItemEntry> itemEntries) {
        mMainActivity = activity;
        mItemEntries = itemEntries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mMainActivity);
            convertView = inflater.inflate(R.layout.item_entry, parent, false);
        }

        final ItemEntry itemEntry = mItemEntries.get(position);
        TextView nameTxv = (TextView) convertView.findViewById(R.id.txv_item_name);
        nameTxv.setText(itemEntry.getItemName());

        TextView quantityTxv = (TextView) convertView.findViewById(R.id.txv_item_quantity);
        quantityTxv.setText(String.valueOf(itemEntry.getQuantity()));
        if (itemEntry.getQuantity() < Constant.QUANTITY_THRESHOLD) {
            quantityTxv.setTextColor(mMainActivity.getResources().getColor(R.color.red));
        } else {
            quantityTxv.setTextColor(mMainActivity.getResources().getColor(R.color.gray));
        }

        Button editBtn = (Button) convertView.findViewById(R.id.btn_edit);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog(itemEntry);
            }
        });

        return convertView;
    }

    private void showEditDialog(final ItemEntry itemEntry){
        //Create a new Dialog Box
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
        builder.setTitle("Edit " + itemEntry.getItemName() + " Quantity");

        // The Linear Layout that holds EditText
        LinearLayout wrapper = new LinearLayout(mMainActivity);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        final EditText edtQuantity = new EditText(mMainActivity);
        edtQuantity.setText(String.valueOf(itemEntry.getQuantity()));

        wrapper.addView(edtQuantity);

        //Add the linear layout to the Alert Dialog
        builder.setView(wrapper);

        final int oldQty = itemEntry.getQuantity();
        builder.setPositiveButton(Constant.TEXT_SAVE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final int newQty = Integer.parseInt(edtQuantity.getText().toString());
                    if (newQty != oldQty) {
                        String url = baseUrl + ItemListLoader.getAuthResult().merchantId + itemStockUrl + itemEntry.getId();

                        AsyncHttpClient postClient = new AsyncHttpClient();
                        postClient.addHeader(Constant.HTTP_HEADER_KEY_AUTH, Constant.HTTP_HEADER_VAL_AUTH + ItemListLoader.getAuthResult().authToken);

                        JSONObject itemJson = new JSONObject();
                        itemJson.put(Constant.JSON_ITEM_ID, itemEntry.getId());

                        JSONObject postJson = new JSONObject();
                        postJson.put(Constant.JSON_ITEM, itemJson);
                        postJson.put(Constant.JSON_QUANTITY, newQty);

                        StringEntity entity = new StringEntity(postJson.toString());
                        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, Constant.HTTP_HEADER_VAL_CONTENT_TYPE));

                        postClient.post(mMainActivity, url, entity, Constant.HTTP_HEADER_VAL_CONTENT_TYPE, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Log.d(TAG, "Http POST onSuccess: " + itemEntry.getId());

                                itemEntry.setQuantity(newQty);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {

                            }
                        });
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(mMainActivity, Constant.ERROR_INVALID_INTEGER, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create and show the dialog box
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setData(List<ItemEntry> data) {
        mItemEntries.clear();
        if (data != null) {
            mItemEntries.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemEntries.size();
    }

    @Override
    public ItemEntry getItem(int position) {
        return mItemEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
