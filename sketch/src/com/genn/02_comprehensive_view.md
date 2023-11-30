# RPC

It makes remote call appear like native call by abstracting some details,  which makes us don't find that some API invocations trigger an HTTP request(or other high-level protocol over TCP/IP). Actually, RPC doesn't present a certain tech stack or third-party component, rather, it presents a technical thought, so there is no necessity to discuss the differences between RPC and HTTP.

RPC is transparent across different processes or business-systems, but it also raises many questions which could be divided to 2 categories:

1. HOW TO HANDLE THE EXCEPTION WHILE PRODUCER PROCESS THE BUSINESS FLOW.
2. HOW TO HANDLE THE EXCEPTION OF NETWORK.

As a consumer, we need to consider about above questions and deal with them in some suitable ways, **which usually invades the content of business-code**. 

A mature RPC component should focus on some features like `message compression ratio`, `message compress performance`, `object-oriented invocation`. There are various and mature RPC components like OpenFeign, Dubbo, gRPC, etc. Some of them are confined to specific language, while the others support across heterogeneous language. some of them are based on HTTP, while the others base on the other protocol over TCP/IP.

# Global Transaction

Distributed Transaction like 2PC and 3PC has a major drawback which it thinks all results are consistent only when all nodes do commit their native transaction.

Traditional Distributed Transaction needs a `coordinator` to complete the whole process. one of the most famous ones is Seata, which include four mode like XA(2PC), AT(rely on the usage of application-level for global lock), TCC(completing transaction over coordinator schedules code, it also invade code deeply), SAGA(is off the beat).

There also be an untraditional coordinator like MQ , it can make whole process of transaction asynchronous base on soft-state and eventual consistency. **But it needs the support of soft-state from requirement and design like A, A-ing(soft-state), B instead of A, B.** besides, the eventual consistency of this kind of global transaction depends on the availability of MQ.

It is same as RPC, Global Transaction also has availability question as request pass through different processes and it is unavoidable, so it is important for mature coordinator to have some extra features like `failure monitoring`, `failure retry`, `failure alarm` so that we could fix it easily to achieve eventual consistency  as a developer, **not just give it to coordinator and let it go unchecked**.