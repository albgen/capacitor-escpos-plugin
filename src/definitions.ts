export interface ESCPOSPluginPlugin {
    echo(options: {value: string;}): Promise<{value: string;}>;
    BluetoothHasPermissions(): Promise<{result: boolean;}>;
    BluetoothIsEnabled(): Promise<{result: boolean;}>;
}
