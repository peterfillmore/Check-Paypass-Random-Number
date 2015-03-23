package com.peterfillmore.android.checkrandomnumberofpaypasscard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.nfc.tech.IsoDep;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.peterfillmore.android.checkrandomnumberofpaypasscard.BERTLV.*;

import org.w3c.dom.Text;

public class MainActivity extends Activity {
    static public String TAG = "CheckPaypassMain";
    private NfcAdapter mNfcAdapter;
    static public byte[] SELECT_PPSE = {(byte)0x00,(byte)0xA4,(byte)0x04,(byte)0x00,(byte)0x0E,(byte)0x32,(byte)0x50,(byte)0x41,(byte)0x59,(byte)0x2E,(byte)0x53,(byte)0x59,(byte)0x53,(byte)0x2E,(byte)0x44,(byte)0x44,(byte)0x46,(byte)0x30,(byte)0x31,(byte)0x00};
    TextView txtTrack1UNLength;
    TextView txtTrack2UNLength;
    TextView txtNumTransactions;
    TextView txtTimeToClone;
    ArrayList<String> logdata;
    Intent transactionlogintent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        txtTrack1UNLength = (TextView) findViewById(R.id.textViewTrack1Results);
        txtTrack2UNLength = (TextView) findViewById(R.id.textViewTrack2Results);
        txtNumTransactions = (TextView) findViewById(R.id.textViewNumTransactions);
        txtTimeToClone = (TextView) findViewById(R.id.textViewTimeToClone);
        logdata = new ArrayList<String>();

