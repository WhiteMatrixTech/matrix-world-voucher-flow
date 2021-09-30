package matrix.flow.sdk;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import com.nftco.flow.sdk.FlowAddress;
import com.nftco.flow.sdk.FlowId;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    private FlowAddress testAdminAccountAddress = new FlowAddress("01cf0e2f2f715450");
    private FlowAddress serviceAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

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
        VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        BigDecimal targetAmount = BigDecimal.valueOf(100000000, 8);
        FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, targetAmount);

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
        VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        BigDecimal realAmount = BigDecimal.valueOf(100000000, 8);
        BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, realAmount);


        adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

    }

    /**
     * Test verifyFUSDTransactionShouldMintVoucherToSender
     *
     * @throws Exception
     */
    @Test
    public void verifyFUSDTransactionShouldMintVoucherToSender() throws Exception {
        VoucherClient adminClient = new VoucherClient("localhost", 3569, TEST_ADMIN_PRIVATE_KEY_HEX,
                testAdminAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS,NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        VoucherClient serviceClient = new VoucherClient("localhost", 3569, SERVICE_PRIVATE_KEY_HEX,
                serviceAccountAddress.getBase16Value(), FUSD_ADDRESS, FUNGIBLE_TOKEN_ADDRESS, NON_FUNGIBLE_TOKEN_ADDRESS, VOUCHER_ADDRESS);

        BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        FlowId txId = serviceClient.transferFUSD(serviceAccountAddress, testAdminAccountAddress, targetAmount);

        // Simulate backend logic because txId will be submitted from frontend in real case
        adminClient.verifyFUSDTransaction(serviceAccountAddress.getBase16Value(), targetAmount, txId.getBase16Value());

        // Mint Voucher if verification is success
        adminClient.mintVoucher(serviceAccountAddress.getBase16Value(), "TEST_HASH");
    }
}
