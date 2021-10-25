import * as fcl from "@onflow/fcl";

export const checkVoucherCollection: string = fcl.script`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldVoucher from 0xVOUCHER_ADDRESS

pub fun main(addr: Address): Bool {
    let ref = getAccount(addr).getCapability<&{NonFungibleToken.CollectionPublic}>(MatrixWorldVoucher.CollectionPublicPath).check()
    return ref
}`;
