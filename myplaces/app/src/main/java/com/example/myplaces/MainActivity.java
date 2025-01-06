package com.example.myplaces;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {


    Socket s;
    PrintWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout buttonColumn = findViewById(R.id.button_column);

        for (int i = 0; i < buttonColumn.getChildCount(); i++) {
            View child = buttonColumn.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(new View.OnClickListener(){
                    final MediaPlayer mp = MediaPlayer.create(child.getContext(), R.raw.excuseme);
                    @Override
                    public void onClick(View view) {
                        String tag = (String) view.getTag();
                        BackGroundTask b1 = new BackGroundTask(tag);
                        b1.execute();
                        mp.start();
//                        new Thread(new ClientThread(tag)).start();
//                        new Thread(() -> {
//                            try {
//                                Socket socket = new Socket();
//                                socket.connect(new InetSocketAddress("192.168.1.3", 5556), 5000); // 5-second timeout
//                                Log.d("Network Test", "Server is reachable on port 6000");
//                                socket.close();
//                            } catch (IOException e) {
//                                Log.d("Network Test", "Server is not reachable: " + e.getMessage());
//                            }
//                        }).start();
                    }
                });
            }
        }


    }

    class BackGroundTask extends AsyncTask<String, Void, Void> {

        private String message;
        public BackGroundTask(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(String... voids) {
            try {
                if(s == null){
                    //change it to your IP
                    s = new Socket("0.0.0.0",5556);
                    writer = new PrintWriter(s.getOutputStream());
                    Log.i("i", "CONNECTED");
                }
                writer.write(message);
                writer.flush();
                //writer.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
