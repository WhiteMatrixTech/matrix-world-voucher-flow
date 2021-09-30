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
    private String id;

    @Default
    private String name="MatrixWorldVoucher";

    @Default
    private String description="MatrixWorldVoucher for redeeming land";

    @Default
    private String animationUrl="";

    private String hash;

    @Default
    private String type="1";
}
