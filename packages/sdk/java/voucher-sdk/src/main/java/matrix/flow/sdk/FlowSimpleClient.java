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
import com.nftco.flow.sdk.FlowId;
import com.nftco.flow.sdk.FlowScript;
import com.nftco.flow.sdk.FlowScriptResponse;
import com.nftco.flow.sdk.FlowTransactionResult;
import com.nftco.flow.sdk.FlowTransactionStatus;
import com.nftco.flow.sdk.cadence.ArrayField;
import com.nftco.flow.sdk.cadence.StringField;
import com.nftco.flow.sdk.cadence.UFix64NumberField;
import com.nftco.flow.sdk.cadence.UInt64NumberField;

import org.apache.commons.io.IOUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FlowSimpleClient {

    protected final FlowAccessApi accessAPI;
    protected final int waitForSealTries;

    static final int DAYS_IN_WEEK = 7;

    public FlowSimpleClient(final String host, final int port, final int waitForSealTries) {

        this.accessAPI = Flow.newAccessApi(host, port);
        this.waitForSealTries = waitForSealTries;
    }

    /**
     * Verify user signatures to cryptographically verify the ownership of a Flow
     * account by verifying a message was signed by a user's private key/s
     *
     * @param message      singed raw message in Hex string
     * @param publicKeyHex a list of public keys in Hex string
     * @param weights      a list of corresponding weights of singed keys
     * @param signAlgos    a list of singed algorithm, where 2 indicates ECDSA_P256
     *                     others for ECDSA_secp256k1
     * @param hashAlogs    a list of hash algorithm, where 2 for SHA2_256 others for
     *                     SHA3_256
     * @param signatures   a list of signatures which are singed by corresponding
     *                     keypairs
     *
     * @return boolean true if verified or false
     */
    public boolean verifyUserSignature(final String message, final String[] publicKeysHex, final double[] weights,
            final int[] signAlgos, final int[] hashAlogs, final String[] signatures) {

        final FlowScript script = new FlowScript(readScript("verify_sig_script.cdc.temp").getBytes());
        final List<StringField> publicKeyHexC = new ArrayList<StringField>();
        final List<UFix64NumberField> weightsC = new ArrayList<UFix64NumberField>();
        final List<UInt64NumberField> signAlgosC = new ArrayList<UInt64NumberField>();
        final List<UInt64NumberField> hashAlogsC = new ArrayList<UInt64NumberField>();
        final List<StringField> signaturesC = new ArrayList<StringField>();

        for (int i = 0; i < publicKeysHex.length; ++i) {
            publicKeyHexC.add(new StringField(publicKeysHex[i]));
            weightsC.add(new UFix64NumberField(Double.toString(weights[i])));
            signAlgosC.add(new UInt64NumberField(Integer.toString(signAlgos[i])));
            hashAlogsC.add(new UInt64NumberField(Integer.toString(hashAlogs[i])));
            signaturesC.add(new StringField(signatures[i]));
        }

        final FlowScriptResponse result = this.accessAPI.executeScriptAtLatestBlock(script,
                Arrays.asList(new FlowArgument(new StringField(message)).getByteStringValue(),
                        new FlowArgument(new ArrayField(publicKeyHexC)).getByteStringValue(),
                        new FlowArgument(new ArrayField(weightsC)).getByteStringValue(),
                        new FlowArgument(new ArrayField(signAlgosC)).getByteStringValue(),
                        new FlowArgument(new ArrayField(hashAlogsC)).getByteStringValue(),
                        new FlowArgument(new ArrayField(signaturesC)).getByteStringValue()));
        FlowSimpleClient.log.info("Signatures are verified with True result");

        return result.getJsonCadence().getValue().toString().equals("true");
    }

    public FlowAccount getAccount(final FlowAddress address) {
        final FlowAccount ret = this.accessAPI.getAccountAtLatestBlock(address);
        return ret;
    }

    public BigDecimal getAccountBalance(final FlowAddress address) {
        final FlowAccount account = this.getAccount(address);
        return account.getBalance();
    }

    protected FlowId getLatestBlockID() {
        return this.accessAPI.getLatestBlockHeader().getId();
    }

    protected FlowAccountKey getAccountKey(final FlowAddress address, final int keyIndex) {
        final FlowAccount account = this.getAccount(address);
        return account.getKeys().get(keyIndex);
    }

    private FlowTransactionResult getTransactionResult(final FlowId txID) {
        final FlowTransactionResult result = this.accessAPI.getTransactionResultById(txID);
        return result;
    }

    protected FlowTransactionResult waitForSeal(final FlowId txID) {
        FlowTransactionResult txResult;
        int countDown = this.waitForSealTries;

        while (countDown > 0) {
            txResult = this.getTransactionResult(txID);
            countDown--;
            if (txResult.getStatus().equals(FlowTransactionStatus.SEALED)) {
                return txResult;
            }

            try {
                Thread.sleep(1000L);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Timed out waiting for sealed transaction");
    }

    private FlowAddress getAccountCreatedAddress(final FlowTransactionResult txResult) {
        if (!txResult.getStatus().equals(FlowTransactionStatus.SEALED) || txResult.getErrorMessage().length() > 0) {
            return null;
        }

        final String rez = txResult.getEvents().get(0).getEvent().getValue().getFields()[0].getValue().getValue()
                .toString();

        return new FlowAddress(rez.substring(2));
    }

    static String readScript(final String name) {
        try (InputStream is = FlowSimpleClient.class.getClassLoader().getResourceAsStream(name);) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
