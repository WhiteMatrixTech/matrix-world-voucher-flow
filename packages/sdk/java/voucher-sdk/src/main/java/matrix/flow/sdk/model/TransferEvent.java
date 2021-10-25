package matrix.flow.sdk.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class TransferEvent {
    private String from;

    private String to;

    private String transactionId;

    private PaymentType paymentType;

    private BigDecimal amountFrom;

    private BigDecimal amountTo;
}
