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

package com.panic08.storage;

import com.google.gson.Gson;
import com.panic08.AbstractFileSnapshotStorage;
import com.panic08.Snapshot;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JsonFileSnapshotStorage<T> extends AbstractFileSnapshotStorage<T> {

    private final Gson gson;
    private final Type typeOfT;

    public JsonFileSnapshotStorage(Gson gson, Type typeOfT) {
        this.gson = gson;
        this.typeOfT = typeOfT;
    }

    public JsonFileSnapshotStorage(Type typeOfT) {
        this.gson = new Gson();
        this.typeOfT = typeOfT;
    }

    @Override
    protected byte[] encode(Snapshot<T> snapshot) {
        String encodedStr = gson.toJson(snapshot.getState());
        return encodedStr.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected Snapshot<T> decode(byte[] data) {
        String str = new String(data, StandardCharsets.UTF_8);
        T state = gson.fromJson(str, typeOfT);
        return new Snapshot<>(state);
    }

}
