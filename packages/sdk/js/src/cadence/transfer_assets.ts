import * as fcl from "@onflow/fcl";

export const transferAssetsScript: string = fcl.transaction`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldAssetsNFT from 0xASSETS_ADDRESS
// transfer MatrixWorldAssetsNFT token with tokenId to given address
transaction(recipient: Address, tokenId: UInt64) {

    let senderCollection: &MatrixWorldAssetsNFT.Collection

    prepare(acct: AuthAccount) {
        self.senderCollection = acct.borrow<&MatrixWorldAssetsNFT.Collection>(from: MatrixWorldAssetsNFT.collectionStoragePath)
            ?? panic("Missing NFT collection on signer account")
    }

    execute {
        
        // transfer token
        let token <- self.senderCollection.withdraw(withdrawID: tokenId)
        let receiverProvider = getAccount(recipient).getCapability<&{NonFungibleToken.CollectionPublic,NonFungibleToken.Receiver}>(MatrixWorldAssetsNFT.collectionPublicPath)
        let receiver = receiverProvider.borrow() ?? panic("Missing NFT collection on receiver account")
        receiver.deposit(token: <- token)

    }
}`;
