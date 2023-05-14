package ConnectionUtil;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProxoolUtil extends DBUtil {

    private static ProxoolUtil instance = new ProxoolUtil();

    private ProxoolUtil() {
        try {
            JAXPConfigurator.configure("src/proxool.xml", false);
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
    }

    public String getConnectState() {
        try {
//            SnapshotIF snapshotIF = ProxoolFacade.getSnapshot("postgres", true);
            SnapshotIF snapshotIF = ProxoolFacade.getSnapshot("openGauss", true);
            int curActiveCnt = snapshotIF.getActiveConnectionCount();
            int availableCnt = snapshotIF.getAvailableConnectionCount();
            int maxCnt = snapshotIF.getMaximumConnectionCount();
            return String.format("--- Active:%d\tAvailable:%d  \tMax:%d ---",
                    curActiveCnt, availableCnt, maxCnt);
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
        return "visit error";
    }

    public static ProxoolUtil getInstance() {
        return instance;
    }

    public Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection("proxool.openGauss");
//            con = DriverManager.getConnection("proxool.openGauss:postgres://10.211.55.6:7654/pro2");
            //get a connection from pool
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}
