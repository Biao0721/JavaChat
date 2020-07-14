import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client extends JFrame implements ActionListener, Runnable {
    private JList userNameList = new JList();
    private JTextArea jTextArea = new JTextArea();
    private JTextArea jTextAreaUserName = new JTextArea();
    private JTextField jTextField = new JTextField();
    private JScrollPane jScrollPane = new JScrollPane();
    private JPanel jPanel = new JPanel();
    private JScrollPane textjScrollPane =new JScrollPane();
    
    private String userName;
    private String friendName = "Group Chat";

    private Socket socket = null;
    private PrintStream printStream = null;
    private BufferedReader bufferedReader = null;

    private Map<String, String> chatHistory = new HashMap<>();

    public Client(Socket socket, PrintStream printStream, BufferedReader bufferedReader, String userName) throws Exception{
        this.socket = socket;
        this.printStream = printStream;
        this.bufferedReader = bufferedReader;
        this.userName = userName;

        this.setTitle(userName);
        this.setLayout(new BorderLayout());
        this.add(jScrollPane, BorderLayout.WEST);
        this.add(jPanel, BorderLayout.CENTER);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                printStream.println("Offline#" + userName);
            }

        });

        chatHistory.put("Group Chat", "#");
        chatHistory.put(userName, "#");

        jPanel.setLayout(new BorderLayout());
        jPanel.add(jTextAreaUserName, BorderLayout.NORTH);
        jPanel.add(textjScrollPane, BorderLayout.CENTER);
        jPanel.add(jTextField, BorderLayout.SOUTH);

        jScrollPane.setPreferredSize(new Dimension(200, 200));
        jScrollPane.setViewportView(userNameList);

        textjScrollPane.setBounds(20,20,100,50);
        textjScrollPane.setViewportView(jTextArea);

        jTextField.addActionListener(this);

        jTextField.setFont(new Font("黑体", 1, 20));
        jTextArea.setFont(new Font("宋体", 0, 20));
        jTextAreaUserName.setFont(new Font("黑体", 1, 20));
        userNameList.setFont(new Font("Times New Roman", 1, 25));

        jTextAreaUserName.setText(friendName);
        jTextAreaUserName.setBackground(Color.GRAY);
        jTextAreaUserName.setEnabled(false);

        printStream.println("UserName#" + userName);
        System.out.println("Client: " + "UserName#" + userName);
        new Thread(this).start();

        userNameList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if(!userNameList.getValueIsAdjusting()){	//设置只有释放鼠标时才触发
                    if (!userNameList.getSelectedValue().toString().equals(userName)){
                        changeFriendName(userNameList.getSelectedValue().toString());
                    }
                }
            }
        });
    }

    private void changeFriendName(String s){
        friendName = s;
        jTextAreaUserName.setText(friendName);
        updateTextArea(friendName);
    }

    private void updateTextArea(String name) {
        jTextArea.setText("");
        String[] history = chatHistory.get(name).split("#");
        for (String item : history) {
            jTextArea.append(item + "\n");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String recivedMsg = bufferedReader.readLine();
                System.out.println("Server: " + recivedMsg);
                String[] tmp = recivedMsg.split("#");
                if (tmp[0].equals("UserName")){ // USERNAME#开头时，表示更新用户信息
                    String[] names = new String[tmp.length];
                    for (int i = 1; i < tmp.length; i++) {
                        if (tmp[i].equals(userName)) {
                            continue;
                        }
                        if (!chatHistory.containsKey(tmp[i])) {
                            chatHistory.put(tmp[i], "#");
                        }
                        names[i] = tmp[i];
                    }
                    names[0] = "Group Chat";
                    userNameList.setListData(names);
                } else if (tmp[0].equals("OFFLINE")) {
                    printStream.println("CHECK#");
                    System.out.println("Client: CHECK");
                    JOptionPane.showMessageDialog(null, "该用户已下线", "Error",JOptionPane.ERROR_MESSAGE);
                    this.dispose();
                    return;
                } else { // 当以其他开头是，表示获取聊天字段

                    /* tmp[1].equals(userName)表示接收者为自己，则显示信息
                     *  tmp[1].equals("Group Chat")表示接收者为群组，则显示信息
                     *  tmp[0].equals(userName)表示发送者是自己，则显示信息*/

                    if (tmp[1].equals(userName)){
                        chatHistory.put(tmp[0], chatHistory.get(tmp[0]) + "#" + tmp[0] + ":" + tmp[2]);
                        if (friendName.equals(tmp[0])) { updateTextArea(tmp[0]); }
                    } else if (tmp[0].equals(userName)) {
                        chatHistory.put(tmp[1], chatHistory.get(tmp[1]) + "#" + tmp[0] + ":" + tmp[2]);
                        updateTextArea(tmp[1]);
                    } else if ( tmp[1].equals("Group Chat")) {
                        chatHistory.put("Group Chat", chatHistory.get("Group Chat") + "#" + tmp[0] + ":" + tmp[2]);
                        if (friendName.equals("Group Chat")) { updateTextArea("Group Chat"); }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        printStream.println(userName + "#" + friendName + "#" + jTextField.getText());
        System.out.println("Client: " + userName + "#" + friendName + "#" + jTextField.getText());
        jTextField.setText("");
    }

    public static void main(String[] args) throws Exception {

    }
}