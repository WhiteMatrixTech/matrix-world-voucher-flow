package matrix.flow.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
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
import com.nftco.flow.sdk.HashAlgorithm;
import com.nftco.flow.sdk.cadence.ArrayField;
import com.nftco.flow.sdk.cadence.StringField;
import com.nftco.flow.sdk.cadence.UFix64NumberField;
import com.nftco.flow.sdk.cadence.UInt64NumberField;
import com.nftco.flow.sdk.crypto.HasherImpl;

import org.apache.commons.io.IOUtils;

import lombok.extern.log4j.Log4j2;
import matrix.flow.sdk.model.FlowClientException;

@Log4j2
public class FlowSimpleClient {

    protected final FlowAccessApi accessAPI;
    protected final int waitForSealTries;
    protected final HasherImpl hasher;

    static final int DAYS_IN_WEEK = 7;

    public FlowSimpleClient(final String host, final int port, final int waitForSealTries) {

        this.accessAPI = Flow.newAccessApi(host, port);
        this.waitForSealTries = waitForSealTries;
        this.hasher = new HasherImpl(HashAlgorithm.SHA3_256);
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
        final List<StringField> publicKeyHexC = new ArrayList<>();
        final List<UFix64NumberField> weightsC = new ArrayList<>();
        final List<UInt64NumberField> signAlgosC = new ArrayList<>();
        final List<UInt64NumberField> hashAlogsC = new ArrayList<>();
        final List<StringField> signaturesC = new ArrayList<>();

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

    /**
     * Generate cadence compatible LandInfoHashHexString
     *
     * @param topLeftX UInt64 topLeftX coordinate
     * @param topLeftY UInt64 topLeftY coordinate
     * @param height UInt64 height of square lands
     * @param width  UInt64 width of square lands
     *
     * @return LandInfoHashHexString of a square of Lands
     */
    public String generateLandInfoHash(final Integer topLeftX, final Integer topLeftY, final Integer height,
            final Integer width) {
        final byte[] topLeftXB = Longs.toByteArray(Integer.toUnsignedLong(topLeftX));
        final byte[] topLeftYB = Longs.toByteArray(Integer.toUnsignedLong(topLeftY));
        final byte[] heightB = Longs.toByteArray(Integer.toUnsignedLong(height));
        final byte[] widthB = Longs.toByteArray(Integer.toUnsignedLong(width));

        return hasher.hashAsHexString(Bytes.concat(topLeftXB, topLeftYB, heightB, widthB));
    }

    public String generateLandInfoHashCadence(final Integer topLeftX, final Integer topLeftY, final Integer height,
            final Integer width) {

        final FlowScript script = new FlowScript(readScript("generate_land_hash.cdc.temp").getBytes());

        final FlowScriptResponse result = this.accessAPI.executeScriptAtLatestBlock(script,
                Arrays.asList(new FlowArgument(new UInt64NumberField(topLeftX.toString())).getByteStringValue(),
                        new FlowArgument(new UInt64NumberField(topLeftY.toString())).getByteStringValue(),
                        new FlowArgument(new UInt64NumberField(height.toString())).getByteStringValue(),
                        new FlowArgument(new UInt64NumberField(width.toString())).getByteStringValue()));

        return result.getJsonCadence().getValue().toString();
    }

    public FlowAccount getAccount(final FlowAddress address) {
        return this.accessAPI.getAccountAtLatestBlock(address);
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
        return this.accessAPI.getTransactionResultById(txID);
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
                FlowSimpleClient.log.error("Interrupted with " + e.toString());
                e.printStackTrace();
                Thread.currentThread().interrupt();
                throw new FlowClientException(e.toString());
            }
        }
        throw new FlowClientException("Timed out waiting for sealed transaction");
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
            throw new FlowClientException(e.toString());
        }
    }

}
