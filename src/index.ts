import { registerPlugin } from '@capacitor/core';

import type { ESCPOSPluginPlugin } from './definitions';

const ESCPOSPlugin = registerPlugin<ESCPOSPluginPlugin>('ESCPOSPlugin', {
  web: () => import('./web').then(m => new m.ESCPOSPluginWeb()),
});

export * from './definitions';
export { ESCPOSPlugin };
