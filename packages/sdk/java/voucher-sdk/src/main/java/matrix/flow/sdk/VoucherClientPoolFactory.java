package matrix.flow.sdk;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import matrix.flow.sdk.model.VoucherClientConfig;

public final class VoucherClientPoolFactory extends BasePooledObjectFactory<VoucherClient> {
    private final AtomicInteger atomicInteger = new AtomicInteger();

    private final VoucherClientConfig clientConfig;

    public VoucherClientPoolFactory(final VoucherClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public VoucherClient create() {
        clientConfig.setKeyIndex(atomicInteger.getAndDecrement());
        return new VoucherClient(clientConfig);
    }

    public PooledObject<VoucherClient> wrap(final VoucherClient client) {
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(final PooledObject<VoucherClient> client) throws Exception {
        super.destroyObject(client);
        atomicInteger.getAndDecrement();
    }
}
