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

package com.panic08.snap;

import com.panic08.snap.event.SnapshotRestoredEvent;
import com.panic08.snap.event.SnapshotSavedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SnapTest {

    static class DummyState {
        private String data;

        public DummyState() {
        }

        public DummyState(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    private DummyState target;
    private Snap<DummyState> snap;

    @BeforeEach
    void setUp() {
        target = new DummyState("initial");
        snap = Snap.of(target);
    }

    @Test
    void testSaveAndRestoreDefault() {
        snap.save();
        target.setData("modified");
        boolean restored = snap.restore();
        assertTrue(restored);
        assertEquals("initial", target.getData());
    }

    @Test
    void testSaveAndRestoreNamed() {
        snap.save("snap1");
        target.setData("changed");
        boolean restored = snap.restore("snap1");
        assertTrue(restored);
        assertEquals("initial", target.getData());
    }

    @Test
    void testRestoreLast() {
        snap.save("first");
        target.setData("second");
        snap.save("second");
        target.setData("third");
        boolean restored = snap.restoreLast();
        assertTrue(restored);
        assertEquals("second", target.getData());
    }

    @Test
    void testDiffNoDifference() {
        snap.save();
        Map<String, String> diff = snap.diff();
        assertTrue(diff.isEmpty());
    }

    @Test
    void testDiffWithNameNoDifference() {
        snap.save("snap");
        Map<String, String> diff = snap.diff("snap");
        assertTrue(diff.isEmpty());
    }

    @Test
    void testDiffWithDifference() {
        snap.save();
        target.setData("changed");
        Map<String, String> diff = snap.diff();
        assertFalse(diff.isEmpty());
        assertEquals("initial -> changed", diff.get("data"));
    }

    @Test
    void testDiffBetweenTwoSnapshots() {
        snap.save("snap1");
        target.setData("first");
        snap.save("snap2");
        Map<String, String> diff = snap.diff("snap1", "snap2");
        assertFalse(diff.isEmpty());
        assertEquals("initial -> first", diff.get("data"));
    }

    @Test
    void testHasSnapshot() {
        snap.save();
        assertTrue(snap.hasSnapshot("default"));
        assertFalse(snap.hasSnapshot("nonexistent"));
    }

    @Test
    void testClear() {
        snap.save();
        snap.clear();
        boolean restored = snap.restore();
        assertFalse(restored);
    }

    @Test
    void testRemoveDefault() {
        snap.save();
        snap.remove();
        assertFalse(snap.hasSnapshot("default"));
    }

    @Test
    void testRemoveNamed() {
        snap.save("snapX");
        snap.remove("snapX");
        assertFalse(snap.hasSnapshot("snapX"));
    }

    @Test
    void testRunAndSaveDefault() {
        snap.save();
        target.setData("changed");
        snap.runAndSave(() -> target.setData("runAndSave"));
        target.setData("modified");
        boolean restored = snap.restore();
        assertTrue(restored);
        assertEquals("runAndSave", target.getData());
    }

    @Test
    void testRunAndSaveNamed() {
        snap.save();
        target.setData("changed");
        snap.runAndSave(() -> target.setData("runAndSaveNamed"), "named");
        target.setData("modified");
        boolean restored = snap.restore("named");
        assertTrue(restored);
        assertEquals("runAndSaveNamed", target.getData());
    }

    @Test
    void listenerShouldBeNotifiedOnSaveAndRestoreWithDetails() {
        DummyState dummy = new DummyState();
        dummy.setData("initial");
        Snap<DummyState> snap = Snap.of(dummy);

        AtomicBoolean saveCalled = new AtomicBoolean(false);
        AtomicBoolean restoreCalled = new AtomicBoolean(false);

        snap.addListener(event -> {
            if (event instanceof SnapshotSavedEvent) {
                saveCalled.set(true);
                assertEquals("default", event.getName());
                assertEquals("initial", event.getTarget().getData());
                assertNotNull(((SnapshotSavedEvent<DummyState>) event).getSnapshot());
            } else if (event instanceof SnapshotRestoredEvent) {
                restoreCalled.set(true);
                assertEquals("default", event.getName());
                assertEquals("initial", event.getTarget().getData());
            }
        });

        snap.save();
        assertTrue(saveCalled.get());

        dummy.setData("changed");
        snap.restore();
        assertTrue(restoreCalled.get());
        assertEquals("initial", dummy.getData());
    }
}