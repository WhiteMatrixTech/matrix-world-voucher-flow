deploy-to-local:
	bash ./sh/deploy-to-local.sh
.PHONY: deploy-to-local

deploy-to-testnet:
	bash ./sh/deploy-to-testnet.sh
.PHONY: deploy-to-testnet

create-collection-nft:
	cd go && go run create_collection/main.go
.PHONY:create-collection-nft

mint-nft:
	cd go && go run mint_nft/main.go
.PHONY: mint-nft

list-nfts:
	cd go && go run list_nfts/main.go
.PHONY: list-nfts

setup-fusd:
	bash ./sh/setup-fusd.sh
.PHONY: setup-fusd

setup-multiple-proposal-keys:
	cd go && go run add_proposal_keys/main.go
.PHONY: setup-multiple-proposal-keys

bootstrap-local:
	bash ./sh/bootstrop.sh
.PHONY: bootstrap-local

upgrade-to-main:
	flow accounts update-contract MatrixWorldVoucher ./cadence/contracts/MatrixWorldVoucherUpgradeMain.cdc --signer main -f ./flow.json -f ./flow.main.json -n mainnet
.PHONY: upgrade-to-main

