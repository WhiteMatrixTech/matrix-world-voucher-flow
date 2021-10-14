import React from "react";
import logo from "./logo.svg";
import "./App.css";
import { fcl, FclVoucherClient, FLowEnv } from "matrix-world-voucher-flow-js-sdk/dist";

function App() {
    const client = new FclVoucherClient();
  const check = async () => {
    await client.setupGlobalFcl(FLowEnv.localEmulator);
    await fcl.logIn();
    await fcl.authenticate();
  };
  check();

  const transferFUSD = async () => {
    // transferFUSD
    let ret;
    ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    console.log(ret);

    ret = await client.transferFUSD("0x01cf0e2f2f715450", "10.0");
    console.log(ret);

    ret = await client.FUSDBalance("0x01cf0e2f2f715450");
    console.log(ret);

  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.tsx</code> and save to reload.
        </p>
        <button
          onClick={() => transferFUSD()}
          className="App-link"
        >
          Learn React
        </button>
      </header>
    </div>
  );
}

export default App;
