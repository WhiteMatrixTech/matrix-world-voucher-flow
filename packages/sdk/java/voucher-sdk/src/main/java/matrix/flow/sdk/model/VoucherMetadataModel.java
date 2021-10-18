package matrix.flow.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherMetadataModel {
    private int id;

    @Default
    private String name = "LandVoucher";

    @Default
    private String description = "Matrix World Land Voucher";

    @Default
    private String animationUrl = "";

    private String hash;

    @Default
    private String type = "Land Voucher";
}