        transactionlogintent = new Intent(this, TransactionLogActivity.class);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_log) {
            startActivity(transactionlogintent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }
    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String response;
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //updateLogText("Card Detected", Color.GREEN);
            String[] techList = tag.getTechList();
            String[] searchedTech = {NfcA.class.getName(),NfcB.class.getName()};
            for (String tech : techList) {
                if ((searchedTech[0].equals(tech)) || (searchedTech[1].equals(tech)) ) { //NFCA,NFCB
                    //new Thread(new Runnable() {
                        //byte[] SELECT_MASTERCARD_AID = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x10, (byte) 0x10, (byte) 0x00};
                        byte[] responseBytes = {};
                        String responseString;
                        String cardAID;
                        //public void run() {
                            IsoDep currenttag = IsoDep.get(tag); //use ISODEP for ISO14443-4 APDUs
                            currenttag.setTimeout(5000);
                            try {
                                currenttag.connect();
                                try{
                                    //updateLogText("SENDING SELECT_PPSE", Color.RED);
                                    //updateLogText(ByteArrayToHexString(SELECT_PPSE), Color.RED);
                                    responseBytes = currenttag.transceive(SELECT_PPSE);
                                    logFromTerminal(BERTLV.ByteArrayToHexString(SELECT_PPSE));
                                    //logdata.add(BERTLV.ByteArrayToHexString(SELECT_PPSE));
                                    logToTerminal(BERTLV.ByteArrayToHexString(responseBytes));
                                    //transactionlogintent.putExtra("LOG_TEXT", BERTLV.ByteArrayToHexString(SELECT_PPSE));
                                    //TransactionLogActivity.txtLog.setText(BERTLV.ByteArrayToHexString(SELECT_PPSE));
                                }
                                catch (IOException e) {
                                    Log.e(TAG, "Transceive Error ", e);
                                }
                                responseString = BERTLV.ByteArrayToHexString(responseBytes);
                                byte[] detected6FTemplateValue = BERTLV.findTLVtag(responseBytes, new byte[]{(byte) 0x6F, (byte) 0x00});
                                Log.i(TAG, "6F=" + BERTLV.ByteArrayToHexString(detected6FTemplateValue));
                                byte[] detectedA5TemplateValue = BERTLV.findTLVtag(detected6FTemplateValue, new byte[]{(byte) 0xA5, (byte) 0x00});
                                Log.i(TAG, "A5=" + BERTLV.ByteArrayToHexString(detectedA5TemplateValue));
                                byte[] detectedBF0CValue = BERTLV.findTLVtag(detectedA5TemplateValue, new byte[]{(byte) 0xBF, (byte) 0x0C});
                                Log.i(TAG, "BF0C=" + BERTLV.ByteArrayToHexString(detectedBF0CValue));
                                byte[] detected61Value = BERTLV.findTLVtag(detectedBF0CValue, new byte[]{(byte) 0x61, (byte) 0x00});
                                Log.i(TAG, "61=" + BERTLV.ByteArrayToHexString(detected61Value));
                                byte[] AIDvalue = BERTLV.findTLVtag(detected61Value, new byte[]{(byte) 0x4F, (byte) 0x00});
                                cardAID = BERTLV.ByteArrayToHexString(AIDvalue);
                                Log.i(TAG, "SELECT PPSE RESPONSE=" + responseString);
                                //updateLogText("SELECT PPSE RESPONSE=" + responseString, Color.BLUE);
                                if (cardAID.contains("A000000004")) {
                                    Log.i(TAG, "Mastercard AID detected");
                                    //updateLogText("Mastercard Detected", Color.GREEN);
                                    processMastercard(currenttag, responseString);
                                    //currenttag.close();
                                }
                                //VISA
                                else if (cardAID.contains("A000000003")) {
                                    Log.i(TAG, "VISA AID detected");
                                    //Toast.makeText(super.this, "Detected a VISA card - please use a Mastercard Paypass", Toast.LENGTH_LONG).show();
                                    //finish();
                                    //return;
                                    //updateLogText("VISA Detected", Color.GREEN);
                                    //processVISA(currenttag, responseString);
                                    //currenttag.close();
                                }
                                transactionlogintent.putStringArrayListExtra("LOG_DATA",logdata);
                            }
                            catch (IOException e) {
                                Log.e(TAG, "IO Error", e);
                            }


                        //}
                    //}).start();
                    break;
                }
            }
        }
    }
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        //IntentFilter  = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{new String[] { NfcA.class.getName()}, new String[] {NfcB.class.getName()}};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        /*
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }
        */

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }
    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
    public int[] calculateUNLength(byte[] rawstream){
        //decode 70 template
        int[] returnedUNlengths = new int[2];
        //BERTLV tag70 = new BERTLV(rawstream);
        int lencounter = 0; //counter to track where we are in the template
        int ktrack1 = 0;
        int ttrack1 = 0;
        int ktrack2 = 0;
        int ttrack2 = 0;
        //decode template 70
        BERTLV tag70 = new BERTLV(rawstream);
        ktrack1 =  NumberOfSetBits(byteArrayToInt(BERTLV.findTLVtag(tag70.getValueBytes(), new byte[]{(byte) 0x9F, (byte) 0x63})));
        ttrack1 = (int)BERTLV.findTLVtag(tag70.getValueBytes(), new byte[]{(byte) 0x9F, (byte) 0x64})[0];
        ktrack2 = NumberOfSetBits(byteArrayToInt(BERTLV.findTLVtag(tag70.getValueBytes(), new byte[]{(byte) 0x9F, (byte) 0x66})));
        ttrack2 = (int)BERTLV.findTLVtag(tag70.getValueBytes(), new byte[]{(byte) 0x9F, (byte) 0x67})[0];
        returnedUNlengths[0] = ktrack1 - ttrack1;
        returnedUNlengths[1] = ktrack2 - ttrack2;
        return returnedUNlengths;
    }
    public void processMastercard(final IsoDep tag, final String selectppseresponse){
        //SELECT AID
        //new Thread(new Runnable() {
            byte[] SELECT_MASTERCARD_AID = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x10, (byte) 0x10, (byte) 0x00};
            byte[] GET_PROCESSING_OPTIONS = {(byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x83, (byte) 0x00, (byte) 0x00};
            byte[] READ_RECORD_1_1 = {(byte) 0x00, (byte) 0xB2, (byte) 0x01, (byte) 0x0C, (byte) 0x00};
            //byte[] COMPUTE_CRYPTOGRAPHIC_CHECKSUM = {};
            String selectaidresponse;
            //StringBuilder gporesponse;
            //String rr1response;
            //String responseString;
            byte[] responseBytes;
            String track2;
            int[] unLengths = new int[2];
            //public void run(){
                try{
                    responseBytes = tag.transceive(SELECT_MASTERCARD_AID);
                    logFromTerminal(BERTLV.ByteArrayToHexString(SELECT_MASTERCARD_AID));
                    logToTerminal(BERTLV.ByteArrayToHexString(responseBytes));
                    selectaidresponse = BERTLV.ByteArrayToHexString(responseBytes);

                    //Log.i(TAG, "SELECT AID RESPONSE=" + responseString);
                    responseBytes = tag.transceive(GET_PROCESSING_OPTIONS);
                    logFromTerminal(BERTLV.ByteArrayToHexString(GET_PROCESSING_OPTIONS));
                    logToTerminal(BERTLV.ByteArrayToHexString(responseBytes));
                    responseBytes = tag.transceive(READ_RECORD_1_1);
                    unLengths = calculateUNLength(responseBytes);
                    logFromTerminal(BERTLV.ByteArrayToHexString(READ_RECORD_1_1));
                    logToTerminal(BERTLV.ByteArrayToHexString(responseBytes));
                    Log.i(TAG, "TRACK1 UN Length=" + unLengths[0]);
                    Log.i(TAG, "TRACK2 UN Length=" + unLengths[1]);
                    txtTrack1UNLength.setText(String.valueOf(unLengths[0])+" decimal places");
                    txtTrack2UNLength.setText(String.valueOf(unLengths[1])+" decimal places");
                    txtNumTransactions.setText(String.valueOf(Math.pow(10,unLengths[0])));
                    float timetoclone = ((150 * (float)Math.pow(10,unLengths[0])) / 1000) / 60;
                    txtTimeToClone.setText(String.valueOf(timetoclone) + " Minutes");
                    Log.i(TAG, "numCVVs=" + (Math.pow(10,unLengths[0])));
                }
                catch(IOException e){
                    Log.i(TAG, "ERROR=" + e.toString());

                }
            //}
        //}).start();
        return;
    }
    public static int byteArrayToInt(byte[] b)
    {
        return   b[6] & 0xFF |
                (b[5] & 0xFF) << 8 |
                (b[4] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24 |
                (b[2] & 0xFF) << 32 |
                (b[1] & 0xFF) << 40 |
                (b[0] & 0xFF) << 48;
    }
    public int NumberOfSetBits(int i) //count number of bits set in an int
    {
        i = i - ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
    }
    public void logFromTerminal(String strData){
        Log.i(TAG, "TO TAG="+strData);
        logdata.add("<font color='#FF0000'>"+strData+"</font>");
    }
    public void logToTerminal(String strData){
        Log.i(TAG, "TO TERMINAL="+strData);

        logdata.add("<font color='#0000FF'>"+strData+"</font>");
    }
}
