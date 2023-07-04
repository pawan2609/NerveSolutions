package servsock;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Server {
    private ServerSocket serverSocket;
    private Map<String, Map<String, Integer>> data;
    private SecretKeySpec secretKey;
    private Cipher cipher;

    public Server(int port, String encryptionKey) {
        try {
            serverSocket = new ServerSocket(port);
            data = new HashMap<>();
            initializeData();

            
            secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Server started and listening on port " + serverSocket.getLocalPort());
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeData() {

        Map<String, Integer> setA = new HashMap<>();
        setA.put("One", 1);
        setA.put("Two", 2);
        data.put("SetA", setA);

        Map<String, Integer> setB = new HashMap<>();
        setB.put("Three", 3);
        setB.put("Four", 4);
        data.put("SetB", setB);

        Map<String, Integer> setC = new HashMap<>();
        setC.put("Five", 5);
        setC.put("Six", 6);
        data.put("SetC", setC);

        Map<String, Integer> setD = new HashMap<>();
        setD.put("Seven", 7);
        setD.put("Eight", 8);
        data.put("SetD", setD);

        Map<String, Integer> setE = new HashMap<>();
        setE.put("Nine", 9);
        setE.put("Ten", 10);
        data.put("SetE", setE);
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientMessage = reader.readLine();
                System.out.println("Received message from client: " + clientMessage);

                
                String decryptedMessage = decryptMessage(clientMessage);
                System.out.println("Decrypted message from client: " + decryptedMessage);

                String[] parts = decryptedMessage.split("-");
                if (parts.length != 2) {
                    writer.println("Invalid message format");
                } else {
                    String key = parts[0];
                    String value = parts[1];

                    if (data.containsKey(key)) {
                        Map<String, Integer> subset = data.get(key);
                        if (subset.containsKey(value)) {
                            int numTimes = subset.get(value);
                            for (int i = 0; i < numTimes; i++) {
                                String encryptedResponse = encryptMessage(getCurrentTime());
                                writer.println(encryptedResponse);

                                Thread.sleep(1000);
                            }
                            writer.println(encryptMessage("close"));
                            writer.close();
                            reader.close();
                            clientSocket.close();
                            return;
                        }
                    }

                    writer.println(encryptMessage("EMPTY"));
                }

                writer.close();
                reader.close();
                clientSocket.close();
                System.out.println("Connection with client closed: " + clientSocket.getInetAddress().getHostAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String encryptMessage(String message) throws Exception {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }

        private String decryptMessage(String encryptedMessage) throws Exception {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        }

        private String getCurrentTime() {
            return new java.util.Date().toString();
        }
    }

    public static void main(String[] args) {
        int port = 1234; 
        String encryptionKey = "MySecretKey1234!";

        Server server = new Server(port, encryptionKey);
        server.start();
    }
}
