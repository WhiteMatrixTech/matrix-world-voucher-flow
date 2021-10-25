package matrix.flow.sdk;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import lombok.extern.log4j.Log4j2;
import matrix.flow.sdk.model.VoucherClientConfig;

@Log4j2
public final class VoucherClientPoolFactory extends BasePooledObjectFactory<VoucherClient> {
    private final ConcurrentLinkedQueue<Integer> keyIndexQueue = new ConcurrentLinkedQueue<Integer>();

    private final VoucherClientConfig clientConfig;

    public VoucherClientPoolFactory(final VoucherClientConfig clientConfig, final int keyStartIndex,
            final int keyCapacity) {
        this.clientConfig = clientConfig;

        // Fulfill keyIndex
        final int keyEndIndex = keyStartIndex + keyCapacity;
        log.info(String.format("Create pool factory from global keyIndex %d to %d", keyStartIndex, keyEndIndex));
        for (int keyIndex = keyStartIndex; keyIndex < keyEndIndex; keyIndex++) {
            this.keyIndexQueue.add(keyIndex);
        }
    }

    public VoucherClient create() {
        final VoucherClientConfig localConfig = this.clientConfig.toBuilder().keyIndex(this.keyIndexQueue.poll())
                .build();
        return new VoucherClient(localConfig);
    }

    public PooledObject<VoucherClient> wrap(final VoucherClient client) {
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(final PooledObject<VoucherClient> client) throws Exception {
        final int keyIndex = client.getObject().getAccountKeyIndex();
        super.destroyObject(client);
        this.keyIndexQueue.add(keyIndex);
    }

}
