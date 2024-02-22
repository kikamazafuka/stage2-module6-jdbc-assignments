package jdbc;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@Setter
public class CustomDataSource implements DataSource {


    private final CustomConnector customConnector;
    private static volatile CustomDataSource instance;
    private final String driver;
    private final String url;
    private final String name;
    private final String password;

    private CustomDataSource(String driver, String url, String password, String name) {
        this.driver = driver;
        this.url = url;
        this.name = name;
        this.password = password;
        this.customConnector = new CustomConnector();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading database driver", e);
        }
    }

    public static CustomDataSource getInstance() {
        Properties properties = new Properties();
        try (InputStream input = CustomDataSource.class.getClassLoader().getResourceAsStream("app.properties")){
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String driver = properties.getProperty("postgres.driver");
        String url = properties.getProperty("postgres.url");
        String username = properties.getProperty("postgres.name");
        String password = properties.getProperty("postgres.password");
        if (instance == null) {
            synchronized (CustomDataSource.class) {
                if (instance == null) {
                    instance = new CustomDataSource(
                            driver,
                            url,
                            username,
                            password);
                }
            }
        }
        return instance;
    }

//    public static CustomDataSource getInstance() {
//        if (instance == null) {
//            throw new IllegalStateException("DataSource not initialized. Call initializeInstance first.");
//        }
//        return instance;
//    }
//
//    public static void initializeInstance(String driver, String url, String name, String password) {
//        if (instance == null) {
//            synchronized (CustomDataSource.class) {
//                if (instance == null) {
//                    instance = new CustomDataSource(driver, url, password, name);
//                }
//            }
//        }
//    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return customConnector.getConnection(url, name, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return customConnector.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
