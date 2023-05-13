
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;


public class Main {

    public static void main(String[] args) {
        ConnectHandler conHandler = new ConnectHandler();
        // load user info from file
        Properties prop = conHandler.loadGaussUser();
        // connect to openGauss
        conHandler.openGauss(prop);
        // get connection
        Connection con = conHandler.getCon();
        // create controller
        Controller controller = new Controller(con);
        controller.handle();
        // close connection
        conHandler.closeDB();
    }
}
