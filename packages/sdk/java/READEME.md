# JAVA-SDK

## VoucherClient

### Core interfaces
```java
// constructor
public VoucherClient(String host, int port, String privateKeyHex, String accountAddress, String fusdAddress, String fungibleTokenAddress, String nonFungibleTokenAddress, String voucherAddress);

// verifyFUSDTransaction
public void verifyFUSDTransaction(String payerAddress, BigDecimal amount, String transactionId) throws Exception;

// mintVoucher
public void mintVoucher(String recipientAddressString, String landInfoHashString) throws Exception;

```

### Init client 

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
public static String HOST = "localhost";
private FlowAddress testAdminAccountAddress = new FlowAddress("01cf0e2f2f715450");
private FlowAddress serviceAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

public static int PORT = 3569;

// Interface
VoucherClient adminClient = new VoucherClient(HOST, PORT, TEST_ADMIN_PRIVATE_KEY_HEX, testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);
```

### Verify transaction and mint Voucher to payer

```java
adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

// Mint Voucher if verification is success
adminClient.mintVoucher(serviceAccountAddress.getBase16Value(), "TEST_HASH");
```

Check [test@verifyFUSDTransactionShouldMintVoucherToSender](./voucher-sdk/src/test/java/matrix/flow/sdk/AppTest.java) for full example
