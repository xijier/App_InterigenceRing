package com.example.a15096.myapplication;

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

public class SendAsyncTask extends AsyncTask<String, Void, Void> {

    //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的
    private Socket client = null;
    private PrintStream out = null;
    SendAsyncTask(Socket client)
    {
        this.client = client;
    }
    @Override
    protected Void doInBackground(String... params) {
        String str = params[0];
        try {
           // setServer(str);
            setClient(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void setServer(String msg)
    {
        ServerSocket serverSocket=null;
        Socket socket=null;
       // String msg="a";
        try {
            //构造ServerSocket实例，指定端口监听客户端的连接请求
            serverSocket=new ServerSocket(8266);
            String ip =  serverSocket.getLocalSocketAddress().toString();
            //建立跟客户端的连接
            socket=serverSocket.accept();
            //向客户端发送消息
            OutputStream os=socket.getOutputStream();
            os.write(msg.getBytes());
            InputStream is=socket.getInputStream();
            //接受客户端的响应
            byte[] b=new byte[1024];
            is.read(b);
            String str = new String(b);
            System.out.println(str.trim()+" "+str.length());
            serverSocket.close();
            socket.close();
        } catch (Exception e) {
            Log.e(e.getMessage(), "setServer: ", e.getCause());
            e.printStackTrace();
        } finally {
            //操作结束，关闭socket
            try {
                serverSocket.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setClient(String msg)
    {
      // Socket socket=null;
        try {
            //socket=new Socket("192.168.0.109", 8266);
            //接受服务端消息并打印
            //InputStream is=socket.getInputStream();
            //byte b[]=new byte[1024];
            //is.read(b);;
            //给服务端发送响应信息
            OutputStream os=client.getOutputStream();
            os.write(msg.getBytes());
            //is.close();
            os.close();
        } catch (Exception e) {
            Log.e(e.getMessage(), "setClient: ", e.getCause());
            e.printStackTrace();
        } finally {
            //操作结束，关闭socket
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
