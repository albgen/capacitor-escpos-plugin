import { registerPlugin } from '@capacitor/core';

import type { ESCPOSPlugin } from './definitions';

const ESCPOSPlugin = registerPlugin<ESCPOSPlugin>('ESCPOSPlugin', {
  web: () => import('./web').then(m => new m.ESCPOSPluginWeb()),
});

export * from './definitions';
export { ESCPOSPlugin };
