package main

import (
	"context"
	"fmt"

	"google.golang.org/grpc"

	"github.com/onflow/cadence"
	"github.com/onflow/flow-go-sdk/client"

	"go/common"
)

var listIds string = fmt.Sprintf(`
import NonFungibleToken from 0x%s
import %s from 0x%s
pub fun main(hash: String) : bool {

    return %s.isHashExists(hash)
}
`, common.Config.NonFungibleTokenAddress, common.Config.ContractName, common.Config.ContractAddress,common.Config.ContractName,)

func main() {
	ctx := context.Background()
	flowClient, err := client.New(common.Config.Node, grpc.WithInsecure())
	if err != nil {
		panic(err)
	}

	if _, err := flowClient.GetLatestBlock(ctx, false); err != nil {
		panic(err)
	}

	hash, _ := cadence.NewString("03e358fecd20e49d6f6ab8537614c2ac5de5ff5489c10e88786697aa4bc95db4")
	value, err := flowClient.ExecuteScriptAtLatestBlock(ctx, []byte(listIds),[]cadence.Value{hash})

	if err != nil {
		panic("failed to execute script")
	}

	ID := value.(cadence.String)

	fmt.Println(ID.ToGoValue())
}
