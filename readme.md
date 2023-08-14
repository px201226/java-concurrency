

## synchronized 락의 단점
- 락이 걸린 객체에서 일어나는 동기화 작업은 모두 균등하게 취급된다.
- 락 획득/해제는 반드시 메서드 수준이나 메서드 내부의 동기화 블록 안에서 이루어져야 한다.
- 락을 얻지 못한 스레드는 블로킹된다. 락을 얻지 못할 경우, 락을 얻어 처리를 계속하려고 시도하는 것조차 불가능하다.

락이 걸린 데이터에 모든 연산이 동등하게 취급된다는 의미는 읽기/쓰기 작업에 동일한 레벨의 락의 필요하다는 의미이다.

## Java 동시성 라이브러리
java.util.concurrent 패키지에는 멀티스레드 애플리케이션을 더 쉽게 개발할 수 있게 설계된 자바의 표준 라이브러리이다. 이 라이브러리에서 제공하는 기능은 크게 다음과 같다.
- 락, 세마포어
- atomics
- Blocking Queue
- latch
- Executor


### java.util.concurrent.locks.Lock

```JAVA
public interface Lock {


    /**
    * 기존 방식대로 락을 획득하고 락을 사용할 수 있을 때까지 블록킹한다
    **/
    void lock();

    /**
    * 대기중인 스레드에 인터럽트할 수 있게 해준다. interrupt() 가 호출되면 대기 상태가 * 인터럽트되며 InterruptedException 이 던져진다.
    **/
    void lockInterruptibly() throws InterruptedException;


    /**
    * 호출 시점에 잠금이 해제된 경우에만 잠금을 획득한다.
    **/
    boolean tryLock();

    
    /**
    * 락을 획득하려고 시도한다. 타임아웃이 설정이 가능하다.
    **/
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;


    /**
    * 락을 해제한다. lock()에 대응되는 후속 호출이다.
    **/
    void unlock();


    /**
    * 락 주위에 조건을 설정해 유연하게 락을 활용할 수 있다. (읽기와 쓰기를 분리할 수 있다)
    **/  
    Condition newCondition();
}

```

ReentrantLock 클래스는 Lock의 주요 구현체로, 내부적으로 int 값으로 compareAndSwap()을 한다.
(AtomicInteger 클래스의 Unsafe 클래스 참조)

### 읽기/쓰기 락
기존 synchronized 나 ReentrantLock 을 이용하면 한 가지 락 정책을 따를 수 밖에 없다.
한 읽기 스레드 때문에 나머지 읽기 스레드를 블로킹하여 처리율이 낮아질 수 있다.
이럴 때 ReentrantReadWriteLock 클래스의 ReadLock, WriteLock을 활용하면 여러 스레드가
읽기 작업을 하는 도중에도 다른 읽기 스레드를 블로킹하지 않게 할 수 있다. 블로킹은 쓰기 작업을 할 때만 발생한다.
ReentrantReadWriteLock의 생성자로 boolean fair 라는 파라미터를 받는데 기본값은 false, 불공정(non-fair) 모드이다.
'불공정’이란 말 자체의 의미처럼 락을 획득한 스레드 하나가 다른 여러 스레드가 고갈되건 말건 상관하지 않습니다. 반대로, '공정’ 모드에서는 어느 정도 스레드 간 공정성이 보장되도록 FIFO에 가까운 방식으로 락을 획득할 수 있게 함으로써 각 스레드가 대기하는 시간을 최대한 균등하게 분배한다.

```JAVA

@Slf4j
public class StockInventory {

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private final Map<String, Integer> stockRepository = new HashMap<>();


    public Integer getStock(String item) {
        readLock.lock();
        log.info("readLock lock");
        try {
            Thread.sleep(2000);
            return stockRepository.getOrDefault(item, 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
            log.info("readLock unLock");
        }
    }

    public void increaseStock(String item) {
        writeLock.lock();
        log.info("writeLock lock");
        try {
            Thread.sleep(2000);
            stockRepository.put(item, stockRepository.getOrDefault(item, 0) + 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
            log.info("writeLock unlock");
        }
    }

}

```

### ReadLock vs ReadLock

```JAVA
    @Test
    void readLock() throws InterruptedException {
        final StockInventory stockInventory = new StockInventory();

        new Thread(
                () -> {
                    final var qty = stockInventory.getStock("aaa");
                    System.out.println(qty);
                }
        ).start();

        new Thread(
                () -> {
                    final var qty = stockInventory.getStock("aaa");
                    System.out.println(qty);
                }
        ).start();

        Thread.sleep(5000L);
    }
```
```console
15:40:09.316 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock lock
15:40:09.316 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- readLock lock
15:40:11.321 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock unLock
0
15:40:11.324 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- readLock unLock
0

```
읽기 vs 읽기 시, 락 경합이 발생하지 않는다.


### WriteLock vs WriteLock

```JAVA
    @Test
    void writeLock() {
        final StockInventory stockInventory = new StockInventory();

        new Thread(
                () -> {
                    stockInventory.increaseStock("aaa");
                }
        ).start();

        new Thread(
                () -> {
                    stockInventory.increaseStock("aaa");
                }
        ).start();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
```

```console
15:42:00.459 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock lock
15:42:02.467 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock lock
15:42:02.467 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock unlock
15:42:04.471 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock unlock
```
먼저 writeLock을 점유한 스레드가 unlock 할 때까지, 뒤에 writeLock 쓰레드가 대기한다.


