package au.com.fantomdigital.fantomeventj;

import junit.framework.TestCase;

public class LooperBinderTest extends TestCase {
    private static class RecordingLooperBinder implements LooperBinder {
        boolean called = false;

        //@Override
        public void bind(EventDispatcher satellite) {
            called = true;
        }
    }

    private RecordingLooperBinder _enforcer;
    private EventDispatcher _satellite;
    private int _handled;

    public LooperBinderTest() {
        //System.out.println("LooperBinderTest() test run");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //System.out.println("setUp");

        _enforcer = new RecordingLooperBinder();
        _satellite = new EventDispatcher(_enforcer, this);

        _handled = 0;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //System.out.println("tearDown");

        // clears
        _satellite.removeAllEventListener();
        _handled = 0;
    }

    /**
     * Test Handler
     * @param event
     */
    private void onTestHandler(IBaseEvent event) {
        _handled ++;
        //System.out.println("onTestHandler() " + _handled);
    }

    /**
     * Test 2 Handler
     * @param event
     */
    private void onTest2Handler(IBaseEvent event) {
        //System.out.println("onTest2Handler() " + _handled);
    }

    public void testAddEventListeners() {
        String handlerMethod = "onTestHandler";

        try {
            BaseEvent event = new BaseEvent("BaseEvent", this);
            EventListener listener = new EventListener(event, this, handlerMethod);

            _satellite.addEventListener(event, listener);
            assertEquals(1, _satellite.countEventListeners(event));
        } catch (NoSuchMethodException e) {
            System.out.println("testAddEventListeners() NoSuchMethodException");
            assertFalse(true);
        }
    }

    public void testRemoveEventListeners() {
        String handlerMethod = "onTestHandler";

        try {
            BaseEvent event = new BaseEvent("BaseEvent", this);
            TestEvent testEvent = new TestEvent("TestEvent", this);
            EventListener listener = new EventListener(event, this, handlerMethod);
            EventListener listener2 = new EventListener(testEvent, this, handlerMethod);

            _satellite.addEventListener(event, listener);
            assertEquals(1, _satellite.countEventListeners(event));

            _satellite.removeEventListener(testEvent, listener2);
            assertEquals(1, _satellite.countEventListeners(event));
        } catch (NoSuchMethodException e) {
            System.out.println("testRemoveEventListeners() NoSuchMethodException");
            assertFalse(true);
        }
    }

    public void testRemoveAllEventListeners() {
        String handlerMethod = "onTestHandler";
        String handlerMethod2 = "onTest2Handler";

        try {
            BaseEvent event = new BaseEvent("BaseEvent", this);
            EventListener listener = new EventListener(event, this, handlerMethod);
            EventListener listener2 = new EventListener(event, this, handlerMethod2);

            _satellite.addEventListener(event, listener);
            assertEquals(1, _satellite.countEventListeners(event));

            _satellite.addEventListener(event, listener2);
            assertEquals(2, _satellite.countEventListeners(event));

            _satellite.removeAllEventListener();
            assertEquals(0, _satellite.countEventListeners(event));
        } catch (NoSuchMethodException e) {
            System.out.println("testRemoveAllEventListeners() NoSuchMethodException");
            assertFalse(true);
        }
    }

    public void testDispatchEvent() {
        String handlerMethod = "onTestHandler";

        try {
            BaseEvent event = new BaseEvent("BaseEvent", this);
            EventListener listener = new EventListener(event, this, handlerMethod);

            _satellite.addEventListener(event, listener);
            assertEquals(1, _satellite.countEventListeners(event));

            _satellite.dispatchEvent(event, this);

            assertEquals(1, _handled);

            for(int i = 0; i < 10; i++) {
                _satellite.dispatchEvent(event, this);
            }

            assertEquals(11, _handled);
        } catch (NoSuchMethodException e) {
            System.out.println("testDispatchEvent() NoSuchMethodException");
            assertFalse(true);
        }
    }
}