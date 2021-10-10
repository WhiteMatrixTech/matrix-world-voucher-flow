import * as fcl from "@onflow/fcl";
import * as t from "@onflow/types";

import {transferFusd} from "../cadence/transfer_fusd";

export interface VoucherClient {
    transferFUSD(to: string, amount: string): Promise<string>;
}

export class FclVoucherClient implements VoucherClient {
    public async transferFUSD(to: string, amount: string): Promise<string> {
        const response = await fcl.send([
            transferFusd,
            fcl.args([fcl.arg(amount, t.UFix64), fcl.arg(to, t.Address)]),
            fcl.proposer(fcl.currentUser().authorization),
            fcl.authorizations([
                fcl.currentUser().authorization,
            ]),
            fcl.limit(1000),
            fcl.payer(fcl.currentUser().authorization)
        ]);
        return await fcl.tx(response).onceSealed();
    }
}
