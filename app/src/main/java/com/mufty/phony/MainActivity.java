package com.mufty.phony;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.mufty.phony.net.TcpClient;

public class MainActivity extends AppCompatActivity {

    TcpClient tcpClient;
    private static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ConnectTask().execute("");

        //init();
    }

    public void init() {
        int i = 0;
        int minBufferSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int sampleRate = 48000;
        byte[] music = null;

        AudioTrack at = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA) // USAGE_VOICE_COMMUNICATION
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // CONTENT_TYPE_SPEECH
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(minBufferSize)
                .build();

        /*try{
            music = new byte[512];
            at.play();

            while((i = is.read(music)) != -1)
                at.write(music, 0, i);

        } catch (IOException e) {
            e.printStackTrace();
        }

        at.stop();
        at.release();*/
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... strings) {
            //we create a TCPClient object
            tcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(byte[] message) {
                    Log.d( LOG_TAG, "Got message: " + message );
                    //this method calls the onProgressUpdate
                    //publishProgress(message);
                    publishProgress("");
                }
            });
            tcpClient.run("192.168.1.10", 18561);

            return null;
        }
    }

}