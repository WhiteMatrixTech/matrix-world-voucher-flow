package matrix.flow.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class VoucherClientConfig {

    private String host;

    private int port;

    @Default
    private int waitForSealTries = 30;

    private String privateKeyHex;

    @Default
    private int keyIndex = 0;

    private String adminAccountAddress;

    private String fusdAddress;

    private String flowTokenAddress;

    private String fungibleTokenAddress;

    private String nonFungibleTokenAddress;

    private String voucherAddress;

}

