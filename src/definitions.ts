export interface ESCPOSPluginPlugin {
  echo(options: {value: string;}): Promise<{value: string;}>;
  HasBTPermissions(): Promise<{result: boolean;}>;
}
