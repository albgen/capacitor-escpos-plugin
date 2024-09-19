import { WebPlugin } from '@capacitor/core';

import type { ESCPOSPluginPlugin, Printers } from './definitions';

export class ESCPOSPluginWeb extends WebPlugin implements ESCPOSPluginPlugin {

  async ListPrinters(options: { type: string; }): Promise<Printers> {
    console.log('ListPrinters not implemented on WEB', options);
    return Promise.resolve({});
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async BluetoothHasPermissions(): Promise<{ result: boolean }> {
    console.log('BluetoothHasPermissions not implemented on WEB');
    return { result: true};
  }

  async BluetoothIsEnabled(): Promise<{ result: boolean }> {
    console.log('BluetoothIsEnabled not implemented on WEB');
    return { result: true};
  }

}
