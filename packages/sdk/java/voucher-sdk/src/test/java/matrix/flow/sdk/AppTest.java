package matrix.flow.sdk;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.text.SimpleDateFormat;

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

import io.grpc.netty.shaded.io.netty.util.concurrent.Future;
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
    private final FlowAddress serviceAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

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
        final VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX, 9,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX, 0,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final BigDecimal targetAmount = BigDecimal.valueOf(100000000, 8);
        final FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, targetAmount);

        adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

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
        final VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX, 9,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX, 0,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final BigDecimal realAmount = BigDecimal.valueOf(100000000, 8);
        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, realAmount);

        adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

    }

    /**
     * Test verifyFUSDTransactionShouldMintVoucherToSender
     *
     * @throws Exception
     */
    @Test
    public void verifyFUSDTransactionShouldMintVoucherToSender() throws Exception {
        final VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX, 9,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX, 0,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, targetAmount);

        // Simulate backend logic because txId will be submitted from frontend in real
        // case
        adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

        // Mint Voucher if verification is success
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String testHash = "TEST_HASH_TEST_VERIFY" + timeStamp;
        VoucherMetadataModel newToken = adminClient.mintVoucher(serviceAccountAddress.getBase16Value(), testHash);
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
        final int simTransactionCount = 30;
        final CountDownLatch updateLatch = new CountDownLatch(simTransactionCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(simTransactionCount);
        final VoucherClientPoolFactory voucherClientPoolFactory = new VoucherClientPoolFactory("localhost", 3569,
                TEST_ADMIN_PRIVATE_KEY_HEX, testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS,
                FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);
        final GenericObjectPoolConfig<VoucherClient> objectPoolConfig = new GenericObjectPoolConfig<>();
        objectPoolConfig.setMaxTotal(5);
        objectPoolConfig.setMaxWaitMillis(120000);
        objectPoolConfig.setBlockWhenExhausted(true);
        final GenericObjectPool<VoucherClient> objectPool = new GenericObjectPool<>(voucherClientPoolFactory,
                objectPoolConfig);
        for (int i = 0; i < simTransactionCount; ++i) {
            final int idx = i;
            executorService.execute(new Thread(() -> {
                VoucherClient client = null;
                try {
                    client = objectPool.borrowObject();
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    client.mintVoucher(serviceAccountAddress.getBase16Value(), "TEST_HASH_POOL" + idx + timeStamp);
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
    public void jvmSdkSignatureVerificationShouldWork() throws Exception {
        final KeyPair keyPair = Crypto.generateKeyPair(SignatureAlgorithm.ECDSA_P256);
        final PublicKey publicKey = Crypto.decodePublicKey(keyPair.getPublic().getHex());
        final PrivateKey privateKey = Crypto.decodePrivateKey(keyPair.getPrivate().getHex());

        final Signer signer = Crypto.getSigner(privateKey, HashAlgorithm.SHA3_256);
        final byte[] signature = signer.signAsUser("TEST".getBytes());

        final VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX, 9,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,
                NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS, 20);

        System.out.println(Hex.encodeHexString("TEST".getBytes()));
        System.out.println(Hex.encodeHexString((signature)));
        System.out.println(publicKey.getHex());
        adminClient.verifySignature(Hex.encodeHexString("TEST".getBytes()), new String[] { publicKey.getHex() },
                new double[] { 1.0 }, new int[] { 2 }, new String[] { Hex.encodeHexString(signature) });
    }
}
