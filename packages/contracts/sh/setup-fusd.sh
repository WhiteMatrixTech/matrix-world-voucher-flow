#!/bin/bash

flow transactions send ./cadence/transactions/emulator/CreateFUSDMinter.cdc || true

flow transactions send ./cadence/transactions/emulator/CreateFUSDVault.cdc --signer local-admin-account || true

flow transactions send ./cadence/transactions/emulator/CreateFUSDVault.cdc || true

flow transactions send ./cadence/transactions/emulator/MintFUSD.cdc f8d6e0586b0a20c7 100.0 || true

flow transactions send ./cadence/transactions/emulator/MintFUSD.cdc 01cf0e2f2f715450 100.0 || true



