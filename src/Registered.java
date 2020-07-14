import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;

public class Registered extends JFrame implements ActionListener {
    private Socket socket = null;
    private PrintStream printStream = null;
    private BufferedReader bufferedReader = null;

    private JTextArea usernameJTA = new JTextArea("请您输入姓名");
    private JTextArea password1JTA = new JTextArea("请您输入密码");
    private JTextArea password2JTA = new JTextArea("再次确认密码");
    private JTextField username = new JTextField(10);
    private JPasswordField password1 = new JPasswordField(10);
    private JPasswordField password2 = new JPasswordField(10);
    private JButton registered = new JButton("注册");
    private JButton exit = new JButton("退出");

    public Registered(Socket socket, PrintStream printStream, BufferedReader bufferedReader) throws Exception {
        this.socket = socket;
        this.printStream = printStream;
        this.bufferedReader = bufferedReader;

        this.setTitle("Registered");
        this.setSize(240, 150);
        this.setLayout(new FlowLayout());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.add(usernameJTA);
        this.add(username);
        this.add(password1JTA);
        this.add(password1);
        this.add(password2JTA);
        this.add(password2);
        this.add(registered);
        this.add(exit);

        registered.addActionListener(this);
        exit.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == registered) {
            String usernameTmp = username.getText();
            String password1Tmp = String.valueOf(password1.getPassword());
            String password2Tmp = String.valueOf(password2.getPassword());
            System.out.println(password1Tmp + password2Tmp);
            if (password1Tmp.equals(password2Tmp)) {
                printStream.println("Registered#" + usernameTmp + "#" + password1Tmp);
                System.out.println("Client: " + "Registered#" + usernameTmp + "#" + password1Tmp);
                try {
                    String recivedMsg = bufferedReader.readLine();
                    if (recivedMsg.equals("Registered")) {
                        JOptionPane.showMessageDialog(this, "注册成功");
                        this.dispose();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "输入密码不一致", "Error",JOptionPane.ERROR_MESSAGE);
            }
        } else if (actionEvent.getSource() == exit) {
            this.dispose();
        }
    }

    public static void main(String[] args) {
//        new Registered();
    }

}
