package MyWebServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;

public class SubThread implements Runnable{

    Socket socket = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    public String log_name=null;

    // 根据socket初始化socket的多线程类
    public SubThread(Socket socket,String log_name) {
        this.socket = socket;
        this.log_name=log_name;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            // 获得该连接的输入输出流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

//            //为该连接建立request，request从inputstream中读入http请求报文，转化为字符串，并截取到请求的资源。
//            Request request = new Request();
//
//            //从request中获得请求资源的uri
//            String uri = request.getUri();

            // 为请求建立response，response根据传入的outputstream和uri，建立请求资源文件到outputstream之间的通路，并响应相应的资源。若资源不存在，响应默认页面
            Response response = new Response(inputStream,outputStream,log_name);

            // response响应请求的资源
            response.responseResource();

        }catch(IOException e) {
            e.printStackTrace();
        }finally {
        }
    }
}