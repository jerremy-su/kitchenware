package org.kitchenware.express.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultNamingThreadFactory implements ThreadFactory{

    static final AtomicInteger poolNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    public DefaultNamingThreadFactory(String name) {
    	this(new ThreadGroup(name));
    }
    
    public DefaultNamingThreadFactory(ThreadGroup group) {
        this.group = group == null ?
                             Thread.currentThread().getThreadGroup()
                             : group
                             ;
        namePrefix = group.getName() +
                      poolNumber.getAndIncrement() +
                     "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
			t.setContextClassLoader(contextClassLoader);
		}
        return t;
    }

    
    public int getThreadGenerateCount() {
    	return this.threadNumber.get();
    }
}
