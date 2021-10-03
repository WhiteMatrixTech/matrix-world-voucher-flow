package main

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/onflow/cadence"
	"github.com/onflow/flow-go-sdk"
	"github.com/onflow/flow-go-sdk/client"

	"go/common"
)

var addKeys string = fmt.Sprintf(`
transaction(numProposalKeys: UInt16) {
  prepare(account: AuthAccount) {
    let key = account.keys.get(keyIndex: 0)!
    var count: UInt16 = 0
    while count < numProposalKeys {
      account.keys.add(
            publicKey: key.publicKey,
            hashAlgorithm: key.hashAlgorithm,
            weight: 1000.0
        )
        count = count + 1
    }
  }
}
`)

func main() {
	ctx := context.Background()
	flowClient, err := client.New(common.Config.Node, grpc.WithInsecure())
	if err != nil {
		panic(err)
	}

	referenceBlock, err := flowClient.GetLatestBlock(ctx, false)
	if err != nil {
		panic(err)
	}

	acctAddress, acctKey, signer := common.ServiceAccount(flowClient, common.Config.SingerAddress, common.Config.SingerPriv)
	tx := flow.NewTransaction().
		SetScript([]byte(addKeys)).
		SetGasLimit(1000).
		SetProposalKey(acctAddress, acctKey.Index, acctKey.SequenceNumber).
		SetReferenceBlockID(referenceBlock.ID).
		SetPayer(acctAddress).
		AddAuthorizer(acctAddress)

    if err := tx.AddArgument(cadence.NewUInt16(10)); err != nil {
        panic(err)
    }

	if err := tx.SignEnvelope(acctAddress, acctKey.Index, signer); err != nil {
		panic(err)
	}

	if err := flowClient.SendTransaction(ctx, *tx); err != nil {
		panic(err)
	}

	common.WaitForSeal(ctx, flowClient, tx.ID())
}
