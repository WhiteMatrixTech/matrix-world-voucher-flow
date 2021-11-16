import NonFungibleToken from 0x1d7e57aa55817448
import MatrixWorldVoucher from 0x0d77ec47bbad8ef6
pub fun main(accountAddress: Address) : [UInt64]{

    let account = getAccount(accountAddress)

    let acctCapability = account.getCapability(MatrixWorldVoucher.CollectionPublicPath)
    let receiverRef = acctCapability.borrow<&{NonFungibleToken.CollectionPublic}>()
        ?? panic("Could not borrow account receiver reference")
    return receiverRef.getIDs()
}
