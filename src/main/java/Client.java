import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by zsc on 2016/5/20.
 */
public class Client {
    private ClientInit clientInit = new ClientInit();
    public static void main(String[] args) {
        Client client = new Client();
        client.startConnect();
    }

    public void startConnect(){
        clientInit.connectWithServer();
    }

    public void sendReady(){
        try {
            clientInit.sendMsg("Ready");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReceiveServerMsg implements Runnable {
        String data = "";
        String namePort = "";


        @Override
        public void run() {
            try {
                while (true) {
                    sendReady();//先发送Ready
                    data = clientInit.receiveMsg();

                    if (data.equals("aaa")) {
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

class ClientInit{
    private ClientConnectServer clientConnectServerMsg = new ClientConnectServer();
    private ClientConnectServer clientConnectServerResult = new ClientConnectServer();

    public void connectWithServer(){
        clientConnectServerMsg.connectServer();
        clientConnectServerResult.connectServer();
    }

    //发送结果
    public void sendResult(String str) throws IOException {
        clientConnectServerResult.getDosWithServer().writeUTF(str);
    }

    //发送Ready信息
    public void sendMsg(String str) throws IOException {
        clientConnectServerMsg.getDosWithServer().writeUTF(str);
    }

    //接收执行指令
    public String receiveMsg() throws IOException {
        return clientConnectServerMsg.getDisWithServer().readUTF();
    }
}

class ClientConnectServer {
    private IOUtil ioUtil = new IOUtil();

    public void connectServer() {
        try {
            ioUtil.setSocket(new Socket("127.0.0.1", 30000));
            ioUtil.setDataInputStream(new DataInputStream(ioUtil.getSocket().getInputStream()));
            ioUtil.setDataOutputStream(new DataOutputStream(ioUtil.getSocket().getOutputStream()));
            System.out.println("客户端已连接");
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
        return ioUtil.getSocket();
    }

    public DataInputStream getDisWithServer() {
        return ioUtil.getDataInputStream();
    }

    public DataOutputStream getDosWithServer() {
        return ioUtil.getDataOutputStream();
    }

}

class IOUtil {
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

