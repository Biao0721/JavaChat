import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SqlConnection {

    // 连接数据库
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/users?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    // 用户名及密码
    static final String USER = "root";
    static final String PASS = "******";

    private Connection connection = null;
    private Map<String, String> userInformation = new HashMap<>();

    public SqlConnection(){
        Statement statement = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            connection = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            statement = connection.createStatement();
            String sql;
            sql = "SELECT username, password FROM user";
            ResultSet resultSet = statement.executeQuery(sql);

            // 展开结果集数据库
            while(resultSet.next()){
                // 通过字段检索
                String user = resultSet.getString("username");
                String password = resultSet.getString("password");

                // 输出数据
                System.out.print("user: " + user);
                System.out.print(", password: : " + password);
                System.out.print("\n");

                userInformation.put(user, password);
            }
            // 完成后关闭
            resultSet.close();
            statement.close();
            connection.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(statement!=null) statement.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(connection!=null) connection.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    Map<String, String> getUserInformation() {
        return this.userInformation;
    }

    boolean insertUser(String username, String password) throws SQLException {
        connection = DriverManager.getConnection(DB_URL,USER,PASS);
        PreparedStatement preparedStatement = null;
        Statement statement = null;
        boolean flg = false;

        try{
            String sql = "insert into user(username, password) values(?,?)";
            preparedStatement = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            flg = true;
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(preparedStatement != null) {
                    preparedStatement.close();
                }
            }catch(SQLException se2){
            }
            try{
                if(connection != null){
                    connection.close();
                }
            }catch(SQLException se){
                se.printStackTrace();
            }
            return flg;
        }
    }

    public static void main(String[] args) throws SQLException {
        SqlConnection sqlConnection = new SqlConnection();
//        if (sqlConnection.insertUser("0", "0")) {
//            System.out.println("Success!!!");
//        }
    }
}