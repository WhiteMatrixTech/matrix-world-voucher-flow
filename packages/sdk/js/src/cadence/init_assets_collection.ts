import * as fcl from "@onflow/fcl";

export const initAssetsCollection: string = fcl.transaction`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldAssetsNFT from 0xASSETS_ADDRESS


// Setup storage for MatrixWorldAssetsNFT on signer account
transaction {
    prepare(acct: AuthAccount) {
        if acct.borrow<&MatrixWorldAssetsNFT.Collection>(from: MatrixWorldAssetsNFT.collectionStoragePath) == nil {
            let collection <- MatrixWorldAssetsNFT.createEmptyCollection() as! @MatrixWorldAssetsNFT.Collection
            acct.save(<-collection, to: MatrixWorldAssetsNFT.collectionStoragePath)
            acct.link<&{NonFungibleToken.CollectionPublic, NonFungibleToken.Receiver, MatrixWorldAssetsNFT.Metadata}>(MatrixWorldAssetsNFT.collectionPublicPath, target: MatrixWorldAssetsNFT.collectionStoragePath)
        }
    }
}`;
