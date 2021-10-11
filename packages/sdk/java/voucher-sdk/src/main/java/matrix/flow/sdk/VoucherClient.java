package matrix.flow.sdk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nftco.flow.sdk.FlowAccountKey;
import com.nftco.flow.sdk.FlowAddress;
import com.nftco.flow.sdk.FlowArgument;
import com.nftco.flow.sdk.FlowEvent;
import com.nftco.flow.sdk.FlowId;
import com.nftco.flow.sdk.FlowScript;
import com.nftco.flow.sdk.FlowTransaction;
import com.nftco.flow.sdk.FlowTransactionProposalKey;
import com.nftco.flow.sdk.FlowTransactionResult;
import com.nftco.flow.sdk.FlowTransactionStatus;
import com.nftco.flow.sdk.Signer;
import com.nftco.flow.sdk.cadence.AddressField;
import com.nftco.flow.sdk.cadence.StringField;
import com.nftco.flow.sdk.cadence.UFix64NumberField;
import com.nftco.flow.sdk.cadence.UInt64NumberField;
import com.nftco.flow.sdk.crypto.Crypto;
import com.nftco.flow.sdk.crypto.PrivateKey;

import matrix.flow.sdk.model.VoucherClientConfig;
import matrix.flow.sdk.model.VoucherMetadataModel;

public final class VoucherClient extends FlowSimpleClient {

    private final FlowAddress accountAddress;
    private final PrivateKey privateKey;
    private final VoucherClientConfig clientConfig;

    static final int DAYS_IN_WEEK = 7;
    static final String FUNGIBLE_TOKEN_ADDRESS_TEMP = "%FUNGIBLE_TOKEN_ADDRESS";
    static final String FUSD_ADDRESS_TEMP = "%FUSD_ADDRESS";
    static final String NON_FUNGIBLE_TOKEN_ADDRESS_TEMP = "%NON_FUNGIBLE_TOKEN_ADDRESS";
    static final String VOUCHER_ADDRESS = "%VOUCHER_ADDRESS";

    public VoucherClient(final VoucherClientConfig clientConfig) {
        super(clientConfig.getHost(), clientConfig.getPort(), clientConfig.getWaitForSealTries());
        this.clientConfig = clientConfig;
        this.privateKey = Crypto.decodePrivateKey(clientConfig.getPrivateKeyHex());
        this.accountAddress = new FlowAddress(clientConfig.getAdminAccountAddress());
    }

