import * as fcl from "@onflow/fcl";

describe("FlowFCLBase", () => {
    before("setup accounts", async () => {
        await fcl
            .config()
            // Point App at Emulator
            .put("accessNode.api", "http://localhost:8080")
            // Point FCL at dev-wallet (default port)
            .put("discovery.wallet", "http://localhost:8701/fcl/authn");
        await fcl.logIn();
        await fcl.authenticate();
    });

    describe("sign message", () => {
        it("connect", async () => {
            await fcl.authenticate();
            console.log(
                await fcl.query({
                    cadence: `
                pub fun main(currentUser: Address): Address {
                return currentUser
                }
            `,
                    args: (arg: any, t: any) => [fcl.currentUser]
                })
            );
            const MSG = Buffer.from("FOO").toString("hex");
            console.log(await fcl.currentUser().snapshot());
            console.log(MSG);
            try {
                const ret = await fcl.currentUser().signUserMessage(MSG);
                console.log("sig:", ret);
            } catch (error) {
                console.log("error");
                console.log(error);
            }
        });
    });
});
