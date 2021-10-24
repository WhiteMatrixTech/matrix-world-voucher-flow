import {FlowEventFetcher} from "../src/client/EventFetcher";
import {FlowEnv, FlowVoucherClient} from "../src/client/VoucherClient";

/* Need to setup local flow env */
describe("VoucherClient", () => {
    let client: FlowVoucherClient;
    let eventFetcher: FlowEventFetcher;
    before("setup client", async () => {
        eventFetcher = new FlowEventFetcher("http://localhost:8989");
        client = new FlowVoucherClient(eventFetcher, FlowEnv.localEmulator);
    });

    describe("query flow transfer events", () => {
        it("query event", async () => {
            console.log(await client.queryFlowTokenDepositedEvent(0, 200));
        });
        it("query event with recipient", async () => {
            console.log(await client.queryFlowTokenDepositedEvent(0, 200, "0x1cf0e2f2f715450"));
        });
    });
});
