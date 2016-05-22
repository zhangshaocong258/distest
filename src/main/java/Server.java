import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
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
                    Socket dataSocket = serverSocket.accept();
                    Socket resultSocket = serverSocket.accept();
                    dataClient = new UserClient(dataSocket);
                    resultClient = new UserClientObject(resultSocket);
                    ReceiveMsg receiveMsg = new ReceiveMsg(dataClient, resultClient);
                    ReceiveResult receiveResult = new ReceiveResult(resultClient);
                    System.out.println("一个客户端已连接！");
                    new Thread(receiveMsg).start();
                    new Thread(receiveResult).start();

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

        public String buildStr(String key, String value){
            String DELIMITER = "\f\r";
            return key + DELIMITER + value;
        }

        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveReady();
                    if (dataFromClient.equals("Ready")) {
                        lock.lock();
                        try {
                            for (Map.Entry<Data, String> entry : tasks.entrySet()) {
                                System.out.println("当前key= " + entry.getKey().getMethod() + " and value= " + entry.getValue());
                                if (entry.getValue().equals("n")) {
                                    System.out.println("还有");
                                    userClientObject.sendObject(entry.getKey());
//                                    userClient.sendData(buildStr(entry.getKey(), entry.getValue()));
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

    class ReceiveResult implements Runnable {
        private boolean isConnected = false;
        private UserClientObject userClientObject;
        private Data dataFromClient;


        ReceiveResult(UserClientObject userClientObject) {
            this.userClientObject= userClientObject;
            isConnected = true;
        }

        public void updateMap(Data data){
            lock.lock();
            try {
                tasks.put(data, "y");
                System.out.println("更新后");
                for (Map.Entry<Data, String> entry : tasks.entrySet()) {
                    System.out.println("key= " + entry.getKey().getMethod() + " and value= " + entry.getValue());
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {

            try {
                while (isConnected) {
                    dataFromClient = (Data)userClientObject.receiveObject();
                    updateMap(dataFromClient);
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

class Data implements Serializable{
    private String method;
    private String result;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
