## Q: How to test an aggregate command handler with a non-injected dependency?

The snippet below instantiates what appears to be a domain service in the Order aggregate constructor:

```
constructor(itemIds: List<UUID>, owner: String): this() {
    val productValidation = ProductValidation()
    val orderItems = mutableListOf<OrderItem>()
    for (itemId in itemIds) {
        val info = productValidation.forProduct(itemId)
            ?: throw IllegalStateException("No product validation available")
        check(info.forSale) { "Product ${info.name} is no longer on sale" }
        orderItems.add(OrderItem(itemId, info.name, info.price))
    }
    // other stuff
}
```
`ProductValidation` is responsible for 
- learning about product changes in the product catalog
- answering the question "is this product still for sale?"

Given it is not injected, is there a straightforward strategy to test Order?
The only option I can think of is injection, by doing this: 
   - declare it as an constructor argument to Order constructor 
   - create a parameter resolver for it:
```kotlin
class ProductValidationResolverFactory : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable?,
        parameters: Array<out Parameter>?,
        parameterIndex: Int
    ): ParameterResolver<*> = productValidationResolver

    private val productValidationResolver = object : ParameterResolver<ProductValidation> {
        override fun resolveParameterValue(message: Message<*>): ProductValidation = ProductValidation()
        override fun matches(message: Message<*>): Boolean = true
    }
}
```
   - register it in the Axon configuration in SCSCApp:
```kotlin
withCustomParameterResolverFactories(
            listOf(
                UuidGenParameterResolverFactory(),
                ProductValidationResolverFactory()
            )
        )
```
   Concerns: 
   - since Cart creates the order directly, not via a command (separate concern), it becomes a dependency of Cart, which doesn't care about it and is in a different context
   - it upsets the architecture test: 
```
     Architecture Violation [Priority: MEDIUM] - Rule 'classes that reside in a package 'demo.scsc.commandside..' should only be accessed by any package ['demo.scsc.commandside..', 'demo.scsc']' was violated (1 times):
     Method <demo.scsc.config.resolver.ProductValidationResolverFactory$productValidationResolver$1.resolveParameterValue(org.axonframework.messaging.Message)> calls constructor <demo.scsc.commandside.order.ProductValidation.<init>()> in (ProductValidationResolverFactory.kt:18)
```

Also, design questions about `ProductValidation`: how do we characterize it in DDD terms?
- `fun forProduct(id: UUID): ProductValidationInfo?` makes it look like a domain service from the Order point of view
- `@EventHandler`-annotated method makes it look like a projection, creating a representation of Product from the inventory context interesting in the Order context

Or should I disregard this question and just accept it as a pragmatic solution?
