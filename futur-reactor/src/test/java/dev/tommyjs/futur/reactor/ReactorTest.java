package dev.tommyjs.futur.reactor;

import dev.tommyjs.futur.executor.SinglePoolExecutor;
import dev.tommyjs.futur.impl.SimplePromiseFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ReactorTest {

    @Test
    void test() {
        Logger logger = LoggerFactory.getLogger(ReactorTest.class);
        var pfac = new SimplePromiseFactory(SinglePoolExecutor.create(1), logger);

        ReactorTransformer.wrapMono(Mono.error(new Exception("Test Error")), pfac)
            .logExceptions("test");
    }

}
