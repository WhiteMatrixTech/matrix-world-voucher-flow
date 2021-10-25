import * as fcl from "@onflow/fcl";

export const initVoucherCollection: string = fcl.transaction`
import NonFungibleToken from 0xNON_FUNGIBLE_TOKEN_ADDRESS
import MatrixWorldVoucher from 0xVOUCHER_ADDRESS
transaction {
    prepare(signer: AuthAccount) {
        if signer.borrow<&MatrixWorldVoucher.Collection>(from: MatrixWorldVoucher.CollectionStoragePath) == nil {
            let collection <- MatrixWorldVoucher.createEmptyCollection()

            signer.save(<-collection, to: MatrixWorldVoucher.CollectionStoragePath)
            signer.link<&MatrixWorldVoucher.Collection{NonFungibleToken.CollectionPublic, MatrixWorldVoucher.MatrixWorldVoucherCollectionPublic}>(MatrixWorldVoucher.CollectionPublicPath, target: MatrixWorldVoucher.CollectionStoragePath)
        }
    }
}`;
