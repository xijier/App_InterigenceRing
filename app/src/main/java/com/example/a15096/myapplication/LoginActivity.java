package com.example.a15096.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.widget.Toast;

import com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2.Constants;
import com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2.SendToWXActivity;
import com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2.weibo.WBAuthActivity;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import	java.net.HttpURLConnection;
import java.net.URLConnection;
import 	java.net.URL;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final int SHOW_RESPONSE = 0;
    private int LOGIN_CHANCES = 5;
    //还剩几次登录机会的标志，初始值就是LOGIN_CHANCES
    private int count = LOGIN_CHANCES;
    //多次认证失败时需要等待的时间
    private float WAIT_TIME = 30000L;

    private SsoHandler mSsoHandler;
    private static final String TAG = "weibosdk";
    /** 显示认证后的信息，如 AccessToken */
   // private TextView mTokenText;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUserLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                   // textView_response.setText(response);
                    break;
                default:
                    break;
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserLoginView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button button_wechatButton = (Button) findViewById(R.id.button_wechat);
        button_wechatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceSetPage();
            }
        });

        Button button_weibo = (Button) findViewById(R.id.button_weibo);
        button_weibo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                weiboSetPage();
            }
        });

        Button email_register_button = (Button) findViewById(R.id.email_register_button);
        email_register_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registerPage();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUserLoginView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }
    /**
     * register page
     */
    private void registerPage()
    {
        Intent intent=new Intent(this,RegisterActivity.class);
        startActivity(intent);

    }

    private void weiboSetPage()
    {
       // WbSdk.install(this,new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        //mSsoHandler = new SsoHandler(this);
        //mSsoHandler.authorizeClientSso(new LoginActivity.SelfWbAuthListener());
        Intent intent=new Intent(this,WBAuthActivity.class);
        startActivity(intent);
    }

    /**
     * Device Set page
     */
    private void deviceSetPage()
    {
        Intent intent=new Intent(this,controlDeviceActivity.class);
        startActivity(intent);
        //Intent intent=new Intent(this,SendToWXActivity.class);
        //startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserLoginView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserLoginView.setError(getString(R.string.error_field_required));
            focusView = mUserLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            try{
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                //输入错误时的时间,如果为空的话就取0L
                long errorTime = sp.getLong("errorTime", 0L);
                //获取当前时间
                long recentTime = System.currentTimeMillis();
                //如果当前时间与出错时间相差超过30s
                if(recentTime - errorTime > WAIT_TIME) {
                    showProgress(true);
                    mAuthTask = new UserLoginTask(username, password);
                    Boolean status = mAuthTask.execute((Void) null).get();
                    if (status) {
                        //认证成功，跳转到欢迎界面
                        Intent intent = new Intent(this, controlDeviceActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    //认证失败
                    else {
                        //如果LOGIN_CHANCES次认证全部失败
                        if(count == 1) {
                            //清除输入框内容
                            //editText.setText("");
                            //count值重置
                            count = LOGIN_CHANCES;
                            //Toast提醒
                            Toast.makeText(LoginActivity.this, "连续" + LOGIN_CHANCES + "次认证失败，请您" + WAIT_TIME / 1000 +"秒后再登陆！", Toast.LENGTH_LONG).show();
                            //LOGIN_CHANCES次登录失败时，获取此时的Java虚拟机运行时刻并保存提交
                            errorTime = System.currentTimeMillis();

                            SharedPreferences sp1 = getSharedPreferences("data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp1.edit();
                            editor.putLong("errorTime", errorTime);
                            editor.commit();
                        }
                        //LOGIN_CHANCES次登录机会未用完
                        else{
                            //剩余次数减1
                            count--;
                            //Toast提醒
                            Toast.makeText(LoginActivity.this, "您还有" + count + "次登录机会！", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                //LOGIN_CHANCES次登录机会全部用完，app锁定WAIT_TIME时间，在此期间登录无效，锁定
                else{
                    //Toast提醒
                    Toast.makeText(LoginActivity.this, "登录界面锁定中，请等待！", Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(LoginActivity.this, "服务器错误", Toast.LENGTH_LONG).show();
            }
        }
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                                                                     .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUserLoginView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                Boolean status = httpUrlConnPost(mUsername,mPassword);
               return  status;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        protected boolean httpUrlConnPost(String name,String password){
            boolean status = false;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
               // url = new URL("http://192.168.0.103:9000/mobilelogin");
                url = new URL("http://192.168.1.7:9000/mobilelogin");
                urlConnection = (HttpURLConnection) url.openConnection();//打开http连接
                urlConnection.setConnectTimeout(3000);//连接的超时时间
                urlConnection.setUseCaches(false);//不使用缓存
                //urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
                urlConnection.setInstanceFollowRedirects(true);//是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
                urlConnection.setReadTimeout(3000);//响应的超时时间
                urlConnection.setDoInput(true);//设置这个连接是否可以写入数据
                urlConnection.setDoOutput(true);//设置这个连接是否可以输出数据
                urlConnection.setRequestMethod("POST");//设置请求的方式
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置消息的类型
                urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接

                JSONObject json = new JSONObject();//创建json对象
                String passwordEn = Base64_.base64encode(password);
                json.put("name", URLEncoder.encode(name, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("password", passwordEn);//把数据put进json对象中
                String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串
                //------------字符流写入数据------------
                OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
                bw.write(jsonstr);//把json字符串写入缓冲区中
                bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                out.close();
                bw.close();//使用完关闭
                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String str = null;
                    StringBuffer buffer = new StringBuffer();
                    while((str = br.readLine())!=null){
                        buffer.append(str);
                    }
                    in.close();
                    br.close();
                    String result = buffer.toString();
                    if(result.equals("ok"))
                    {
                        status  = true;
                    }
                    else
                    {
                        status =  false;
                    }
                }else{
                    status = false;
                    // handler.sendEmptyMessage(2);
                }
            } catch (Exception e) {
                //handler.sendEmptyMessage(2);
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                return status;
            }
        }
    }

    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener{
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccessToken = token;
                    if (mAccessToken.isSessionValid()) {
                        // 显示 Token
                        updateTokenView(false);
                        // 保存 Token 到 SharedPreferences
                        AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
                        Toast.makeText(LoginActivity.this,
                                R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

        @Override
        public void cancel() {
            Toast.makeText(LoginActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            Toast.makeText(LoginActivity.this, errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 显示当前 Token 信息。
     *
     * @param hasExisted 配置文件中是否已存在 token 信息并且合法
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
       // mTokenText.setText(String.format(format, mAccessToken.getToken(), date));

        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
        }
        //mTokenText.setText(message);
    }
}

