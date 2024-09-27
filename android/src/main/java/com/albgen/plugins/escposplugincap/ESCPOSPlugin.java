package com.albgen.plugins.escposplugincap;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
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

import android.bluetooth.BluetoothDevice;
import android.hardware.usb.UsbDevice;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnections;

import org.json.JSONObject;
import org.json.JSONException;

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
public class ESCPOSPlugin extends Plugin {

    private static final Integer REQUEST_ENABLE_BT = 1;

    // Reference to the Bridge
    protected Bridge bridge;

    // The same as defined in alias at @Permission
    private static final String BT_ALIAS = "BT";
    private static String[] alsiasesPermissions = new String[]{BT_ALIAS};

    private final HashMap<String, DeviceConnection> connections = new HashMap<>();

    @Override
    public void load() {
        Log.i("ESCPOSPlugin", "loading the plugin...");
    }

    @PluginMethod
    public void bluetoothHasPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("result", bluetoothHasPermissions());
        call.resolve(ret);
    }

    @PermissionCallback
    private void BTPermsCallback(PluginCall call) {
        if (getPermissionState(BT_ALIAS) == PermissionState.GRANTED) {
            Log.i("ESCPOSPlugin", "PermissionState.GRANTED already");
        } else {
            if (getPermissionState(BT_ALIAS) == PermissionState.DENIED) {
                //Log.i("ESCPOSPlugin", "Permission is required for bluetooth");
                call.reject("You have denied the permission. Go to app settings and give the permission manually. In alternative you can clear the data and the system will ask you again.");
            }
        }
    }

    @PluginMethod
    public void bluetoothIsEnabled(PluginCall call) throws Exception {
        JSObject ret = new JSObject();
        ret.put("result", bluetoothIsEnabled());
        call.resolve(ret);
    }

    @PluginMethod
    public void listPrinters(PluginCall call) throws Exception{
        try {
            JSObject printers = new JSObject();
            String type = call.getString("type");
            if (type.equals("bluetooth")) {
                if (!bluetoothIsEnabled()) {
                    throw new JSONException("Bluetooth not enabled");
                }
                if (!bluetoothHasPermissions()) {
                    askForBTPermissionIfNotHaveAlready(call);
                    throw new JSONException("Missing permission for bluetooth");
                }
                try {
                    BluetoothConnections printerConnections = new BluetoothConnections();
                    for (BluetoothConnection bluetoothConnection : printerConnections.getList()) {
                        BluetoothDevice bluetoothDevice = bluetoothConnection.getDevice();
                        JSONObject printerObj = new JSONObject();
                        try {
                            printerObj.put("address", bluetoothDevice.getAddress());
                        } catch (Exception ignored) {
                        }  // String
                        try {
                            printerObj.put("bondState", String.valueOf(bluetoothDevice.getBondState()));
                        } catch (SecurityException ignored) {
                        } // Ensure bondState is a string
                        try {
                            printerObj.put("name", bluetoothDevice.getName());
                        } catch (SecurityException ignored) {
                        }  // String
                        try {
                            printerObj.put("type", String.valueOf(bluetoothDevice.getType()));
                        } catch (SecurityException ignored) {
                        } // Convert type to string
                        //try { printerObj.put("features", String.valueOf(bluetoothDevice.getUuids())); } catch (SecurityException ignored) {}  // Convert type to string
                        try {
                            printerObj.put("deviceClass", String.valueOf(bluetoothDevice.getBluetoothClass().getDeviceClass()));
                        } catch (SecurityException ignored) {
                        }
                        try {
                            printerObj.put("majorDeviceClass", String.valueOf(bluetoothDevice.getBluetoothClass().getMajorDeviceClass()));
                        } catch (SecurityException ignored) {
                        }  // Convert type to string
                        try {
                            printers.put(bluetoothDevice.getName(), printerObj);
                        } catch (SecurityException ignored) {
                        }
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
                    try {
                        printerObj.put("productName", Objects.requireNonNull(usbDevice.getProductName()).trim());
                    } catch (Exception ignored) {
                    }
                    try {
                        printerObj.put("manufacturerName", usbDevice.getManufacturerName());
                    } catch (Exception ignored) {
                    }
                    try {
                        printerObj.put("deviceId", usbDevice.getDeviceId());
                    } catch (Exception ignored) {
                    }
                    try {
                        printerObj.put("serialNumber", usbDevice.getSerialNumber());
                    } catch (Exception ignored) {
                    }
                    try {
                        printerObj.put("vendorId", usbDevice.getVendorId());
                    } catch (Exception ignored) {
                    }
                    printers.put(usbDevice.getDeviceName(), printerObj);
                }
            }
            Log.i("Printer Object", printers.toString());
            call.resolve(printers);
        }
        catch(Exception ex)
        {
            call.reject(ex.getMessage(),"COD02");
        }
    }

    @PluginMethod
    public void printFormattedText(PluginCall call) throws Exception {
        try
        {
            JSONObject data = new JSObject();
            data.put("action", call.getString("action"));
            data.put("mmFeedPaper", call.getString("mmFeedPaper"));
            data.put("id", call.getString("id"));
            data.put("address", call.getString("address"));
            data.put("text", call.getString("text"));
            data.put("type", call.getString("type"));
            data.put("port", call.getString("port"));
            data.put("useEscPosAsterik", call.getBoolean("useEscPosAsterik", false));
            data.put("charsetEncoding", call.getObject("charsetEncoding"));

            if (call.getString("type").equals("bluetooth")) {
                if (!bluetoothIsEnabled()) {
                    throw new JSONException("Bluetooth not enabled");
                }
                if (!bluetoothHasPermissions()) {
                    askForBTPermissionIfNotHaveAlready(call);
                    throw new JSONException("Missing permission for bluetooth");
                }
            }

            EscPosPrinter printer = this.getPrinter(data);
            printer.useEscAsteriskCommand(data.getBoolean("useEscPosAsterik"));

            try {
                int dotsFeedPaper = data.has("mmFeedPaper")
                        ? printer.mmToPx((float) data.getDouble("mmFeedPaper"))
                        : data.optInt("dotsFeedPaper", 20);
                if (data.has("action") && data.getString("action").endsWith("Cut")) {
                    printer.printFormattedTextAndCut(data.getString("text"), dotsFeedPaper);
                } else {
                    printer.printFormattedText(data.getString("text"), dotsFeedPaper);
                }

            } catch (EscPosConnectionException e) {
                call.reject("Error",e.getMessage());
                return;
            } catch (Exception e) {
                call.reject("Error",e.getMessage());
                return;
            }
        }
        catch(Exception ex)
        {
            call.reject(ex.getMessage(),"COD01");
        }
    }

    @PluginMethod
    public void logCat(PluginCall call)  {
        String str = call.getString("message");
        if (str != null)
            Log.i("ESCPOSPlugin", str);
    }

    //Remove start - Test only
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
    public void throwException(PluginCall call) throws Exception {
        throw new Exception("Exception from java");
    }
    @PluginMethod
    public void rejectTest(PluginCall call) throws Exception {
        call.reject("Error msg",new Exception("exception object"));
    }
    //Remove end - Test only

    private JSONObject getEncoding(JSONObject data) throws Exception {
        EscPosPrinter printer = this.getPrinter(data);
        JSONObject retData = null;
        EscPosCharsetEncoding encoding = printer.getEncoding();
        if (encoding != null) {
            retData = (new JSONObject(new HashMap<String, Object>() {{
                put("name", encoding.getName());
                put("command", encoding.getCommand());
            }}));
        } else {
        }
        return retData;
    }

    private void disconnectPrinter(JSONObject data) throws Exception {
        EscPosPrinter printer = this.getPrinter(data);
        printer.disconnectPrinter();
    }

    private EscPosPrinter getPrinter(JSONObject data) throws Exception {
        DeviceConnection deviceConnection = this.getPrinterConnection(data);
        if (deviceConnection == null) {
            throw new JSONException("Device not found");
        }

        EscPosCharsetEncoding charsetEncoding = null;
        try {
            if (data.optJSONObject("charsetEncoding") != null) {
                JSONObject charsetEncodingData = data.optJSONObject("charsetEncoding");
                if (charsetEncodingData == null) {
                    charsetEncodingData = new JSONObject();
                }
                charsetEncoding = new EscPosCharsetEncoding(
                        charsetEncodingData.optString("charsetName", "windows-1252"),
                        charsetEncodingData.optInt("charsetId", 16)
                );
            }
        } catch (Exception exception) {
            throw new JSONException(exception.getMessage());
        }

        try {
            return new EscPosPrinter(
                    deviceConnection,
                    data.optInt("printerDpi", 203),
                    (float) data.optDouble("printerWidthMM", 48f),
                    data.optInt("printerNbrCharactersPerLine", 32),
                    charsetEncoding
            );
        } catch (Exception e) {
            throw new JSONException(e.getMessage());
        }
    }

    private DeviceConnection getPrinterConnection(JSONObject data) throws Exception {
        String type = data.getString("type");
        String id = data.getString("id");
        String hashKey = type + "-" + id;
        DeviceConnection deviceConnection = this.getDevice(
                data.getString("type"),
                data.optString("id"),
                data.optString("address"),
                data.optInt("port", 9100)
        );
        if (deviceConnection == null) {
            throw new JSONException(String.valueOf(new HashMap<String, Object>() {{
                put("error", "Device not found or not connected!");
                put("type", type);
                put("id", id);
            }}));
        }
        if (!this.connections.containsKey(hashKey)) {
            this.connections.put(hashKey, deviceConnection);
        }
        return deviceConnection;
    }

    private DeviceConnection getDevice(String type, String id, String address, int port) throws Exception {
        String hashKey = type + "-" + id;
        if (this.connections.containsKey(hashKey)) {
            DeviceConnection connection = this.connections.get(hashKey);
            if (connection != null) {
                if (connection.isConnected()) {
                    return connection;
                } else {
                    this.connections.remove(hashKey);
                }
            }
        }

        if (type.equals("bluetooth")) {
            if (!bluetoothHasPermissions()) {
                throw new JSONException("Missing permission for bluetooth");
            }
            if (id.equals("first")) {
                return BluetoothPrintersConnections.selectFirstPaired();
            }
            BluetoothConnections printerConnections = new BluetoothConnections();
            for (BluetoothConnection bluetoothConnection : printerConnections.getList()) {
                BluetoothDevice bluetoothDevice = bluetoothConnection.getDevice();
                try { if (bluetoothDevice.getAddress().equals(id)) { return bluetoothConnection; } } catch (Exception ignored) {}
                try { if (bluetoothDevice.getName().equals(id)) { return bluetoothConnection; } } catch (SecurityException ignored) {}
            }
        } else if (type.equals("tcp")) {
            return new TcpConnection(address, port);
        } else {
            UsbConnections printerConnections = new UsbConnections(this.getActivity());
            for (UsbConnection usbConnection : printerConnections.getList()) {
                UsbDevice usbDevice = usbConnection.getDevice();
                try { if (usbDevice.getDeviceId() == Integer.parseInt(id)) { return usbConnection; } } catch (Exception ignored) {}
                try { if (Objects.requireNonNull(usbDevice.getProductName()).trim().equals(id)) { return usbConnection; } } catch (Exception ignored) {}
            }
        }

        return null;
    }

    private void bytesToHexadecimalString(JSONObject data) throws Exception {
        EscPosPrinter printer = this.getPrinter(data);
        try {
            byte[] bytes = (byte[]) data.get("bytes");
            Bitmap decodedByte = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            callbackContext.success(PrinterTextParserImg.bitmapToHexadecimalString(printer, decodedByte));
        } catch (Exception e) {
//            callbackContext.error(new JSONObject(new HashMap<String, Object>() {{
//                put("error", e.getMessage());
//            }}));
        }
    }

    private boolean bluetoothHasPermissions() {
        return getPermissionState(BT_ALIAS) == PermissionState.GRANTED;
    }

    private boolean bluetoothIsEnabled() throws Exception {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i("ESCPOSPlugin", (mBluetoothAdapter == null) + " < - (bluetoothManager == null) ");
        if (mBluetoothAdapter == null)
            throw new Exception("Device doesn't support Bluetooth!");

        // Here check only whether the Bluetooth hardware is off
        if(mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        }
        // Bluetooth is off by convention is bluetoothManager is not defined
        return false;
    }

    private void checkBluetooth() throws Exception {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new Exception("Device doesn't support Bluetooth!");
        } else if (!mBluetoothAdapter.isEnabled()) {
            throw new Exception("Device not enabled Bluetooth!");
        }
    }

    private void askForBTPermissionIfNotHaveAlready(PluginCall call) throws Exception
    {
        checkBluetooth();
        if (!this.bluetoothHasPermissions()) {
            requestPermissionForAliases(alsiasesPermissions, call, "BTPermsCallback");
            return;
        }
    }
}