    /**
     * Mint a Voucher NFT
     *
     * @param recipientAddressString recipient account address
     * @param landInfoHashString flow type landInfoHashString in hex
     *
     * @return Voucher Model
     *
     * @throws Exception unknown runtime error
     */
    public VoucherMetadataModel mintVoucher(final String recipientAddressString, final String landInfoHashString)
            throws Exception {

        // Setup cadence script
        final FlowAddress recipientAddress = new FlowAddress(recipientAddressString);
        final FlowAccountKey senderAccountKey = this.getAccountKey(this.accountAddress,
                this.clientConfig.getKeyIndex());
        String cadenceScript = readScript("mint_voucher.cdc.temp");
        cadenceScript = cadenceScript.replaceAll(VoucherClient.NON_FUNGIBLE_TOKEN_ADDRESS_TEMP,
                this.clientConfig.getNonFungibleTokenAddress());
        cadenceScript = cadenceScript.replaceAll(VoucherClient.VOUCHER_ADDRESS, this.clientConfig.getVoucherAddress());

        // NFT metadata
        final VoucherMetadataModel metadata = VoucherMetadataModel.builder().hash(landInfoHashString).build();

        // Build flow transaction
        FlowTransaction tx = new FlowTransaction(new FlowScript(cadenceScript.getBytes()),
                Arrays.asList(new FlowArgument(new AddressField(recipientAddress.getBase16Value())),
                        new FlowArgument(new StringField(metadata.getName())),
                        new FlowArgument(new StringField(metadata.getDescription())),
                        new FlowArgument(new StringField(metadata.getAnimationUrl())),
                        new FlowArgument(new StringField(metadata.getHash())),
                        new FlowArgument(new StringField(metadata.getType()))),
                this.getLatestBlockID(), 100L,
                new FlowTransactionProposalKey(this.accountAddress, senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                this.accountAddress, Arrays.asList(this.accountAddress), new ArrayList<>(), new ArrayList<>());

        final Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(this.accountAddress, senderAccountKey.getId(), signer);

        final FlowId txID = this.accessAPI.sendTransaction(tx);
        final FlowTransactionResult result = this.waitForSeal(txID);
        if (result.getStatus() != FlowTransactionStatus.SEALED) {
            throw new Exception("There is something wrong with the transaction");
        }

        final VoucherMetadataModel mintedToken = new VoucherMetadataModel();
        for (final FlowEvent event : result.getEvents()) {
            if (event.getType().contains(this.clientConfig.getVoucherAddress() + ".MatrixWorldVoucher.Minted")) {
                final UInt64NumberField id = (UInt64NumberField) event.getField("id");
                mintedToken.setId(id.toInt());
                final StringField name = (StringField) event.getField("name");
                mintedToken.setName(name.getValue().toString());
                final StringField description = (StringField) event.getField("description");
                mintedToken.setDescription(description.getValue().toString());
                final StringField animationUrl = (StringField) event.getField("animationUrl");
                mintedToken.setAnimationUrl(animationUrl.getValue().toString());
                final StringField hash = (StringField) event.getField("hash");
                mintedToken.setHash(hash.getValue().toString());
                final StringField type = (StringField) event.getField("type");
                mintedToken.setType(type.getValue().toString());
            }
            break;
        }
        return mintedToken;
    }

    /**
     * Verify a FUSD transaction
     *
     * @param payerAddress  payer account address
     * @param amount        expected amount to be received
     * @param transactionId flow transactionId
     *
     * @throws Exception with reason of unexpected error
     * @TODO: Custom RunTimeException
     */
    public boolean verifyFUSDTransaction(final String payerAddress, final BigDecimal amount, final String transactionId)
            throws Exception {
        final FlowTransactionResult txResult = this.waitForSeal((new FlowId(transactionId)));

        if (amount.scale() != 8) {
            throw new Exception("FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        final List<FlowEvent> events = txResult.getEvents();

        if (events.size() != 2) {
            throw new Exception("This not an official FUSD transferTokens event");
        }

        final FlowEvent firstEvent = events.get(0);
        final FlowEvent secondEvent = events.get(1);

        if (!firstEvent.getType().toString().equals("A." + this.clientConfig.getFusdAddress() + ".FUSD.TokensWithdrawn")
                || !secondEvent.getType().toString()
                        .equals("A." + this.clientConfig.getFusdAddress() + ".FUSD.TokensDeposited")) {
            throw new Exception("This not an official FUSD transferTokens event");
        }
        final UFix64NumberField amountFrom = (UFix64NumberField) firstEvent.getField("amount");

        if (!amountFrom.toBigDecimal().equals(amount)) {
            throw new Exception("Withdrawn FUSD amount not match");
        }
        final AddressField from = (AddressField) firstEvent.getField("from").getValue();
        if (!from.getValue().toString().substring(2).equals(payerAddress)) {
            throw new Exception("Withdrawn from wrong address");
        }

        final UFix64NumberField amountTo = (UFix64NumberField) secondEvent.getField("amount");
        if (!amountTo.toBigDecimal().equals(amount)) {
            throw new Exception("Deposited FUSD amount not match");
        }

        final AddressField to = (AddressField) secondEvent.getField("to").getValue();
        if (!to.getValue().toString().substring(2).equals(this.accountAddress.getBase16Value())) {
            throw new Exception("Deposited to wrong address");
        }
        return true;
    }

    // ============================ Only for test
    public FlowId transferFUSD(final FlowAddress senderAddress, final FlowAddress recipientAddress,
            final BigDecimal amount) throws Exception {
        if (amount.scale() != 8) {
            throw new Exception("FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        final FlowAccountKey senderAccountKey = this.getAccountKey(senderAddress, this.clientConfig.getKeyIndex());
        String cadenceScript = readScript("transfer_fusd.cdc.temp");
        cadenceScript = cadenceScript.replaceAll(VoucherClient.FUNGIBLE_TOKEN_ADDRESS_TEMP,
                this.clientConfig.getFungibleTokenAddress());
        cadenceScript = cadenceScript.replaceAll(VoucherClient.FUSD_ADDRESS_TEMP, this.clientConfig.getFusdAddress());
        FlowTransaction tx = new FlowTransaction(new FlowScript(cadenceScript.getBytes()),
                Arrays.asList(new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                        new FlowArgument(new AddressField(recipientAddress.getBase16Value()))),
                this.getLatestBlockID(), 100L,
                new FlowTransactionProposalKey(senderAddress, senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                senderAddress, Arrays.asList(senderAddress), new ArrayList<>(), new ArrayList<>());

        final Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        final FlowId txID = this.accessAPI.sendTransaction(tx);
        this.waitForSeal(txID);
        return txID;

    }

}
