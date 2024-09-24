package MyWebServer;

import sun.nio.ch.ThreadPool;

import java.io.*;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MainThread {

    //定义html页面等的存放位置。（注：System.getProperty("user.dir")用于获取当前工程路径）
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    //定义web服务器占用的端口号
    public static final int port = 8888;

    //定义一个列表，存放为每个连接的socket建立的线程。
    private LinkedList<Thread> threadPool = new LinkedList<>();
    private int maxConnectNum=10;

    public String log_name=null;

    //main方法，服务器开始启动
    public static void main(String[] args) {
        //webserver 开始启动
        MainThread server = new MainThread();
        server.start();
    }

    public void start() {
        Calendar now = Calendar.getInstance();
        log_name=WEB_ROOT+"\\"+"log"+"\\";
        log_name +=  now.get(Calendar.YEAR)+ "-" +
                (now.get(Calendar.MONTH)+1) + "-" +
                now.get(Calendar.DATE) + " " +
                now.get(Calendar.HOUR_OF_DAY) + "时" +
                now.get(Calendar.MINUTE) + "分" +
                now.get(Calendar.SECOND)+"秒"
                + ".txt";
//        File file=new File(log_name);
//        System.out.println(log_name);
//        file.mkdir();
//
//        try{//异常处理
//            //如果Qiju_Li文件夹下没有Qiju_Li.txt就会创建该文件
//            BufferedWriter bw=new BufferedWriter(new FileWriter(log_name));
//            bw.write("Hello I/O!");//在创建好的文件中写入"Hello I/O"
//            bw.close();//一定要关闭文件
//        }catch(IOException e) {
//            e.printStackTrace();
//        }
        ServerSocket serversocket = null;
        try {
            //server 监听127.0.0.1:8888
            serversocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));

            //server 已经启动
            System.out.println("MainThread is running!!");
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        //用来记录请求的数量
        int count = 1;

        //存放各线程的列表
        while(true) {
            Socket socket = null;

            try {
                //为该请求建立连接
                socket = serversocket.accept();

                System.out.println("连接"+ count +"已建立！");

                //为该socket建立多线程，启动，并加入列表
                SubThread connectionthread = new SubThread(socket,log_name);
                Thread thread = new Thread(connectionthread);
                thread.start();
                if(threadPool.size()==maxConnectNum){
                    Thread oldThread=threadPool.removeFirst();
                }
                threadPool.addLast(thread);

                System.out.println("连接"+ count++ +"的线程已启动并成功加入队列！");

            }catch(Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}