package MyWebServer;

import java.io.*;
import java.util.Calendar;

public class Response {

    // 定义读取文件时byte[] 数组的大小
    private static final int BUFFER_SIZE = 1024;
    private InputStream inputStream = null;
    private String method=null;//get or post or head
    private String uri = null;
    private String postData=null;
    private String fileName=null;
    private String request=null;
    private int status_code=200;
    private int file_size=0;
    public String log_name=null;

    // 用来响应的outputstream，该输出流从为该请求建立的socket中获得，并传入
    OutputStream outputStream = null;

    // 用outputstream初始化response
    public Response(InputStream inputStream,OutputStream outputStream,String log_name) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.log_name=log_name;

        // 根据请求报文的特点，请求的文件在第一个和第二个空格之间
        request = requestToString();
        if(request==null)return;
        //将请求报文按行分割，得到最后一行的post数据
        for(String line:request.split("\n")){
            postData=line;
        }
        //System.out.println(postData+"123");

        if(request != null) {
            int space1 = -1;
            int space2 = -1;
            space1 = request.indexOf(' ');
            if(space1 != -1) {
                this.method=request.substring(0,space1);
                //System.out.println(method+"123");
                space2 = request.indexOf(' ',space1 + 1);
            }
            if(space2 > space1) {
                // 截取第一个和第二个空格之间的字符串，即请求资源的uri
                this.uri = request.substring(space1 + 1, space2);
                //System.out.println(this.uri.split("."));
                String s=this.uri.split("\\.")[0];
                this.fileName=s.substring(0,s.length());
            }
        }
    }

    public String requestToString() {
        String requestString = null;
        // 字节流转字符流
        BufferedReader bfreader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();

        char[] temp = new char[2048];
        int length = 0;
        try {
            length = bfreader.read(temp);
            buffer.append(temp,0,length);
            requestString = buffer.toString();
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            // 输出request
            if(requestString != null)
            {
                System.out.println("request为:");
                System.out.println(requestString);
            }
            return requestString;
        }
    }

    // 该请求资源的文件路径outputstream
    public void responseResource() {
        // 读取文件时的byte[]
        if(request==null)return;
        byte[] resourcetemp = new byte[BUFFER_SIZE];

        // 输入流
        FileInputStream fileinputstream = null;
        if(uri == null)
            return;
        else if(uri.equals("/")){
            uri="/index.html";
        }
        try {
            // 建立文件
            File resource = new File(MainThread.WEB_ROOT,uri);
            // 判断文件是否存在
            if(this.method.equals("GET") || this.method.equals("HEAD")){
                if(resource.exists()) {
                    String resourceName=resource.getName();
                    System.out.println("请求的资源是：" + resourceName);
                    int suffixSub= resourceName.indexOf('.');
                    String suffix=resourceName.substring(suffixSub+1,resourceName.length());
                    fileinputstream = new FileInputStream(resource);
                    int length = 0;

                    String responseHead = "HTTP/1.1 200 OK\r\nContent-Type: text/" + suffix+";charset=utf-8\r\n\r\n";
                    //System.out.println(responseHead);
                    status_code=200;
                    outputStream.write(responseHead.getBytes());
                    if(this.method.equals("GET")){
                        while((length = fileinputstream.read(resourcetemp)) > 0) {
                            this.file_size+=length;
                            outputStream.write(resourcetemp, 0, length);
                        }
                    }
                }
                else {
                    String errorPage = "HTTP/1.1 404 Not Found\r\n" + "Content-Type: text/html\r\n"+ ";charset=utf-8\r\n"+ "\r\n" + "<h1>404 Not Found</h1>";
                    status_code=404;
                    outputStream.write(errorPage.getBytes());
                }
            }
            else if(this.method.equals("POST")){
                String command="python "+MainThread.WEB_ROOT+File.separator+uri.substring(1,uri.length())+" "+'"'+postData+'"';
                //System.out.println(command);
                Process process = Runtime.getRuntime().exec(command);
                int exitCode=process.waitFor();
                //System.out.println(exitCode);
                if(exitCode == 0){
                    String responseHead = "HTTP/1.1 200 OK\r\nContent-Type: text/html;charset=utf-8\r\n";
                    status_code=200;
                    outputStream.write(responseHead.getBytes());
                    BufferedReader bfreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    char[] temp = new char[2048];
                    int length = 0;
                    length = bfreader.read(temp);
                    buffer.append(temp,0,length);
                    file_size=length;
                    outputStream.write(buffer.toString().getBytes());
                }
                else{
                    String errorPage = "HTTP/1.1 403 Forbidden\r\n" + "Content-Type: text/html\r\n"+ ";charset=utf-8\r\n"+ "\r\n" + "<h1>403 Forbidden</h1>";
                    status_code=403;
                    outputStream.write(errorPage.getBytes());
                }
            }
            else{
                String errorPage = "HTTP/1.1 400 Bad Request\r\n" + "Content-Type: text/html\r\n"+ ";charset=utf-8\r\n"+ "\r\n" + "<h1>400 Bad Request</h1>";
                status_code=400;
                outputStream.write(errorPage.getBytes());
            }

            outputStream.close();
            writeLog();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }finally {
            outputStream = null;
            fileinputstream = null;
        }
    }
    public void writeLog() throws IOException {
        Calendar now = Calendar.getInstance();
        String logContent = "127.0.0.1";
        logContent += "--";
        logContent = logContent + "[" + now.get(Calendar.YEAR) + "-" +
                (now.get(Calendar.MONTH)+1) + "-" +
                now.get(Calendar.DATE) + "-" +
                now.get(Calendar.HOUR_OF_DAY) + "-" +
                now.get(Calendar.MINUTE)+"-"+
                now.get(Calendar.SECOND) + "]";
        logContent = logContent + " \"" + this.method;
        logContent = logContent + " " + this.uri;
        logContent +=" HTTP/1.1\"";
        logContent = logContent + " "+status_code;
        logContent = logContent + " "+file_size+" ";
        for(String line :request.split("\n")){
            if(line.split(":")[0].equals("Referer")){
                int len=line.length();
                logContent=logContent+'"'+line.substring(9,len-1)+"\" ";
                break;
            }
        }
        for(String line :request.split("\n")){
            if(line.split(":")[0].equals("User-Agent")){
                int len=(line.split(":")[1]).length();
                logContent=logContent+'"'+(line.split(":")[1]).substring(1,len-1)+'"'+"\n";
                break;
            }
        }
        FileOutputStream fileOutputStream=new FileOutputStream(log_name,true);
        //System.out.println(log_name);
        fileOutputStream.write(logContent.getBytes());
    }
}