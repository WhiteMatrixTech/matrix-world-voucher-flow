package matrix.flow.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nftco.flow.sdk.Flow;
import com.nftco.flow.sdk.FlowAccessApi;
import com.nftco.flow.sdk.FlowAccount;
import com.nftco.flow.sdk.FlowAccountKey;
import com.nftco.flow.sdk.FlowAddress;
import com.nftco.flow.sdk.FlowArgument;
import com.nftco.flow.sdk.FlowEvent;
import com.nftco.flow.sdk.FlowId;
import com.nftco.flow.sdk.FlowPublicKey;
import com.nftco.flow.sdk.FlowScript;
import com.nftco.flow.sdk.FlowTransaction;
import com.nftco.flow.sdk.FlowTransactionProposalKey;
import com.nftco.flow.sdk.FlowTransactionResult;
import com.nftco.flow.sdk.FlowTransactionStatus;
import com.nftco.flow.sdk.HashAlgorithm;
import com.nftco.flow.sdk.SignatureAlgorithm;
import com.nftco.flow.sdk.Signer;
import com.nftco.flow.sdk.cadence.AddressField;
import com.nftco.flow.sdk.cadence.StringField;
import com.nftco.flow.sdk.cadence.UFix64NumberField;
import com.nftco.flow.sdk.cadence.UInt64NumberField;
import com.nftco.flow.sdk.crypto.Crypto;
import com.nftco.flow.sdk.crypto.PrivateKey;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;

import matrix.flow.sdk.model.VoucherMetadataModel;

public final class VoucherClient {

    private final FlowAccessApi accessAPI;
    private final FlowAddress accountAddress;
    private final PrivateKey privateKey;
    private final int keyIndex;
    private final String fusdAddress;
    private final String fungibleTokenAddress;
    private final String nonFungibleTokenAddress;
    private final String voucherAddress;
    private final int waitForSealTries;

    static final int DAYS_IN_WEEK = 7;
    static final String FUNGIBLE_TOKEN_ADDRESS_TEMP = "%FUNGIBLE_TOKEN_ADDRESS";
    static final String FUSD_ADDRESS_TEMP = "%FUSD_ADDRESS";
    static final String NON_FUNGIBLE_TOKEN_ADDRESS_TEMP = "%NON_FUNGIBLE_TOKEN_ADDRESS";
    static final String VOUCHER_ADDRESS = "%VOUCHER_ADDRESS";

    public VoucherClient(String host, int port, String privateKeyHex, int keyIndex, String accountAddress, String fusdAddress,
            String fungibleTokenAddress, String nonFungibleTokenAddress, String voucherAddress, int waitForSealTries) {
        // TODO: Build a config model
        this.accessAPI = Flow.newAccessApi(host, port);
        this.privateKey = Crypto.decodePrivateKey(privateKeyHex);
        this.keyIndex = keyIndex;
        this.accountAddress = new FlowAddress(accountAddress);
        this.fusdAddress = fusdAddress;
        this.fungibleTokenAddress = fungibleTokenAddress;
        this.nonFungibleTokenAddress = nonFungibleTokenAddress;
        this.voucherAddress = voucherAddress;
        this.waitForSealTries = waitForSealTries;
    }

