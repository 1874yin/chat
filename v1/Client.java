import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        // 检查执行参数是否正确
        if (args.length != 2) {
            System.out.println("Usage: java <filename> <host> <port>");
            System.exit(1);
        }
        String host = args[0];  // 服务端 ip 地址
        int port = Integer.parseInt(args[1]);   // 端口号

        // 和服务端建立连接
        Socket client = null;
        try {
            client = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host, please retry.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Connection failed, please retry.");
            System.exit(1);
        }

        new Thread(new SendThread(client)).start();
        new Thread(new ReceiveThread(client)).start();


    }

    /**
     * 发送线程
     */
    private static class SendThread implements Runnable {

        private static final BufferedReader KEYBOARD_INPUT = new BufferedReader(new InputStreamReader(System.in));
        private PrintStream out;
        private Socket client;
        private String username;

        public SendThread(Socket client) throws IOException {
            this.client = client;
            // 从输出流发送消息
            this.out = new PrintStream(client.getOutputStream(), false, "UTF8");
            this.username = getString("Please enter username.").trim();
            this.out.println(username + " has joined the chat.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    this.out.close();
                    this.client.close();
                    System.out.println("SendThread 资源已经释放");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        @Override
        public void run() {
            System.out.println("You have joined the chat. Type 'bye' to quit.");
            while (true) {
                try {
                    String message = getString("").trim();
                    if (message.equals("bye")) {
                        break;
                    }
                    out.println(username + ": " + message);
                } catch (IOException e) {
                    System.out.println("发送失败");
                    e.printStackTrace();
                }
            }
            try {
                out.println(username + " has quit.");
                this.out.close();
                this.client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("You have quit. Press CTRL+C to leave.");
        }

        private String getString(String prompt) throws IOException {
            if (!"".equals(prompt)) {
                System.out.println(prompt);
            }
            String str;
            str = KEYBOARD_INPUT.readLine();
            return str;
        }

    }

    /**
     * 接收线程
     */
    private static class ReceiveThread implements Runnable {
        private Scanner scanner;

        public ReceiveThread(Socket client) throws IOException {
            // 从输入流获取消息
            this.scanner = new Scanner(client.getInputStream(), "UTF8");
            this.scanner.useDelimiter("\n");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                this.scanner.close();
                System.out.println("ReceiveThread 资源已经释放");
            }));
        }

        @Override
        public void run() {
            while (true) {
                if (this.scanner.hasNext()) {
                    System.out.println(this.scanner.next());
                }
            }
        }

    }
}
