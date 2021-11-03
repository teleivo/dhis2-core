package org.hisp.dhis.analytics.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.StopWatch;

class ExecutionTimeTestExecutionListener extends AbstractTestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger( ExecutionTimeTestExecutionListener.class );

    private StopWatch stopWatch;
    private StopWatch untilTest;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);
        stopWatch = new StopWatch(testContext.getTestClass().getSimpleName());
        untilTest = new StopWatch(testContext.getTestClass().getSimpleName());
        untilTest.start(testContext.getTestClass().getName());
        logger.debug("beforeTestClass: {}", testContext.getTestClass().getSimpleName());
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        if (untilTest.isRunning()) {
            untilTest.stop();
        }
        logger.debug("beforeTestMethod: {} test started after {} seconds", testContext.getTestMethod().getName(), untilTest.getTotalTimeSeconds());
        stopWatch.start(testContext.getTestMethod().getName());
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        super.afterTestMethod(testContext);
        logger.debug("afterTestMethod: {}", testContext.getTestMethod().getName());
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
//        stopWatch.getLastTaskInfo();
        System.out.println(stopWatch.prettyPrint());
        logger.debug("afterTestClass: {} finished after {} seconds", testContext.getTestClass().getSimpleName(), stopWatch.getTotalTimeSeconds());

        logger.debug("afterTestClass: {} finished after {} seconds", testContext.getTestClass().getSimpleName(), stopWatch.getTotalTimeSeconds());

        super.afterTestClass(testContext);
    }
}
