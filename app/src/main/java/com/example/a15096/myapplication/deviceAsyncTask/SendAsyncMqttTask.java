package com.example.a15096.myapplication.deviceAsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by 15096 on 2018/1/16.
 */

public class SendAsyncMqttTask extends AsyncTask<String, Void, Boolean> {

    //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的
    private PrintStream out = null;
    private boolean isReceive = false;
    public SendAsyncMqttTask(boolean isReceive)
    {
        this.isReceive= isReceive;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String value = params[0];
        String deviceId = params[1];
        try {
            // setServer(str);
            if(value.equals("on"))
            {
                value = "1";
            }
            else if(value.equals("off"))
            {
                value = "0";
            }
            else
            {

            }
             httpUrlConnPost(value,deviceId);
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected boolean httpUrlConnPost(String value,String deviceId){
        boolean status = false;
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            // url = new URL("http://192.168.0.103:9000/mobilelogin");
            url = new URL("http://192.168.0.103:9000/setespStatus");
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
            json.put("value",value );//使用URLEncoder.encode对特殊和不可见字符进行编码
            json.put("deviceid", deviceId);//把数据put进json对象中
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
