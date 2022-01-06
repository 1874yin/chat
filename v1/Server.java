import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static Map<String, Socket> sockets = new HashMap<>();

    public static void main(String[] args) throws IOException {


        // 检查参数
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>.");
            return;
        }
        // 监听端口
        int port = Integer.parseInt(args[0]);
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Connection failed, please retry.");
            System.exit(1);
        }
        System.out.println("Port: " + port + " opened.");
        System.out.println("Waiting for connections...");

        while (true) {
            Socket client = server.accept();
//            System.out.println("检测到连接：" + client.getRemoteSocketAddress());
            new Thread(new ReceiveThread(client)).start();
//            System.out.println(client.getRemoteSocketAddress());
            sockets.put(client.getRemoteSocketAddress().toString(), client);
        }

    }

    /**
     * 接收线程
     */
    private static class ReceiveThread implements Runnable {
        private Scanner scanner;
        private Socket client;
        private boolean flag = true;

        public ReceiveThread(Socket client) throws IOException {
            this.client = client;
            // 从输入流获取消息
            this.scanner = new Scanner(client.getInputStream());
            this.scanner.useDelimiter("\n");
        }

        @Override
        public void run() {
            PrintStream out;
            while (this.flag) {
                if (this.scanner.hasNext()) {
                    String val = this.scanner.next();
                    System.out.println(val);
                    for (Socket socket : sockets.values()) {
                        if (this.client == socket) {
                            continue;
                        }
                        try {
                            out = new PrintStream(socket.getOutputStream());
                            out.println(val.trim());
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (goodbye(val.trim())) destroySocket();

                }
            }
        }

        private void destroySocket() {
            System.out.println(this.client.getRemoteSocketAddress() + " destroyed.");;
            try {
                this.flag = false;
                sockets.remove(this.client.getRemoteSocketAddress().toString());
                this.scanner.close();
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private boolean goodbye(String str) {
            return str.matches(".+has quit.");
        }
    }

}
