package matrix.flow.sdk;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nftco.flow.sdk.FlowAddress;
import com.nftco.flow.sdk.FlowId;
import com.nftco.flow.sdk.HashAlgorithm;
import com.nftco.flow.sdk.SignatureAlgorithm;
import com.nftco.flow.sdk.Signer;
import com.nftco.flow.sdk.crypto.Crypto;
import com.nftco.flow.sdk.crypto.KeyPair;
import com.nftco.flow.sdk.crypto.PrivateKey;
import com.nftco.flow.sdk.crypto.PublicKey;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import matrix.flow.sdk.model.VoucherClientConfig;
import matrix.flow.sdk.model.VoucherMetadataModel;

/**
 * Unit test for simple App.
 */
public class AppTest {
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

    final VoucherClientConfig userClientConfig = VoucherClientConfig.builder().host("localhost").port(3569)
            .privateKeyHex(SERVICE_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(userAccountAddress.getBase16Value())
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS).build();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    /**
     * Test transferCorrectFUSDShouldNotThrowException
     *
     * @throws Exception
     */
    @Test
    public void transferCorrectFUSDShouldNotThrowException() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal targetAmount = BigDecimal.valueOf(100000000, 8);
        final FlowId txId = userClient.transferFUSD(userAccountAddress, testAdminAccountAddress, targetAmount);

        adminClient.verifyFUSDTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

        // expected no exception
        assertTrue(true);
    }

    /**
     * Test transferCorrectFUSDShouldNotThrowException
     *
     * @throws Exception
     */
    @Test
    public void transferIncorrectFUSDShouldThrowException() throws Exception {
        exceptionRule.expectMessage("Withdrawn FUSD amount not match");
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal realAmount = BigDecimal.valueOf(100000000, 8);
        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = userClient.transferFUSD(userAccountAddress, testAdminAccountAddress, realAmount);

        adminClient.verifyFUSDTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

    }

    /**
     * Test verifyFUSDTransactionShouldMintVoucherToSender
     *
     * @throws Exception
     */
    @Test
    public void verifyFUSDTransactionShouldMintVoucherToSender() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = userClient.transferFUSD(userAccountAddress, testAdminAccountAddress, targetAmount);

        // Simulate backend logic because txId will be submitted from frontend in real
        // case
        adminClient.verifyFUSDTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

        // Mint Voucher if verification is success
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String testHash = "TEST_HASH_TEST_VERIFY" + timeStamp;
        VoucherMetadataModel newToken = adminClient.mintVoucher(userAccountAddress.getBase16Value(), testHash);
        assert (newToken.getHash().equals(testHash));
        System.out.println(newToken.toString());
    }

    /**
     * Test voucherClientPoolconcurrentlysendTransaction
     *
     * @throws Exception
     */
    @Test(timeout = 10000000)
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

    @Test
    public void correctSignatureVerificationShouldReturnTrue() throws Exception {
        final KeyPair keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        final PublicKey publicKey = Crypto.decodePublicKey(keyPair.getPublic().getHex());
        final PrivateKey privateKey = Crypto.decodePrivateKey(keyPair.getPrivate().getHex());

        final Signer signer = Crypto.getSigner(privateKey, HashAlgorithm.SHA3_256);
        final byte[] signature = signer.signAsUser("TEST".getBytes());

        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        System.out.println(Hex.encodeHexString("TEST".getBytes()));
        System.out.println(Hex.encodeHexString((signature)));
        System.out.println(publicKey.getHex());
        final boolean verified = adminClient.verifyUserSignature(Hex.encodeHexString("TEST".getBytes()), new String[] { publicKey.getHex() },
                new double[] { 1.0 }, new int[] { 2 }, new int[] { 3 }, new String[] { Hex.encodeHexString(signature) });

        assertTrue("Correct signature verification should return true", verified);

    }

    @Test
    public void incorrectSignatureVerificationShouldReturnFalse() throws Exception {
        final KeyPair keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        final PublicKey publicKey = Crypto.decodePublicKey(keyPair.getPublic().getHex());
        final PrivateKey privateKey = Crypto.decodePrivateKey(keyPair.getPrivate().getHex());

        final Signer signer = Crypto.getSigner(privateKey, HashAlgorithm.SHA3_256);
        final byte[] signature = signer.signAsUser("TEST".getBytes());

        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final boolean verified = adminClient.verifyUserSignature(Hex.encodeHexString("TEST2".getBytes()), new String[] { publicKey.getHex() },
                new double[] { 1.0 }, new int[] { 2 }, new int[] { 3 }, new String[] { Hex.encodeHexString(signature) });

        assertTrue("Incorrect signature verification should return false", !verified);

    }
}
