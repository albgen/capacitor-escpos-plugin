import { WebPlugin } from '@capacitor/core';

import type { ESCPOSPluginPlugin } from './definitions';

export class ESCPOSPluginWeb extends WebPlugin implements ESCPOSPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async HasBTPermissions(): Promise<{ result: boolean }> {
    console.log('HasBTPermissions not implemented on WEB');
    return { result: true};
  }
}
