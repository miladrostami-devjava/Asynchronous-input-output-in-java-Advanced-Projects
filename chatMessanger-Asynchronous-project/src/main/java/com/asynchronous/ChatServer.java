package com.asynchronous;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private AsynchronousServerSocketChannel serverSocketChannel;
    private List<AsynchronousSocketChannel> connectedClients = new ArrayList<>();

    public ChatServer(int port) throws IOException {
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        System.out.println("Server Client started on the port :" + port);
    }

    public void start() {
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                    @Override
                    public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                        // New client registration
                        connectedClients.add(clientChannel);
                        System.out.println("New client Added " + clientChannel);

                        // Ready to accept new connection
                        serverSocketChannel.accept(null, this);

                        // Start reading messages from the new client
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer attachment) {
                                if (result == -1) {
                                    try {
                                        clientChannel.close();
                                        connectedClients.remove(clientChannel);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                                // Prepare received data
                                buffer.flip();
                                String message = new String(buffer.array(), 0, buffer.limit());
                                System.out.println("Received Message :" + message);

                                // Send messages to other clients
                                broadCastMessage(message, clientChannel);

                                // Continue reading from the client
                                buffer.clear();
                                clientChannel.read(buffer, buffer, this);
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer attachment) {
                                exc.printStackTrace();
                                try {
                                    clientChannel.close();
                                    connectedClients.remove(clientChannel);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        exc.printStackTrace();
                        System.out.println(attachment.toString());
                    }
                }

        );
    }
    // Send messages to other clients
    private void broadCastMessage(String message, AsynchronousSocketChannel senderChannel) {
for(AsynchronousSocketChannel clientChannel : connectedClients){
    if (clientChannel != senderChannel){
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer byteBuffer) {
                if (byteBuffer.hasRemaining()){
                    clientChannel.write(buffer,buffer,this);
                }else {
                    System.out.println("Message sent successfully to client :" + clientChannel);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        });
    }
}
    }

    public static void main(String[] args) throws IOException {
ChatServer server = new ChatServer(5000);
server.start();

// Create an ExecutorService to manage server execution
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // This thread runs indefinitely until the server is manually stopped
        executor.submit(()->{
            while (!Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });

    }
}
