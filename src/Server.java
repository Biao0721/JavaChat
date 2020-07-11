import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends JFrame implements ActionListener, Runnable{
    public ArrayList<ChatThread> arrayList = new ArrayList<>();

    private JTextArea jTextArea = new JTextArea();
    private JList jList = new JList();
    private JScrollPane jScrollPane = new JScrollPane();
    private JTextField jTextField = new JTextField();
    private JPanel jPanel = new JPanel();

    public Server(){
        this.setLayout(new BorderLayout(5, 5));
        this.add(jScrollPane, BorderLayout.WEST);
        this.add(jPanel, BorderLayout.CENTER);
        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Server");
        this.setLocationRelativeTo(null);

        jPanel.setLayout(new BorderLayout());
        jPanel.add(jTextArea, BorderLayout.CENTER);
        jPanel.add(jTextField, BorderLayout.SOUTH);

        jTextField.setFont(new Font("黑体", 1, 20));
        jTextArea.setFont(new Font("宋体", 0, 20));
        jList.setFont(new Font("Times New Roman", 1, 25));


        jScrollPane.setPreferredSize(new Dimension(200, 200));
        jScrollPane.setViewportView(jList);
        jTextField.addActionListener(this);

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                Socket socket = serverSocket.accept();
                ChatThread chatThread = new ChatThread(socket);
                chatThread.start();
                arrayList.add(chatThread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ChatThread extends Thread {
        public BufferedReader bufferedReader;
        public PrintStream printStream;
        public String userName;
        public volatile boolean stop = false;

        public ChatThread(Socket socket) throws Exception {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printStream = new PrintStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            while (true){
                try {
                    String string = bufferedReader.readLine();

                    System.out.println(string);
                    if (string.startsWith("UserName#")) {
                        String[] tmp = string.split("#");
                        userName = tmp[1];
                        string = "UserName#";
                        int flag = 0;
                        String[] names = new String[arrayList.size()];
                        for (int i = 0; i < arrayList.size(); i++) {
                            names[flag] = arrayList.get(i).userName;
                            string = string + arrayList.get(i).userName + "#";
                            flag += 1;
                        }
                        jList.setListData(names);
                        jTextArea.append(userName + "已上线\n");
                    } else if (string.equals("CHECK#")) {
                        break;
                    } else if (string.startsWith("Offline")){
                        String[] tmp = string.split("#");
                        offlineUser(tmp[1]);
                        continue;
                    } else{
                        jTextArea.append(string.split("#")[0] + "(" + string.split("#")[1] + "): " + string.split("#")[2] + "\n");
                    }
                    for (int i = 0; i < arrayList.size(); i++) {
                        arrayList.get(i).printStream.println(string);
                        System.out.println(arrayList.get(i).userName + string);
                    }

//                    this.printStream.println(string);
//                    System.out.println(this.userName + string);

                } catch (Exception e) {
                    break;
//                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String jTextFieldString = jTextField.getText();

        if (jTextField.getText().startsWith("offline")) {
            offlineUser(jTextFieldString.split(" ")[1]);
        } else {
            for (int i = 0; i < arrayList.size(); i++) {
                arrayList.get(i).printStream.println("Group Chat#" + "Group Chat#" + jTextFieldString);
                System.out.println(arrayList.get(i).userName + "(send)Group Chat#" + "Group Chat#" + jTextFieldString);
            }
            jTextArea.append("Group Chat: " + jTextFieldString + "\n");
        }

        jTextField.setText("");
    }

    public void offlineUser(String name) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).userName.equals(name)){
                arrayList.get(i).stop = true;
                arrayList.get(i).printStream.println("OFFLINE#");
                arrayList.remove(i);
                break;
            }
        }
        String string = "UserName#";
        String[] names = new String[arrayList.size()];

        // 向所有已开启进程更新在线状态
        for (int j = 0; j < arrayList.size(); j++) {
            string = string + arrayList.get(j).userName + "#";
            names[j] = arrayList.get(j).userName;
        }

        for (String s: names) {
            System.out.println(s);
        }

        jList.setListData(names);

        for (int k = 0; k < arrayList.size(); k++) {
            System.out.println(arrayList.get(k) + string);
            arrayList.get(k).printStream.println(string);
        }

        jTextArea.append(name + "已下线\n");
    }

    public static void main(String[] args) {
        new Server().setVisible(true);
    }
}