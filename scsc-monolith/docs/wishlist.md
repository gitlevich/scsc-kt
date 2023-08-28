# Simple AF tools I want

**Replay Events from store to store**

As a developer, 
I would like to replay events from the store in one environment to the store in another
So that I could test my code in a production-like environment.

**Replay Events from store to store up to a certain time point**

As a developer,
I would like to replay events from the store in one environment to the store in another up to a certain time point
So that I could reproduce a scenario

**Record a scenario**

By a scenario, I mean the given-when-then type of sequence. 

As a developer,
I would like to be able to record various interesting scenarios as that allows me to:
- capture a narrative the events are telling
- use these scenarios for tests instead of hand-coding events myself
- visualize these scenarios

For testing purpose, this tool is about capturing just the events (given).
For visualization and narrative purpose, it's about capturing all messages, policies, etc. and rendering that
  as a narrative across different contexts (kind of a reverse-engineered event storming session, created complete
  from the messages, aggregates, policies, for example to inspect it graphically for compliance with what was
  originally envisioned).
For analysis purpose, only the events are interesting, but per actor and over time, to capture behavioral patterns
  of the actors (an actor could represent a human user, and could be something else, modeled as an aggregate). In this
  context, I equate Aggregates, Actors and Agents: something that needs to make a decision based on environmental 
  stimuli, and that has a persistent identity over time. 

**Replay a scenario**

For example, to visualize it, or to generate a test from the point to which th events brought the state of a given
aggregate, automatically. 

**A simple way to migrate from one representation of events to another**

Probably not as often used, as in probably once in a project lifetime if initial event representation no longer serves, 
like XML to JSON. 


**Visualization tool**

Essentially, when looking at event stream, we have multiple timelines that happen in parallel. Visualization 
can be multi-lane, with each lane representing a context. For example, as a user moves through the system, 
its different aspects are interesting in different contexts, like when a user creates an account: 
- in security context, the narrative is about how we now linked the user to some external auth system, or granted them
    certain privileges.
- in the Account context, we care about user PII, like contact information or how several users share an account, 
- in Payment context, we are interested in payment methods, etc.
- etc. 

This would be just so useful to look at when thinking about a particular aspect of the system. 
