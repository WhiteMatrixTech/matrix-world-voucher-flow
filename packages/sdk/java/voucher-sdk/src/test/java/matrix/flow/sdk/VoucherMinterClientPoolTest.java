package matrix.flow.sdk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nftco.flow.sdk.FlowAddress;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import matrix.flow.sdk.model.VoucherClientConfig;

/**
 * Unit test for simple App.
 */
public class VoucherMinterClientPoolTest {
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
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS).flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

    final VoucherClientConfig userClientConfig = VoucherClientConfig.builder().host("localhost").port(3569)
            .privateKeyHex(SERVICE_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(userAccountAddress.getBase16Value())
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS).flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void VoucherMinterClientPoolShouldWork() throws Exception {
        // Simulate concurrent requests in backend
        final int simTransactionCount = 100;
        final CountDownLatch updateLatch = new CountDownLatch(simTransactionCount);
        final ExecutorService executorService = Executors.newFixedThreadPool(simTransactionCount);

        final VoucherMinterClientPool pool = new VoucherMinterClientPool(0, 10, adminClientConfig);

        // Start
        for (int i = 0; i < simTransactionCount; ++i) {
            final int idx = i;
            executorService.execute(new Thread(() -> {
                try {
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    pool.mintVoucher(userAccountAddress.getBase16Value(), "TEST_HASH_POOL" + idx + timeStamp);
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    updateLatch.countDown();
                }
            }));
        }
        updateLatch.await();
        executorService.shutdown();
        pool.close();
    }
}
