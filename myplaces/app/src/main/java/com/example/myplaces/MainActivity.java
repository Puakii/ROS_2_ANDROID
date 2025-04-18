package com.example.myplaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {


    Socket s;
    private Socket monitorSocket;
    private BufferedReader monitorReader;
    private volatile long lastMonitorReceiveTime = 0;
    private final Handler monitorHandler = new Handler();
    private Runnable monitorRunnable;
    PrintWriter writer;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        LinearLayout ipLayout = findViewById(R.id.ip_address_layout);
        Button submitButton = findViewById(R.id.submit_ip_button);
        EditText ipInput = findViewById(R.id.ip_address_input);
        LinearLayout buttonLayout = findViewById(R.id.button_layout);

        submitButton.setOnClickListener(v -> {
            submitButton.setEnabled(false);
            String ipAddress = ipInput.getText().toString();
            s = new Socket();
            monitorSocket = new Socket();
            new Thread(() -> {
                try {
                    s.connect(new InetSocketAddress(ipAddress,5556), 5000);
                    writer = new PrintWriter(s.getOutputStream());
                    Log.i("i", "CONNECTED");
                    monitorSocket.connect(new InetSocketAddress(ipAddress, 5557), 5000); // <-- different port
                    monitorReader = new BufferedReader(new InputStreamReader(monitorSocket.getInputStream()));
                    lastMonitorReceiveTime = System.currentTimeMillis();
                    new Thread(() -> {
                        String line;
                        try {
                            while ((line = monitorReader.readLine()) != null) {
                                lastMonitorReceiveTime = System.currentTimeMillis();
                                Log.i("Monitor", "Received: " + line);
                            }
                        } catch (IOException e) {
                            Log.e("Monitor", "Monitor socket closed");
                        }
                    }).start();
                    monitorRunnable = new Runnable() {
                        @Override
                        public void run() {
                            long now = System.currentTimeMillis();
                            if (monitorSocket == null || monitorSocket.isClosed() || !monitorSocket.isConnected() || now - lastMonitorReceiveTime > 10_000) {
                                runOnUiThread(() -> {
                                    TextView tv = findViewById(R.id.statusButton);
                                    tv.setTextColor(getResources().getColor(R.color.red));
                                    tv.setText(getResources().getText(R.string.badStatus));
                                });
                            } else {
                                monitorHandler.postDelayed(this, 2000);
                            }
                        }
                    };
                    monitorHandler.postDelayed(monitorRunnable, 10000);
                    runOnUiThread(() -> {
                        submitButton.setVisibility(View.GONE);
                        TextView tv = findViewById(R.id.connected_message);
                        tv.setText(getResources().getText(R.string.connected));
                        tv.setTextColor(getResources().getColor(R.color.green));
                        tv.setVisibility(View.VISIBLE);

                        // Delay for 2 seconds, then transition to the buttons screen
                        new android.os.Handler().postDelayed(() -> {
                            ipLayout.setVisibility(View.GONE);
                            buttonLayout.setVisibility(View.VISIBLE);
                            LinearLayout leftButtonColumn = findViewById(R.id.button_column_left);
                            initiateButtons(leftButtonColumn);
                            LinearLayout rightButtonColumn = findViewById(R.id.button_column_right);
                            initiateButtons(rightButtonColumn);
                        }, 200); // 2000 milliseconds = 2 seconds
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        TextView tv = findViewById(R.id.connected_message);
                        tv.setTextColor(getResources().getColor(R.color.red));
                        tv.setText(getResources().getText(R.string.ioexception));
                        tv.setVisibility(View.VISIBLE);
                        new android.os.Handler().postDelayed((
                        ) -> {
                            tv.setVisibility(View.GONE);
                            submitButton.setEnabled(true);
                        }, 500);

                    });
                    if (s != null) {
                        try {
                            s.close();
                        } catch (IOException ex) {
                            e.printStackTrace();
                        }
                    }
                    if (monitorSocket != null) {
                        try {
                            monitorSocket.close();
                        } catch (IOException ex) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        });


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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                    final MediaPlayer mp = getMediaPlayer(tag);

                    private MediaPlayer getMediaPlayer(String tag) {
                        switch (tag) {
                            case "1":
                                return MediaPlayer.create(child.getContext(), R.raw.excuseme);
                            case "2":
                                return MediaPlayer.create(child.getContext(), R.raw.attention);
                            case "3":
                                return MediaPlayer.create(child.getContext(), R.raw.giveway);
                            case "4":
                                return MediaPlayer.create(child.getContext(), R.raw.left);
                            case "5":
                                return MediaPlayer.create(child.getContext(), R.raw.right);
                            default:
                                return null;
                        }
                    }
                    @Override
                    public void onClick(View view) {
                        BackGroundTask b1 = new BackGroundTask(tag);
                        b1.execute();
                        if (mp != null) {
                            mp.start();
                        }
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
            synchronized (writer) {
                writer.write(message);
                writer.flush();
            }
            //writer.close();
            return null;
        }
    }
}
