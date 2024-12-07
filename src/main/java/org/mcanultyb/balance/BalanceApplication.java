package org.mcanultyb.balance;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.mcanultyb.balance.audit.BatchAuditConsole;
import org.mcanultyb.balance.audit.BatchingAuditImpl;
import org.mcanultyb.balance.model.BalanceTracker;
import org.mcanultyb.balance.model.ConsumeOnlyQueue;
import org.mcanultyb.balance.model.PublishOnlyQueue;
import org.mcanultyb.balance.model.Transaction;
import org.mcanultyb.balance.model.TransactionType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
/**
 * The main SpringBoot entry point
 */
public class BalanceApplication {
	private static final int MIN_TRANSACTION_VALUE = 20000; //£200.00
  private static final int MAX_TRANSACTION_VALUE = 50000000; //£500,000.00

	private static final BalanceTracker tracker = new BalanceTracker(new BatchingAuditImpl(new BatchAuditConsole(), 1000, 100000000));

	private static TimerTask createTimerTask(final TransactionType type, final Random rand, final PublishOnlyQueue queue) {
		return new TransactionGeneratorTask(type, rand, queue, MIN_TRANSACTION_VALUE, MAX_TRANSACTION_VALUE);
	}

	public static void main(final String[] args) {
		SpringApplication.run(BalanceApplication.class, args);
		final BlockingQueue<Transaction> queue = new ArrayBlockingQueue(1000);
		final Random rand = new Random();

		//wrap the queue to ensure publishers can't consume and vice versa
		final PublishOnlyQueue publishOnlyQueue = new PublishOnlyQueue(queue);
		final ConsumeOnlyQueue consumeOnlyQueue = new ConsumeOnlyQueue(queue);

		//requirement is for these to run in separate threads so 1 timer each, 25/s = interval of 40ms
		new Timer().scheduleAtFixedRate(createTimerTask(TransactionType.DEBIT,rand,publishOnlyQueue),0,40);
		new Timer().scheduleAtFixedRate(createTimerTask(TransactionType.CREDIT,rand,publishOnlyQueue),0,40);

		final Thread consumerThread = new Thread(new TransactionConsumer(consumeOnlyQueue, tracker));

		consumerThread.start();

//		TransactionGenerator is 'self throttling' so manages the 25 per sec better/more dynamically than TimerTask impl
//		final Thread debitThread = new Thread(new TransactionGenerator(TransactionType.DEBIT, rand, queue));
//		final Thread creditThread = new Thread(new TransactionGenerator(TransactionType.CREDIT, rand, queue));
//		debitThread.start();
//		creditThread.start();
	}

	@Bean
	public static BalanceTracker getBalanceTracker() {
		return tracker;
	}

}
