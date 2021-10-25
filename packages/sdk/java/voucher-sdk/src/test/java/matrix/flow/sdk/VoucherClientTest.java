package matrix.flow.sdk;

import java.math.BigDecimal;

import com.nftco.flow.sdk.FlowAddress;
import com.nftco.flow.sdk.FlowId;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import matrix.flow.sdk.model.PaymentType;
import matrix.flow.sdk.model.TransferEvent;
import matrix.flow.sdk.model.VoucherClientConfig;

/**
 * Unit test for simple App.
 */
public class VoucherClientTest {
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
    public static final String API_HOST = "localhost";
    public static final int API_PORT = 3569;
    public static final String ADMIN_ADDRESS = "01cf0e2f2f715450";
    public static final String ADMIN_PRIVATE_KEY = "a996c6d610d93faf82ad5b15407b66d3a2b72a284b5c2fd4097b5a3e735a79e1";
    public static final int WAIT_FOR_SEAL_TRIES = 20;


    private final FlowAddress testAdminAccountAddress = new FlowAddress(ADMIN_ADDRESS);
    private final FlowAddress userAccountAddress = new FlowAddress("f8d6e0586b0a20c7");

    final VoucherClientConfig adminClientConfig = VoucherClientConfig.builder().host(API_HOST).port(API_PORT)
            .privateKeyHex(TEST_ADMIN_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(ADMIN_ADDRESS)
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(WAIT_FOR_SEAL_TRIES).fusdAddress(FUSD_ADDRESS).flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

    final VoucherClientConfig userClientConfig = VoucherClientConfig.builder().host(API_HOST).port(API_PORT)
            .privateKeyHex(SERVICE_PRIVATE_KEY_HEX).keyIndex(0).nonFungibleTokenAddress(NON_FUNGIBLE_TOKEN_ADDRESS)
            .fungibleTokenAddress(FUNGIBLE_TOKEN_ADDRESS).adminAccountAddress(userAccountAddress.getBase16Value())
            .voucherAddress(VOUCHER_ADDRESS).waitForSealTries(20).fusdAddress(FUSD_ADDRESS).flowTokenAddress(FLOW_TOKEN_ADDRESS).build();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void resolveFLOWTransferEventShouldWorkProperly() throws Exception {
        final VoucherClient adminClient = new VoucherClient(adminClientConfig);

        final VoucherClient userClient = new VoucherClient(userClientConfig);

        // Simulate payment to get transactionId
        final BigDecimal targetAmount = BigDecimal.valueOf(10000000, 8);
        final FlowId txId = userClient.transferFlowToken(userAccountAddress, testAdminAccountAddress, targetAmount);

        // Resolve FLOW transfer event

        final TransferEvent resolved = adminClient.resolveTransferEventFromTransactionId(txId.getBase16Value(), PaymentType.FLOW);

        System.out.println(resolved.toString());
        assert (resolved.getTo().equals(ADMIN_ADDRESS));
        assert (resolved.getFrom().equals(userAccountAddress.getBase16Value()));
        assert (resolved.getAmountFrom().equals(targetAmount));
        assert (resolved.getAmountTo().equals(targetAmount));

    }
}
