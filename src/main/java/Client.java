import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by zsc on 2016/5/20.
 */
public class Client {
    private ClientInit clientInit = new ClientInit();

    public static void main(String[] args) {
        Client client = new Client();
        client.startConnect();
        new Thread(client.new ReceiveServerMsg()).start();
    }

    public void startConnect() {
        clientInit.connectWithServer();
        System.out.println("连接");
    }

    public void sendReady() {
        try {
            clientInit.sendMsg("Ready");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMethod(Data data) {
        return data.getMethod();
    }

    public String buildResult(String str) {
        String DELIMITER = "\f\r";
        return str + DELIMITER + "y";
    }

    class ReceiveServerMsg implements Runnable {
        //        String data = "";
        Data data;
        MyOperation myOperation;

        @Override
        public void run() {

            try {
                while (true) {
                    sendReady();//先发送Ready
                    data = (Data) clientInit.receiveData();
                    System.out.println("method " + getMethod(data));
                    myOperation = OperationFactory.CreateOperation(getMethod(data));
                    data.setResult(String.valueOf(myOperation.getResult()));
                    clientInit.sendResult(data);
                    Random ra = new Random();
//                    Thread.sleep((ra.nextInt(8) + 1) * 1000);
                    Thread.sleep(300);
                    System.out.println("result  " + String.valueOf(myOperation.getResult()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

class ClientInit {
    private ClientConnectServer clientConnectServerMsg = new ClientConnectServer();
    private ClientConnectServerObject clientConnectServerObject = new ClientConnectServerObject();

    public void connectWithServer() {
        clientConnectServerMsg.connectServer();
        clientConnectServerObject.connectServer2();

    }

    //发送结果
    public void sendResult(Data data) throws IOException {
        clientConnectServerObject.getDosWithServer().writeObject(data);
    }

    //发送Ready信息
    public void sendMsg(String str) throws IOException {
        clientConnectServerMsg.getDosWithServer().writeUTF(str);
    }

    //接收执行指令
    public Object receiveData() throws IOException, ClassNotFoundException {
        return clientConnectServerObject.getDisWithServer().readObject();
    }
}

class ClientConnectServer {
    private DataIO dataIO = new DataIO();

    public void connectServer() {
        try {
            dataIO.setSocket(new Socket("127.0.0.1", 7777));
            dataIO.setDataInputStream(new DataInputStream(dataIO.getSocket().getInputStream()));
            dataIO.setDataOutputStream(new DataOutputStream(dataIO.getSocket().getOutputStream()));
            System.out.println("客户端已连接1");
        } catch (UnknownHostException e) {
            System.out.println("服务端未启动");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("服务端未启动");
            System.exit(1);
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return dataIO.getSocket();
    }

    public DataInputStream getDisWithServer() {
        return dataIO.getDataInputStream();
    }

    public DataOutputStream getDosWithServer() {
        return dataIO.getDataOutputStream();
    }

}

class ClientConnectServerObject {
    private ObjectIO objectIO = new ObjectIO();

    public void connectServer2() {
        try {
            objectIO.setSocket(new Socket("127.0.0.1", 7777));
            objectIO.setObjectInputStream(new ObjectInputStream(objectIO.getSocket().getInputStream()));
            objectIO.setObjectOutputStream(new ObjectOutputStream(objectIO.getSocket().getOutputStream()));
            System.out.println("客户端已连接2");
        } catch (UnknownHostException e) {
            System.out.println("服务端未启动");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("服务端未启动");
            System.exit(1);
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return objectIO.getSocket();
    }

    public ObjectInputStream getDisWithServer() {
        return objectIO.getObjectInputStream();
    }

    public ObjectOutputStream getDosWithServer() {
        return objectIO.getObjectOutputStream();
    }

}

class DataIO {
    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

class ObjectIO {
    private Socket socket = null;
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

class OperationFactory {
    public static MyOperation CreateOperation(String operate) {
        MyOperation myOperation = null;
        switch (operate) {
            case "+":
                myOperation = new OperationAdd();
                break;
            case "-":
                myOperation = new OperationSub();
                break;
            case "*":
                myOperation = new OperationMul();
                break;
            case "/":
                myOperation = new OperationDiv();
                break;
            case "a²":
                myOperation = new OperationSquare();
                break;
            case "√":
                myOperation = new OperationSqrt();
                break;
        }
        return myOperation;
    }
}

class MyOperation {
    private double num_A = 6;
    private double num_B = 5;

    public double getNum_A() {
        return num_A;
    }

    public void setNum_A(double num_A) {
        this.num_A = num_A;
    }

    public double getNum_B() {
        return num_B;
    }

    public void setNum_B(double num_B) {
        this.num_B = num_B;
    }

    public double getResult() {
        double result = 0;
        return result;
    }
}

class OperationAdd extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = getNum_A() + getNum_B();
        return result;
    }
}

class OperationSub extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = getNum_A() - getNum_B();
        return result;
    }
}

class OperationMul extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = getNum_A() * getNum_B();
        return result;
    }
}

class OperationDiv extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = getNum_A() / getNum_B();
        return result;
    }
}

class OperationSqrt extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = Math.sqrt(getNum_A());
        return result;
    }
}

class OperationCube extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = Math.sqrt(getNum_A());
        return result;
    }
}

class OperationSquare extends MyOperation {

    @Override
    public double getResult() {
        double result = 0;
        result = Math.pow(getNum_A(), 2);
        return result;
    }
}

