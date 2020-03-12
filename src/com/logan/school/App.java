
package com.logan.school;

import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeySpecException, ClassNotFoundException {
        /*PublicKey publickey;
        PrivateKey privateKey;
        int keylength = 2048;
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keylength);
        KeyPair pair = keyGen.generateKeyPair();
        publickey = pair.getPublic();
        privateKey = pair.getPrivate();
        //System.out.println("the public key is: " + publickey);
        //System.out.println("the private key is: " + privateKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        //System.out.println(Arrays.toString("hello this is a string".getBytes()));
        byte[] encrypted = cipher.doFinal("ng".getBytes());
        //System.out.println(Arrays.toString(encrypted));
        //System.out.println(encrypted.length);
        byte[] privateKeyBytes = privateKey.getEncoded();
        //System.out.println(privateKeyBytes.length);
        System.out.println(privateKey + "hello");
        FileOutputStream fileOut = new FileOutputStream("privatekey");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOut);
        objectOutputStream.writeObject(privateKey);
        objectOutputStream .close();

        FileInputStream fileInputStream = new FileInputStream("privatekey");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        PrivateKey gottenkey =(PrivateKey) objectInputStream.readObject();

        if(gottenkey.equals(privateKey)){
            System.out.println("these keys are the same");
        }
        //privateKey = KeyFactory.getInstance("RSA").generatePrivate(new X509EncodedKeySpec(privateKeyBytes));
        System.out.println(gottenkey + "hello");
        */




        File settings = new File("settings");
        if(settings.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(settings));
            String type = reader.readLine();
            if(type.equals("Server")){
                System.out.println("According to the settings file this is a server");
                Server server = new Server();
            }else if(type.equals("Client")){
                System.out.println("According to the settings file this is a client");
                Client client = new Client();
            }else{
                System.out.println("The settings file is mis-formatted");
            }
        }else{

            System.out.println("please choose if this is a server or a client:");

            System.out.println("1) Server");
            System.out.println("2) Client");
            String answer = new String();

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                try {
                    answer = input.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (answer.equals("1")) {
                    settings.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(settings));
                    writer.write("Server");
                    writer.close();
                    Server server = new Server();
                    break;
                } else if (answer.equals("2")) {
                    settings.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(settings));
                    writer.write("Client");
                    writer.close();
                    Client client = new Client();
                    break;
                } else {
                    System.out.println("please enter 1 or 2");
                }
            }
        }
    }
}
