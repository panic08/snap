/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.panic08;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SnapSchedulerTest {
    static class Dummy {
        int value;
    }

    @Test
    void savesSnapshotsWhenConditionIsTrue() throws InterruptedException {
        Dummy obj = new Dummy();
        obj.value = 100;

        Snap<Dummy> snap = Snap.of(obj);

        SnapScheduler<Dummy> scheduler = new SnapScheduler<>(
                snap,
                Duration.ofMillis(100),
                () -> true,
                () -> "tick",
                new ArrayList<>()
        );

        scheduler.start();
        TimeUnit.MILLISECONDS.sleep(250);
        obj.value = 50;
        scheduler.stopAll();

        snap.restore("tick");

        assertEquals(100, obj.value);
    }

    @Test
    void skipsSnapshotsWhenConditionIsFalse() throws InterruptedException {
        Dummy obj = new Dummy();
        Snap<Dummy> snap = Snap.of(obj);

        SnapScheduler<Dummy> scheduler = new SnapScheduler<>(
                snap,
                Duration.ofMillis(100),
                () -> false,
                () -> "nope",
                new ArrayList<>()
        );

        scheduler.start();
        obj.value = 999;
        TimeUnit.MILLISECONDS.sleep(200);
        scheduler.stopAll();

        assertFalse(snap.hasSnapshot("nope"));
    }

    @Test
    void savesMultipleSnapshotsWithDynamicNames() throws InterruptedException {
        Dummy obj = new Dummy();
        Snap<Dummy> snap = Snap.of(obj);
        AtomicInteger counter = new AtomicInteger();

        SnapScheduler<Dummy> scheduler = new SnapScheduler<>(
                snap,
                Duration.ofMillis(100),
                () -> true,
                () -> "snap-" + counter.incrementAndGet(),
                new ArrayList<>()
        );

        scheduler.start();
        obj.value = 1;
        TimeUnit.MILLISECONDS.sleep(400);
        scheduler.stopAll();

        assertTrue(snap.hasSnapshot("snap-1"));
        assertTrue(snap.hasSnapshot("snap-2"));
        assertTrue(snap.hasSnapshot("snap-3"));
    }

    @Test
    void isRunningReflectsSchedulerState() {
        Dummy obj = new Dummy();
        Snap<Dummy> snap = Snap.of(obj);

        SnapScheduler<Dummy> scheduler = new SnapScheduler<>(
                snap,
                Duration.ofMillis(100),
                () -> true,
                () -> "x",
                new ArrayList<>()
        );

        scheduler.start();

        assertTrue(scheduler.isRunning());
        scheduler.stopAll();
        assertFalse(scheduler.isRunning());
    }
}
