package ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DBUtil {
    public abstract Connection getConnection();

    public abstract String getConnectState();

    public void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
                //ordinary: close connection directly
                //proxool: return connection to pool
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