    // ============================ Voucher Related Functions
    public FlowId transferFUSD(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount)
            throws Exception {
        if (amount.scale() != 8) {
            throw new Exception("FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }
        FlowAccountKey senderAccountKey = this.getAccountKey(senderAddress, this.keyIndex);
        String cadenceScript = readScript("transfer_fusd.cdc.temp");
        cadenceScript = cadenceScript.replaceAll(VoucherClient.FUNGIBLE_TOKEN_ADDRESS_TEMP, this.fungibleTokenAddress);
        cadenceScript = cadenceScript.replaceAll(VoucherClient.FUSD_ADDRESS_TEMP, this.fusdAddress);
        FlowTransaction tx = new FlowTransaction(new FlowScript(cadenceScript.getBytes()),
                Arrays.asList(new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                        new FlowArgument(new AddressField(recipientAddress.getBase16Value()))),
                this.getLatestBlockID(), 100L,
                new FlowTransactionProposalKey(senderAddress, senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                senderAddress, Arrays.asList(senderAddress), new ArrayList<>(), new ArrayList<>());

        Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        FlowId txID = this.accessAPI.sendTransaction(tx);
        this.waitForSeal(txID);
        return txID;
    }

    public VoucherMetadataModel mintVoucher(String recipientAddressString, String landInfoHashString) throws Exception {

        // Setup cadence script
        FlowAddress recipientAddress = new FlowAddress(recipientAddressString);
        FlowAccountKey senderAccountKey = this.getAccountKey(this.accountAddress, this.keyIndex);
        String cadenceScript = readScript("mint_voucher.cdc.temp");
        cadenceScript = cadenceScript.replaceAll(VoucherClient.NON_FUNGIBLE_TOKEN_ADDRESS_TEMP,
                this.nonFungibleTokenAddress);
        cadenceScript = cadenceScript.replaceAll(VoucherClient.VOUCHER_ADDRESS, this.voucherAddress);

        // NFT metadata
        VoucherMetadataModel metadata = VoucherMetadataModel.builder().hash(landInfoHashString).build();

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

        Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(this.accountAddress, senderAccountKey.getId(), signer);

        FlowId txID = this.accessAPI.sendTransaction(tx);
        FlowTransactionResult result = this.waitForSeal(txID);
        if (result.getStatus() != FlowTransactionStatus.SEALED) {
            throw new Exception("There is something wrong with the transaction");
        }

        VoucherMetadataModel mintedToken = new VoucherMetadataModel();
        for (FlowEvent event: result.getEvents()) {
            if (event.getType().contains(this.voucherAddress+".MatrixWorldVoucher.Minted")){
                UInt64NumberField id = (UInt64NumberField) event.getField("id");
                mintedToken.setId(id.toInt());
                StringField name = (StringField) event.getField("name");
                mintedToken.setName(name.getValue().toString());
                StringField description = (StringField) event.getField("description");
                mintedToken.setDescription(description.getValue().toString());
                StringField animationUrl = (StringField) event.getField("animationUrl");
                mintedToken.setAnimationUrl(animationUrl.getValue().toString());
                StringField hash = (StringField) event.getField("hash");
                mintedToken.setHash(hash.getValue().toString());
                StringField type = (StringField) event.getField("type");
                mintedToken.setType(type.getValue().toString());
            }
            break;
        }
        return mintedToken;
    }

    public void verifyFUSDTransaction(String payerAddress, BigDecimal amount, String transactionId) throws Exception {
        FlowTransactionResult txResult = this.waitForSeal((new FlowId(transactionId)));

        if (amount.scale() != 8) {
            throw new Exception("FUSD amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        List<FlowEvent> events = txResult.getEvents();

        if (events.size() != 2) {
            throw new Exception("This not an official FUSD transferTokens event");
        }

        FlowEvent firstEvent = events.get(0);
        FlowEvent secondEvent = events.get(1);

        if (!firstEvent.getType().toString().equals("A." + this.fusdAddress + ".FUSD.TokensWithdrawn")
                || !secondEvent.getType().toString().equals("A." + this.fusdAddress + ".FUSD.TokensDeposited")) {
            throw new Exception("This not an official FUSD transferTokens event");
        }
        UFix64NumberField amountFrom = (UFix64NumberField) firstEvent.getField("amount");

        if (!amountFrom.toBigDecimal().equals(amount)) {
            throw new Exception("Withdrawn FUSD amount not match");
        }
        AddressField from = (AddressField) firstEvent.getField("from").getValue();
        if (!from.getValue().toString().substring(2).equals(payerAddress)) {
            throw new Exception("Withdrawn from wrong address");
        }

        UFix64NumberField amountTo = (UFix64NumberField) secondEvent.getField("amount");
        if (!amountTo.toBigDecimal().equals(amount)) {
            throw new Exception("Deposited FUSD amount not match");
        }

        AddressField to = (AddressField) secondEvent.getField("to").getValue();
        if (!to.getValue().toString().substring(2).equals(this.accountAddress.getBase16Value())) {
            throw new Exception("Deposited to wrong address");
        }
    }

    // ============================ Flow Util Functions
    public FlowAddress createAccount(FlowAddress payerAddress, String publicKeyHex) {
        FlowAccountKey payerAccountKey = this.getAccountKey(payerAddress, 0);
        FlowAccountKey newAccountPublicKey = new FlowAccountKey(0, new FlowPublicKey(publicKeyHex),
                SignatureAlgorithm.ECDSA_P256, HashAlgorithm.SHA2_256, 1, 0, false);

        FlowTransaction tx = new FlowTransaction(new FlowScript(loadScript("create_account.cdc")),
                Arrays.asList(new FlowArgument(new StringField(Hex.toHexString(newAccountPublicKey.getEncoded())))),
                this.getLatestBlockID(), 100L,
                new FlowTransactionProposalKey(payerAddress, payerAccountKey.getId(),
                        payerAccountKey.getSequenceNumber()),
                payerAddress, Arrays.asList(payerAddress), new ArrayList<>(), new ArrayList<>());

        Signer signer = Crypto.getSigner(this.privateKey, payerAccountKey.getHashAlgo());
        tx = tx.addPayloadSignature(payerAddress, 0, signer);
        tx = tx.addEnvelopeSignature(payerAddress, 0, signer);

        FlowId txID = this.accessAPI.sendTransaction(tx);
        FlowTransactionResult txResult = this.waitForSeal(txID);

        return this.getAccountCreatedAddress(txResult);
    }

    public void transferTokens(FlowAddress senderAddress, FlowAddress recipientAddress, BigDecimal amount)
            throws Exception {
        // exit early
        if (amount.scale() != 8) {
            throw new Exception("FLOW amount must have exactly 8 decimal places of precision (e.g. 10.00000000)");
        }

        FlowAccountKey senderAccountKey = this.getAccountKey(senderAddress, 0);
        FlowTransaction tx = new FlowTransaction(new FlowScript(loadScript("transfer_flow.cdc")),
                Arrays.asList(new FlowArgument(new UFix64NumberField(amount.toPlainString())),
                        new FlowArgument(new AddressField(recipientAddress.getBase16Value()))),
                this.getLatestBlockID(), 100L,
                new FlowTransactionProposalKey(senderAddress, senderAccountKey.getId(),
                        senderAccountKey.getSequenceNumber()),
                senderAddress, Arrays.asList(senderAddress), new ArrayList<>(), new ArrayList<>());

        Signer signer = Crypto.getSigner(this.privateKey, senderAccountKey.getHashAlgo());
        tx = tx.addEnvelopeSignature(senderAddress, senderAccountKey.getId(), signer);

        FlowId txID = this.accessAPI.sendTransaction(tx);
        this.waitForSeal(txID);
    }

    public FlowAccount getAccount(FlowAddress address) {
        FlowAccount ret = this.accessAPI.getAccountAtLatestBlock(address);
        return ret;
    }

    public BigDecimal getAccountBalance(FlowAddress address) {
        FlowAccount account = this.getAccount(address);
        return account.getBalance();
    }

    private FlowId getLatestBlockID() {
        return this.accessAPI.getLatestBlockHeader().getId();
    }

    private FlowAccountKey getAccountKey(FlowAddress address, int keyIndex) {
        FlowAccount account = this.getAccount(address);
        return account.getKeys().get(keyIndex);
    }

    private FlowTransactionResult getTransactionResult(FlowId txID) {
        FlowTransactionResult result = this.accessAPI.getTransactionResultById(txID);
        return result;
    }

    private FlowTransactionResult waitForSeal(FlowId txID) {
        FlowTransactionResult txResult;
        int countDown = this.waitForSealTries;

        while (countDown>0) {
            txResult = this.getTransactionResult(txID);
            countDown--;
            if (txResult.getStatus().equals(FlowTransactionStatus.SEALED)) {
                return txResult;
            }

            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Timed out waiting for sealed transaction");
    }

    private FlowAddress getAccountCreatedAddress(FlowTransactionResult txResult) {
        if (!txResult.getStatus().equals(FlowTransactionStatus.SEALED) || txResult.getErrorMessage().length() > 0) {
            return null;
        }

        String rez = txResult.getEvents().get(0).getEvent().getValue().getFields()[0].getValue().getValue().toString();
        return new FlowAddress(rez.substring(2));
    }

    private byte[] loadScript(String name) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);) {
            return is.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readScript(String name) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);) {

            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
