package com.example.a15096.myapplication.deviceAsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 15096 on 2017/12/24.
 */

public class SendAsyncTask extends AsyncTask<String, Void, Boolean> {

    //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的
    private Socket client = null;
    private PrintStream out = null;
    private boolean isReceive = false;
    public SendAsyncTask(Socket client,boolean isReceive)
    {
        this.client = client;
        this.isReceive= isReceive;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String str = params[0];
        String add = params[1];
        try {
           // setServer(str);
            boolean isSuccess =setClient(str,add);
            return  isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected  boolean setClient(String msg,String add)
    {
        boolean isSuccess = false;
        Socket socket=null;
        try {
            socket=new Socket(add, 8266);
            //给服务端发送响应信息
            //OutputStream os=client.getOutputStream();
            OutputStream os=socket.getOutputStream();
            os.write(msg.getBytes());
            //is.close();
            //接受服务端消息并打印
            if(isReceive)
            {
                InputStream is=client.getInputStream();
                byte[] b  = new byte[is.available()];
                is.read(b);
                String str = new String(b);
                is.close();
            }
            os.close();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            Log.e(e.getMessage(), "setClient: ", e.getCause());
            e.printStackTrace();
        } finally {
            //操作结束，关闭socket
            try {
              //  client.close();
                socket.close();
                return  isSuccess;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }
}
