# bankservice
Bank Service Technical Exercise

Spring Boot application that creates a series of transactions (debit/credit) and applies them to a balance.

Credit and debit transactions are created on separate threads and published to a shared queue.

Each thread is intended to publish, on average, 25 transactions per second in a defined range.

TransactionConsumer polls this queue and processes transactions in turn. 

Each transaction is audited when it is processed.  Audits are batched before pushing to downstream audit mechanism.

REST API <host:port>/ - gives the current balance.

BalanceApplication is the entry point.




