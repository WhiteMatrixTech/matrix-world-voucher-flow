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
import com.nftco.flow.sdk.cadence.ArrayField;
import com.nftco.flow.sdk.cadence.StringField;
import com.nftco.flow.sdk.cadence.UFix64NumberField;
import com.nftco.flow.sdk.cadence.UInt64NumberField;
import com.nftco.flow.sdk.crypto.Crypto;
import com.nftco.flow.sdk.crypto.PrivateKey;

import matrix.flow.sdk.model.FlowClientException;
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
     * @param landInfoHashString     flow type landInfoHashString in hex
     *
     * @return Voucher Model
     *
     * @throws Exception unknown runtime error
     */
    public VoucherMetadataModel mintVoucher(final String recipientAddressString, final String landInfoHashString)
            throws FlowClientException {

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
            throw new FlowClientException("There is something wrong with the transaction");
        }

        final VoucherMetadataModel mintedToken = new VoucherMetadataModel();
        for (final FlowEvent event : result.getEvents()) {
            if (event.getType().contains(this.clientConfig.getVoucherAddress() + ".MatrixWorldVoucher.Minted")) {
                final UInt64NumberField id = (UInt64NumberField) event.getField("id");
                mintedToken.setId(id.toInt());
                final StringField name = (StringField) event.getField("name");
                mintedToken.setName(name.getValue());
                final StringField description = (StringField) event.getField("description");
                mintedToken.setDescription(description.getValue());
                final StringField animationUrl = (StringField) event.getField("animationUrl");
                mintedToken.setAnimationUrl(animationUrl.getValue());
                final StringField hash = (StringField) event.getField("hash");
                mintedToken.setHash(hash.getValue());
                final StringField type = (StringField) event.getField("type");
                mintedToken.setType(type.getValue());
                break;
            }
        }
        return mintedToken;
    }

    /**
     * Mint a batch of Vouchers
     *
     * @param recipientAddressStringList list of recipient account address
     * @param landInfoHashStringList list of landInfoHash
     *
     * @return a list of Minted token
     *
     * @throws FlowClientException runtime exception
     */
    public List<VoucherMetadataModel> batchMintVoucher(final String[] recipientAddressStringList,
            final String[] landInfoHashStringList) throws FlowClientException {

        final List<AddressField> recipientAddressListC = new ArrayList<>();
        final List<StringField> landInfoHashStringListC = new ArrayList<>();
        final List<StringField> namesC = new ArrayList<>();
        final List<StringField> descriptionsC = new ArrayList<>();
        final List<StringField> animationUrlsC = new ArrayList<>();
        final List<StringField> typesC = new ArrayList<>();

        for (int i = 0; i < recipientAddressStringList.length; ++i) {
            final VoucherMetadataModel metadata = VoucherMetadataModel.builder().hash(landInfoHashStringList[i])
                    .build();
            recipientAddressListC
                    .add(new AddressField(new FlowAddress(recipientAddressStringList[i]).getBase16Value()));
            landInfoHashStringListC.add(new StringField(landInfoHashStringList[i]));
            namesC.add(new StringField(metadata.getName()));
            descriptionsC.add(new StringField(metadata.getDescription()));
            animationUrlsC.add(new StringField(metadata.getAnimationUrl()));
            typesC.add(new StringField(metadata.getType()));
        }

        // Setup cadence script
        final FlowAccountKey senderAccountKey = this.getAccountKey(this.accountAddress,
                this.clientConfig.getKeyIndex());
        String cadenceScript = readScript("batch_mint_voucher.cdc.temp");
        cadenceScript = cadenceScript.replaceAll(VoucherClient.NON_FUNGIBLE_TOKEN_ADDRESS_TEMP,
                this.clientConfig.getNonFungibleTokenAddress());
        cadenceScript = cadenceScript.replaceAll(VoucherClient.VOUCHER_ADDRESS, this.clientConfig.getVoucherAddress());

        // Build flow transaction
        FlowTransaction tx = new FlowTransaction(new FlowScript(cadenceScript.getBytes()), Arrays.asList(
                new FlowArgument(new ArrayField(recipientAddressListC)), new FlowArgument(new ArrayField(namesC)),
                new FlowArgument(new ArrayField(descriptionsC)), new FlowArgument(new ArrayField(animationUrlsC)),
                new FlowArgument(new ArrayField(landInfoHashStringListC)), new FlowArgument(new ArrayField(typesC))),
                this.getLatestBlockID(), 9999L,
                new FlowTransactionProposalKey(this.accountAddress, senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                this.accountAddress, Arrays.asList(this.accountAddress), new ArrayList<>(), new ArrayList<>());

        final Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(this.accountAddress, senderAccountKey.getId(), signer);

        final FlowId txID = this.accessAPI.sendTransaction(tx);
        final FlowTransactionResult result = this.waitForSeal(txID);
        if (result.getStatus() != FlowTransactionStatus.SEALED) {
            throw new FlowClientException("There is something wrong with the transaction");
        }

        // TokenList
        final List<VoucherMetadataModel> mintedTokens = new ArrayList<>();
        for (final FlowEvent event : result.getEvents()) {
            if (event.getType().contains(this.clientConfig.getVoucherAddress() + ".MatrixWorldVoucher.Minted")) {
                final VoucherMetadataModel mintedToken = new VoucherMetadataModel();
                final UInt64NumberField id = (UInt64NumberField) event.getField("id");
                mintedToken.setId(id.toInt());
                final StringField name = (StringField) event.getField("name");
                mintedToken.setName(name.getValue());
                final StringField description = (StringField) event.getField("description");
                mintedToken.setDescription(description.getValue());
                final StringField animationUrl = (StringField) event.getField("animationUrl");
                mintedToken.setAnimationUrl(animationUrl.getValue());
                final StringField hash = (StringField) event.getField("hash");
                mintedToken.setHash(hash.getValue());
                final StringField type = (StringField) event.getField("type");
                mintedToken.setType(type.getValue());
                mintedTokens.add(mintedToken);
            }
        }

        return mintedTokens;
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
            throws FlowClientException {
        final FlowTransactionResult txResult = this.waitForSeal((new FlowId(transactionId)));

        if (amount.scale() != 8) {
            throw new FlowClientException(
                    "FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        final List<FlowEvent> events = txResult.getEvents();

        if (events.size() != 2) {
            throw new FlowClientException("This not an official FUSD transferTokens event");
        }

        final FlowEvent firstEvent = events.get(0);
        final FlowEvent secondEvent = events.get(1);

        if (!firstEvent.getType().equals("A." + this.clientConfig.getFusdAddress() + ".FUSD.TokensWithdrawn")
                || !secondEvent.getType().equals("A." + this.clientConfig.getFusdAddress() + ".FUSD.TokensDeposited")) {
            throw new FlowClientException("This not an official FUSD transferTokens event");
        }
        final UFix64NumberField amountFrom = (UFix64NumberField) firstEvent.getField("amount");

        if (!amountFrom.toBigDecimal().equals(amount)) {
            throw new FlowClientException("Withdrawn FUSD amount not match");
        }
        final AddressField from = (AddressField) firstEvent.getField("from").getValue();
        if (!from.getValue().substring(2).equals(payerAddress)) {
            throw new FlowClientException("Withdrawn from wrong address");
        }

        final UFix64NumberField amountTo = (UFix64NumberField) secondEvent.getField("amount");
        if (!amountTo.toBigDecimal().equals(amount)) {
            throw new FlowClientException("Deposited FUSD amount not match");
        }

        final AddressField to = (AddressField) secondEvent.getField("to").getValue();
        if (!to.getValue().substring(2).equals(this.accountAddress.getBase16Value())) {
            throw new FlowClientException("Deposited to wrong address");
        }
        return true;
    }

    // ============================ Only for test
    public FlowId transferFUSD(final FlowAddress senderAddress, final FlowAddress recipientAddress,
            final BigDecimal amount) throws FlowClientException {
        if (amount.scale() != 8) {
            throw new FlowClientException(
                    "FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
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
