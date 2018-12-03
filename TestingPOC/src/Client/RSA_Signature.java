package Client;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class RSA_Signature
{
    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSA_Signature(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
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
    public String sign(String data, RSA_Signature g) throws InvalidKeyException, Exception{
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(g.getPrivateKey());
        rsa.update(data.getBytes());
        return Base64.getEncoder().encodeToString(rsa.sign());
//        return Base64.getEncoder().encode(sin).toString();
    }

    public static String storePublicKey (PublicKey publ) throws GeneralSecurityException
    {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
        return Base64.getEncoder().encodeToString(spec.getEncoded());
    }

    public static PublicKey loadPublicKey(String stored) throws GeneralSecurityException
    {
        byte[] data = Base64.getDecoder().decode(stored);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

    // public PublicKey getKey(String key)
    // {
    //     try
    //     {
    //         byte[] byteKey = Base64.getDecoder().decode(key.getEncoded());
    //         X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
    //         KeyFactory kf = KeyFactory.getInstance("RSA");

    //         return kf.generatePublic(X509publicKey);
    //     }
    //     catch(Exception e)
    //     {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }

    // method to verify signature - RECEIVER METHOD
    public boolean verifySignature(String data, String signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        // System.out.println("Signature inside verify(): " + signature);
        // System.out.println("Public Key inside veriffy: " + publicKey);
        // System.out.println("data inside verify: " + data);
        // System.out.println("Base64: " + Base64.getDecoder().decode(signature));
        // System.out.println("getBytes(): " + signature.getBytes());

        return sig.verify(Base64.getDecoder().decode(signature));
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
        String signature = gk.sign(msg, gk).toString();
        System.out.println("Message : " + msg);
        System.out.println(signature);

//        testing verification of signature
        System.out.println(gk.verifySignature(msg, signature, gk.getPublicKey()));
    }
}
