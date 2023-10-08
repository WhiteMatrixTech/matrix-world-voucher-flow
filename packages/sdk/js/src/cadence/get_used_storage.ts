import * as fcl from "@blocto/fcl";

export const getUsedStorageScript: string = fcl.script`
pub fun main(address: Address): UInt64 {
    let account = getAccount(address)
    log(account.storageUsed)
    log(account.storageCapacity)
    return account.storageUsed
}`;
