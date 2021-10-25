package matrix.flow.sdk;

import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.extern.log4j.Log4j2;
import matrix.flow.sdk.model.VoucherClientConfig;
import matrix.flow.sdk.model.VoucherMetadataModel;

@Log4j2
public class VoucherMinterClientPool {
    private final GenericObjectPool<VoucherClient> objectPool;

    public VoucherMinterClientPool(final int keyStartIndex, final int keyCapacity,
            final VoucherClientConfig minterClientBaseConfig) {

        final VoucherClientPoolFactory voucherClientPoolFactory = new VoucherClientPoolFactory(minterClientBaseConfig,
                keyStartIndex, keyCapacity);
        final GenericObjectPoolConfig<VoucherClient> objectPoolConfig = new GenericObjectPoolConfig<>();
        objectPoolConfig.setMaxTotal(keyCapacity);
        objectPoolConfig.setMaxWaitMillis(120000); // FIXME: how to proper configure this from external
        objectPoolConfig.setBlockWhenExhausted(true);
        // Build pool
        this.objectPool = new GenericObjectPool<>(voucherClientPoolFactory, objectPoolConfig);
    }

    public List<VoucherMetadataModel> batchMintVoucher(final List<String> recipientList,
            final List<String> landInfoHashStringList) {

        VoucherClient client = null;
        try {
            client = objectPool.borrowObject();
            log.info(String.format("[VoucherMinterClientPool.batchMint] use key index %d to mint",
                    client.getAccountKeyIndex()));
            return client.batchMintVoucher(recipientList, landInfoHashStringList);
        } catch (final Exception e) {
            log.error(String.format("[VoucherMinterClientPool.batchMint] use key index %d to mint failed with %s",
                    client.getAccountKeyIndex(), e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                objectPool.returnObject(client);
            }
        }
    }

    public VoucherMetadataModel mintVoucher(final String recipient, final String landInfoHashString) {

        VoucherClient client = null;
        try {
            client = objectPool.borrowObject();
            log.info(String.format("[VoucherMinterClientPool.mintVoucher] use key index %d to mint",
                    client.getAccountKeyIndex()));
            return client.mintVoucher(recipient, landInfoHashString);
        } catch (final Exception e) {
            log.error(String.format("[VoucherMinterClientPool.mintVoucher] use key index %d to mint failed with %s",
                    client.getAccountKeyIndex(), e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                objectPool.returnObject(client);
            }
        }
    }

    public void close() {
        this.objectPool.close();
    }

}
