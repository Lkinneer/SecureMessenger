package com.logan.school;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server{
    Server()throws java.io.IOException {
        System.out.println("please enter the port this server should run on");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        int port = Integer.valueOf(input.readLine());
        ServerSocket socket = new ServerSocket(port);
        System.out.print("The server is running");
        ArrayList<String> messages = new ArrayList<String>();
        List<String> synchronizedMessageList = Collections.synchronizedList(messages);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


        while(true){
            pool.execute(new Handler(socket.accept(), synchronizedMessageList));
        }
    }
    private static class Handler implements Runnable {
        private Socket socket;
        private List<String> messages;
        public Handler(Socket socket, List<String> messages){
            this.socket = socket;
            this.messages = messages;
        }
        public void run(){
            System.out.print("A client has connected");
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                String request = dataInputStream.readUTF();
                System.out.println(request);
                JSONObject jsonSettings = new JSONObject(request);
                String method = jsonSettings.getString("method");
                if(method.equals("get")){
                    System.out.println("the method is get");
                    String name = jsonSettings.getString("user");
                    //DEBUG
                    /*System.out.println("the keys in the rquest json array are:");
                    Iterator<String> keys = jsonSettings.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        System.out.println(key);
                    }*/

                    JSONObject response = new JSONObject();
                    for(String string: messages){
                        JSONObject messagejson = new JSONObject(string);
                        //MORE DEBUG
                        //System.out.println(messages);
                        /*System.out.println(messagejson.toString());
                        System.out.println("the keys in the messages json array are:");
                        keys = messagejson.keys();
                        while(keys.hasNext()) {
                            String key = keys.next();
                            System.out.println(key);
                        }*/

                        String reciever = messagejson.getString("user");
                        if(reciever.equals(name)){
                            response.append("messages",string);
                        }
                    }
                    System.out.println("The response given to the client is:");
                    System.out.println(response.toString());
                    dataOutputStream.writeUTF(response.toString());
                    socket.close();
                    //
                }else if(method.equals("send")){
                    System.out.println("the method is send");
                    //save the message
                    messages.add(request);
                    dataOutputStream.writeUTF("message received");
                    socket.close();
                    //respond with message recieved and close connection
                }else{
                    System.out.println("the method is neither send nor get");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
