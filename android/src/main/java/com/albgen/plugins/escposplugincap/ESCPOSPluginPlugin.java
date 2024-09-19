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

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Base64;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

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

    private final HashMap<String, DeviceConnection> connections = new HashMap<>();

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
            Log.i("ESCPOSPlugin", "PermissionState.GRANTED already");
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

    @PluginMethod
    public void ListPrinters(PluginCall call){
        JSObject printers = new JSObject();
        String type = call.getString("type");
        if (type.equals("bluetooth")) {
            if (!this.BluetoothHasPermissions()) {
                requestPermissionForAliases(alsiasesPermissions, call, "BTPermsCallback");
                //call.resolve(printers);
                return;
            }
            if (!BluetoothIsEnabled()) {
                return;
            }
            try {
                BluetoothConnections printerConnections = new BluetoothConnections();
                for (BluetoothConnection bluetoothConnection : printerConnections.getList()) {
                    BluetoothDevice bluetoothDevice = bluetoothConnection.getDevice();
                    JSONObject printerObj = new JSONObject();
                    try { printerObj.put("address", bluetoothDevice.getAddress()); } catch (Exception ignored) {}  // String
                    try { printerObj.put("bondState",  String.valueOf(bluetoothDevice.getBondState())); } catch (SecurityException ignored) {} // Ensure bondState is a string
                    try { printerObj.put("name", bluetoothDevice.getName()); } catch (SecurityException ignored) {}  // String
                    try { printerObj.put("type",  String.valueOf(bluetoothDevice.getType())); } catch (SecurityException ignored) {} // Convert type to string
                    //try { printerObj.put("features", String.valueOf(bluetoothDevice.getUuids())); } catch (SecurityException ignored) {}  // Convert type to string
                   try { printerObj.put("deviceClass", String.valueOf(bluetoothDevice.getBluetoothClass().getDeviceClass())); } catch (SecurityException ignored) {}
                   try { printerObj.put("majorDeviceClass", String.valueOf(bluetoothDevice.getBluetoothClass().getMajorDeviceClass())); } catch (SecurityException ignored) {}  // Convert type to string
                   try { printers.put(bluetoothDevice.getName(),printerObj);} catch (SecurityException ignored) {}
                }
            } catch (Exception e) {
                printers.put("error", e.getMessage());
                call.resolve(printers);
                return;
            }
        } else {
            UsbConnections printerConnections = new UsbConnections(getContext());
            for (UsbConnection usbConnection : printerConnections.getList()) {
                UsbDevice usbDevice = usbConnection.getDevice();
                JSONObject printerObj = new JSONObject();
                try { printerObj.put("productName", Objects.requireNonNull(usbDevice.getProductName()).trim()); } catch (Exception ignored) {}
                try { printerObj.put("manufacturerName", usbDevice.getManufacturerName()); } catch (Exception ignored) {}
                try { printerObj.put("deviceId", usbDevice.getDeviceId()); } catch (Exception ignored) {}
                try { printerObj.put("serialNumber", usbDevice.getSerialNumber()); } catch (Exception ignored) {}
                try { printerObj.put("vendorId", usbDevice.getVendorId()); } catch (Exception ignored) {}
                printers.put(usbDevice.getDeviceName(),printerObj);
            }
        }
        Log.i("Printer Object", printers.toString());
        call.resolve(printers);
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
