package com.example.bluetooth_capstone;

public class DeviceInformationModel {

    private String deviceName, hardwareAddr;

    public DeviceInformationModel(){}

    public DeviceInformationModel(String deviceName, String hardwareAddr){
        this.deviceName = deviceName;
        this.hardwareAddr = hardwareAddr;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getHardwareAddr() {
        return hardwareAddr;
    }
}
