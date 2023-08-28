# Multi-track UI

...like in a DAW, where all messages are represented along a single timeline along a bunch of tracks, where each track
represents what happens in a particular context.

1. Multi-track UI
   Because everything interesting that happens in the system is a narrative unfolding in different contexts at the
   same time, it would be nice to not just visualize it as such, but perhaps also to construct it as such. A context
   would occupy a track, the _main character_ of any narrative would be something like an aggregate (or any other
   type of actor), it would be shown in the track of its "home context" that is the command context in which it is
   defined as an aggregate. The look is that of a tidy event storming session.

2. Automated component creation

The number of building blocks Axon offers is small, and they are quite standard: aggregates, policies, domain
events, commands, queries... It would be nice to generate them based on the event timeline.
Perhaps, having defined a multi-track representation of a specific narrative (what Eric calls "reference scenario"),
we could use some code generation mechanism (like an LLM) to create the underlying code.

Then editing of the narrative becomes purely graphical interaction in the semantic space of the given track context,
and the "downmix" is simply a playback of the narrative. I want the ability to zoom in/out, mute a track or
solo it, and just watch it play. The watching part will reveal certain regular patterns, given they consist the "phrases"
I defined.

Perhaps there is a place for the loosely defined behavior in such system as well: for example, in some scenarios we
might not care exactly how a specific part of the story is told, as long as it fits within certain guidelines. So
perhaps we can use LLM agents as a part of the narrative without having to code them explicitly. Purely natural language
policy. Perhaps we can define our policies explicitly, but still in natural language, and then have the "moral code" to
cover those rare cases where none of the explicit policies apply. 
