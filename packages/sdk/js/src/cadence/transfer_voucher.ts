import * as fcl from "@blocto/fcl";

export const transferVoucherScript: string = fcl.transaction`
import FungibleToken from 0xFUNGIBLE_TOKEN_ADDRESS
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldVoucher from 0xVOUCHER_ADDRESS
transaction(recipient: Address, tokenId: UInt64) {
    let senderProvider: &MatrixWorldVoucher.Collection
    prepare(signer: AuthAccount) {
        self.senderProvider = signer.borrow<&MatrixWorldVoucher.Collection>(from: MatrixWorldVoucher.CollectionStoragePath)
            ?? panic("Could not borrow a reference to the NFT senderProvider")
    }
    execute {
        let recipient = getAccount(recipient)

        let receiver = recipient
            .getCapability(MatrixWorldVoucher.CollectionPublicPath)!
            .borrow<&{NonFungibleToken.CollectionPublic}>()
            ?? panic("Could not get receiver reference to the NFT Collection")

        let token <- self.senderProvider.withdraw(withdrawID: tokenId)
        receiver.deposit(token:<-token)
    }
}`;
