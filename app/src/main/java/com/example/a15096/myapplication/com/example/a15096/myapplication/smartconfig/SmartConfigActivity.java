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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a15096.myapplication.R;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.EsptouchTask;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.IEsptouchResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SmartConfigActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
        , AdapterView.OnItemSelectedListener {
    private static final String SSID_PASSWORD = "ssid_password";
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private SharedPreferences mShared;
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private final static String PREFRENCE_Device_KEY = "com.example.a15096.shared_preferences_devicename";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences mSharedPreferencesDeviceName;
    private Socket socket = null;
    private SendAsyncGetIdTask mSendAsyncTask;
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
    private MulticastSocket ds;
    private String multicastHost = "224.0.0.1";
    private InetAddress receiveAddress;
    private String mCurrentSSID;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateCurrentConnectionInfo();
            }
        }

    };

    private void updateCurrentConnectionInfo() {
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiinfo = wifimanager.getConnectionInfo();

        mCurrentSSID = wifiinfo.getSSID();
        if (mCurrentSSID == null) {
            mCurrentSSID = "";
        }
        mCurrentSsidTV.setText(getString(R.string.esp_esptouch_current_ssid, mCurrentSSID));
        String replaceStr = mCurrentSSID;
        char quto = '"';
        if (replaceStr.charAt(0) == quto && replaceStr.charAt(replaceStr.length() - 1) == quto) {
            replaceStr = replaceStr.substring(1, replaceStr.length());
            replaceStr = replaceStr.substring(0, replaceStr.length() - 1);
        }
        mSsidET.setText(replaceStr);
        if (!TextUtils.isEmpty(mCurrentSSID)) {
            scanWifi();
            mWifiAdapter.notifyDataSetChanged();
            for (int i = 0; i < mScanResultList.size(); i++) {
                String ssid = mScanResultList.get(i).SSID;
                if (ssid.equals(mCurrentSSID)) {
                    //mConfigureSP.setSelection(i);
                    break;
                }
            }
        } else {
            mPasswordET.setText("");
        }
    }

    private void accessLocationPermission() {
        int accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

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
        mScanResultList = wifimanager.getScanResults();
        mScanResultSsidList.clear();
        for (ScanResult scanResult : mScanResultList) {
            mScanResultSsidList.add(scanResult.SSID);
        }
    }

    private void adddeviceSet(String address) {
        char quto = '/';
        if (address.charAt(0) == quto) {
            address = address.substring(1, address.length());
        }
        Long timeSpan = System.currentTimeMillis();
        String deviceId = getConnectSocket("esp8266" + String.valueOf(timeSpan), address, true);
    }

    private String getConnectSocket(String msg, String address, boolean isReceive) {
        String deviceId = "";
        try {
            mSendAsyncTask = new SendAsyncGetIdTask(socket, address, isReceive);
            deviceId = mSendAsyncTask.execute(msg).get();
        } catch (Exception e) {

        }
        return deviceId;
    }

    public class SetSocketThreadID extends Thread {
        String ip = null;

        SetSocketThreadID(String ip) {
            this.ip = ip;
        }

        public void run() {
            try {
                socket = new Socket(this.ip, 8266);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public class SendAsyncGetIdTask extends AsyncTask<String, Void, String> {
        //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的
        private Socket client = null;
        private PrintStream out = null;
        private boolean isReceive = false;
        private String address;

        public SendAsyncGetIdTask(Socket client, String address, boolean isReceive) {
            this.client = client;
            this.isReceive = isReceive;
            this.address = address;
        }

        @Override
        protected String doInBackground(String... params) {
            String str = params[0];
            try {
                String deviceId = setClient(str);
                return deviceId;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String deviceId) {
            //mAuthTask = null;
            //showProgress(false);
            Long timeSpan = System.currentTimeMillis();
            TextView deviceSetname = (TextView) findViewById(R.id.deviceSetname);
            TextView wifipassword = (TextView) findViewById(R.id.esptouch_pwd);
            String deviceName = deviceSetname.getText().toString();
            saveShareData(deviceName,address,deviceId);
        }

        @Override
        protected void onCancelled() {
            // mAuthTask = null;
            //showProgress(false);
        }

        protected String setClient(String msg) {
            String deviceId = "";
            Socket newclient = null;
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
                newclient = new Socket(address, 8266);
                //给服务端发送响应信息
                OutputStream os = newclient.getOutputStream();
                os.write(msg.getBytes());
                //服务器回应
                InputStream is = newclient.getInputStream();
                String str = "";
                while (true) {
                    byte[] b = new byte[is.available()];
                    is.read(b);
                    deviceId = new String(b);
                    if (deviceId.length() > 0) {
                        for (int i = 0; i < deviceId.length(); i++) {
                            char value = deviceId.charAt(i);
                            if (value != 0) {
                                str += String.valueOf(value);
                            }
                        }
                        break;
                    }
                }
                deviceId = str;
                is.close();
                os.close();
                return deviceId;
            } catch (Exception e) {
                Log.e(e.getMessage(), "setClient: ", e.getCause());
                e.printStackTrace();
                return deviceId;
            } finally {
                //操作结束，关闭socket
                try {
                    client.close();
                    return deviceId;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_config);
        mShared = getSharedPreferences(SSID_PASSWORD, Context.MODE_PRIVATE);

        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);

        mSharedPreferencesDeviceName = getSharedPreferences(PREFRENCE_Device_KEY, Context.MODE_PRIVATE);

        mCurrentSsidTV = (TextView) findViewById(R.id.esptouch_current_ssid);
        // mConfigureSP = (Spinner)findViewById(R.id.esptouch_configure_wifi);
        mPasswordET = (EditText) findViewById(R.id.esptouch_pwd);
        mSsidET = (TextView) findViewById(R.id.esptouch_ssid);
        mShowPasswordCB = (CheckBox) findViewById(R.id.esptouch_show_pwd);
        // mIsSsidHiddenCB = (CheckBox)findViewById(R.id.esptouch_isSsidHidden);
        //  mDeletePasswordBtn = (Button)findViewById(R.id.esptouch_delete_pwd);
        mConfirmBtn = (Button) findViewById(R.id.esptouch_confirm);

        mShowPasswordCB.setOnCheckedChangeListener(this);

        //mDeletePasswordBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);


        Button btnfamilySharing = (Button) findViewById(R.id.familySharing);
        btnfamilySharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                familySharing();
            }
        });

        mScanResultList = new ArrayList<ScanResult>();
        mScanResultSsidList = new ArrayList<String>();
        mWifiAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mScanResultSsidList);
        // mConfigureSP.setAdapter(mWifiAdapter);
        // mConfigureSP.setOnItemSelectedListener(this);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

    }

    private void familySharing() {
        try {
            ds = new MulticastSocket(8267);
            multicastHost = "224.0.0.1";
            receiveAddress = InetAddress.getByName(multicastHost);
            ds.joinGroup(receiveAddress);
            getSharingSocket myThread = new getSharingSocket();
            myThread.start();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private String reciveData = "";
    private InetAddress iaddress;

    public class getSharingSocket extends Thread {
        String ip = "";

        public void run() {
            try {
                byte buf[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                while (ip.isEmpty()) {
                    try {
                        ds.receive(dp);
                        reciveData = new String(buf, 0, dp.getLength());
                        ip = dp.getAddress().toString();
                        iaddress = dp.getAddress();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //Toast.makeText(SmartConfigActivity.this, reciveData, Toast.LENGTH_LONG).show();
                String deviceName = splitData(reciveData, "name:", "address:");
                String address = splitData(reciveData, "address:", "end:");
                String deviceId = splitData(reciveData, "id:", "name:");
                saveShareData(deviceName, address, deviceId);
                ShareUdpSocket mThread = new ShareUdpSocket();
                mThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    public String splitData(String str, String strStart, String strEnd) {
        String tempStr;
        tempStr = str.substring(str.indexOf(strStart) + strStart.length(), str.lastIndexOf(strEnd));
        return tempStr;
    }

    private void saveShareData(String deviceName, String address, String deviceId) {
        if (!mSharedPreferencesDeviceName.getAll().containsKey(deviceName) && !deviceName.isEmpty()) {
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        } else if (deviceName.isEmpty()) {
            int i = 1;
            while (mSharedPreferencesDeviceName.getAll().containsKey(deviceName + String.valueOf(i))) {
                i++;
            }
            deviceName = deviceName + String.valueOf(i);
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        } else {
            int i = 1;
            while (mSharedPreferencesDeviceName.getAll().containsKey("智能环设备" + String.valueOf(i))) {
                i++;
            }
            deviceName = "智能环设备" + String.valueOf(i);
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> setValue = new HashSet<String>();
        setValue.add("deviceName:" + deviceName);
        setValue.add("address:" + address);
        editor.putStringSet(deviceId, setValue);
        editor.commit();
    }

    public class ShareUdpSocket extends Thread {
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(iaddress, 8268);
                //接受服务端消息并打印
                //  InputStream is=socket.getInputStream();
                // byte b[]=new byte[1024];
                //  is.read(b);
                //给服务端发送响应信息
                OutputStream os = socket.getOutputStream();
                os.write(reciveData.getBytes());
                os.close();
                socket.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == mShowPasswordCB) {
            if (b) {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

    }

    private class ConfigureTask extends AsyncTask<String, Void, IEsptouchResult> implements DialogInterface.OnCancelListener {
        private Activity mActivity;

        private ProgressDialog mDialog;

        private EsptouchTask mTask;

        private final String mSsid;

        private String passwordId;

        public ConfigureTask(Activity activity, String apSsid, String apBssid, String password, boolean isSsidHidden) {
            mActivity = activity;
            mSsid = apSsid;
            mTask = new EsptouchTask(apSsid, apBssid, password, isSsidHidden, SmartConfigActivity.this);
            passwordId = password;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_esptouch_configure_message, mSsid));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnCancelListener(this);
            mDialog.show();
        }

        @Override
        protected IEsptouchResult doInBackground(String... params) {
            return mTask.executeForResult();
        }

        @Override
        protected void onPostExecute(IEsptouchResult result) {
            int toastMsg;
            if (result.isSuc()) {
                String add = result.getInetAddress().toString();
                adddeviceSet(add);
                toastMsg = R.string.esp_esptouch_result_suc;
            } else if (result.isCancelled()) {
                toastMsg = R.string.esp_esptouch_result_cancel;
            } else if (mCurrentSSID.equals(mSsid)) {
                toastMsg = R.string.esp_esptouch_result_failed;
            } else {
                toastMsg = R.string.esp_esptouch_result_over;
            }
            mDialog.dismiss();
            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (mTask != null) {
                mTask.interrupt();
                Toast.makeText(mActivity, R.string.esp_esptouch_result_cancel, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mConfirmBtn) {
            if (!TextUtils.isEmpty(mCurrentSSID)) {
                String ssid = mSsidET.getText().toString();
                String password = mPasswordET.getText().toString();
                mShared.edit().putString(ssid, password).commit();
                // boolean isSsidHidden = mIsSsidHiddenCB.isChecked();
                boolean isSsidHidden = true;
                // find the bssid is scanList
                String bssid = scanApBssidBySsid(ssid);
                if (bssid == null) {
                    Toast.makeText(this, getString(R.string.esp_esptouch_cannot_find_ap_hing, ssid), Toast.LENGTH_LONG)
                            .show();
                } else {
                    new ConfigureTask(this, ssid, bssid, password, isSsidHidden).execute();

                }
            } else {
                Toast.makeText(this, R.string.esp_esptouch_connection_hint, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String scanApBssidBySsid(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return null;
        }
        String bssid = null;
        for (int retry = 0; bssid == null && retry < 3; retry++) {
            scanWifi();
            for (ScanResult scanResult : mScanResultList) {
                if (scanResult.SSID.equals(ssid)) {
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
