package com.logan.school;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;

import java.net.UnknownHostException;
import java.security.*;

public class Client {
    private String address;
    private int port;
    private String clientName;
    private Socket socket = null;
    private File clientSettings = new File("clientSettings");

    private PublicKey publicKey;
    private PrivateKey privateKey;
    int keylength = 2048;

    ArrayList<User> friends = new ArrayList<User>();
    Set<String> JSONMessages = new HashSet<String>();

    public Client() throws IOException, NoSuchAlgorithmException, ClassNotFoundException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        if (clientSettings.exists()){
            runClient();
        }else{
            settup();
            runClient();
        }
    }
    void settup() throws IOException, NoSuchAlgorithmException {
        //set up the server for this client
        System.out.println("please enter the url of this clients home server");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        address = input.readLine();
        System.out.println("please enter the port of this clients home server");
        port = Integer.valueOf(input.readLine());
        System.out.println("please enter your name");
        clientName = input.readLine();
        generateKeys();

        String jsonString = new JSONObject().put("address",address).put("port",port).put("clientName",clientName).toString();
        clientSettings.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(clientSettings));
        writer.write(jsonString);
        writer.close();
    }
    void readSettings() throws IOException, ClassNotFoundException {
        restoreKeys();
        BufferedReader input = new BufferedReader(new FileReader(clientSettings));
        String jsonString = input.readLine();
        System.out.println("the string recoverd is " + jsonString);
        JSONObject jsonSettings = new JSONObject(jsonString);
        port = jsonSettings.getInt("port");
        address = jsonSettings.getString("address");
        clientName = jsonSettings.getString("clientName");
        System.out.println(address);
    }
    void runClient() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException {
        readSettings();
        String answer;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            System.out.println("1) add a friend");
            System.out.println("2) send a message");
            System.out.println("3) download your messages");
            System.out.println("4) view your messages");
            System.out.println("5) exit");
            answer = input.readLine();
            if (answer.equals("1")){
                addUser();
            }else if(answer.equals("2")){
                sendMessage();
            }else if(answer.equals("3")){
                getMessgaes();
            }else if(answer.equals("4")){
                viewMessages();
            }else if(answer.equals("5")){
                System.exit(0);
            }else{
                System.out.println("please choose a number");
            }
        }
    }
    void generateKeys() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keylength);
        KeyPair pair = keyGen.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();

        FileOutputStream fileOut = new FileOutputStream("privatekey");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOut);
        objectOutputStream.writeObject(privateKey);
        objectOutputStream .close();

        fileOut = new FileOutputStream("publickey");
        objectOutputStream = new ObjectOutputStream(fileOut);
        objectOutputStream.writeObject(publicKey);
        objectOutputStream .close();


    }

    void restoreKeys() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("privatekey");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        privateKey =(PrivateKey) objectInputStream.readObject();

        fileInputStream = new FileInputStream("publickey");
        bufferedInputStream = new BufferedInputStream(fileInputStream);
        objectInputStream = new ObjectInputStream(bufferedInputStream);
        publicKey =(PublicKey) objectInputStream.readObject();
    }

    //retrieve the users to be added's public key from disk
    void addUser() throws IOException {
        PublicKey testPublicKey;
        System.out.println("Please enter the path to the public key of the user you wish to add");
        String answer;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        answer = input.readLine();
        try{
            FileInputStream fileInputStream = new FileInputStream(answer);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
            testPublicKey =(PublicKey) objectInputStream.readObject();
            System.out.println("please enter the name for this user.");
            answer = input.readLine();
            System.out.println("please input the home server address for this user.");
            String homeServerURL = input.readLine();
            System.out.println("Please input the port of the user's home server.");
            int homeServerPort = Integer.valueOf(input.readLine());
            friends.add(new User(answer, testPublicKey,homeServerURL,homeServerPort));
        }catch(Exception ex){
            System.out.println("adding user failed");
            System.out.println(ex);
        }
    }
    void sendMessage() throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        System.out.println("sendmessage");
        System.out.println("please enter the name of the person you wish to send a message to.");
        for(User user : friends){
            System.out.println(user.getUserName());
        }
        String answer;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        answer = input.readLine();
        User selectedUser = null;
        for(User user : friends){
            if(user.getUserName().equals(answer)){
                selectedUser = user;
            }
        }
        if(selectedUser == null){
            System.out.println("that user is not in your friends list");
            return;
        }
        System.out.println("please enter your message");
        answer = input.readLine();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, selectedUser.getPublicKey());
        byte[] encrypted = cipher.doFinal(answer.getBytes());

        //String encryptedString = DatatypeConverter.parseBase64Binary(encrypted);
        String encryptedString = Base64.getEncoder().encodeToString(encrypted);
        //String encryptedString = "stuff";
        String jsonString = new JSONObject().put("method","send").put("user",selectedUser.getUserName()).put("message",encryptedString).toString();

        //send the message
        DataInputStream inputStream;
        DataOutputStream outputStream;
        try {
            socket = new Socket(selectedUser.getServerURL(), selectedUser.getServerPort());
            System.out.println("Connected");
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());


            outputStream.writeUTF(jsonString);
            outputStream.flush();
            String response = inputStream.readUTF();
            System.out.println(response);
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception u) {
            System.out.println(u);
            System.out.println("Sending of the message failed");
        }

    }
    void getMessgaes() throws IOException {
        System.out.println("getmessages");

        /*System.out.println("Please enter your username");
        String username;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        username = input.readLine();*/

        String jsonString = new JSONObject().put("method","get").put("user",clientName).toString();
        DataInputStream inputStream;
        DataOutputStream outputStream;
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());


            outputStream.writeUTF(jsonString);
            outputStream.flush();
            String response = inputStream.readUTF();
            System.out.println("The raw data received was :\n\n");
            System.out.println(response);
            JSONObject messages = new JSONObject(response);
            JSONArray messageList = messages.getJSONArray("messages");
            System.out.println("The server returned : "+messageList.length()+" messages.");
            for (int i = 0; i<messageList.length(); i++){
                JSONMessages.add(messageList.getString(i));
            }
            //ArrayList<String> = messages.getJSONArray("")
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception u) {
            System.out.println(u);
            System.out.println("failed to get messages");
        }

    }
    void viewMessages(){
        System.out.println("viewmessages");
        Iterator<String> itr = JSONMessages.iterator();
        while(itr.hasNext()){
            JSONObject currentMessage = new JSONObject(itr.next());
            String messageText = currentMessage.getString("message");
            System.out.println("Attempting to decrypt message: "+messageText);
            try {
                byte[] encryptedString = Base64.getDecoder().decode(messageText);
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] decrypted = cipher.doFinal(encryptedString);
                System.out.println(new String(decrypted));
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                //e.printStackTrace();
                System.out.println("Failed to decrypt message: "+messageText);
            }
        }
    }
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        Client client = new Client();
    }
}
