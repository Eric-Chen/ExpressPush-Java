package com.github.expresspush.protocol.basic;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用于在主调类和匿名内部类之间使用场景的封装
 */
class InnerOnewaySemaphore {
    final AtomicBoolean onceFlag = new AtomicBoolean(false);
    final Semaphore onewaySemaphore;
    InnerOnewaySemaphore(final Semaphore onewaySemaphore){
        this.onewaySemaphore = onewaySemaphore;
    }

    static InnerOnewaySemaphore of(final Semaphore onewaySemaphore){
        return new InnerOnewaySemaphore(onewaySemaphore);
    }

    /* 为避免误用，导致多次释放，加入开关 */
    void release(){
        if(this.onewaySemaphore != null
            && this.onceFlag.compareAndSet(false, true)){
            this.onewaySemaphore.release();
        }
    }
}
