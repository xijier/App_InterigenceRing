package com.example.a15096.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.widget.Toast;

import	java.net.HttpURLConnection;
import java.net.URLConnection;
import 	java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class RegisterActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE = 0;
    private UserRegisterTask mAuthTask = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button confirm_button = (Button) findViewById(R.id.btn_confirm_register);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInformation(view);
            }
        });
        Button btn_forgetPassword = (Button) findViewById(R.id.btn_forgetPassword);
        btn_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword(view);
            }
        });
    }
    private Handler handler = new Handler() {
        //当有消息发送出来的时候就执行Handler的这个方法来处理消息分发
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //处理UI
        }
    };

    private void resetPassword(View view)
    {
        Intent intent=new Intent(this,resetPasswordActivity.class);
        startActivity(intent);
    }
    /**
     * Device Set page
     */
    private void registerInformation(View view)
    {
        if (mAuthTask != null) {
            return;
        }
        TextView usenameView = (TextView) findViewById(R.id.register_username);
        TextView phone = (TextView) findViewById(R.id.register_phoneNumber);
        TextView emailView = (TextView) findViewById(R.id.register_EmailAddress);
        TextView passwordView = (TextView) findViewById(R.id.register_textPassword);
        TextView register_area = (TextView) findViewById(R.id.register_area);

        try{
            String phoneNumber = phone.getText().toString().trim();
            String username = usenameView.getText().toString().trim();
            String email = emailView.getText().toString().trim();
            String area = register_area.getText().toString().trim();
            String password = passwordView.getText().toString();
            mAuthTask = new UserRegisterTask(phoneNumber,username,password,email,area);
            Boolean status = mAuthTask.execute((Void) null).get();

       }
       catch (Exception e)
       {
           e.printStackTrace();
       }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mphoneNumber;
        private final String muserName;
        private final String mpassword;
        private final String memail;
        private final String marea;
        private boolean status= false;
        UserRegisterTask(String phoneNumber,String username,String password,String email,String area) {
            mphoneNumber = phoneNumber;
            muserName = username;
            mpassword = password;
            memail = email;
            marea = area;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                status = httpUrlConnPost(mphoneNumber, muserName, mpassword, memail, marea);
                return status;
            } catch (Exception e) {
                return false;
            }
            // TODO: register the new account here.
            //return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            // showProgress(false);
            if (success) {
                finish();
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "注冊失敗", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            // showProgress(false);
        }

        protected boolean httpUrlConnPost(String phoneNumber,String name,String password,String email,String area){
            boolean status = false;
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL("http://192.168.1.7:9000/registeruser");
                // url = new URL("http://192.168.1.6:9000/registeruser");
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
                json.put("phoneNumber", URLEncoder.encode(phoneNumber, "UTF-8"));
                json.put("email", URLEncoder.encode(email, "UTF-8"));
                json.put("area", URLEncoder.encode(area, "UTF-8"));
                String jsonstr = json.toString();//把JSON对象按JSON的编码格式转换为字符串
                //------------字符流写入数据------------
                OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
                bw.write(jsonstr);//把json字符串写入缓冲区中
                bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
                out.close();
                bw.close();//使用完关闭
                if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){
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
                        status = true;
                    }
                    else
                    {
                        status = false;
                    }
                }else{
                    status = false;
                    // handler.sendEmptyMessage(2);
                }
            } catch (Exception e) {
                status = false;
                //handler.sendEmptyMessage(2);
            }finally{
                urlConnection.disconnect();//使用完关闭TCP连接，释放资源
            }
            return  status;
        }
    }
}
