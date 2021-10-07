# MatrixWorldVoucher FLow Contracts

## Structure
```bash
.
├── LICENSE
├── package.json
├── packages
│   ├── contracts   # Cadence projects
│   └── sdk         # TS client
├── README.md
```
## Setup Flow CLI
[Official Documents](https://docs.onflow.org/flow-cli/install/)
```
sh -ci "$(curl -fsSL https://storage.googleapis.com/flow-cli/install.sh)"
```
## Development with SDK

### JAVA SDK

`cd ./packages/sdk/java/voucher-sdk`

### Sample steps (Local Emulator):
1. Setup Flow CLI [(Above)](#setup-flow-cli)
2. Got to contracts directory, where a **flow.json** is located
    
    `cd ./packages/contracts/`

3. Open a new terminal and start emulator

    `flow emulator start`

4. Open a new terminal at same directory, and deploy contracts to emulator 

    `make deploy-to-local`

5. Setup local FUSD

    `make setup-fusd`

6. Setup test account Voucher Collection

    ```bash
    export SIGNER_PRIV=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111
    export SIGNER_ADDRESS=f8d6e0586b0a20c7
    export NODE="127.0.0.1:3569"
    export FUNTOKEN_ADDRESS=ee82856bf20e2aa6
    export NONFUNTOKEN_ADDRESS=f8d6e0586b0a20c7
    export FLOWTOKEN_ADDRESS=0ae53cb6e3f42a79
    export CONTRACT_NAME=MatrixWorldVoucher
    export CONTRACT_ADDRESS=01cf0e2f2f715450
    export FUSD_ADDRESS=f8d6e0586b0a20c7

    make create-collection-nft
    ```

7. Check current owned NFT 

    ```bash
    export SIGNER_PRIV=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111
    export SIGNER_ADDRESS=f8d6e0586b0a20c7
    export NODE="127.0.0.1:3569"
    export FUNTOKEN_ADDRESS=ee82856bf20e2aa6
    export NONFUNTOKEN_ADDRESS=f8d6e0586b0a20c7
    export FLOWTOKEN_ADDRESS=0ae53cb6e3f42a79
    export CONTRACT_NAME=MatrixWorldVoucher
    export CONTRACT_ADDRESS=01cf0e2f2f715450
    export FUSD_ADDRESS=f8d6e0586b0a20c7

    make list-nfts 
    ```

8. Setup multiple proposal keys for sending mint transaction concurrently - admin account(01cf0e2f2f715450)

    ```bash
    export SIGNER_PRIV=a996c6d610d93faf82ad5b15407b66d3a2b72a284b5c2fd4097b5a3e735a79e1
    export SIGNER_ADDRESS=01cf0e2f2f715450
    export NODE="127.0.0.1:3569"
    export FUNTOKEN_ADDRESS=ee82856bf20e2aa6
    export NONFUNTOKEN_ADDRESS=f8d6e0586b0a20c7
    export FLOWTOKEN_ADDRESS=0ae53cb6e3f42a79
    export CONTRACT_NAME=MatrixWorldVoucher
    export CONTRACT_ADDRESS=01cf0e2f2f715450
    export FUSD_ADDRESS=f8d6e0586b0a20c7

    make setup-multiple-proposal-keys
    ```

9. Simulate a FUSD transfer to Voucher Admin account (01cf0e2f2f715450)

    ```bash
    export SIGNER_PRIV=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111
    export SIGNER_ADDRESS=f8d6e0586b0a20c7
    export NODE="127.0.0.1:3569"
    export FUNTOKEN_ADDRESS=ee82856bf20e2aa6
    export NONFUNTOKEN_ADDRESS=f8d6e0586b0a20c7
    export FLOWTOKEN_ADDRESS=0ae53cb6e3f42a79
    export CONTRACT_NAME=MatrixWorldVoucher
    export CONTRACT_ADDRESS=01cf0e2f2f715450
    export FUSD_ADDRESS=f8d6e0586b0a20c7

    cd go && go run transfer_fusd/main.go 01cf0e2f2f715450 10.0
    ```

    You will see txID on both emulator and go running log output

Keep the emulator running and setup SDK to connect to local access node, as well as keep all contract and admin information matching the LOCAL env.

#### !! Step 4,5,6,8 can be simply applied by running bootstrap script
`make bootstrap-local`
