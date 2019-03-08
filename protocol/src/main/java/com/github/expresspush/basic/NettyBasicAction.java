package com.github.expresspush.basic;

import com.github.expresspush.config.ServerConfig;
import com.github.expresspush.exception.RemoteTimeoutException;
import com.github.expresspush.handler.TransferCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyBasicAction {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //fixme 针对单向传输，确定合适的限制
    private static final int DEFAULT_ONEWAY_PROCESS_LIMIT = 65535;

    //fixme 针对异步处理，确定合适的限制
    private static final int DEFAULT_ASYNC_PROCESS_LIMIT = 65535;

    private static final String REQUEST_THREAD_NAME_PREFIX = "request_process_";


    /* oneway方式在默认情况的操作方式是，打完就跑，所以在请求量过大情况下，会导致内存压力 */
    private Semaphore semaphoreOneway;

    /* 异步方式在默认情况的操作方式是，打完就跑，所以在请求量过大情况下，会导致内存压力 */
    private Semaphore semaphoreAsync;

    private ConcurrentMap<Long/* RequestId */, ResponseFuture /* ResultFuture */> responseTable = new ConcurrentHashMap();

    /**
     * 主要服务请求的处理分发
     */
    private ExecutorService requestExecutor;


    public NettyBasicAction(){
        this(DEFAULT_ONEWAY_PROCESS_LIMIT, DEFAULT_ASYNC_PROCESS_LIMIT);
    }

    public NettyBasicAction(final int onewayLimit, final int asyncLimit){
        this.semaphoreOneway = new Semaphore(onewayLimit, false);
        this.semaphoreAsync = new Semaphore(asyncLimit, false);
    }

    protected void initRequestExecutor(){
        this.requestExecutor = Executors.newFixedThreadPool(ServerConfig.DEFAULT_REQUESTS_CONCURRENCY, new DefaultThreadFactory(REQUEST_THREAD_NAME_PREFIX, true));
    }

    /*
    * 不断扫描responseTable中的结果，对于已经超时
    * 未超时的返回对象，会在获取返回时处理
    */
    protected void refreshResponseTable(){
        //1.iterate responseTable
        List<ResponseFuture> rl = new LinkedList();
        Iterator<Map.Entry<Long, ResponseFuture>> respIterator = this.responseTable.entrySet().iterator();
        while(respIterator.hasNext()){
            Map.Entry<Long, ResponseFuture> entry = respIterator.next();
            ResponseFuture respFuture = entry.getValue();
            //2.check each response timeout
            if(respFuture.isTimeout()){
                //!!important!!
                respFuture.release();
                rl.add(respFuture);
                //3.remove each timeout and valid response
                respIterator.remove();
            }
        }

        //4.trigger the callback func of each response
        rl.forEach(future -> future.executeCallback());
    }

    /**
     * @param channel
     * @param request
     * @param timeoutmillisec 超时保护
     */
    protected void remoteSendOneway(final Channel channel, TransferCommand request, long timeoutmillisec){
        validateChannelStatus(channel);
        try {
            if (this.semaphoreOneway.tryAcquire(timeoutmillisec, TimeUnit.MILLISECONDS)){
                InnerOnewaySemaphore oneTrigger = InnerOnewaySemaphore.of(this.semaphoreOneway);
                channel.writeAndFlush(request).addListener(new ChannelFutureListener(){
                    @Override public void operationComplete(ChannelFuture f) throws Exception {
                        oneTrigger.release();
                        if (!f.isSuccess()){
                            logger.info("Handle request fail: {} on channel [{}]", request, channel.id());
                        }
                    }
                });
            } else {
                //超时情况下，抛出异常
                logger.info("Timeout to get flag, request: {}", request);
                throw new RemoteTimeoutException("Fail to get send flag");
            }
        } catch (InterruptedException e) {
            logger.error("oneway sending interrupted", e);
        }
    }

    protected TransferCommand remoteSendSync(final Channel channel, TransferCommand request, long timeoutmillisec) {
        validateChannelStatus(channel);
        long tickStart = System.nanoTime();
        ChannelFuture rf = channel.writeAndFlush(request);
        final ResponseFuture respFuture = new ResponseFuture(rf, null, tickStart, timeoutmillisec, null);
        final Long requestId = request.getReqId();
        this.responseTable.put(requestId, respFuture);
        rf.addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    respFuture.setSucceed(true);
                    return;
                } else {
                    respFuture.setSucceed(false);
                }
                respFuture.setResp(null);
            }
        });

        TransferCommand result = null;
        try{
            if(respFuture.await(timeoutmillisec)){
                result = respFuture.getResp();
            }
        } catch (InterruptedException e){
            logger.error("Sync send interrupted", e);
        } finally {
            //避免内存泄漏
            this.responseTable.remove(requestId);
        }

        return result;
    }

    protected void remoteSendAsync(final Channel channel,
        TransferCommand request,
        long timeoutMillisec,
        RemoteAsyncCallback asyncCallback) {

        validateChannelStatus(channel);

        long tickStart = System.nanoTime();
        try {
            if(this.semaphoreAsync.tryAcquire(timeoutMillisec, TimeUnit.MILLISECONDS)){
                checkTimeout("async send command "+request, tickStart, timeoutMillisec, () -> this.semaphoreAsync.release() );
                ChannelFuture cf = channel.writeAndFlush(request);
                final ResponseFuture irf = new ResponseFuture(cf,
                                                                        this.semaphoreAsync,
                                                                        tickStart,
                                                                        timeoutMillisec,
                                                                        asyncCallback);
                this.responseTable.put(request.getReqId(), irf);
                cf.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            irf.setSucceed(true);
                            return;
                        }
                        irf.setSucceed(false);
                        irf.executeCallback();
                        irf.release();
                    }
                });
            }

        } catch (InterruptedException e){
            logger.error("Async send interrupted", e);
        } finally {
            //resource release

        }
    }

    private void validateChannelStatus(Channel channel) {
        if(!isValid(channel)){
            throw new InvalidChannelStatusException("Channel isn't active, channel id: [{"+ channel.id() +"}]");
        }
    }

    /* default time unit: ms */
    private void checkTimeout(String exceptionMsg, long startTick, long timeoutLimit, Runnable externalTask){
        long now = System.nanoTime();
        long elapseInMs = (now - startTick)/1000000;
        if(elapseInMs > timeoutLimit){
            externalTask.run();
            throw new RemoteTimeoutException(exceptionMsg);
        }
    }

    private boolean isValid(Channel channel){
        return channel == null || channel.isActive();
    }

    public ConcurrentMap<Long, ResponseFuture> getResponseTable() {
        return responseTable;
    }

    public ExecutorService getRequestExecutor() {
        return requestExecutor;
    }
}
