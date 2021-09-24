deploy-to-local:
	bash ./sh/deploy-to-local.sh
.PHONY: deploy-to-local

deploy-to-testnet:
	bash ./sh/deploy-to-testnet.sh

create-collection-nft:
	cd go && go run create_collection/main.go
.PHONY: mint-nft

mint-nft:
	cd go && go run mint_nft/main.go
.PHONY: mint-nft
