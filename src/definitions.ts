export interface ESCPOSPluginPlugin {
    echo(options: {value: string;}): Promise<{value: string;}>;
    BluetoothHasPermissions(): Promise<{result: boolean;}>;
    BluetoothIsEnabled(): Promise<{result: boolean;}>;
    ListPrinters(options: {type: string;}): Promise<Printers>;
    printFormattedText(options: { type: string; id: string; address?: string; port?: string; action?: string; text: string, mmFeedPaper?:String}): Promise<void>;
    rejectTest(): Promise<void>;
    throwException(): Promise<void>;
}

export interface PrinterInfo {
    address: string;
    bondState: string;
    name: string;
    type: string;
    //features: string;
    deviceClass: string;
    majorDeviceClass: string;
    //[uuid: string]: string; // For dynamic UUID keys
  }

  export interface Printers
  {
    [key: string]: PrinterInfo;  // Dynamic key, where key is the printer name found also on name property of the PrinterInfo
  }

