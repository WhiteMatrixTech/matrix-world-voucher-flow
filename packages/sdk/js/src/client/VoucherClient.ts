import * as fcl from "@onflow/fcl";
import * as t from "@onflow/types";
import {getFUSDBalance} from "../cadence/get_fusd_balance";

import {transferFusd} from "../cadence/transfer_fusd";

export enum FLowEnv {
    localEmulator,
    flowTestnet,
    flowMainnet
}

export interface VoucherClient {
    setupGlobalFcl(env: FLowEnv): Promise<void>;
    transferFUSD(to: string, amount: string): Promise<string>;
    FUSDBalance(address: string): Promise<number>;
    transferFLOW(to: string, amount: string): Promise<string>;
    FLOWBalance(): Promise<number>;
}

export class FclVoucherClient implements VoucherClient {

    /** Setup global FCL instance
     *
     * @async
     * @param {FLowEnv} env - FlowEnv.{localEmulator, flowTestnet, flowMainnet}
     * @returns {Promise<void>}
     */
    public async setupGlobalFcl(env: FLowEnv): Promise<void> {
        switch (env) {
            case FLowEnv.localEmulator: {
                await fcl
                    .config()
                    // Point App at Emulator
                    .put("accessNode.api", "http://localhost:8080")
                    // Point FCL at dev-wallet (default port)
                    .put("discovery.wallet", "http://localhost:8701/fcl/authn")
                    .put("0xFUNGIBLE_TOKEN_ADDRESS", "0xee82856bf20e2aa6")
                    .put("0xFUSD_ADDRESS", "0xf8d6e0586b0a20c7");
                break;
            }

            case FLowEnv.flowTestnet: {
                return Promise.reject("Testnet env is not ready");
            }

            case FLowEnv.flowMainnet: {
                return Promise.reject("Mainnet env is not ready");
            }
            default:
                return Promise.reject(`${env} is not supported`);
        }
    }

    /**
     * Send transferFUSD transaction
     *
     * @async
     * @param {string} to - addresses of recipient
     * @param {string} amount - amount in string (1.0 means 1.0 FUSD)
     * @returns {Promise<string>} transactionId which can be used to verify the payment to server
     */
    public async transferFUSD(to: string, amount: string): Promise<string> {
        try {
            const response = await fcl.send([
                transferFusd,
                fcl.args([fcl.arg(amount, t.UFix64), fcl.arg(to, t.Address)]),
                fcl.proposer(fcl.currentUser().authorization),
                fcl.authorizations([fcl.currentUser().authorization]),
                fcl.limit(1000),
                fcl.payer(fcl.currentUser().authorization)
            ]);
            const ret = await fcl.tx(response).onceSealed();
            if (ret.errorMessage !== "" && ret.status != 4) {
                return Promise.reject(ret.errorMessage);
            }
            return response.transactionId;
        } catch (error) {
            console.error(error);
            return Promise.reject("Something is wrong with this transaction");
        }
    }

    /**
     * Get FUSDBalance of an Account
     *
     * @async
     * @param {string} address - account address
     * @returns {Promise<number>} account balance (1.0 means 1.0 FUSD)
     */
    public async FUSDBalance(address: string): Promise<number> {
        try {
            const response = await fcl.send([getFUSDBalance, fcl.args([fcl.arg(address, t.Address)]), fcl.limit(1000)]);
            return fcl.decode(response);
        } catch (error) {
            console.error(error);
            return Promise.reject("Something is wrong with fetching FUSD balance");
        }
    }

    public async transferFLOW(to: string, amount: string): Promise<string> {
        // TODO may or may not need
        return "000000000000000000000000000000000000000000000";
    }

    public async FLOWBalance(): Promise<number> {
        // TODO may or may not need
        return 0;
    }
}
