import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Login extends JFrame implements ActionListener {
    private Socket socket;
    private PrintStream printStream;
    private BufferedReader bufferedReader;

    private JTextArea usernameJTA = new JTextArea("输入用户：");
    private JTextArea passwordJTA = new JTextArea("输入密码：");
    private JTextField username = new JTextField(10);
    private JPasswordField password = new JPasswordField(10);
    private JButton login = new JButton("登录");
    private JButton registered = new JButton("注册");
    private JButton exit = new JButton("退出");

    public Login() throws Exception {
        this.setLayout(new FlowLayout());
        this.setTitle("Login");
        this.setSize(240, 130);
//        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.add(usernameJTA);
        this.add(username);
        this.add(passwordJTA);
        this.add(password);
        this.add(login);
        this.add(registered);
        this.add(exit);

        login.addActionListener(this);
        registered.addActionListener(this);
        exit.addActionListener(this);

        socket = new Socket("127.0.0.1", 8080);
        printStream = new PrintStream(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == login) {
            String usernameTmp = username.getText();
            String passwordTmp = String.valueOf(password.getPassword());
            printStream.println("Login#" + usernameTmp + "#" + passwordTmp);
            System.out.println("Client: " + "Login#" + usernameTmp + "#" + passwordTmp);
            try {
                String recivedMsg = bufferedReader.readLine();
                System.out.println("Server: " + recivedMsg);
                if (recivedMsg.equals("Login")) {
                    new Client(socket, printStream, bufferedReader, usernameTmp);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "用户名或密码错误", "Error",JOptionPane.ERROR_MESSAGE);
                    username.setText("");
                    password.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (actionEvent.getSource() == registered) {
            try {
                new Registered(socket, printStream, bufferedReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (actionEvent.getSource() == exit) {
            this.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        new Login();
    }
}
