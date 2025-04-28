package sslUtility;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvLoader {
    public static void loadEnv(String path) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(path));
            for (String name : props.stringPropertyNames()) {
                System.setProperty(name, props.getProperty(name));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
