package com.albgen.plugins.escposplugincap;

import android.util.Log;

public class ESCPOSPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
