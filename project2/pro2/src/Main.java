
import ConnectionUtil.ConnectHandler;
import ConnectionUtil.OrdinaryUtil;
import ConnectionUtil.ProxoolUtil;
import Handler.Controller;

import java.sql.*;
import java.util.List;
import java.util.Properties;


public class Main {

    public static void main(String[] args) {
        try {
            dbRequestArrived(1);

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void dbRequestArrived(int count) {
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                // create a new controller
                Controller controller = new Controller(ProxoolUtil.getInstance());
                // get connection
                controller.getConnection();
                // handle request
                controller.handle();
                // close connection
                controller.closeConnection();
            }).start();
        }
    }
}
