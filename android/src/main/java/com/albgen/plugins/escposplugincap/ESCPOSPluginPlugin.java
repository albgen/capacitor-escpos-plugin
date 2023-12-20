package com.albgen.plugins.escposplugincap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
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

    private ESCPOSPlugin implementation = new ESCPOSPlugin();

    // The same as defined in alias at @Permission
    private static final String BT_ALIAS = "BT";
    private static String[] alsiasesPermissions = new String[]{BT_ALIAS};

    @Override
    public void load() {
    }

    @PluginMethod
    public Boolean HasBTPermissions()
    {
        return (getPermissionState(BT_ALIAS) == PermissionState.GRANTED);
    }

    @PermissionCallback
    private void BTPermsCallback(PluginCall call) {
        if (getPermissionState(BT_ALIAS) == PermissionState.GRANTED) {
            Log.i("Echo", "5");
        } else {
            Log.i("Echo", "6");
            call.reject("Permission is required for bluetooth");
        }
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();

        // requestAllPermissions(call, "BTPermsCallback");
        requestPermissionForAliases(alsiasesPermissions, call, "BTPermsCallback");

        if (getPermissionState(BT_ALIAS) == PermissionState.GRANTED) {
            Log.i("Echo", "3");
            BluetoothConnections printerConnections = new BluetoothConnections();
            ret.put("value", printerConnections.getList());
            call.resolve(ret);
            //ret.put("value", implementation.echo(value));
        } else {
            Log.i("Echo", "7");
        }
        Log.i("Echo", "4");
    }
}