### WriteLock vs ReadLock
```JAVA
    @Test
    void writeLockVsReadLock() throws InterruptedException {
        final StockInventory stockInventory = new StockInventory();

        new Thread(
                () -> {
                    stockInventory.increaseStock("aaa");
                }
        ).start();

        new Thread(
                () -> {
                    final var qty = stockInventory.getStock("aaa");
                    System.out.println(qty);
                }
        ).start();

        Thread.sleep(3000L);
    }
```

```Console
15:44:10.126 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock lock
15:44:12.133 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- readLock lock
15:44:12.133 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock unlock
15:44:14.144 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- readLock unLock
1
```
writeLock을 점유한 스레드가 unlock 할 때 까지, readLock이 대기한다.


### ReadWrite vs WriteLock
```JAVA
15:46:12.891 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock lock
15:46:14.899 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock lock
15:46:14.899 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock unLock
0
15:46:16.904 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock unlock
```

```Console
15:46:56.315 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock lock
15:46:58.323 [Thread-0] INFO com.example.javaconcurrency.lib.StockInventory -- readLock unLock
0
15:46:58.325 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock lock
15:47:00.330 [Thread-1] INFO com.example.javaconcurrency.lib.StockInventory -- writeLock unlock
```
readLock 점유한 스레드가 unlock 할 때 까지, writeLock 대기한다.



## 세마포어
세마포어는 풀 스레드나 DB 접속 객체 등 여러 리소스를 최대 X개 객체까지만 액세스를 허용한다는 전제하에 정해진 수량의 퍼밋으로 액세스를 제어한다.
Semaphore 클래스의 acquire() 메서드는 사용 가능한 퍼밋 수를 하나씩 줄이는데, 더 이상 쓸 수 있는 퍼밋이 없을 경우 블로킹된다.
release() 메서드는 퍼밋을 반납하고 대기 중인 스레드 중에서 하나에게 해제한 퍼밋을 전달한다.
세마포어는 리소스를 기다리는 스레드가 리소스를 점유하지 못하는 기아상태가 될 가능성이 커서 공정모드로 초기화하는 경우가 많다.
퍼밋이 하나뿐인 세마포어는 뮤텍스와 동등하다. 그러나 뮤텍스는 뮤텍스가 걸린 스레드만 해제할 수 있는 반면, 세마포어는 비소유 스레드도 락을 해제할 수 있다는 점이 다르다.

### Connection Pool 예제
```JAVA
@Slf4j
public class ConnectionPool {

    private final Semaphore semaphore;
    private final Queue<String> connectionPool;

    public ConnectionPool(int poolSize) {
        this.semaphore = new Semaphore(poolSize, true);
        this.connectionPool = new LinkedList<>();
        for (int i = 0; i < poolSize; i++) {
            connectionPool.add("pool" + i);
        }
    }

    public String acquireConnection() throws InterruptedException {
        semaphore.acquire();
        log.info("acquire connection");
        return connectionPool.poll();
    }

    public void releaseConnection(String connection) {
        log.info("release connection");
        connectionPool.offer(connection);
        semaphore.release();
    }

}
```

```JAVA
@Slf4j
class ConnectionPoolTest {

    @Test
    void semaphoreTest() throws InterruptedException {
        final var connectionPool = new ConnectionPool(2);

        final var executorService = Executors.newFixedThreadPool(4);

        for (int i=0; i<4; i++){
            executorService.execute(() -> {
                try {
                    final var connection = connectionPool.acquireConnection();
                    log.info("connection acquired = {}", connection);
                    Thread.sleep(2000L);
                    connectionPool.releaseConnection(connection);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

        }


        Thread.sleep(15000L);

    }
}
```
```Console
16:26:59.908 [pool-1-thread-1] INFO com.example.javaconcurrency.lib.ConnectionPool -- acquire connection
16:26:59.908 [pool-1-thread-2] INFO com.example.javaconcurrency.lib.ConnectionPool -- acquire connection
16:26:59.912 [pool-1-thread-2] INFO com.example.javaconcurrency.lib.ConnectionPoolTest -- connection acquired = pool1
16:26:59.911 [pool-1-thread-1] INFO com.example.javaconcurrency.lib.ConnectionPoolTest -- connection acquired = pool0
16:27:01.918 [pool-1-thread-2] INFO com.example.javaconcurrency.lib.ConnectionPool -- release connection
16:27:01.918 [pool-1-thread-1] INFO com.example.javaconcurrency.lib.ConnectionPool -- release connection
16:27:01.919 [pool-1-thread-3] INFO com.example.javaconcurrency.lib.ConnectionPool -- acquire connection
16:27:01.919 [pool-1-thread-4] INFO com.example.javaconcurrency.lib.ConnectionPool -- acquire connection
16:27:01.919 [pool-1-thread-3] INFO com.example.javaconcurrency.lib.ConnectionPoolTest -- connection acquired = pool1
16:27:01.919 [pool-1-thread-4] INFO com.example.javaconcurrency.lib.ConnectionPoolTest -- connection acquired = pool0
16:27:03.925 [pool-1-thread-3] INFO com.example.javaconcurrency.lib.ConnectionPool -- release connection
16:27:03.925 [pool-1-thread-4] INFO com.example.javaconcurrency.lib.ConnectionPool -- release connection
```

