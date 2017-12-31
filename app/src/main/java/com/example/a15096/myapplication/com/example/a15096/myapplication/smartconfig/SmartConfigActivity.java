package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a15096.myapplication.R;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.EsptouchTask;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.IEsptouchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class SmartConfigActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
,AdapterView.OnItemSelectedListener
{
    private static final String SSID_PASSWORD = "ssid_password";
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private SharedPreferences mShared;
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private SharedPreferences mSharedPreferences;

    private TextView mCurrentSsidTV;
   // private Spinner mConfigureSP;
    private TextView mSsidET;
    private EditText mPasswordET;
    private CheckBox mShowPasswordCB;
    //private CheckBox mIsSsidHiddenCB;
    //private Button mDeletePasswordBtn;
    private Button mConfirmBtn;
    private WifiManager wifimanager;

    private BaseAdapter mWifiAdapter;
    private volatile List<ScanResult> mScanResultList;
    private volatile List<String> mScanResultSsidList;

    private String mCurrentSSID;

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                updateCurrentConnectionInfo();
            }
        }

    };
    private void updateCurrentConnectionInfo()
    {
        wifimanager=(WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiinfo=wifimanager.getConnectionInfo();

        mCurrentSSID = wifiinfo.getSSID();
        if (mCurrentSSID == null)
        {
            mCurrentSSID = "";
        }
        mCurrentSsidTV.setText(getString(R.string.esp_esptouch_current_ssid, mCurrentSSID));
        String replaceStr =  mCurrentSSID;
        char quto='"';
        if(replaceStr.charAt(0)==quto&& replaceStr.charAt(replaceStr.length()-1)==quto)
        {
            replaceStr = replaceStr.substring(1, replaceStr.length());
            replaceStr = replaceStr.substring(0,replaceStr.length() - 1);
        }
        mSsidET.setText(replaceStr);
        if (!TextUtils.isEmpty(mCurrentSSID))
        {
            scanWifi();
            mWifiAdapter.notifyDataSetChanged();
            for (int i = 0; i < mScanResultList.size(); i++)
            {
                String ssid = mScanResultList.get(i).SSID;
                if (ssid.equals(mCurrentSSID))
                {
                    //mConfigureSP.setSelection(i);
                    break;
                }
            }
        }
        else
        {
            mPasswordET.setText("");
        }
    }

    private void accessLocationPermission() {
        int accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation   = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    //TODO - Add your code here to start Discovery

                }
                break;
            default:
                return;
        }
    }

    private void scanWifi() {
        accessLocationPermission();
        wifimanager.setWifiEnabled(true);
        wifimanager.startScan();
        mScanResultList=wifimanager.getScanResults();
        mScanResultSsidList.clear();
        for(ScanResult scanResult : mScanResultList)
        {
            mScanResultSsidList.add(scanResult.SSID);
        }
    }

    private void adddeviceSet(String address)
    {
        char quto='/';
        if(address.charAt(0)==quto)
        {
            address = address.substring(1, address.length());
        }
        Long timeSpan= System.currentTimeMillis();
        TextView deviceSetname = (TextView) findViewById(R.id.deviceSetname);
        TextView wifipassword = (TextView) findViewById(R.id.esptouch_pwd);
        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
        String deviceName = deviceSetname.getText().toString();
        if(!mSharedPreferences.getAll().containsKey(deviceName)&&!deviceName.isEmpty())
        {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(deviceSetname.getText().toString(), address);
            editor.commit();
        }
        else if(deviceName.isEmpty())
        {
            int i = 1;
            while(mSharedPreferences.getAll().containsKey(deviceName+String.valueOf(i)))
            {
                i++;
            }
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(deviceName+String.valueOf(i), address);
            editor.commit();
        }
        else
        {
            int i = 1;
            while(mSharedPreferences.getAll().containsKey("智能环设备"+String.valueOf(i)))
            {
                i++;
            }
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("智能环设备"+String.valueOf(i),address);
            editor.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_config);
        mShared = getSharedPreferences(SSID_PASSWORD, Context.MODE_PRIVATE);

        mCurrentSsidTV = (TextView)findViewById(R.id.esptouch_current_ssid);
       // mConfigureSP = (Spinner)findViewById(R.id.esptouch_configure_wifi);
        mPasswordET = (EditText)findViewById(R.id.esptouch_pwd);
        mSsidET = (TextView)findViewById(R.id.esptouch_ssid);
        mShowPasswordCB = (CheckBox)findViewById(R.id.esptouch_show_pwd);
       // mIsSsidHiddenCB = (CheckBox)findViewById(R.id.esptouch_isSsidHidden);
      //  mDeletePasswordBtn = (Button)findViewById(R.id.esptouch_delete_pwd);
        mConfirmBtn = (Button)findViewById(R.id.esptouch_confirm);

        mShowPasswordCB.setOnCheckedChangeListener(this);

        //mDeletePasswordBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);

        mScanResultList = new ArrayList<ScanResult>();
        mScanResultSsidList = new ArrayList<String>();
        mWifiAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mScanResultSsidList);
       // mConfigureSP.setAdapter(mWifiAdapter);
       // mConfigureSP.setOnItemSelectedListener(this);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == mShowPasswordCB)
        {
            if (b)
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

    }
    private class ConfigureTask extends AsyncTask<String, Void, IEsptouchResult> implements DialogInterface.OnCancelListener
    {
        private Activity mActivity;

        private ProgressDialog mDialog;

        private EsptouchTask mTask;

        private final String mSsid;

        public ConfigureTask(Activity activity, String apSsid, String apBssid, String password, boolean isSsidHidden)
        {
            mActivity = activity;
            mSsid = apSsid;
            mTask = new EsptouchTask(apSsid, apBssid, password, isSsidHidden, SmartConfigActivity.this);
        }

        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_esptouch_configure_message, mSsid));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnCancelListener(this);
            mDialog.show();
        }

        @Override
        protected IEsptouchResult doInBackground(String... params)
        {
            return mTask.executeForResult();
        }

        @Override
        protected void onPostExecute(IEsptouchResult result)
        {
            mDialog.dismiss();
            String add1 = result.getInetAddress().toString();
            int toastMsg;
            if (result.isSuc())
            {
                String add = result.getInetAddress().toString();
                adddeviceSet(add);
                toastMsg = R.string.esp_esptouch_result_suc;
            }
            else if (result.isCancelled())
            {
                toastMsg = R.string.esp_esptouch_result_cancel;
            }
            else if (mCurrentSSID.equals(mSsid))
            {
                toastMsg = R.string.esp_esptouch_result_failed;
            }
            else
            {
                toastMsg = R.string.esp_esptouch_result_over;
            }

            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(DialogInterface dialog)
        {
            if (mTask != null)
            {
                mTask.interrupt();
                Toast.makeText(mActivity, R.string.esp_esptouch_result_cancel, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mConfirmBtn)
        {
            if (!TextUtils.isEmpty(mCurrentSSID))
            {
                String ssid = mSsidET.getText().toString();
                String password = mPasswordET.getText().toString();
                mShared.edit().putString(ssid, password).commit();
               // boolean isSsidHidden = mIsSsidHiddenCB.isChecked();
                boolean isSsidHidden = true;
                // find the bssid is scanList
                String bssid = scanApBssidBySsid(ssid);
                if (bssid == null)
                {
                    Toast.makeText(this, getString(R.string.esp_esptouch_cannot_find_ap_hing, ssid), Toast.LENGTH_LONG)
                            .show();
                }
                else
                {
                    new ConfigureTask(this, ssid, bssid, password, isSsidHidden).execute();

                }
            }
            else
            {
                Toast.makeText(this, R.string.esp_esptouch_connection_hint, Toast.LENGTH_LONG).show();
            }
        }
//        else if (view == mDeletePasswordBtn)
//        {
//            String selectionSSID = mConfigureSP.getSelectedItem().toString();
//            if (!TextUtils.isEmpty(selectionSSID))
//            {
//                mShared.edit().remove(selectionSSID).commit();
//                mPasswordET.setText("");
//            }
//        }

    }

    private String scanApBssidBySsid(String ssid) {
        if (TextUtils.isEmpty(ssid))
        {
            return null;
        }
        String bssid = null;
        for (int retry = 0; bssid == null && retry < 3; retry++)
        {
            scanWifi();
            for (ScanResult scanResult : mScanResultList)
            {
                if (scanResult.SSID.equals(ssid))
                {
                    bssid = scanResult.BSSID;
                    return bssid;
                }
            }
        }
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String ssid = mScanResultList.get(i).SSID;
        String password = mShared.getString(ssid, "");
        mPasswordET.setText(password);
        mSsidET.setText(ssid);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
