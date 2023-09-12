# What this is

This is an attempt to implement a CQRS/ES project backed by [Axon Framework](https://www.axoniq.io) in the Kotlin ecosystem. 

It is based on [Axon Demo Project](https://github.com/azzazzel/scsc) by [Milen Dyankov](https://github.com/azzazzel).

You can find the reference scenarios (WiP) [here](docs/ReferenceScenarios.md).

The goal of this project is to develop a small Axon application demonstrating various interesting features of Axon Framework
in what I think is idiomatic Kotlin. It is motivated by the many questions I've had while developing such applications 
over the last few years.

I don't have answers to many of these questions, and hope this project will help me find them. 

At the moment, this is a verbatim port of SCSC monolith Java implementation.

Because I _replaced_ the original Java files with their Kotlin equivalents, the commit history,
and thus their attribution to [Milen Dyankov](https://github.com/azzazzel) is lost, so I want to 
reiterate his authorship of the code here.

Next steps:
- add tests to understand what works, as well to support refactoring
- refactor it to a more idiomatic Kotlin (original "translation" was done by IntelliJ)
- see how it looks with Kotlin frameworks like Ktor, maybe Exposed, etc. 
- expose commands via HTTP endpoints (I can see the UI Vue client attempts to connect to 8080, but I don't see the app listening...)
- expose queries via HTTP endpoints (consider streaming, e.g., Kotlin flows mated to Axon streaming queries)
- try the plain Kotlin coroutines option end to end: should both look and work nicely, and not scare people who dislike functional style.
- draw context map with respective aggregate roots, etc. 
- ???

### [Reference scenarios](docs/ReferenceScenarios.md)

### [Context map](docs/ContextMap.md)

### [Implementation details](docs/ImplementationDetails.md)


