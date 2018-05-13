# Example project reactive programming 
This project contains 2 examples that are similar to what we do in influence factors and value Based

## Resources

#### Initial context specification with examples:

https://spring.io/blog/2016/06/07/notes-on-reactive-programming-part-i-the-reactive-landscape

https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code

https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application

https://www.infoq.com/articles/reactor-by-example

http://www.baeldung.com/reactor-core

#### Best detailed documentation by project reactor:

http://projectreactor.io/docs/core/release/reference/

#### How to combine multiple fluxes:

http://javasampleapproach.com/reactive-programming/reactor/reactor-how-to-combine-flux-mono-reactive-programming


## Lessons learned

### Using an emitter

Very difficult to get it working correclty as it is both a subscriber and a publisher. 
Either the messages came in but not out or nothing happened.

### Using the stepVerifier

Do not forget to append verify() at the end otherwise nothing will be checked.

### Debugging issues

- Adding log() methods at multiple places in your chain gives you a good information about what messages were seen where
- Debugging works well, do not forget that it is pull based so you will see message by message passing through your chain.

### Handle method on flux

- Only allows to call next once for each method invocation. Otherwise the famous can only be called at most once is thrown.

### often made mistakes

- forgetting to specify verify in test
- forgetting to subscribe an intermediate flux to the original one which results in waiting forever for results.
- Operators on a flux are decorators and thus return a different instance. Either chain them or (re)-assign to variable when adding a subsequent operation. Correct usage below:

`Flux<String> flux = Flux.just("foo", "chain");
 flux = flux.map(secret -> secret.replaceAll(".", "*"));
 flux.subscribe(next -> System.out.println("Received: " + next));`

### Adding threading behavior

- use publishOn(Scheduler) and all operators below will have a different thread affinity

### How to do reactive

- Start with a simple flow and add operators in between while testing the full flow. This makes it simple to determine where the failure is.

