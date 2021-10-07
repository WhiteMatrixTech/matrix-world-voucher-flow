```bash
docker run -it \
    -e PORT=8701 \
    -e FLOW_ACCESS_NODE=http://localhost:8080 \
    -e FLOW_ACCOUNT_KEY_ID=0 \
    -e FLOW_ACCOUNT_PRIVATE_KEY=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111 \
    -e FLOW_ACCOUNT_PUBLIC_KEY=31a053a2003d95760d8fff623aeedcc927022d8e0767972ab507608a5f611636e81857c6c46b048be6f66eddc13f5553627861153f6ce301caf5a056d68efc29 \
    -e FLOW_INIT_ACCOUNTS=0 \
    -e FLOW_ACCOUNT_ADDRESS=0xf8d6e0586b0a20c7 \
    -e FLOW_AVATAR_URL=https://avatars.onflow.org/avatar/ \
    --network host \
    -p 8701:8701 \
    ghcr.io/onflow/fcl-dev-wallet:latest 
```
```bash
export PORT=8701 \
export FLOW_ACCESS_NODE=http://localhost:8080 \
export FLOW_ACCOUNT_KEY_ID=0 \
export FLOW_ACCOUNT_PRIVATE_KEY=2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111 \
export FLOW_ACCOUNT_PUBLIC_KEY=31a053a2003d95760d8fff623aeedcc927022d8e0767972ab507608a5f611636e81857c6c46b048be6f66eddc13f5553627861153f6ce301caf5a056d68efc29 \
export FLOW_INIT_ACCOUNTS=0 \
export FLOW_ACCOUNT_ADDRESS=0xf8d6e0586b0a20c7 \
export FLOW_AVATAR_URL=https://avatars.onflow.org/avatar/ \
```
