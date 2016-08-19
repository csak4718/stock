package com.cloverexamples.stock.service;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.ItemStock;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Refund;
import com.cloverexamples.stock.R;
import com.cloverexamples.stock.activity.MainActivity;
import com.cloverexamples.stock.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dewei.kung on 6/13/16.
 */
public class StockAPIService extends Service {
    private static final String TAG = StockAPIService.class.getSimpleName();
    private OrderConnector mOrderConnector;
    private Account mAccount;
    private InventoryConnector mInventoryConnector;
    private MerchantConnector mMerchantConnector;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        setupAccount();
        connect();

        switch (intent.getStringExtra(Constant.EXTRA_INTENT_ACTION_TYPE)) {
            case Constant.INTENT_ACTION_PAYMENT:
                new PaymentAsyncTask().execute(intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID));
                break;

            case Constant.INTENT_ACTION_REFUND:
                RefundAsyncTaskParams refundParams = new RefundAsyncTaskParams(intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID), intent.getStringArrayListExtra(Constant.EXTRA_REFUND_LINE_ITEM_IDS));
                new RefundAsyncTask().execute(refundParams);
                break;

            case Constant.INTENT_ACTION_EXCHANGED:
                ExchangedAsyncTaskParams exchangedParams = new ExchangedAsyncTaskParams(intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID), intent.getStringExtra(Constant.EXTRA_OLD_LINE_ITEM_ID), intent.getStringExtra(Constant.EXTRA_NEW_LINE_ITEM_ID));
                new ExchangedAsyncTask().execute(exchangedParams);
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class PaymentAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... orderId) {
            try {
//                mMerchantConnector.setTrackStock(true); // don't need

                final Map<String, Integer> idToCount = new HashMap<>();
                Order order = mOrderConnector.getOrder(orderId[0]);
                List<LineItem> lineItems = order.getLineItems();
                for (LineItem lineItem: lineItems) {
                    String itemId = lineItem.getItem().getId();
                    if (idToCount.containsKey(itemId)) {
                        idToCount.put(itemId, idToCount.get(itemId) + 1);
                    } else {
                        idToCount.put(itemId, 1);
                    }
                }

                boolean someItemAlmostRunOut = false;
                for (String itemId: idToCount.keySet()) {
                    ItemStock itemStock = mInventoryConnector.getItem(itemId).getItemStock();
//                    TODO bug: ItemStock is null, StockCount is null as well

                    if (itemStock.hasQuantity()) {
                        itemStock.setQuantity(itemStock.getQuantity() - idToCount.get(itemId));
                    } else {
                        itemStock.setQuantity(-1.0 * idToCount.get(itemId));
                    }

                    Log.d(TAG, "after decrement, newQty: " + String.valueOf(itemStock.getQuantity()));
                    if (itemStock.getQuantity() < Constant.QUANTITY_THRESHOLD) {
                        someItemAlmostRunOut = true;
                    }
                }
                if (someItemAlmostRunOut) {
                    SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(StockAPIService.this);
                    if (mPref.getBoolean(Constant.PREF_DO_NOTIF, true)) {
                        sendNotification();
                    }
                }

// trivial codes
//                for (LineItem lineItem: lineItems) {
//                    String itemId = lineItem.getItem().getId();
//                    Item item = mInventoryConnector.getItem(itemId);
//                    int orderedQty = lineItem.getUnitQty();
//                    Log.d(TAG, "orderedQty: " + String.valueOf(orderedQty));
//
//                    ItemStock itemStock = item.getItemStock();
//                    if (itemStock.hasQuantity()) {
//                        Log.d(TAG, "new Qty: " + String.valueOf(itemStock.getQuantity() - orderedQty));
//                        itemStock.setQuantity(itemStock.getQuantity() - orderedQty);
//                    } else {
//                        itemStock.setQuantity(-1.0 * orderedQty);
//                    }
//
//
//                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finishService();
        }
    }

    private class RefundAsyncTask extends AsyncTask<RefundAsyncTaskParams, Void, Void> {

        @Override
        protected Void doInBackground(RefundAsyncTaskParams... params) {
            String orderId = params[0].orderId;
            List<String> refundLineItemIds = params[0].refundLineItemIds;
            Map<String, Integer> itemIdToCount = new HashMap<>();

            try {
                List<LineItem> orderLineItems = mOrderConnector.getOrder(orderId).getLineItems();
                Map<String, LineItem> lineItemIdToLineItem = new HashMap<>();
                for (LineItem orderLineItem: orderLineItems) {
                    lineItemIdToLineItem.put(orderLineItem.getId(), orderLineItem);
                }

                for (String refundLineItemId: refundLineItemIds) {
                    String refundItemId = lineItemIdToLineItem.get(refundLineItemId).getItem().getId();

                    if (itemIdToCount.containsKey(refundItemId)) {
                        itemIdToCount.put(refundItemId, itemIdToCount.get(refundItemId) + 1);
                    } else {
                        itemIdToCount.put(refundItemId, 1);
                    }
                }
                for (String itemId: itemIdToCount.keySet()) {
                    ItemStock itemStock = mInventoryConnector.getItem(itemId).getItemStock();
//                    TODO bug: ItemStock is null, StockCount is null as well

                    if (itemStock.hasQuantity()) {
                        itemStock.setQuantity(itemStock.getQuantity() + itemIdToCount.get(itemId));
                    } else {
                        itemStock.setQuantity(1.0 * itemIdToCount.get(itemId));
                    }

                    Log.d(TAG, "after increment, newQty: " + String.valueOf(itemStock.getQuantity()));
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finishService();
        }
    }

    private class RefundAsyncTaskParams {
        String orderId;
        List<String> refundLineItemIds;
        RefundAsyncTaskParams(String orderId, List<String> refundLineItemIds) {
            this.orderId = orderId;
            this.refundLineItemIds = refundLineItemIds;
        }
    }

    private class ExchangedAsyncTask extends AsyncTask<ExchangedAsyncTaskParams, Void, Void> {

        @Override
        protected Void doInBackground(ExchangedAsyncTaskParams... params) {
            String orderId = params[0].orderId;
            String oldLineItemId = params[0].oldLineItemId;
            String newLineItemId = params[0].newLineItemId;

            List<LineItem> orderLineItems = null;
            try {
                orderLineItems = mOrderConnector.getOrder(orderId).getLineItems();
                Map<String, LineItem> lineItemIdToLineItem = new HashMap<>();
                for (LineItem orderLineItem: orderLineItems) {
                    lineItemIdToLineItem.put(orderLineItem.getId(), orderLineItem);
                }

                String oldItemId = lineItemIdToLineItem.get(oldLineItemId).getItem().getId();
                String newItemId = lineItemIdToLineItem.get(newLineItemId).getItem().getId();
                if (!oldItemId.equals(newItemId)) {
                    ItemStock oldItemStock = mInventoryConnector.getItem(oldItemId).getItemStock();
//                    TODO bug: ItemStock is null, StockCount is null as well
                    if (oldItemStock.hasQuantity()) {
                        oldItemStock.setQuantity(oldItemStock.getQuantity() + 1);
                    } else {
                        oldItemStock.setQuantity(1.0);
                    }

                    ItemStock newItemStock = mInventoryConnector.getItem(newItemId).getItemStock();
//                    TODO bug: ItemStock is null, StockCount is null as well
                    if (newItemStock.hasQuantity()) {
                        newItemStock.setQuantity(newItemStock.getQuantity() - 1);
                    } else {
                        newItemStock.setQuantity(-1.0);
                    }

                    if (newItemStock.getQuantity() < Constant.QUANTITY_THRESHOLD) {
                        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(StockAPIService.this);
                        if (mPref.getBoolean(Constant.PREF_DO_NOTIF, true)) {
                            sendNotification();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finishService();
        }
    }

    private class ExchangedAsyncTaskParams {
        String orderId;
        String oldLineItemId;
        String newLineItemId;
        ExchangedAsyncTaskParams(String orderId, String oldLineItemId, String newLineItemId) {
            this.orderId = orderId;
            this.oldLineItemId = oldLineItemId;
            this.newLineItemId = newLineItemId;
        }
    }

    private void setupAccount() {
        if (mAccount == null) {
            Log.d(TAG, "mAccount is null. call getAccount");
            mAccount = CloverAccount.getAccount(this);

            if (mAccount == null) {
                Toast.makeText(this, Constant.ERROR_NO_ACCOUNT, Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        }
    }

    private void connect() {
        disconnect();
        if (mAccount != null) {
            mOrderConnector = new OrderConnector(this, mAccount, null);
            mOrderConnector.connect();
            mInventoryConnector = new InventoryConnector(this, mAccount, null);
            mInventoryConnector.connect();
            mMerchantConnector = new MerchantConnector(this, mAccount, null);
            mMerchantConnector.connect();
        }
    }

    private void disconnect() {
        if (mOrderConnector != null) {
            mOrderConnector.disconnect();
            mOrderConnector = null;
        }
        if (mInventoryConnector != null) {
            mInventoryConnector.disconnect();
            mInventoryConnector = null;
        }
        if (mMerchantConnector != null) {
            mMerchantConnector.disconnect();
            mMerchantConnector = null;
        }
    }

    private void finishService() {
        Log.d(TAG, "finishService");
        disconnect();
        mAccount = null;
        stopSelf();
    }

    private void sendNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notif_icon)
                        .setContentTitle(Constant.TEXT_ALMOST_OUT_OF_STOCK)
                        .setContentText(Constant.TEXT_CHECK_STOCK_REMINDER);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
