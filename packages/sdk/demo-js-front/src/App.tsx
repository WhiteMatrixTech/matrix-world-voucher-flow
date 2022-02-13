import React from "react";
import logo from "./logo.svg";
import "./App.css";
import {
  fcl,
  FclVoucherClient,
  FlowEnv,
} from "@white-matrix/matrix-world-voucher-flow-js-sdk/dist";

function App() {
  const client = new FclVoucherClient();
  const check = async () => {
    // await client.setupGlobalFcl(FlowEnv.localEmulator);
    // await client.setupGlobalFcl(FlowEnv.flowTestnet);
    await client.setupGlobalFcl(FlowEnv.flowMainnet);
    await fcl.logIn();
    await fcl.authenticate();
  };
  check();

  const transfer = async () => {
    let ret;
    const user = await fcl.currentUser().snapshot();
    console.log(user)

    // check assets (mainnet)
    ret = await client.getAssets("0xf20df769e658c257");
    console.log(ret);

    // ret = await client.checkAssetsCollection(user.addr);
    // console.log(ret);

    // ret = await client.initAssetsCollection();
    // console.log(ret);

    // ret = await client.checkVoucherCollection(user.addr);
    // console.log(ret);
    //
    // ret = await client.initVoucherCollection();
    // console.log(ret);

    // ret = await client.transferVoucher("0x01cf0e2f2f715450", 0);
    // console.log(ret);

    // transferFUSD
    // console.log("FUSD test");
    // ret = await client.FUSDBalance(user.addr);
    // console.log(ret);
    //
    // ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    // console.log(ret);
    //
    // ret = await client.transferFUSD("0x01cf0e2f2f715450", "10.0");
    // console.log(ret);
    //
    // ret = await client.FUSDBalance(user.addr);
    // console.log(ret);
    //
    // ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    // console.log(ret);

    // console.log("FLOW test");
    // ret = await client.FLOWBalance(user.addr);
    // console.log(ret);
    //
    // console.log("check capacity");
    // try {
    //     await client.checkCapacity(user.addr, ret, 1000, 50);
    // } catch (error) {
    //     console.log(error);
    //     // no error
    // }
    //
    // try {
    //     await client.checkCapacity(user.addr, ret, ret-0.0009, 50);
    // } catch (error) {
    //     console.log(error);
    //     // Please may sure you have > 0.001 FLOW balance after payment
    // }
    //
    // try {
    //     await client.checkCapacity(user.addr, ret, ret-0.002, 50000);
    // } catch (error) {
    //     console.log(error);
    //     // Please reserve more FLOW in your wallet, it seems like will run out of storage and likely cause a failed mint
    // }

    // ret = await client.FLOWBalance("0x01cf0e2f2f715450");
    // console.log(ret);
    //
    // ret = await client.transferFLOW("0x01cf0e2f2f715450", "11.1");
    // console.log(ret);
    //
    // ret = await client.FLOWBalance(user.addr);
    // console.log(ret);
    // ret = await client.FLOWBalance("0x01cf0e2f2f715450");
    // console.log(ret);
  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.tsx</code> and save to reload.
        </p>
        <button onClick={() => transfer()} className="App-link">
          Learn React
        </button>
      </header>
    </div>
  );
}

export default App;
