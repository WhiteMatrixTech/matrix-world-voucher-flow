package matrix.flow.sdk;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public final class VoucherClientPoolFactory extends BasePooledObjectFactory<VoucherClient>{
    private AtomicInteger atomicInteger = new AtomicInteger();

    private final String host;
    private final int port;
    private final String accountAddress;
    private final String privateKeyHex;
    private final String fusdAddress;
    private final String fungibleTokenAddress;
    private final String nonFungibleTokenAddress;
    private final String voucherAddress;

    public VoucherClientPoolFactory(String host, int port, String accountAddress, String privateKeyHex, String fusdAddress, String fungibleTokenAddress, String nonFungibleTokenAddress, String voucherAddress) {
        this.host = host;
        this.port = port;
        this.accountAddress = accountAddress;
        this.privateKeyHex = privateKeyHex;
        this.fusdAddress = fusdAddress;
        this.fungibleTokenAddress = fungibleTokenAddress;
        this.nonFungibleTokenAddress = nonFungibleTokenAddress;
        this.voucherAddress = voucherAddress;
    }

    public VoucherClient create() {
        return new VoucherClient(host, port, privateKeyHex, atomicInteger.getAndIncrement(), accountAddress, fusdAddress, fungibleTokenAddress, nonFungibleTokenAddress, voucherAddress);
    }

    public PooledObject<VoucherClient> wrap(VoucherClient client) {
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<VoucherClient> client) throws Exception {
        atomicInteger.getAndDecrement();
        super.destroyObject(client);
    }

    @Override
    public boolean validateObject(PooledObject<VoucherClient> client) {
        return super.validateObject(client);
    }

    @Override
    public void activateObject(PooledObject<VoucherClient> client) throws Exception {
        super.activateObject(client);
    }

    @Override
    public void passivateObject(PooledObject<VoucherClient> client) throws Exception {
        super.passivateObject(client);
    }
}
