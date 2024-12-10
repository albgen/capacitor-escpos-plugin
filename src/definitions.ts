export interface ESCPOSPlugin {   
    bluetoothHasPermissions(): Promise<{result: boolean;}>;
    bluetoothIsEnabled(): Promise<{result: boolean;}>;
    listPrinters(options: {type: string;}): Promise<Printers>;
    printFormattedText(options: { type: string; id: string; address?: string; port?: string; action?: string; text: string, mmFeedPaper?:String, useEscPosAsterik?: boolean, initializeBeforeSend?: boolean, sendDelay?: string; chunkSize?: string}): Promise<void>;
    logCat(options: { message: string; }): Promise<void>;
    rejectTest(): Promise<void>;
    throwException(): Promise<void>;
    echo(options: {value: string;}): Promise<{value: string;}>;
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

