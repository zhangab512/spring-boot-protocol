package com.github.netty.core;

import io.netty.bootstrap.ServerBootstrap;

/**
 * Server listening
 * Created by wangzihao on 2018/11/12/012.
 */
public interface ServerListener extends Ordered{

    default void onServerStart() throws Exception{}
    default void onServerStop() throws Exception{}
    default void conifg(ServerBootstrap bootstrap) throws Exception{}

    /**
     * default Priority order 0
     * @return The smaller the value of order, the more likely it is to be executed first
     */
    @Override
    default int getOrder(){
        return 0;
    }
}
