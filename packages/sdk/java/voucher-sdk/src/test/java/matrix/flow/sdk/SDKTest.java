package matrix.flow.sdk;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import matrix.flow.sdk.model.PaymentType;
import matrix.flow.sdk.model.VoucherClientConfig;
import matrix.flow.sdk.model.VoucherMetadataModel;

/**
 * Unit test for simple App.
 */
public class SDKTest {
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
    public static final String FLOW_TOKEN_ADDRESS = "0ae53cb6e3f42a79";
    public static final String NON_FUNGIBLE_TOKEN_ADDRESS = "f8d6e0586b0a20c7";
    public static final String VOUCHER_ADDRESS = "01cf0e2f2f715450";

    private final FlowAddress testAdminAccountAddress = new FlowAddress("01cf0e2f2f715450");
    private final FlowAddress userAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

    final VoucherClientConfig adminClientConfig = VoucherClientConfig.builder().host("localhost").port(3569)
            .privateKeyHex(TEST_ADMIN_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(testAdminAccountAddress.getBase16Value())
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS)
            .flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

    final VoucherClientConfig userClientConfig = VoucherClientConfig.builder().host("localhost").port(3569)
            .privateKeyHex(SERVICE_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(userAccountAddress.getBase16Value())
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS)
            .flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

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
     * Test transferCorrectFLOWShouldNotThrowException
     *
     * @throws Exception
     */
    @Test
    public void transferCorrectFLOWShouldNotThrowException() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal targetAmount = BigDecimal.valueOf(100000000, 8);
        final FlowId txId = userClient.transferFlowToken(userAccountAddress, testAdminAccountAddress, targetAmount);

        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FLOW);

        // expected no exception
        assertTrue(true);
    }

    /**
     * Test transferInCorrectFLOWShouldThrowException
     *
     * @throws Exception
     */
    @Test
    public void transferIncorrectFLOWShouldThrowException() throws Exception {
        exceptionRule.expectMessage("Insufficient payment");
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal realAmount = BigDecimal.valueOf(100000, 8);
        final BigDecimal targetAmount = BigDecimal.valueOf(1000000, 8);
        final FlowId txId = userClient.transferFlowToken(userAccountAddress, testAdminAccountAddress, realAmount);

        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FLOW);

    }

    /**
     * Test verifyFLOWTransactionShouldMintVoucherToSender
     *
     * @throws Exception
     */
    @Test
    public void verifyFLOWTransactionShouldMintVoucherToSender() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = userClient.transferFlowToken(userAccountAddress, testAdminAccountAddress, targetAmount);

        // Simulate backend logic because txId will be submitted from frontend in real
        // case
        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FLOW);

        // Mint Voucher if verification is success
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String testHash = "TEST_HASH_TEST_VERIFY,verifyFLOWTransactionShouldMintVoucherToSender," + timeStamp;
        VoucherMetadataModel newToken = adminClient.mintVoucher(userAccountAddress.getBase16Value(), testHash);
        assert (newToken.getHash().equals(testHash));
        System.out.println(newToken.toString());
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

        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FUSD);

        // expected no exception
        assertTrue(true);
    }

    /**
     * Test transferIncorrectFUSDShouldThrowException
     *
     * @throws Exception
     */
    @Test
    public void transferIncorrectFUSDShouldThrowException() throws Exception {
        exceptionRule.expectMessage("Insufficient payment");
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        final BigDecimal realAmount = BigDecimal.valueOf(1000000, 8);
        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = userClient.transferFUSD(userAccountAddress, testAdminAccountAddress, realAmount);

        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FUSD);

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
        adminClient.verifyPaymentTransaction(userAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value(),
                PaymentType.FUSD);

        // Mint Voucher if verification is success
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String testHash = "TEST_HASH_TEST_VERIFY,verifyFUSDTransactionShouldMintVoucherToSender," + timeStamp;
        VoucherMetadataModel newToken = adminClient.mintVoucher(userAccountAddress.getBase16Value(), testHash);
        assert (newToken.getHash().equals(testHash));
        System.out.println(newToken.toString());
    }

    /**
     * Test verifyFUSDTransactionShouldMintVoucherToSender
     *
     * @throws Exception
     */
    @Test
    public void batchMintVoucherShouldWorldProperly() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final int simBatchSize = 50;
        final List<VoucherMetadataModel> targetTokens = new ArrayList<>();
        final List<String> recipientList = new ArrayList<>();
        final List<String> landInfoHashStringList = new ArrayList<>();
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String testHash = "TEST_HASH_TEST_VERIFY" + timeStamp;
        for (int i = 0; i < simBatchSize; i++) {
            recipientList.add(userAccountAddress.getBase16Value());
            landInfoHashStringList.add(testHash + i);
            final VoucherMetadataModel newToken = VoucherMetadataModel.builder().hash(testHash + i).build();
            targetTokens.add(newToken);
        }

        // Mint Voucher if verification is success
        final List<VoucherMetadataModel> newTokens = adminClient.batchMintVoucher(recipientList,
                landInfoHashStringList);

        assert (newTokens.size() == simBatchSize);
        for (int i = 0; i < simBatchSize; i++) {
            final VoucherMetadataModel target = targetTokens.get(i);
            final VoucherMetadataModel mintToken = newTokens.get(i);
            assert (target.getHash().equals(mintToken.getHash()));
            assert (target.getAnimationUrl().equals(mintToken.getAnimationUrl()));
            assert (target.getName().equals(mintToken.getName()));
            assert (target.getType().equals(mintToken.getType()));
            assert (target.getDescription().equals(mintToken.getDescription()));
        }
    }

    /**
     * Test voucherClientPoolconcurrentlysendTransaction
     *
     * @throws Exception
     */
    @Test(timeout = 10000000)
    public void voucherClientPoolconcurrentlysendTransaction() throws Exception {
        // Simulate concurrent requests in backend
        final int simTransactionCount = 100;
        final CountDownLatch updateLatch = new CountDownLatch(simTransactionCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(simTransactionCount);

        // Build pool
        final VoucherClientPoolFactory voucherClientPoolFactory = new VoucherClientPoolFactory(adminClientConfig, 1, 5);
        final GenericObjectPoolConfig<VoucherClient> objectPoolConfig = new GenericObjectPoolConfig<>();
        objectPoolConfig.setMaxTotal(3); // do not exceed adminAccount's number of proposal keys
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
                    /* System.out.println(client.getAccountKeyIndex()); */
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    client.mintVoucher(userAccountAddress.getBase16Value(), "TEST_HASH_POOL" + idx + timeStamp);
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
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

        // System.out.println(Hex.encodeHexString("TEST".getBytes()));
        // System.out.println(Hex.encodeHexString((signature)));
        // System.out.println(publicKey.getHex());
        final boolean verified = adminClient.verifyUserSignature(Hex.encodeHexString("TEST".getBytes()),
                new String[] { publicKey.getHex() }, new double[] { 1.0 }, new int[] { 2 }, new int[] { 3 },
                new String[] { Hex.encodeHexString(signature) });

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

        final boolean verified = adminClient.verifyUserSignature(Hex.encodeHexString("TEST2".getBytes()),
                new String[] { publicKey.getHex() }, new double[] { 1.0 }, new int[] { 2 }, new int[] { 3 },
                new String[] { Hex.encodeHexString(signature) });

        assertTrue("Incorrect signature verification should return false", !verified);

    }

    @Test
    public void generateLandInfoHashEqualsCadenceVersion() throws Exception {

        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final String hashAsHexString = adminClient.generateLandInfoHash(111, 1, 1, 1);
        final String hashAsHexStringCadence = adminClient.generateLandInfoHashCadence(111, 1, 1, 1);
        // System.out.println(hashAsHexString);
        // System.out.println(hashAsHexStringCadence);
        assertTrue("Java implementation of generateLandInfoHash should equals cadence version",
                StringUtils.equals(hashAsHexStringCadence, hashAsHexString));
    }
}
