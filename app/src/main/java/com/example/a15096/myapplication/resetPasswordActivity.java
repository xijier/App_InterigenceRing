package com.example.a15096.myapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.os.CountDownTimer;

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
    private static boolean statusCode = false;
    private UserGetCodeTask mAuthGetCodeTask = null;
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
    }

    /**
     * 取消倒计时
     * @param v
     */
    public void oncancel(View v) {
        timer.cancel();
    }


    public void confirm(View v) {

        new Thread() {
            @Override
            public void run() {
                // 需要执行的方法
                // 执行完毕后给handler发送一个空消息
                String phoneNumber = text_getCodePhone.getText().toString();
                String code = text_code.getText().toString();
                String password = text_newPassword.getText().toString();
                statusCode =httpUrlConnPostModifyPassword(phoneNumber,code,password);
            }
        }.start();

    }

    public boolean httpUrlConnPostModifyPassword(String phoneNumber,String code,String password){
        boolean status = false;
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

    /**
     * 开始倒计时
     * @param v
     */
    public void restart(View v) {
        if (mAuthGetCodeTask != null) {
            return;
        }
        timer.start();
        text_newPassword.setEnabled(true);
        text_confirmPassword.setEnabled(true);
        text_code.setEnabled(true);
        btn_confirmpassword.setEnabled(true);
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

            if (success) {
                finish();
                mTvShow.setText("修改成功");
            } else {
                mTvShow.setError("未验证成功，请重试");
             //   mPasswordView.setError(getString(R.string.error_incorrect_password));
              //  mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthGetCodeTask = null;
           // showProgress(false);
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
