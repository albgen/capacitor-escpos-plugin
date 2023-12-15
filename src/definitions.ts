export interface ESCPOSPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
