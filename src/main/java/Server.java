import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by zsc on 2016/5/20.
 */
public class Server {
    private static Map<String, String> tasks = new HashMap<>();
    private Lock lock = new ReentrantLock();
    public static void main(String[] args) {
        initMap();
        new Thread(new Server().new ServerStart()).start();
    }

    private static void initMap(){
        tasks.put("+", "n");
        tasks.put("-", "n");
        tasks.put("*", "n");
        tasks.put("/", "n");
    }

    //启动服务端
    class ServerStart implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient dataClient;
        private UserClient resultClient;
        private boolean start = false;

        public void run() {
            try {
                serverSocket = new ServerSocket(30000);
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
                    resultClient = new UserClient(resultSocket);
                    ReceiveMsg receiveMsg = new ReceiveMsg(dataClient);
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
        private String dataFromClient = "";


        ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
            isConnected = true;
        }

        public String buildStr(String key, String value){
            String DELIMITER = "\f\r";
            return key + DELIMITER + value;
        }

        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveData();
                    if (dataFromClient.equals("Ready")) {
                        try {
                            for (Map.Entry<String, String> entry : tasks.entrySet()) {
                                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
                                if (entry.getValue().equals("n")) {
                                    userClient.sendData(buildStr(entry.getKey(), entry.getValue()));
                                }
                                break;
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveResult implements Runnable {
        private boolean isConnected = false;
        private UserClient userClient;
        private String dataFromClient = "";


        ReceiveResult(UserClient userClient) {
            this.userClient = userClient;
            isConnected = true;
        }

        public void updateMap(String str){
            String DELIMITER = "\f\r";
            List<String> data = Arrays.asList(str.split(DELIMITER));
            try {
                tasks.put(data.get(0), data.get(1));
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveData();
                    updateMap(dataFromClient);
                }
            } catch (IOException e) {
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

    public String receiveData() throws IOException {
        return disWithClient.readUTF();
    }
}
