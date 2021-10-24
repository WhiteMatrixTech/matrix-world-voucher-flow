import {FlowEventFetcher} from "./EventFetcher";
import {pino} from "pino";

export enum FlowEnv {
    localEmulator,
    flowTestnet,
    flowMainnet
}

export interface FlowTokenDepositEvent {
    blockId: number;
    transactionId: string;
    eventId: string;
    index: number;
    to: string;
    amount: number;
}

export interface VoucherClient {
    queryFlowTokenDepositedEvent(
        fromBlockHeight: number,
        toBlockHeight: number,
        recipient?: string
    ): Promise<FlowTokenDepositEvent[]>;
}

export class FlowVoucherClient implements VoucherClient {
    private eventFetcher: FlowEventFetcher;
    private flowTokenAddress: string | undefined;
    private logger: pino.Logger;

    constructor(eventFetcher: FlowEventFetcher, env: FlowEnv) {
        this.eventFetcher = eventFetcher;
        this.logger = pino();
        switch (env) {
            case FlowEnv.flowTestnet: {
                this.flowTokenAddress = "0x7e60df042a9c0868";
                break;
            }

            case FlowEnv.flowMainnet: {
                this.flowTokenAddress = "0x1654653399040a61";
                break;
            }

            case FlowEnv.localEmulator:
            default:
                this.flowTokenAddress = "0x0ae53cb6e3f42a79";
        }
    }

    public async queryFlowTokenDepositedEvent(
        fromBlockHeight: number,
        toBlockHeight: number,
        recipient?: string
    ): Promise<FlowTokenDepositEvent[]> {
        try {
            // query event all deposited and withdrawn events
            this.logger.info(`Query FLOW depositedEvents from ${fromBlockHeight} to ${toBlockHeight} START`);
            let depositedEvents = await this.eventFetcher.queryEventByBlockRange(
                `A.${this.flowTokenAddress}.FlowToken.TokensDeposited`,
                fromBlockHeight,
                toBlockHeight
            );
            this.logger.info(
                `Query FLOW depositedEvents from ${fromBlockHeight} to ${toBlockHeight} DONE with ${depositedEvents.length} events`
            );

            // filter events by recipient address if defined
            if (recipient !== undefined) {
                this.logger.info(
                    `Query FLOW depositedEvents from ${fromBlockHeight} to ${toBlockHeight} filter for ${recipient}`
                );
                depositedEvents = depositedEvents.filter(
                    event => event.values.filter(value => value.name === "to" && value.value === recipient).length > 0
                );
                this.logger.info(`After filtering got ${depositedEvents.length} events`);
            }

            // transform events
            return depositedEvents.map(event => ({
                ...event,
                amount: parseFloat(event.values[0].value),
                to: event.values[1].value
            }));
        } catch (error) {
            console.error(error);
            return Promise.reject("Something is wrong with this transaction");
        }
    }
}
