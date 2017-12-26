package com.example.a15096.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.os.CountDownTimer;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class resetPasswordActivity extends AppCompatActivity {

    private TextView mTvShow;
    private Button btn_getCode;
    private TextView text_newPassword;
    private TextView  text_confirmPassword;
    private TextView  text_code;
    private TextView text_getCodePhone;
    private Button btn_confirmpassword;
    private UserGetCodeTask mAuthGetCodeTask = null;
    private UserResetTask mAuthResetTask = null;
    private View mResetPasswordProgress;
    private View mResetPassworForm;
    private String statusCode=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mTvShow = (TextView) findViewById(R.id.show);
        text_newPassword= (TextView) findViewById(R.id.text_newPassword);
        text_confirmPassword= (TextView) findViewById(R.id.text_confirmPassword);
        text_getCodePhone=(TextView) findViewById(R.id.text_getCodePhone);
        text_code= (TextView) findViewById(R.id.text_code);
        btn_getCode = (Button) findViewById(R.id.btn_getCode);
        btn_confirmpassword= (Button) findViewById(R.id.btn_confirmpassword);
        text_newPassword.setEnabled(false);
        text_confirmPassword.setEnabled(false);
        text_code.setEnabled(false);
        btn_confirmpassword.setEnabled(false);
        mResetPassworForm = findViewById(R.id.resetpassword_form);
        mResetPasswordProgress = findViewById(R.id.resetpassword_progress);
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

            mResetPassworForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mResetPassworForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mResetPassworForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mResetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mResetPasswordProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mResetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mResetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mResetPassworForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * 取消倒计时
     * @param v
     */
    public void oncancel(View v) {
        timer.cancel();
    }


    public class UserResetTask extends AsyncTask<Void, Void, Boolean> {

        private final String mphoneNumber;
        private final String mpassword;
        private final String mcode;

        UserResetTask(String phoneNumber,String password,String code) {
            mphoneNumber = phoneNumber;
            mpassword = password;
            mcode = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // Simulate network access
                Boolean status = httpUrlConnPostModifyPassword(mphoneNumber,mcode,mpassword);
                return status;
            } catch (Exception e) {
                return false;
            }
            // TODO: register the new account here.
            //return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mAuthResetTask = null;
            showProgress(false);
            if(success)
            {
                Toast.makeText(getApplicationContext(), "修改成功，请重新登陆", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(statusCode.equals("networkissue"))
                {
                    Toast.makeText(getApplicationContext(), "网络状态差", Toast.LENGTH_SHORT).show();
                }
                else if(statusCode.equals("wrong"))
                {
                    Toast.makeText(getApplicationContext(), "验证码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                }
                else if(statusCode.equals("outofdate"))
                {
                    Toast.makeText(getApplicationContext(), "验证码已过期", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "服务器未知错误，请重新打开应用", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthResetTask = null;
            showProgress(false);
        }

        protected Boolean httpUrlConnPostModifyPassword(String phoneNumber,String code,String password){
            Boolean status = false;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL("http://192.168.0.103:9000/mobileresetpassword");
                //  url = new URL("http://192.168.1.6:9000/mobilelogin");
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
                json.put("phoneNumber", URLEncoder.encode(phoneNumber, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
                json.put("code", URLEncoder.encode(code, "UTF-8"));
                String passwordEn = Base64_.base64encode(password);
                json.put("password", passwordEn);
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
                    else if(result.equals("wrong"))
                    {
                        statusCode = "wrong";
                        status =  false;
                    }
                    else
                    {
                        statusCode =  "outofdate";
                        status =  false;
                    }
                }else{
                    statusCode =  "networkissue";
                    status =  false;
                    // handler.sendEmptyMessage(2);
                }
            } catch (Exception e) {
              //  String str = e.getMessage();
                //Log.d(str, "httpUrlConnPost: ");
                //handler.sendEmptyMessage(2);
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                return status;
            }
        }
    }
    public void confirm(View v) {
        if (mAuthResetTask != null) {
            return;
        }
        String code = text_code.getText().toString();
        String password = text_newPassword.getText().toString();
        String confirmpassword = text_confirmPassword.getText().toString();
        if(password.length()<6)
        {
            Toast.makeText(getApplicationContext(), "密码长度不能小于6位，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!confirmpassword.equals(password)) {
            Toast.makeText(getApplicationContext(), "两次密码输入不一致，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        text_code.setEnabled(true);
        btn_confirmpassword.setEnabled(true);
        try {
            String phoneNumber = text_getCodePhone.getText().toString();
            mAuthResetTask = new UserResetTask(phoneNumber,password,code);
            Boolean status = mAuthResetTask.execute((Void) null).get();
        }catch (Exception e)
        {
            e.getMessage();
        }
    }

    /**
     * 开始倒计时
     * @param v
     */
    public void restart(View v) {
        if (mAuthGetCodeTask != null) {
            return;
        }
        timer.start();
        text_code.setEnabled(true);
        btn_confirmpassword.setEnabled(true);
        text_newPassword.setEnabled(true);
        text_confirmPassword.setEnabled(true);
        try {
            mAuthGetCodeTask = new UserGetCodeTask(text_getCodePhone.getText().toString());
            Boolean status = mAuthGetCodeTask.execute((Void) null).get();
        }catch (Exception e)
        {
            e.getMessage();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserGetCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String mphoneNumber;
        UserGetCodeTask(String phoneNumber) {
            mphoneNumber = phoneNumber;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // Simulate network access
                Boolean status = httpUrlConnPost(mphoneNumber);
                return status;
            } catch (Exception e) {
                return false;
            }
            // TODO: register the new account here.
            //return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mAuthGetCodeTask = null;
           // showProgress(false);
            showProgress(false);
            if (success) {
               // finish();
                //mTvShow.setText("修改成功");
                Toast.makeText(getApplicationContext(), "请查看手机短信验证码", Toast.LENGTH_SHORT).show();
            } else {
              //  mTvShow.setError("未验证成功，请重试");
             //   mPasswordView.setError(getString(R.string.error_incorrect_password));
              //  mPasswordView.requestFocus();
                Toast.makeText(getApplicationContext(), "网络状态差，请重新发送", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthGetCodeTask = null;
            showProgress(false);
        }

        protected boolean httpUrlConnPost(String phoneNumber){
            boolean status = false;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL("http://192.168.0.103:9000/mobilecode");
                //  url = new URL("http://192.168.1.6:9000/mobilelogin");
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
                json.put("phoneNumber", URLEncoder.encode(phoneNumber, "UTF-8"));//使用URLEncoder.encode对特殊和不可见字符进行编码
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
                String str = e.getMessage();
                Log.d(str, "httpUrlConnPost: ");
                //handler.sendEmptyMessage(2);
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
                return status;
            }
        }
    }

    private CountDownTimer timer = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            btn_getCode.setEnabled(false);
            mTvShow.setText((millisUntilFinished / 1000) + "秒后可重发");
        }

        @Override
        public void onFinish() {
            btn_getCode.setEnabled(true);
            mTvShow.setEnabled(true);
            mTvShow.setText("");
        }
    };
}
