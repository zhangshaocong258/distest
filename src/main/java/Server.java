import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by zsc on 2016/5/20.
 */
public class Server {
    private final static Map<Data, String> tasks = new HashMap<>();
    private final static Data data1 = new Data();
    private final static Data data2 = new Data();
    private final static Data data3 = new Data();
    private final static Data data4 = new Data();
    private final static Data data5 = new Data();
    private final static Data data6 = new Data();

    private Lock lock = new ReentrantLock();
    public static void main(String[] args) {
        initMap();
        new Thread(new Server().new ServerStart()).start();
    }

    //将所有data放到map中
    private static void initMap(){
        data1.setMethod("+");
        data2.setMethod("-");
        data3.setMethod("*");
        data4.setMethod("/");
        data5.setMethod("a²");
        data6.setMethod("√");

        tasks.put(data1, "n");
        tasks.put(data2, "n");
        tasks.put(data3, "n");
        tasks.put(data4, "n");
        tasks.put(data5, "n");
        tasks.put(data6, "n");

    }

    //启动服务端
    class ServerStart implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient dataClient;
        private UserClientObject resultClient;
        private boolean start = false;

        public void run() {
            try {
                serverSocket = new ServerSocket(7777);
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中...");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (start) {
                    Socket dataSocket = serverSocket.accept();//接收dataoutputstream
                    Socket resultSocket = serverSocket.accept();//接收objectoutputstream
                    dataClient = new UserClient(dataSocket);
                    resultClient = new UserClientObject(resultSocket);
                    ReceiveMsg receiveMsg = new ReceiveMsg(dataClient, resultClient);//连接
                    ReceiveResult receiveResult = new ReceiveResult(resultClient);//连接
                    System.out.println("一个客户端已连接！");
                    new Thread(receiveMsg).start();//启动线程
                    new Thread(receiveResult).start();//启动线程
                }
            } catch (IOException e) {
                System.out.println("服务端错误位置");
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                    start = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //接收dataoutputstream的封装
    class ReceiveMsg implements Runnable {
        private boolean isConnected = false;
        private UserClient userClient;
        private UserClientObject userClientObject;
        private String dataFromClient = "";

        ReceiveMsg(UserClient userClient, UserClientObject userClientObject) {
            this.userClient = userClient;
            this.userClientObject = userClientObject;
            isConnected = true;
        }

        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveReady();//接收Ready
                    if (dataFromClient.equals("Ready")) {
                        //线程加锁，防止其他线程调用Map
                        lock.lock();
                        try {
                            //找到没有完成的任务
                            for (Map.Entry<Data, String> entry : tasks.entrySet()) {
                                System.out.println("当前key= " + entry.getKey().getMethod() + "result " +
                                        entry.getKey().getDataResult() + " and value= " + entry.getValue());
                                if (entry.getValue().equals("n")) {
                                    userClientObject.sendObject(entry.getKey());
                                    break;
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                    System.out.println("执行完毕");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //接收objectoutputstream的封装
    class ReceiveResult implements Runnable {
        private boolean isConnected = false;
        private UserClientObject userClientObject;
        private Data dataFromClient;

        ReceiveResult(UserClientObject userClientObject) {
            this.userClientObject= userClientObject;
            isConnected = true;
        }

        public void updateMap(Data data){
            //map加锁
            lock.lock();
            try {
                tasks.remove(data);
                tasks.put(data, "y");//更新标记，表示完成

                for (Map.Entry<Data, String> entry : tasks.entrySet()) {
                    System.out.println("key= " + entry.getKey().getMethod() + " 结果 " +
                            entry.getKey().getDataResult() + " and value= " + entry.getValue());
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            try {
                //接收任务完成后得到的结果
                while (isConnected) {
                    dataFromClient = (Data)userClientObject.receiveObject();
                    updateMap(dataFromClient);//更新状态
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }

    }


}

class UserClient {
    private Socket socket = null;
    private DataInputStream disWithClient;
    private DataOutputStream dosWithClient;

    public UserClient(Socket socket) {
        this.socket = socket;
        try {
            disWithClient = new DataInputStream(socket.getInputStream());
            dosWithClient = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        try {
            if (disWithClient != null) disWithClient.close();
            if (socket != null) socket.close();
            if (dosWithClient != null) dosWithClient.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void sendData(String str) throws IOException {
        dosWithClient.writeUTF(str);
    }

    public String receiveReady() throws IOException {
        return disWithClient.readUTF();
    }
}

class UserClientObject {
    private Socket socket = null;
    private ObjectInputStream disWithClient;
    private ObjectOutputStream dosWithClient;

    public UserClientObject(Socket socket) {
        this.socket = socket;
        try {
            dosWithClient = new ObjectOutputStream(socket.getOutputStream());
            disWithClient = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void close() throws IOException {
//        try {
//            if (disWithClient != null) disWithClient.close();
//            if (socket != null) socket.close();
//            if (dosWithClient != null) dosWithClient.close();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//    }

    public void sendObject(Data data) throws IOException {
        dosWithClient.writeObject(data);
        dosWithClient.flush();
    }

    public Object receiveObject() throws IOException, ClassNotFoundException {
        return disWithClient.readObject();
    }
}

//任务类，重写equals和hashcode
class Data implements Serializable{
    private String method = "";
    private String dataResult = "";

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDataResult() {
        return dataResult;
    }

    public void setDataResult(String dataResult) {
        this.dataResult = dataResult;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Data)){
            return false;
        }
        Data data = (Data) o;
        return data.getMethod().equals(method);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
//        result = result * 31 + ((dataResult == null) ? 0 : dataResult.hashCode());
        return result;
    }


}
