# capacitor-escpos-plugin

Send high level print commands to ESC/POS Printers

## Install

```bash
npm install capacitor-escpos-plugin
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`BluetoothHasPermissions()`](#bluetoothhaspermissions)
* [`BluetoothIsEnabled()`](#bluetoothisenabled)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### BluetoothHasPermissions()

```typescript
BluetoothHasPermissions() => Promise<{ result: boolean; }>
```

**Returns:** <code>Promise&lt;{ result: boolean; }&gt;</code>

--------------------


### BluetoothIsEnabled()

```typescript
BluetoothIsEnabled() => Promise<{ result: boolean; }>
```

**Returns:** <code>Promise&lt;{ result: boolean; }&gt;</code>

--------------------

</docgen-api>
