/*
 * Copyright (c) 2008-2012, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.impl;

import com.hazelcast.nio.Data;
import com.hazelcast.nio.DataSerializable;
import com.hazelcast.util.Clock;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static com.hazelcast.nio.IOUtil.toObject;



@SuppressWarnings("VolatileLongOrDoubleField")
public abstract class AbstractRecord implements Record, DataSerializable {

    protected volatile long id;
    protected volatile RecordState state;
    protected volatile RecordStats stats;
    protected volatile Data keyData;

    public AbstractRecord(long id, Data keyData, long ttl, long maxIdleMillis) {
        this.id = id;
        if(ttl > 0 || maxIdleMillis > 0){
            state = new RecordState();
            state.setTtl(ttl);
            state.setMaxIdleMillis(maxIdleMillis);
        }
        this.keyData = keyData;
    }

    public AbstractRecord() {
    }

    public long getId() {
        return id;
    }

    public Data getKey() {
        return keyData;
    }

    public void setMaxIdleMillis(long maxIdleMillis){
        if(maxIdleMillis > 0 && state == null) {
            state = new RecordState();
        }
        if(state != null) {
            state.setMaxIdleMillis(maxIdleMillis);
        }
    }

    public void setTTL(long ttl){
        if(ttl > 0 && state == null) {
            state = new RecordState();
        }
        if(state != null) {
            state.setTtl(ttl);
        }
    }

    public void setActive(boolean b) {
        if (state != null)
            state.setActive(b);
    }

    public void setDirty(boolean b) {
        if (state != null)
            state.setDirty(b);
    }

    public Record clone() {
        // todo implement
        return null;
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeLong(id);
        if(state != null) {
            out.writeBoolean(true);
            state.writeData(out);
        }
        else {
            out.writeBoolean(false);
        }

        if(stats != null) {
            out.writeBoolean(true);
            stats.writeData(out);
        }
        else {
            out.writeBoolean(false);
        }
    }

    public void readData(DataInput in) throws IOException {
        id = in.readLong();
        boolean stateEnabled = in.readBoolean();
        if(stateEnabled) {
            state = new RecordState();
            state.readData(in);
        }
        boolean statsEnabled = in.readBoolean();
        if(statsEnabled) {
            stats = new RecordStats();
            stats.readData(in);
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRecord)) return false;
        Record record = (Record) o;
        return record.getId() == getId();
    }

    public String toString() {
        return "Record id=" + getId();
    }

    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
