package com.nerdinand.smssender;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends ActionBarActivity {
    private static final int SOCKET_PORT = 1337;
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private ServerSocket serverSocket;
    private Handler updateConversationHandler;
    private TextView textTextView;
    private Thread serverThread;
    private SmsManager smsManager;

    private PendingIntent sentPendingIntent;
    private PendingIntent sentDeliveredPendingIntent;
    private static CommunicationThread communicationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTextView = (TextView) findViewById(R.id.tv_text);

        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        smsManager = SmsManager.getDefault();

        setupIntents();
    }

    private void setupIntents() {
        sentPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        sentDeliveredPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        postString("SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        postString("Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        postString("No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        postString("Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        postString("Radio off");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        postString("SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        postString("SMS not delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ServerThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SOCKET_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();

                    communicationThread = new CommunicationThread(socket);
                    new Thread(communicationThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new PrintWriter(this.clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String read;
            try {
                while (!Thread.currentThread().isInterrupted() && ((read = input.readLine()) != null)) {
                    postString(read);

                    JSONParser jsonParser = new JSONParser(read);
                    try {
                        SMSMessage smsMessage = jsonParser.parse();
                        postString(smsMessage.toString());
                        sendSMS(smsMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        postString("Invalid JSON");
                        write("Invalid JSON");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write(String string) {
            output.write(string + "\n");
            output.flush();
        }
    }

    private void sendSMS(SMSMessage smsMessage) {
        smsManager.sendTextMessage(smsMessage.getRecipient(), null, smsMessage.getText(), sentPendingIntent, sentDeliveredPendingIntent);
    }

    private void postString(String string) {
        updateConversationHandler.post(new UpdateUIThreadRunnable(string));
    }

    public static CommunicationThread getCommunicationThread() {
        return communicationThread;
    }

    class UpdateUIThreadRunnable implements Runnable {
        private String msg;

        public UpdateUIThreadRunnable(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            textTextView.setText(textTextView.getText().toString() + msg + "\n");
        }
    }
}
