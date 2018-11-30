import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.util.Base64;

public class RSA_Signature
{
    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSA_Signature(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keylength);
    }

    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {

        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    // method to sign message - SENDER METHOD
    public String sign(String data) throws InvalidKeyException, Exception{
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(this.getPrivateKey());
        rsa.update(data.getBytes());
        return Base64.getEncoder().encodeToString(rsa.sign());
//        return Base64.getEncoder().encode(sin).toString();
    }

    // method to verify signature - RECEIVER METHOD
    private boolean verifySignature(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(Base64.getDecoder().decode(signature.getBytes()));
    }


    public static void main(String[] args) throws Exception
    {
        RSA_Signature gk;
        gk = new RSA_Signature(2048); // may use 1024, but using 2048 for better security
        gk.createKeys();
//        may use these for accessing the keys
//        gk.getPrivateKey();
//        gk.getPublicKey();
        System.out.println("Public Key: "+ gk.getPublicKey()); // just for testing

//        testing signing of message
        String msg = "Hello";
        String signature = gk.sign(msg).toString();
        System.out.println("Message : " + msg);
        System.out.println(signature);

//        testing verification of signature
        System.out.println(gk.verifySignature(msg, signature, gk.getPublicKey()));
    }
}
