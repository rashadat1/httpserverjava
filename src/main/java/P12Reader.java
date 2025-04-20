import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class P12Reader {
    KeyStore keyStore;

    public P12Reader() {
        this.keyStore = null;
    }
    public void readP12File() {
        try {
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir"));
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            
            keyStore.load(fis, "changeit".toCharArray());

            this.keyStore = keyStore;

        } catch (KeyStoreException e) {
            System.out.println("Error occurred creating Key Store: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Error occurred while opening keystore.p12 for reading: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error occurred while opening keystore.p12 for reading: " + e.getMessage());
        } catch (CertificateException e) {
            System.out.println("Error occurred while reading keystore.p12 stored certificate: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error occurred while reading / writing from / to keystore.p12 input stream: " + e.getMessage());
        }
    }
}
