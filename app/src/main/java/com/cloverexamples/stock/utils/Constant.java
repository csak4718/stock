package com.cloverexamples.stock.utils;

import com.cloverexamples.stock.entry.ItemEntry;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by dewei.kung on 6/8/16.
 */
public class Constant {
    public static final String ERROR_NO_ACCOUNT = "No account";
    public static final String ERROR_INVALID_INTEGER = "Invalid integer";
    public static final String ERROR_NO_NETWORK = "Internet not found! Please Check connection.";

    public static final String PREF_DO_TRACK = "doTrack";
    public static final String PREF_DO_NOTIF = "doNotif";

    public static final String EXTRA_NOTIF_CONTENT = "notif content";
    public static final String EXTRA_NOTIF_TITLE = "notif title";
    public static final String EXTRA_INTENT_ACTION_TYPE = "intent action type";
    public static final String EXTRA_REFUND_LINE_ITEM_IDS = "com.clover.intent.extra.LINE_ITEM_IDS";
    public static final String EXTRA_OLD_LINE_ITEM_ID = "com.clover.intent.extra.OLD_LINE_ITEM_ID";
    public static final String EXTRA_NEW_LINE_ITEM_ID = "com.clover.intent.extra.NEW_LINE_ITEM_ID";

    public static final String INTENT_ACTION_PAYMENT = "com.clover.intent.action.PAYMENT_PROCESSED";
    public static final String INTENT_ACTION_REFUND = "com.clover.intent.action.REFUND_PROCESSED";
    public static final String INTENT_ACTION_EXCHANGED = "com.clover.intent.action.EXCHANGED_PROCESSED";

    public static final String JSON_QUANTITY = "quantity";
    public static final String JSON_ITEM_ID = "id";
    public static final String JSON_ITEM = "item";
    public static final String JSON_ELEMENTS = "elements";
    public static final String JSON_STOCK_COUNT = "stockCount";
    public static final String JSON_NAME = "name";


    public static final String HTTP_HEADER_KEY_AUTH = "Authorization";
    public static final String HTTP_HEADER_VAL_AUTH = "Bearer ";
    public static final String HTTP_HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_VAL_CONTENT_TYPE = "application/json";

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    public static final int MAIN_FRAGMENT_LOADER_ID = 1;

    public static final double QUANTITY_THRESHOLD = 4.0;

    public static final String TEXT_SAVE = "Save";
    public static final String TEXT_STOCK = "Stock";
    public static final String TEXT_SETTING = "Setting";
    public static final String TEXT_ALMOST_OUT_OF_STOCK = "Some item almost runs out";
    public static final String TEXT_CHECK_STOCK_REMINDER = "Remember to check the stock";

    /**
     * Performs alphabetical comparison of ItemEntry objects.
     */
    public static final Comparator<ItemEntry> ITEM_ALPHA_COMPARATOR = new Comparator<ItemEntry>() {
        Collator mCollator = Collator.getInstance();

        @Override
        public int compare(ItemEntry object1, ItemEntry object2) {
            return mCollator.compare(object1.getItemName(), object2.getItemName());
        }
    };
}
