package net.tolleri.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.tolleri.android.fragments.resultreceiverfragment.BuildConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment for receiving results from services. Instance state is retained.
 *
 * @author Timo Eskola
 */
public abstract class ResultReceiverFragment extends Fragment {

    public static final String TAG = "ResultReceiverFragment";

    private static final String EXTRA_CODE = "net.tolleri.android.broadcasts.extra.CODE";

    private static final String ACTION_UPDATE = "net.tolleri.android.broadcasts.actions.UPDATE";

    private final List<Result> results = new ArrayList<>();

    private final Set<Integer> updates = new HashSet<>();

    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Received result: code=" + resultCode + ", resumed=" + isResumed());
            results.add(new Result(resultCode, resultData));
            if (isResumed())
                deliverResults();
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int updateCode = intent.getIntExtra(EXTRA_CODE, -1);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Received broadcast: code=" + updateCode + ", resumed=" + isResumed());
            if (updateCode >= 0) {
                updates.add(updateCode);
                if (isResumed())
                    deliverUpdates();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_UPDATE));
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        deliverResults();
        deliverUpdates();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Broadcast the given update code to all interested BroadcastReceivers.
     *
     * @param context    The context
     * @param updateCode Arbitrary update code to deliver, as defined by you.
     */
    public static void sendUpdate(Context context, int updateCode) {
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(EXTRA_CODE, updateCode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Returns <code>ResultReceiver</code> instance.
     *
     * @return <code>ResultReceiver</code> instance
     */
    public ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    /**
     * Override to receive results delivered to this object. This method will be called in
     * resumed state.
     *
     * @param resultCode Arbitrary result code to deliver, as defined by you.
     * @param resultData Any additional data provided by you.
     */
    protected abstract void onReceiveResult(int resultCode, Bundle resultData);

    /**
     * Override to receive update broadcasts to this object. This method will be called in
     * resumed state.
     *
     * @param updateCode Arbitrary update code to deliver, as defined by you.
     */
    protected abstract void onUpdate(int updateCode);

    private void deliverResults() {
        for (Result result : results) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Delivering result: code=" + result.getCode());
            onReceiveResult(result.getCode(), result.getData());
        }
        results.clear();
    }

    private void deliverUpdates() {
        for (Integer updateCode : updates) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Delivering update: code=" + updateCode);
            onUpdate(updateCode);
        }
        updates.clear();
    }

    private static class Result {

        private int code;

        private Bundle data;

        private Result(int code, Bundle data) {
            this.code = code;
            this.data = data;
        }

        public int getCode() {
            return code;
        }

        public Bundle getData() {
            return data;
        }
    }
}
