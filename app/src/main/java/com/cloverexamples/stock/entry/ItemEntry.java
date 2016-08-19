package com.cloverexamples.stock.entry;

import com.cloverexamples.stock.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dewei.kung on 6/28/16.
 */
public class ItemEntry {
    private String id;
    private String mItemName;
    private int quantity;

    public ItemEntry(JSONObject item) {
        try {
            id = item.getString(Constant.JSON_ITEM_ID);
            mItemName = item.getString(Constant.JSON_NAME);
            quantity = item.has(Constant.JSON_STOCK_COUNT) ? item.getInt(Constant.JSON_STOCK_COUNT) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getItemName() {
        return mItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int qty) {
        quantity = qty;
    }
}
