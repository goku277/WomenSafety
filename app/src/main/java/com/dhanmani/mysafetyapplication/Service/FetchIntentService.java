package com.dhanmani.mysafetyapplication.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

public class FetchIntentService extends IntentService {

    private ResultReceiver resultReceiver;

    public FetchIntentService() {
        super("FetchIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}