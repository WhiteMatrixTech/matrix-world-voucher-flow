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
    await client.setupGlobalFcl(FlowEnv.localEmulator);
    await fcl.logIn();
    await fcl.authenticate();
  };
  check();

  const transfer = async () => {
    // transferFUSD
    console.log("FUSD test");
    let ret;
    ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    console.log(ret);

    ret = await client.transferFUSD("0x01cf0e2f2f715450", "10.0");
    console.log(ret);

    ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    console.log(ret);

    console.log("FLOW test");
    ret = await client.FLOWBalance("0x01cf0e2f2f715450");
    console.log(ret);

    ret = await client.transferFLOW("0x01cf0e2f2f715450", "11.1");
    console.log(ret);

    ret = await client.FLOWBalance("0x01cf0e2f2f715450");
    console.log(ret);
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
