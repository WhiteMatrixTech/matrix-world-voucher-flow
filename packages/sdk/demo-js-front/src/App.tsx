import React from "react";
import logo from "./logo.svg";
import "./App.css";
import { fcl, FclVoucherClient } from "matrix-world-voucher-flow-js-sdk/dist";

function App() {
  const check = async () => {
    await fcl
      .config()
      // Point App at Emulator
      .put("accessNode.api", "http://localhost:8080")
      // Point FCL at dev-wallet (default port)
      .put("discovery.wallet", "http://localhost:8701/fcl/authn")
      .put("0xFUNGIBLE_TOKEN_ADDRESS", "0xee82856bf20e2aa6")
      .put("0xFUSD_ADDRESS", "0xf8d6e0586b0a20c7");
    await fcl.logIn();
    await fcl.authenticate();
  };
  check();

  const transferFUSD = async () => {
    // transferFUSD
    const client = new FclVoucherClient();
    const ret = await client.transferFUSD("0x01cf0e2f2f715450", "10.0");
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
