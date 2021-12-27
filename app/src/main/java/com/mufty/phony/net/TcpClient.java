package com.mufty.phony.net;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    private String serverIp;
    private int serverPort;
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedInputStream mBufferIn;

    public TcpClient(OnMessageReceived listener){
        mMessageListener = listener;
    }

    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mBufferOut != null){
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void stopClient(){
        mRun = false;

        if(mBufferOut != null){
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run(String ip, int port){
        if(ip != null)
            serverIp = ip;
        if(port != 0)
            serverPort = port;

        mRun = true;

        try {
            InetAddress serverAddr = InetAddress.getByName(serverIp);

            Log.d("TCP Client", "C: Connecting...");

            Socket socket = new Socket(serverAddr, serverPort);

            try {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedInputStream(socket.getInputStream());

                //buffer 16bit size 16384
                byte[] buffer = new byte[16384];

                while(mRun){
                    int bytesRead = mBufferIn.read(buffer);
                    Log.d("TCP Client", "Bytes read: " + bytesRead);
                    //mServerMessage = mBufferIn.read(buffer);

                    if(bytesRead > 0 && mMessageListener != null){
                        mMessageListener.messageReceived(buffer);
                    }
                }

                Log.d("RESPONSE FROM SERVER", "S: Received Message '" + mServerMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    public interface OnMessageReceived {
        public void messageReceived(byte[] message);
    }
}
