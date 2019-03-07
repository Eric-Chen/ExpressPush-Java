package com.github.expresspush;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class Minimum {

    @Test
    public void test_basics() throws Exception{
        for(int i = 0; i < 30; i++){
            long s = System.nanoTime();
            TimeUnit.MILLISECONDS.sleep(100);
            long e = System.nanoTime();
            System.out.println((e - s)/1000000);
        }

    }

}
