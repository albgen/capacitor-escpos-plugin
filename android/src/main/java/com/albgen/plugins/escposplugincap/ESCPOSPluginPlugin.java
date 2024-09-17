package com.albgen.plugins.escposplugincap;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;


import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
        name = "ESCPOSPlugin",
        permissions = {
                @Permission(
                        strings = {
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_ADVERTISE
                        }, alias = "BT"
                )
        }
)
public class ESCPOSPluginPlugin extends Plugin {

    private static final Integer REQUEST_ENABLE_BT = 1;
    private ESCPOSPlugin implementation = new ESCPOSPlugin();

    // Reference to the Bridge
    protected Bridge bridge;

    // The same as defined in alias at @Permission
    private static final String BT_ALIAS = "BT";
    private static String[] alsiasesPermissions = new String[]{BT_ALIAS};

    @Override
    public void load() {
    }

    @PluginMethod
    public void BluetoothHasPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("result", BluetoothHasPermissions());
        call.resolve(ret);
    }

    @PermissionCallback
    private void BTPermsCallback(PluginCall call) {
        if (getPermissionState(BT_ALIAS) == PermissionState.GRANTED) {
            Log.i("ESCPOSPlugin", "3..2");
        } else {
            Log.i("ESCPOSPlugin", "Permission is required for bluetooth");
            call.reject("Permission is required for bluetooth");
        }
    }

    @PluginMethod
    public void echo(PluginCall call) {

        requestPermissionForAliases(alsiasesPermissions, call, "BTPermsCallback");

        String value = call.getString("value");
        JSObject ret = new JSObject();

        if (getPermissionState(BT_ALIAS) == PermissionState.GRANTED) {
            Log.i("ESCPOSPlugin", " True -> getPermissionState(BT_ALIAS) == PermissionState.GRANTED");

//            if (BluetoothIsEnabled()) return;

            BluetoothConnections printerConnections = new BluetoothConnections();

            if ( printerConnections.getList() == null)
            {
                ret.put("value", "Disabled Bluetooth?");
                call.resolve(ret);
                return;
            }

            ret.put("value", printerConnections.getList().length);
            call.resolve(ret);
            //ret.put("value", implementation.echo(value));
        } else {
            Log.i("ESCPOSPlugin", " False -> getPermissionState(BT_ALIAS) == PermissionState.GRANTED");
        }
        Log.i("ESCPOSPlugin", " end of echo call!");
    }

    @PluginMethod
    public void BluetoothIsEnabled(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("result", BluetoothIsEnabled());
        call.resolve(ret);
    }

    private boolean BluetoothHasPermissions() {
        return getPermissionState(BT_ALIAS) == PermissionState.GRANTED;
    }

    private boolean BluetoothIsEnabled() {
        BluetoothAdapter bluetoothManager = BluetoothAdapter.getDefaultAdapter();
        Log.i("ESCPOSPlugin", (bluetoothManager == null) + " < - (bluetoothManager == null) ");

        // Here check only whether the Bluetooth hardware is off
        if(bluetoothManager != null) {
            return bluetoothManager.isEnabled();
        }
        // Bluetooth is off by convention is bluetoothManager is not defined
        return false;
    }

}
