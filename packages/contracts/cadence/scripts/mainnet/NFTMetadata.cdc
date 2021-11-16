import NonFungibleToken from 0x1d7e57aa55817448
import MatrixWorldVoucher from 0x0d77ec47bbad8ef6
pub fun main(accountAddress: Address, tokenId: UInt64) : MatrixWorldVoucher.Metadata{

    let account = getAccount(accountAddress)

    let acctCapability = account.getCapability(MatrixWorldVoucher.CollectionPublicPath)
    let receiverRef = acctCapability.borrow<&{MatrixWorldVoucher.MatrixWorldVoucherCollectionPublic}>()
        ?? panic("Could not borrow account receiver reference")
    let nft = receiverRef.borrowVoucher(id: tokenId) ?? panic("Could not borrow NFT")
    return nft.metadata
}
