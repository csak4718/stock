package com.cloverexamples.stock.loader;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.cloverexamples.stock.entry.ItemEntry;
import com.cloverexamples.stock.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by dewei.kung on 6/29/16.
 */
public class ItemListLoader extends AsyncTaskLoader<List<ItemEntry>> {
    private static final String TAG = ItemListLoader.class.getSimpleName();
    private static final boolean DEBUG = true;

    // Hold a reference to the Loader's data here.
    private List<ItemEntry> mItemEntries;

    private Account mAccount;
    private static CloverAuth.AuthResult mCloverAuth;
    private String baseUrl = "https://sandbox.dev.clover.com/v3/merchants/";
    private String itemsUrl = "/items";

    public ItemListLoader(Context context) {
        // Loaders may be used across multiple Activities (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(context);
        setupAccount();
    }

    /****************************************************/
    /** (1) A task that performs the asynchronous load **/
    /****************************************************/

    /**
     * This method is called on a background thread and generates a List of ItemEntry objects.
     */
    @Override
    public List<ItemEntry> loadInBackground() {
        if (DEBUG) {
            Log.i(TAG, "+++ loadInBackground() called! +++");
        }

        final List<ItemEntry> itemEntries = new ArrayList<>();
        try {
            // call CloverAuth.authenticate to get the auth token
            mCloverAuth = CloverAuth.authenticate(getContext(), mAccount);

            final String url = baseUrl + mCloverAuth.merchantId + itemsUrl;
            SyncHttpClient getClient = new SyncHttpClient();
            getClient.addHeader(Constant.HTTP_HEADER_KEY_AUTH, Constant.HTTP_HEADER_VAL_AUTH + mCloverAuth.authToken);
            getClient.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                    Log.d(TAG, "Http GET onSuccess");
                    try {
                        JSONArray items = object.getJSONArray(Constant.JSON_ELEMENTS);
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            itemEntries.add(new ItemEntry(item));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {

                }
            });
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sort the list.
        Collections.sort(itemEntries, Constant.ITEM_ALPHA_COMPARATOR);

        return itemEntries;
    }

    /*******************************************/
    /** (2) Deliver the results to the client **/
    /*******************************************/

    /**
     * Called when there is new data to deliver to the client. The superclass will
     * deliver it to the registered listener (i.e. the LoaderManager), which will
     * forward the results to the client through a call to onLoadFinished.
     */
    @Override
    public void deliverResult(List<ItemEntry> itemEntries) {
        if (isReset()) {
            if (DEBUG) {
                Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            }
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (itemEntries != null) {
                releaseResources(itemEntries);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<ItemEntry> oldItemEntries = mItemEntries;
        mItemEntries = itemEntries;

        if (isStarted()) {
            if (DEBUG) {
                Log.i(TAG, "+++ Delivering results to the LoaderManager for the MainActivity to display! +++");
            }
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(itemEntries);
        }

        // Invalidate the old data as we don't need it anymore.
        if (oldItemEntries != null && oldItemEntries != itemEntries) {
            if (DEBUG) {
                Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            }
            releaseResources(oldItemEntries);
        }
    }

    /*********************************************************/
    /** (3) Implement the Loader state-dependent behavior **/
    /*********************************************************/

    @Override
    protected void onStartLoading() {
        if (DEBUG) {
            Log.i(TAG, "+++ onStartLoading() called! +++");
        }

        if (mItemEntries == null) {
            // If the current data is null... then we should make it non-null.
            if (DEBUG) {
                Log.i(TAG, "+++ The current data is data is null... so force load! +++");
            }
            forceLoad();
        } else {
            // Deliver any previously loaded data immediately.
            if (DEBUG) {
                Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            }
            deliverResult(mItemEntries);
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) {
            Log.i(TAG, "+++ onStopLoading() called! +++");
        }

        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        if (mItemEntries != null) {
            releaseResources(mItemEntries);
            mItemEntries = null;
        }
    }

    @Override
    protected void onReset() {
        if (DEBUG) {
            Log.i(TAG, "+++ onReset() called! +++");
        }

        // Ensure the loader is stopped.
        onStopLoading();
    }

    @Override
    public void onCanceled(List<ItemEntry> itemEntries) {
        if (DEBUG) {
            Log.i(TAG, "+++ onCanceled() called! +++");
        }

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(itemEntries);

        // The load has been canceled, so we should release the resources
        // associated with 'mItemEntries'.
        releaseResources(itemEntries);
    }

    @Override
    public void forceLoad() {
        if (DEBUG) {
            Log.i(TAG, "+++ forceLoad() called! +++");
        }
        super.forceLoad();
    }

    /**
     * Helper method to take care of releasing resources associated with an
     * actively loaded data set.
     */
    private void releaseResources(List<ItemEntry> itemEntries) {
        // For a simple List, there is nothing to do. For something like a Cursor,
        // we would close it in this method. All resources associated with the
        // Loader should be released here.
    }

    /**************************/
    /** (4) Everything else  **/
    /**************************/

    private void setupAccount() {
        if (mAccount == null) {
            mAccount = CloverAccount.getAccount(getContext());

            if (mAccount == null) {
                Toast.makeText(getContext(), Constant.ERROR_NO_ACCOUNT, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static CloverAuth.AuthResult getAuthResult() {
        return mCloverAuth;
    }
}
