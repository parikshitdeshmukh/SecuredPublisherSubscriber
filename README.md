
# **Secured Publisher-Subscriber System**

* It is an **asynchronous messaging model** where Subscribers can subscribe to
their Topics of interest. Publishers can publish data for multiple topics; and,
subscribers will receive the data for Topics they have subscribed to.
* Broker is like a medium that facilitates the flow of the data with efficient
speed and takes care of heavy operations like 
...*handling multiple transactions
..*connecting with database
..*handling node failure
..*persisting snapshot of subscribers
..100% message delivery on large scale

* Publishers and Subscribers have no knowledge of each other and are loosely
coupled.
* Use case: Sensor network, Micro-services, Event notification



