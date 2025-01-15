package com.example.myplaces;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        hideSystemUI();

        LinearLayout leftButtonColumn = findViewById(R.id.button_column_left);
        initiateButtons(leftButtonColumn);
        LinearLayout rightButtonColumn = findViewById(R.id.button_column_right);
        initiateButtons(rightButtonColumn);

    }

    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
    }

    private void hideSystemUI() {
        // Hides system bars and enables immersive mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hides navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);   // Hides status bar
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI(); // Reapply immersive mode when activity resumes
    }

    private void initiateButtons(LinearLayout buttonColumn) {
        for (int i = 0; i < buttonColumn.getChildCount(); i++) {
            View child = buttonColumn.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(new View.OnClickListener(){
                    String tag = (String) child.getTag();

                    final MediaPlayer mp = MediaPlayer.create(child.getContext(), getAudio(tag));

                    private int getAudio(String tag) {
                        if (tag.equals("1")) {
                            return R.raw.excuseme;
                        } else if (tag.equals("2")) {
                            return R.raw.attention;
                        } else if (tag.equals("3")) {
                            return R.raw.left;
                        } else if (tag.equals("4")) {
                            return R.raw.giveway;
                        } else if (tag.equals("5")) {
                            return R.raw.behind;
                        } else {
                            return R.raw.right;
                        }
                    }
                    @Override
                    public void onClick(View view) {
                        BackGroundTask b1 = new BackGroundTask(tag);
                        b1.execute();
                        mp.start();
//                        new Thread(() -> {
//                            try {
//                                Socket socket = new Socket();
//                                socket.connect(new InetSocketAddress("192.168.68.59", 5556), 5000); // 5-second timeout
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
                    s = new Socket("192.168.123.18",5556);
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
