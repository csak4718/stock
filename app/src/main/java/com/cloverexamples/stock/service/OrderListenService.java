package com.cloverexamples.stock.service;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;

import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.order.IOnOrderUpdateListener2;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Refund;
import com.cloverexamples.stock.R;
import com.cloverexamples.stock.activity.MainActivity;
import com.cloverexamples.stock.utils.Constant;

import java.util.List;

/**
 * Created by dewei.kung on 6/8/16.
 */
//TODO: delete
public class OrderListenService extends Service implements OrderConnector.OnOrderUpdateListener2 {
    private static final String TAG = OrderListenService.class.getSimpleName();
    private OrderConnector mOrderConnector;
    private Account mAccount;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        setupAccount();

        // Create and Connect to the OrderConnector
        connect();

        mOrderConnector.addOnOrderChangedListener(this);
        return super.onStartCommand(intent, flags, startId);
//        return START_NOT_STICKY;
//        return START_STICKY;
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
        }
    }

    private void disconnect() {
        if (mOrderConnector != null) {
            mOrderConnector.disconnect();
            mOrderConnector = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

//        Release resource
//        disconnect();
//        mAccount = null;
//        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onPaymentProcessed(String orderId, String paymentId) {
        Log.d(TAG, "onPaymentProcessed");
//        Toast.makeText(this, "onPaymentProcessed", Toast.LENGTH_SHORT).show();
//        Toast.makeText(this.getApplicationContext(), "onPaymentProcessed", Toast.LENGTH_SHORT).show();
        sendNotification(this, orderId);
//        sendNotification(getApplicationContext(), orderId);

//        broadcast(orderId);
    }

    private void broadcast(String orderId) {
        Intent it = new Intent("stock.service.broadcast");
        it.putExtra(Constant.EXTRA_NOTIF_CONTENT, orderId);
        it.putExtra(Constant.EXTRA_NOTIF_TITLE, "Stock Notification");
        sendBroadcast(it);
    }

    private void sendNotification(Context context, String orderId) {
        if (context == null) {
            Log.d(TAG, "context is null");
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notif_icon)
                        .setContentTitle("Stock notification")
                        .setContentText(orderId);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onOrderUpdated(String orderId, boolean selfChange) {

    }

    @Override
    public void onOrderCreated(String orderId) {

    }

    @Override
    public void onOrderDeleted(String orderId) {

    }

    @Override
    public void onOrderDiscountAdded(String orderId, String discountId) {

    }

    @Override
    public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {

    }

    @Override
    public void onLineItemsAdded(String orderId, List<String> lineItemIds) {

    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {

    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {

    }

    @Override
    public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {

    }

    @Override
    public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {

    }

    @Override
    public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {
        Log.d(TAG, "onLineItemExchanged");
    }

    @Override
    public void onRefundProcessed(final String orderId, final String refundId) {
        Log.d(TAG, "onRefundProcessed");
//        new AsyncTask<Void, Void, Void> () {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    Log.d(TAG, "doInBackground");
//                    Order order = mOrderConnector.getOrder(orderId);
//                    List<LineItem> lineItems = order.getLineItems();
//                    for (LineItem lineItem: lineItems) {
//                        Log.d(TAG, lineItem.getName());
//                    }
//
//
//                    List<Refund> refunds = order.getRefunds();
//                    List<Reference> items = refunds.get(0).getLineItems(); // item id doesn't match
//
//
//                    Order refundOrder = mOrderConnector.getOrder(refundId); // return null
//                    List<LineItem> refundLineItems = refundOrder.getLineItems();
//                    for (LineItem lineItem: refundLineItems) {
//                        Log.d(TAG, lineItem.getName());
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                } catch (ClientException e) {
//                    e.printStackTrace();
//                } catch (ServiceException e) {
//                    e.printStackTrace();
//                } catch (BindingException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute();
    }

    @Override
    public void onCreditProcessed(String orderId, String creditId) {
        Log.d(TAG, "onCreditProcessed");
    }
}
