import * as fcl from "@blocto/fcl";

export const checkAssetsCollection: string = fcl.script`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldAssetsNFT from 0xASSETS_ADDRESS

pub fun main(addr: Address): Bool {
    let ref = getAccount(addr).getCapability<&{NonFungibleToken.CollectionPublic}>(MatrixWorldAssetsNFT.collectionPublicPath).check()
    return ref
}`;
