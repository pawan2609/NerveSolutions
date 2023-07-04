package servsock;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private String serverIP;
    private int serverPort;
    private SecretKeySpec secretKey;
    private Cipher cipher;

    public Client(String serverIP, int serverPort, String encryptionKey) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;


        secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
        try {
            cipher = Cipher.getInstance("AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws Exception {
        try {
            Socket clientSocket = new Socket(serverIP, serverPort);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

       
            String encryptedMessage = encryptMessage(message);
            writer.println(encryptedMessage);

            String response = "";
            do {
                String encryptedServerResponse = reader.readLine();
               
                String serverResponse = decryptMessage(encryptedServerResponse);
                response = serverResponse;

                Thread.sleep(1000);
                System.out.println("Server response: " + serverResponse);
            } while (!response.equals("close"));
            writer.close();
            reader.close();
            clientSocket.close();
        } catch (IOException e) {
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

    public static void main(String[] args) throws Exception {
        String serverIP = "127.0.0.1";
        int serverPort = 1234;
        String encryptionKey = "MySecretKey1234!"; 

        Client client = new Client(serverIP, serverPort, encryptionKey);

       
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Enter the message: ");
            String message = userInputReader.readLine();
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
