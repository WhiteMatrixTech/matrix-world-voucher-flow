### Install
`yarn add @white-matrix/matrix-world-voucher-flow-js-sdk`

### API reference
core apis [VoucherClient](./src/client/VoucherClient.ts)
You can also check [Demo-front-page]('../demo-js-front/src/App.tsx') for reference
```typescript
/** Setup global FCL instance
*
* @async
* @param {FLowEnv} env - FlowEnv.{localEmulator, flowTestnet, flowMainnet}
* @returns {Promise<void>}
*/
public async setupGlobalFcl(env: FLowEnv): Promise<void>; 

/**
* Send transferFUSD transaction
*
* @async
* @param {string} to - addresses of recipient
* @param {string} amount - amount in string (1.0 means 1.0 FUSD)
* @returns {Promise<string>} transactionId which can be used to verify the payment to server
*/
public async transferFUSD(to: string, amount: string): Promise<string>;

/**
* Get FUSDBalance of an Account
*
* @async
* @param {string} address - account address
* @returns {Promise<number>} account balance (1.0 means 1.0 FUSD)
*/
public async FUSDBalance(address: string): Promise<number>;

// TODO
public async transferFLOW(to: string, amount: string): Promise<string>;
public async FLOWBalance(): Promise<number>;
```

### Local development guide
#### Setup Flow CLI
[Official Documents](https://docs.onflow.org/flow-cli/install/)
```
sh -ci "$(curl -fsSL https://storage.googleapis.com/flow-cli/install.sh)"
```

#### Sample steps (Local Emulator):
1. Setup Flow CLI [(Above)](#setup-flow-cli)
2. Got to contracts directory, where a **flow.json** is located
    
    `cd ./packages/contracts/`

3. Open a new terminal and start emulator

    `flow emulator start`

4. Run bootstrap script 

    `make bootstrap-local`

5. Start dev-wallet docker (Account information here is default service account in Local Emulator)

    ```bash
    docker run -it \
        -e PORT=8701 \
        -e FLOW_ACCESS_NODE=http://localhost:8080 \
        -e FLOW_ACCOUNT_KEY_ID=0 \
        -e FLOW_ACCOUNT_PRIVATE_KEY=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111 \
        -e FLOW_ACCOUNT_PUBLIC_KEY=31a053a2003d95760d8fff623aeedcc927022d8e0767972ab507608a5f611636e81857c6c46b048be6f66eddc13f5553627861153f6ce301caf5a056d68efc29 \
        -e FLOW_INIT_ACCOUNTS=0 \
        -e FLOW_ACCOUNT_ADDRESS=0xf8d6e0586b0a20c7 \
        -e FLOW_AVATAR_URL=https://avatars.onflow.org/avatar/ \
        --network host \
        ghcr.io/onflow/fcl-dev-wallet:latest 
    ```

6. Import FCL from SDK
    ```typescript
    import { fcl, FclVoucherClient, FLowEnv } from "matrix-world-voucher-flow-js-sdk/dist";
    const client = new FclVoucherClient();
    await client.setupGlobalFcl(FLowEnv.localEmulator);
    await fcl.logIn();
    await fcl.authenticate();
    ```

    sample code for sending FUSD
    ```typescript
    import { fcl, FclVoucherClient } from "matrix-world-voucher-flow-js-sdk/dist";
    // transferFUSD
    const client = new FclVoucherClient();
    const ret = await client.transferFUSD("0x01cf0e2f2f715450", "10.0");
    console.log(ret);
    ```

    sample code for getting FUSD balance
    ```typescript
    import { fcl, FclVoucherClient } from "matrix-world-voucher-flow-js-sdk/dist";
    // transferFUSD
    const client = new FclVoucherClient();
    const ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    console.log(ret);
    ```
