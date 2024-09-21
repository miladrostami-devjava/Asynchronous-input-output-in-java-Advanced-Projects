package com.asynchronous;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

public class ChatClient {

    private AsynchronousSocketChannel clientChannel;

    public ChatClient(String host, int port) throws IOException {
        clientChannel = AsynchronousSocketChannel.open();
        clientChannel.connect(new InetSocketAddress(host, port), null, new CompletionHandler<Void, Void>() {

            @Override
            public void completed(Void result, Void attachment) {
                System.out.println("Connected to Server!");
                listenToMessages();
            }



            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
    }


    private void listenToMessages() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                buffer.flip();
                String message = new String(buffer.array(),0,buffer.limit());
                System.out.println("Received message : " + message);
                buffer.clear();
                listenToMessages();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
exc.printStackTrace();
            }
        });
    }


    public void sendMessage(String message){
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (attachment.hasRemaining()){
                    clientChannel.write(buffer,buffer,this);
                }else {
                    System.out.println("message sent successfully to client :" + clientChannel);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
exc.printStackTrace();
            }
        });
    }


    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",5000);
        Scanner input = new Scanner(System.in);
        while (true){
            System.out.println("Please Enter the messages :");
            String message = input.nextLine();
            client.sendMessage(message);
        }
    }


}
