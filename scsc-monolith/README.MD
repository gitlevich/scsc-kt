# What this is

This is an attempt to implement a CQRS/ES project backed by [Axon Framework](https://www.axoniq.io) in the Kotlin ecosystem. 

It is based on [Axon Demo Project](https://github.com/azzazzel/scsc) by [Milen Dyankov](https://github.com/azzazzel).

You can find the reference scenarios (WiP) [here](docs/ReferenceScenarios.md).

The goal of this project is to develop a small Axon application demonstrating various interesting features of Axon Framework
in what I think is idiomatic Kotlin. It is motivated by the many questions I've had while developing such applications 
over the last few years.

I don't have answers to many of these questions, and hope this project will help me find them. 

At the moment, this is a verbatim port of the Java implementation. 
Because I _replaced_ the original Java files with their Kotlin equivalents, the commit history,
and thus their attribution to [Milen Dyankov](https://github.com/azzazzel) is lost, so I want to 
reiterate his authorship of the code here.

Next steps:
- add tests for all the reference scenarios
- refactor it to a more idiomatic Kotlin
- see how query side looks with Kotlin frameworks like Ktor, Exposed, etc. 
- see how it all looks with coroutines
- ???
