package com.example.a15096.myapplication;

import android.content.Intent;
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
    }
    private Handler handler = new Handler() {
        //当有消息发送出来的时候就执行Handler的这个方法来处理消息分发
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //处理UI
        }
    };
    /**
     * Device Set page
     */
    private void registerInformation(View view)
    {
        TextView usenameView = (TextView) findViewById(R.id.register_username);
        TextView phone = (TextView) findViewById(R.id.register_phoneNumber);
        TextView email = (TextView) findViewById(R.id.register_EmailAddress);
        TextView passwordView = (TextView) findViewById(R.id.register_textPassword);
        TextView register_area = (TextView) findViewById(R.id.register_area);
        final String  usename= usenameView.getText().toString();
        final String password = passwordView.getText().toString();
        Intent intent = new Intent();
        intent.setClass(this,LoadingActivity.class);//跳转到加载界面
        intent.putExtra("username",usenameView.getText().toString());
        intent.putExtra("password",passwordView.getText().toString());
        startActivity(intent);
        try{
           new Thread() {
               @Override
               public void run() {
                 // loginByGet(usename,password);
                 // httpUrlConnPost(usename,password);
                   // 执行完毕后给handler发送一个空消息
                 //  handler.sendEmptyMessage(0);
               }
           }.start();

       }
       catch (Exception e)
       {
           e.printStackTrace();
       }

        // 此处的urlConnection对象实际上是根据URL的
        // 请求协议(此处是http)生成的URLConnection类
        // 的子类HttpURLConnection,故此处最好将其转化
        // 为HttpURLConnection类型的对象,以便用到
        // HttpURLConnection更多的API.如下:


    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    
    public String loginByGet(String username, String password)
             {
//把会出现中文的内容进行URL编码,只有进行了编码之后的才能组合到url地址上提交给服务器
//不然会数据会提交失败
        try{
            username = URLEncoder.encode(username, "utf-8");
//组拼url地址,根据浏览器get方式提交数据的格式来组拼的
       // String path = "http://192.168.0.110:9000/registeruser?username=\"+username+\"&password=\"+password";
        String path =    "http://192.168.0.110:9000/register?username="+username+"&password="+password;
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//设置请求头
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
//获得状态码
        int code = conn.getResponseCode();
        if (code == 200) {
            InputStream in = conn.getInputStream();
           // String text = StrStreamUtils.streamToText(in);
            return "oks";
        } else {
            return null;
        }}
        catch (Exception e)
        {
            handler.sendEmptyMessage(2);
        }
        return  "s";
    }

    public void httpUrlConnPost(String name,String password){
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
          //  url = new URL("http://192.168.0.110:9000");
            url = new URL("http://192.168.1.9:9000/registeruserTest");
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
            //-------------使用字节流发送数据--------------
            //OutputStream out = urlConnection.getOutputStream();
            //BufferedOutputStream bos = new BufferedOutputStream(out);//缓冲字节流包装字节流
            //byte[] bytes = jsonstr.getBytes("UTF-8");//把字符串转化为字节数组
            //bos.write(bytes);//把这个字节数组的数据写入缓冲区中
            //bos.flush();//刷新缓冲区，发送数据
            //out.close();
            //bos.close();
            //------------字符流写入数据------------
            OutputStream out = urlConnection.getOutputStream();//输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));//创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
            bw.write(jsonstr);//把json字符串写入缓冲区中
            bw.flush();//刷新缓冲区，把数据发送出去，这步很重要
            out.close();
            bw.close();//使用完关闭

            if(urlConnection.getResponseCode()==HttpURLConnection.HTTP_OK){//得到服务端的返回码是否连接成功
                //------------字节流读取服务端返回的数据------------
                //InputStream in = urlConnection.getInputStream();//用输入流接收服务端返回的回应数据
                //BufferedInputStream bis = new BufferedInputStream(in);//高效缓冲流包装它，这里用的是字节流来读取数据的，当然也可以用字符流
                //byte[] b = new byte[1024];
                //int len = -1;
                //StringBuffer buffer = new StringBuffer();//用来接收数据的StringBuffer对象
                //while((len=bis.read(b))!=-1){
                //buffer.append(new String(b, 0, len));//把读取到的字节数组转化为字符串
                //}
                //in.close();
                //bis.close();
                //Log.d("zxy", buffer.toString());//{"json":true}
                //JSONObject rjson = new JSONObject(buffer.toString());//把返回来的json编码格式的字符串数据转化成json对象
                //------------字符流读取服务端返回的数据------------
                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String str = null;
                StringBuffer buffer = new StringBuffer();
                while((str = br.readLine())!=null){//BufferedReader特有功能，一次读取一行数据
                    buffer.append(str);
                }
                in.close();
                br.close();
                String result = buffer.toString();
                if(result.equals("ok"))
                {
                   //
                }
            }else{
                handler.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            handler.sendEmptyMessage(2);
        }finally{
            urlConnection.disconnect();//使用完关闭TCP连接，释放资源
        }
    }
}
