# JAVA-SDK

## VoucherClient

### Core interfaces
[VoucherClient.java](./voucher-sdk/src/main/java/matrix/flow/sdk/VoucherClient.java)

Constructor
```
public VoucherClient(final VoucherClientConfig clientConfig);
```
verifyUserSignature
```java
/**
* Verify user signatures to cryptographically verify the ownership of a Flow
* account by verifying a message was signed by a user's private key/s
*
* @param message      singed raw message in Hex string
* @param publicKeyHex a list of public keys in Hex string
* @param weights      a list of corresponding weights of singed keys
* @param signAlgos    a list of singed algorithm, where 2 indicates ECDSA_P256
*                     others for ECDSA_secp256k1
* @param hashAlogs    a list of hash algorithm, where 2 for SHA2_256 others for
*                     SHA3_256
* @param signatures   a list of signatures which are singed by corresponding
*                     keypairs
*
* @return boolean true if verified or false
*/
public boolean verifyUserSignature(final String message, final String[] publicKeysHex, final double[] weights,
    final int[] signAlgos, final int[] hashAlogs, final String[] signatures)
```
verifyFUSDTransaction
```java
/**
* Verify a FUSD transaction
*
* @param payerAddress payer account address
* @param amount expected amount to be received
* @param transactionId flow transactionId
*
* @throws Exception with reason of unexpected error
* @TODO: Custom RunTimeException  
*/
public void verifyFUSDTransaction(final String payerAddress, final BigDecimal amount, final String transactionId) throws Exception
```
mintVoucher
```java
/**
* Mint a Voucher NFT
*
* @param recipientAddressString recipient account address
* @param landInfoHashString flow type landInfoHashString in hex
*
* @return Voucher Model
*
* @throws Exception unknown runtime error
*/
public VoucherMetadataModel mintVoucher(final String recipientAddressString, final String landInfoHashString) throws Exception;

```

### Usage 
Init client
```java
public static final String TEST_ADMIN_PRIVATE_KEY_HEX = "a996c6d610d93faf82ad5b15407b66d3a2b72a284b5c2fd4097b5a3e735a79e1"; // emulator
                                                                                                                            // admin
                                                                                                                            // private
                                                                                                                            // key
public static final String SERVICE_PRIVATE_KEY_HEX = "2eae2f31cb5b756151fa11d82949c634b8f28796a711d7eb1e52cc301ed11111"; // emulator
                                                                                                                            // admin
                                                                                                                            // private
                                                                                                                            // key
public static final String FUNGIBLE_TOKEN_ADDRESS = "ee82856bf20e2aa6";
public static final String FUSD_ADDRESS = "f8d6e0586b0a20c7";
public static final String NON_FUNGIBLE_TOKEN_ADDRESS = "f8d6e0586b0a20c7";
public static final String VOUCHER_ADDRESS = "01cf0e2f2f715450";

private final FlowAddress testAdminAccountAddress = new FlowAddress("01cf0e2f2f715450");
private final FlowAddress userAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

final VoucherClientConfig adminClientConfig = VoucherClientConfig.builder().host("localhost").port(3569)
        .privateKeyHex(TEST_ADMIN_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
        .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(testAdminAccountAddress.getBase16Value())
        .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS).build();

final VoucherClient adminClient = new VoucherClient(adminClientConfig);
```

Verify transaction and mint Voucher to payer
```java
if (adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value())){
    // Mint Voucher if verification is success
    VoucherMetadataModel newToken = adminClient.mintVoucher(userAccountAddress.getBase16Value(), testHash);
};

```
Use it with Object pool for concurrently sending MINT transaction
```java
public void voucherClientPoolconcurrentlysendTransaction() throws Exception {
    // Simulate concurrent requests in backend
    final int simTransactionCount = 30;
    final CountDownLatch updateLatch = new CountDownLatch(simTransactionCount);
    final ExecutorService executorService = Executors.newFixedThreadPool(simTransactionCount);

    // Build pool
    final VoucherClientPoolFactory voucherClientPoolFactory = new VoucherClientPoolFactory(adminClientConfig);
    final GenericObjectPoolConfig<VoucherClient> objectPoolConfig = new GenericObjectPoolConfig<>();
    objectPoolConfig.setMaxTotal(5); // do not exceed adminAccount's number of proposal keys
    objectPoolConfig.setMaxWaitMillis(120000);
    objectPoolConfig.setBlockWhenExhausted(true);
    final GenericObjectPool<VoucherClient> objectPool = new GenericObjectPool<>(voucherClientPoolFactory,
            objectPoolConfig);

    // Start 
    for (int i = 0; i < simTransactionCount; ++i) {
        final int idx = i;
        executorService.execute(new Thread(() -> {
            VoucherClient client = null;
            try {
                client = objectPool.borrowObject();
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                client.mintVoucher(userAccountAddress.getBase16Value(), "TEST_HASH_POOL" + idx + timeStamp);
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    objectPool.returnObject(client);
                }
                updateLatch.countDown();
            }
        }));
    }
    updateLatch.await();
    executorService.shutdown();
    objectPool.close();
}
```
Check [Tests](./voucher-sdk/src/test/java/matrix/flow/sdk/AppTest.java) for full example

### Test
Start local emulator with bootstrap script

`cd ./packages/contracts`

`make bootstrap-local`

Run test

`cd ./packages/sdk/java/voucher-sdk`

`mvn test`

UlTest.vim output
```bash
? src/test/java/matrix/flow/sdk/AppTest.java
  ? AppTest
    ✔ shouldAnswerWithTrue
    ✔ transferCorrectFUSDShouldNotThrowException
    ✔ transferIncorrectFUSDShouldThrowException
    ✔ verifyFUSDTransactionShouldMintVoucherToSender
    ✔ voucherClientPoolconcurrentlysendTransaction
    ✔ correctSignatureVerificationShouldReturnTrue
    ✔ incorrectSignatureVerificationShouldReturnFalse
```
