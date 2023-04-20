package com.example.bluetooth_capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        RecyclerView recyclerView = findViewById(R.id.deviceList);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},1);
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},1);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},1);
        }
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        List<Object> deviceList = new ArrayList<>();
        for (BluetoothDevice device : pairedDevices){
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress();
            DeviceInformationModel deviceInfoModel = new DeviceInformationModel(deviceName,deviceHardwareAddress);
            deviceList.add(deviceInfoModel);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DeviceListAdapter listAdapter = new DeviceListAdapter(this,deviceList);
        recyclerView.setAdapter(listAdapter);


    }
}