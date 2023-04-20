package com.example.bluetooth_capstone;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String devAddr = null;
    public static Handler handler;
    public static BluetoothSocket btSocket;
    public static CreateConnectThread createConThread;
    public static ConnectedThread conThread;
    private final static int CONNECTION_STATUS = 1;
    private final static int MESSAGE_READIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView btStatus = findViewById(R.id.conStatusText);
        Button conBtn = findViewById(R.id.conButton);
        Button disconBtn = findViewById(R.id.disconButton);
        Button sndBtn = findViewById(R.id.sndBtn);
        EditText txtInput = findViewById(R.id.txtInput);



        conBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DeviceSelectionActivity.class);
                startActivity(intent);
            }
        });

        disconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createConThread.cancel();
                btStatus.setText("Bluetooth has been Disconnected");
            }
        });

        devAddr = getIntent().getStringExtra("deviceAddress");

        if (devAddr != null) {
            btStatus.setText("Currently Connecting...");
            BluetoothAdapter btAdapater = BluetoothAdapter.getDefaultAdapter();

            createConThread = new CreateConnectThread(btAdapater, devAddr);
            createConThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTION_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                btStatus.setText("Bluetooth Connected");
                                break;
                            case -1:
                                btStatus.setText("Connection Failed");
                                break;
                        }
                        break;

                    case MESSAGE_READIN:

                        break;
                }
            }

        };

    sndBtn.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
        String androidCMD = String.valueOf(txtInput.getText());
        conThread.write(androidCMD);
        }
    });
    }


    class CreateConnectThread extends Thread {

        @SuppressLint("MissingPermission")
        public CreateConnectThread(BluetoothAdapter btAdapter, String addr) {

            BluetoothDevice btDevice = btAdapter.getRemoteDevice(addr);
            BluetoothSocket temp = null;

            @SuppressLint("MissingPermission") UUID uuid = btDevice.getUuids()[0].getUuid();
            try {
                temp = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e){
            }
            btSocket = temp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            btAdapter.cancelDiscovery();
            try{
                btSocket.connect();
                handler.obtainMessage(CONNECTION_STATUS,1, -1).sendToTarget();
            } catch (IOException conException){
                try{
                    btSocket.close();
                    handler.obtainMessage(CONNECTION_STATUS, -1, -1).sendToTarget();
                } catch(IOException closeException){}
                    return;
            }
            conThread = new ConnectedThread(btSocket);
            conThread.run();
        }

        public void cancel(){
            try{
                btSocket.close();
            } catch(IOException e) {}
        }
    }

    public static class ConnectedThread extends Thread {

        private final InputStream btInStream;
        private final OutputStream btOutStream;
        private final BluetoothSocket btSocket;


        // Getting input and output of Arduino
        public ConnectedThread(BluetoothSocket socket) {
            btSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            btInStream = tmpIn;
            btOutStream = tmpOut;
        }

        // Read arduino message
        public void run() {
            byte[] buffer = new byte[1024];  // general buffer to store the stream data
            int inputBytes = 0; // create a inputBytes var to store no of inputBytes
            while (true) {
                try {
                    buffer[inputBytes] = (byte) btInStream.read();
                    String arduinoMsg = null;
                    if (buffer[inputBytes] == '\n'){
                        arduinoMsg = new String(buffer,0,inputBytes);
                        handler.obtainMessage(MESSAGE_READIN,arduinoMsg).sendToTarget();
                        inputBytes = 0;
                    } else {
                        inputBytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        //write data to arduino lcd
        public void write(String input) {
            byte[] inputBytes = input.getBytes();
            try {
                btOutStream.write(inputBytes);
            } catch (IOException e) { }
        }
    }
}