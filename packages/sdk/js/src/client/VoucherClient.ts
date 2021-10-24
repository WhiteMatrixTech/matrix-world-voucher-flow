import * as fcl from "@onflow/fcl";
import * as t from "@onflow/types";
import {getFLOWBalanceScript} from "../cadence/get_flow_balance";
import {getFUSDBalanceScript} from "../cadence/get_fusd_balance";
import {transferFLOWScript} from "../cadence/transfer_flow";
import {transferFUSDScript} from "../cadence/transfer_fusd";

export enum FlowEnv {
    localEmulator,
    flowTestnet,
    flowMainnet
}

export interface VoucherClient {
    setupGlobalFcl(env: FlowEnv): Promise<void>;
    transferFUSD(to: string, amount: string): Promise<string>;
    FUSDBalance(address: string): Promise<number>;
    transferFLOW(to: string, amount: string): Promise<string>;
    FLOWBalance(address: string): Promise<number>;
}

export class FclVoucherClient implements VoucherClient {
    /** Setup global FCL instance
     *
     * @async
     * @param {FlowEnv} env - FlowEnv.{localEmulator, flowTestnet, flowMainnet}
     * @returns {Promise<void>}
     */
    public async setupGlobalFcl(env: FlowEnv): Promise<void> {
        switch (env) {
            case FlowEnv.flowTestnet: {
                await fcl
                    .config()
                    .put("accessNode.api", "http://localhost:8080")
                    .put("discovery.wallet", "http://localhost:8701/fcl/authn")
                    .put("0xFUNGIBLE_TOKEN_ADDRESS", "0x9a0766d93b6608b7")
                    .put("0xFUSD_ADDRESS", "0xe223d8a629e49c68")
                    .put("0xFLOW_TOKEN_ADDRESS", "0x7e60df042a9c0868");
                break;
            }
            case FlowEnv.flowMainnet: {
                await fcl
                    .config()
                    .put("accessNode.api", "http://localhost:8080")
                    .put("discovery.wallet", "http://localhost:8701/fcl/authn")
                    .put("0xFUNGIBLE_TOKEN_ADDRESS", "0xf233dcee88fe0abe")
                    .put("0xFUSD_ADDRESS", "0x3c5959b568896393")
                    .put("0xFLOW_TOKEN_ADDRESS", "0x1654653399040a61");
                break;
            }
            case FlowEnv.localEmulator:
            default:
                await fcl
                    .config()
                    .put("accessNode.api", "http://localhost:8080")
                    .put("discovery.wallet", "http://localhost:8701/fcl/authn")
                    .put("0xFUNGIBLE_TOKEN_ADDRESS", "0xee82856bf20e2aa6")
                    .put("0xFUSD_ADDRESS", "0xf8d6e0586b0a20c7")
                    .put("0xFLOW_TOKEN_ADDRESS", "0x0ae53cb6e3f42a79");
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
                transferFUSDScript,
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
            const response = await fcl.send([
                getFUSDBalanceScript,
                fcl.args([fcl.arg(address, t.Address)]),
                fcl.limit(1000)
            ]);
            return fcl.decode(response);
        } catch (error) {
            console.error(error);
            return Promise.reject("Something is wrong with fetching FUSD balance");
        }
    }

    /**
     * Send transferFLOW transaction
     *
     * @async
     * @param {string} to - addresses of recipient
     * @param {string} amount - amount in string (1.0 means 1.0 FLOW)
     * @returns {Promise<string>} transactionId which can be used to verify the payment to server
     */
    public async transferFLOW(to: string, amount: string): Promise<string> {
        try {
            const response = await fcl.send([
                transferFLOWScript,
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
     * Get FLOWBalance of an Account
     *
     * @async
     * @param {string} address - account address
     * @returns {Promise<number>} account balance (1.0 means 1.0 FLOW)
     */
    public async FLOWBalance(address: string): Promise<number> {
        try {
            const response = await fcl.send([
                getFLOWBalanceScript,
                fcl.args([fcl.arg(address, t.Address)]),
                fcl.limit(1000)
            ]);
            return fcl.decode(response);
        } catch (error) {
            console.error(error);
            return Promise.reject("Something is wrong with fetching FUSD balance");
        }
    }
}
