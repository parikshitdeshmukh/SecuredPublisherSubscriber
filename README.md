
# **Secured Publisher-Subscriber System**

* It is an **asynchronous messaging model** where Subscribers can subscribe to
their Topics of interest. Publishers can publish data for multiple topics; and,
subscribers will receive the data for Topics they have subscribed to.
* Broker is like a medium that facilitates the flow of the data with efficient
speed and takes care of heavy operations like:
  * Handling multiple transactions
  * Connecting with database
  * Handling node failure
  * Persisting snapshot of subscribers
  * 100% message delivery on large scale
  * Handling, verifying and storing public keys of clients

* Publishers and Subscribers have no knowledge of each other and are loosely
coupled.
* Use case: Sensor network, Micro-services, Event notification

![alt text](https://github.com/parikshitdeshmukh/SecuredPublisherSubscriber/blob/master/Documentation/pubsub.jpg)

### Performance Metrics


| Criteria        | Outcome           |
| ------------- |:-------------:|
| Functional testing     | 100% working |
| Message delivery small scale network | 100%      |
| Message delivery Large scale network | 100%      |
| Average Latency | 143.28 ms     |
| Worse case latency | 490 ms |
| Fault tolerant/ Node failure | YES |

