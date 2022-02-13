import * as fcl from "@onflow/fcl";

export const getAssetsScript: string = fcl.script`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldAssetsNFT from 0xASSETS_ADDRESS
pub fun main(address: Address): [{String: String}]{
    let collection = getAccount(address)
        .getCapability(MatrixWorldAssetsNFT.collectionPublicPath)
        .borrow<&{MatrixWorldAssetsNFT.Metadata,NonFungibleToken.CollectionPublic,NonFungibleToken.Receiver}>() ?? panic("NFT Collection not found")
    let ret : [{String: String}] = []
    let ids = collection.getIDs()
    for tokenId in ids {
        ret.append(collection.getMetadata(id: tokenId))
    }
    return ret
}`;
