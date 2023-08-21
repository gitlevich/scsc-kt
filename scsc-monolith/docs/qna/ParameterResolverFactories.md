### How do I register a custom parameter resolver factory?
I started writing some tests, and ran into a problem: can’t figure out how to register a parameter resolver. I need this 
for a test to control UUIDs generated in a saga event handler: wanted to inject a predictable implementation of `() -> UUID` 
to make assertions about commands.

#### Answer (by Steven van Beelen):

Concerning the parameter resolvers, I think the predicament is straightforward once you know the internals.

Axon Framework's components using the ParameterResolvers expect a single `ParameterResolverFactory` to be registered with them.
If you check the `defaultParameterResolverFactory` method in the [DefaultConfigurer](https://github.com/AxonFramework/AxonFramework/blob/4d26ee351455d09fce7c871acd50d4964cd1365d/config/src/main/java/org/axonframework/config/DefaultConfigurer.java#L383), 
you see we do the following:
```
protected ParameterResolverFactory defaultParameterResolverFactory(Configuration config) {
    return defaultComponent(ParameterResolverFactory.class, config)
        .orElseGet(() -> MultiParameterResolverFactory.ordered(ClasspathParameterResolverFactory.forClass(getClass()),
            new ConfigurationParameterResolverFactory(config)));
}
```
You see we set a singular `ParameterResolverFactory` of type `MultiParameterResolverFactory`.

The `ConfigurationParameterResolverFactory` ensures all registered components are resolvable parameters.

The `ClasspathParameterResolverFactory` itself also creates a `MultiParameterResolverFactory`, containing all the [ParameterResolverFactory](https://github.com/AxonFramework/AxonFramework/blob/4d26ee351455d09fce7c871acd50d4964cd1365d/config/src/main/java/org/axonframework/config/DefaultConfigurer.java) 
instances it can find through the Service Loader mechanism.

In my mind, you have roughly two ways forward:
- Ensure you set a MultiParameterResolverFactory with all the ParameterResolverFactories from your list. When doing so, please add ClasspathParameterResolverFactory and ConfigurationParameterResolverFactory too to make AF's basic functionality work too.
- Add your own Parameter Resolvers as loaded services. To that end, you can add a org.axonframework.messaging.annotation.ParameterResolverFactory file to `../resources/META-INF/services` containing the FQCN of your own parameter resolvers.
