This project contains examples of the right way to test Cascalog workflows using [midje-cascalog](https://github.com/sritchie/midje-cascalog). The tests and code mirror the discussion at [this blog post](http://sritchie.github.com/2011/09/30/testing-cascalog-with-midje.html).

## Excerpt

I've been working on a Cascalog testing suite these past few weeks, an extension to Brian Marick's [Midje](https://github.com/marick/Midje), that eases much of the pain of testing MapReduce workflows. I think a lot of the dull work we see in the Hadoop community is a direct result of fear. Without proper tests, Hadoop developers can't help but be scared of making changes to production code. When creativity might bring down a workflow, it's easiest to get it working once and leave it alone.

The antidote to all of this fear is a functional testing suite. As I discussed in [Getting Creative with MapReduce](http://sritchie.github.com/2011/09/29/getting-creative-with-mapreduce.html), Hadoop workflows are difficult to test at all; testing application logic in isolation of data storage is impossible.

Cascalog is free of this weakness. [midje-cascalog](https://github.com/sritchie/midje-cascalog) allows you to test Cascalog queries as pure functions, both in isolation and as components of more complicated workflows. the resulting tests are truly beautiful.
