import { registerPlugin } from '@capacitor/core';

import type { ESCPOSPlugin } from './definitions.js';

const ESCPOSPlugin = registerPlugin<ESCPOSPlugin>('ESCPOSPlugin', {
  web: () => import('./web.js').then(m => new m.ESCPOSPluginWeb()),
});

export * from './definitions.js';
export { ESCPOSPlugin };
