package ConnectionUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OrdinaryUtil extends DBUtil {
    private static OrdinaryUtil instance = new OrdinaryUtil();
    private String host = "10.211.55.6";
    private String dbname = "pro2";
    private String user = "coopergauss";
    private String pwd = "cooper@0517";
    private String port = "7654";

    private OrdinaryUtil() {
    }

    public static OrdinaryUtil getInstance() {
        return instance;
    }


    public Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

            return DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    @Override
    public String getConnectState() {
        return "null";
    }
}
