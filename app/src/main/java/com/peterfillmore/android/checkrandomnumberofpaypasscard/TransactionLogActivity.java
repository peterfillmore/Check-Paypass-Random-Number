package com.peterfillmore.android.checkrandomnumberofpaypasscard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.ArrayList;
import android.util.Log;

public class TransactionLogActivity extends Activity {
    static public String TAG = "TransactionLogActivity";
    public static TextView txtLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_log);
        txtLog = (TextView)findViewById(R.id.textViewLogText);
        Intent i = getIntent();
        ArrayList<String> something = i.getStringArrayListExtra("LOG_DATA");
        for(String s : something){
            Log.i(TAG, s);
            txtLog.append(Html.fromHtml("<br />" + s + "<br />"));
        }
        //txtLog.setText(something);
    }
}
