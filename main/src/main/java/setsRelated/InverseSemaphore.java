package setsRelated;

public class InverseSemaphore {

    private int threadCount;
    private int runningTaskCount = 0;
    private Object lock = new Object();

    public InverseSemaphore(int threadCount) {
        this.threadCount = threadCount;
    }

    public void beforeSubmit() {
        synchronized (lock) {
            runningTaskCount++;
        }
    }

    public void taskCompleted() {
        synchronized (lock) {
            runningTaskCount--;
            if (runningTaskCount < threadCount)
                lock.notifyAll();
        }
    }
    
    public void awaitFreeOfTaskThread() throws InterruptedException {
        synchronized (lock) {
            while (runningTaskCount >= threadCount)
                lock.wait();
        }
    }

    public void awaitCompletion() throws InterruptedException {
        synchronized (lock) {
            while (runningTaskCount > 0)
                lock.wait();
        }
    }
}
