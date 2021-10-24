import request from "umi-request";

export interface FlowEvent {
    blockId: number;
    eventId: string;
    index: number;
    transactionId: string;
    type: string;
    values: {name: string; value: string}[];
}

export interface EventFetcher {
    queryEventByBlockRange(from: number, to: number): FlowEvent[];
}

export class FlowEventFetcher {
    private providerEndpoint: string;

    constructor(providerEndpoint: string) {
        this.providerEndpoint = providerEndpoint;
    }

    public async queryEventByBlockRange(event: string, from: number, to: number): Promise<FlowEvent[]> {
        return await request.post(this.providerEndpoint + "/queryEventByBlockRange", {
            method: "post",
            headers: {"Content-Type": "application/json"},
            data: {
                event: event,
                start: from,
                end: to
            }
        });
    }
}
