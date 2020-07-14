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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server extends JFrame implements ActionListener, Runnable{
    public ArrayList<ChatThread> arrayList = new ArrayList<>();

    public  Map<String, String> userInformation;
    private SqlConnection sqlConnection = new SqlConnection();

    private JTextArea jTextArea = new JTextArea();
    private JList jList = new JList();
    private JScrollPane jScrollPane = new JScrollPane();
    private JTextField jTextField = new JTextField();
    private JPanel jPanel = new JPanel();
    private JScrollPane textjScrollPane =new JScrollPane();

    public Server(){
        userInformation = sqlConnection.getUserInformation();

        this.setLayout(new BorderLayout( ));
        this.add(jScrollPane, BorderLayout.WEST);
        this.add(jPanel, BorderLayout.CENTER);
        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Server");
        this.setLocationRelativeTo(null);

        jPanel.setLayout(new BorderLayout());
        jPanel.add(textjScrollPane, BorderLayout.CENTER);
        jPanel.add(jTextField, BorderLayout.SOUTH);

        jTextField.setFont(new Font("黑体", 1, 20));
        jTextArea.setFont(new Font("宋体", 0, 20));
        jList.setFont(new Font("Times New Roman", 1, 25));


        jScrollPane.setPreferredSize(new Dimension(200, 200));
        jScrollPane.setViewportView(jList);

        textjScrollPane.setBounds(20,20,100,50);
        textjScrollPane.setViewportView(jTextArea);

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
                    System.out.println("Client: " + string);

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
                    } else if (string.startsWith("Login")) {
                        String[] tmp = string.split("#");
                        boolean flg = false;

                        Iterator keys = userInformation.keySet().iterator();
                        while(keys.hasNext()){
                            String key = (String)keys.next();

                            if(tmp[1].equals(key) && tmp[2].equals(userInformation.get(tmp[1]))){
                                flg = true;
                            }
                        }
                        if (flg) {
                            printStream.println("Login");
                            System.out.println("Server: Login");
                        } else {
                            printStream.println("Error");
                            System.out.println("Server: Error");
                        }
                        continue;
                    } else if (string.startsWith("Registered")) {
                        String[] tmp = string.split("#");
                        userInformation.put(tmp[1], tmp[2]);
                        if (sqlConnection.insertUser(tmp[1], tmp[2])) {
                            printStream.println("Registered");
                            System.out.println("Server: Registered");
                        }
                        jTextArea.append("|" + tmp[1] + "\t|" + tmp[2] + "\t|\n");
                        continue;
                    } else {
                        jTextArea.append(string.split("#")[0] + "(" + string.split("#")[1] + "): " + string.split("#")[2] + "\n");
                    }
                    for (int i = 0; i < arrayList.size(); i++) {
                        arrayList.get(i).printStream.println(string);
                    }
                    System.out.println("Server: " + string);

                } catch (Exception e) {
                    break;
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String jTextFieldString = jTextField.getText();

        if (jTextField.getText().startsWith("offline")) {
            offlineUser(jTextFieldString.split(" ")[1]);
        } else if (jTextField.getText().equals("sql")) {
            Iterator keys = userInformation.keySet().iterator();
            while(keys.hasNext()){
                String key = (String)keys.next();
                jTextArea.append("----------------------------\n");
                jTextArea.append("|" + key + "\t|" + userInformation.get(key) + "\t|\n");
            }
            jTextArea.append("----------------------------\n");
        } else {
            for (int i = 0; i < arrayList.size(); i++) {
                arrayList.get(i).printStream.println("Group Chat#" + "Group Chat#" + jTextFieldString);
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

        jList.setListData(names);

        for (int k = 0; k < arrayList.size(); k++) {
            arrayList.get(k).printStream.println(string);
        }

        jTextArea.append(name + "已下线\n");
    }

    public static void main(String[] args) {
        new Server().setVisible(true);
    }
}