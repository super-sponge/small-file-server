package com.sponge.srd.flume;

import com.google.common.base.Charsets;
import com.sponge.srd.utils.HbaseUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.AsyncHbaseEventSerializer;
import org.apache.flume.sink.hbase.SimpleRowKeyGenerator;
import org.apache.hadoop.hbase.util.Bytes;
import org.hbase.async.AtomicIncrementRequest;
import org.hbase.async.PutRequest;

import java.util.ArrayList;
import java.util.List;

public class SPAsyncHbaseEventSerializer implements AsyncHbaseEventSerializer {
    private byte[] table;
    private byte[] cf;
    private byte[] payload;
    private byte[] payloadColumn;
    private byte[] incrementColumn;
    private String rowPrefix;
    private byte[] incrementRow;
    private SPHbaseEventSerializer.KeyType keyType;
    private byte[] rowKey = null;
    private int rowkeyHeaderSize;

    @Override
    public void initialize(byte[] table, byte[] cf) {
        this.table = table;
        this.cf = cf;
    }

    @Override
    public List<PutRequest> getActions() {
        List<PutRequest> actions = new ArrayList<PutRequest>();
        if (payloadColumn != null) {
            try {
                switch (keyType) {
                    case TS:
                        rowKey = SimpleRowKeyGenerator.getTimestampKey(rowPrefix);
                        break;
                    case TSNANO:
                        rowKey = SimpleRowKeyGenerator.getNanoTimestampKey(rowPrefix);
                        break;
                    case RANDOM:
                        rowKey = SimpleRowKeyGenerator.getRandomKey(rowPrefix);
                        break;
                    default:
                        if(rowKey == null)
                            rowKey = SimpleRowKeyGenerator.getUUIDKey(rowPrefix);
                        break;
                }
                PutRequest putRequest =  new PutRequest(table, rowKey, cf,
                        payloadColumn, payload);
                actions.add(putRequest);
            } catch (Exception e) {
                throw new FlumeException("Could not get row key!", e);
            }
        }
        return actions;
    }

    public List<AtomicIncrementRequest> getIncrements() {
        List<AtomicIncrementRequest> actions = new ArrayList<AtomicIncrementRequest>();
        if (incrementColumn != null) {
            AtomicIncrementRequest inc = new AtomicIncrementRequest(table,
                    incrementRow, cf, incrementColumn);
            actions.add(inc);
        }
        return actions;
    }

    @Override
    public void cleanUp() {
        // TODO Auto-generated method stub

    }

    @Override
    public void configure(Context context) {
        String pCol = context.getString("payloadColumn", "pCol");
        String iCol = context.getString("incrementColumn", "iCol");
        rowkeyHeaderSize = context.getInteger("rowkeyheadersize", 0);

        rowPrefix = context.getString("rowPrefix", "default");
        String suffix = context.getString("suffix", "uuid");
        if (pCol != null && !pCol.isEmpty()) {
            if (suffix.equals("timestamp")) {
                keyType = SPHbaseEventSerializer.KeyType.TS;
            } else if (suffix.equals("random")) {
                keyType = SPHbaseEventSerializer.KeyType.RANDOM;
            } else if (suffix.equals("nano")) {
                keyType = SPHbaseEventSerializer.KeyType.TSNANO;
            } else if (suffix.equals("key")) {
                keyType = SPHbaseEventSerializer.KeyType.KEY;
            } else {
                keyType = SPHbaseEventSerializer.KeyType.UUID;
            }
            payloadColumn = pCol.getBytes(Charsets.UTF_8);
        }
        if (iCol != null && !iCol.isEmpty()) {
            incrementColumn = iCol.getBytes(Charsets.UTF_8);
        }
        incrementRow = context.getString("incrementRow", "incRow").getBytes(Charsets.UTF_8);
    }

    @Override
    public void setEvent(Event event) {
        this.payload = event.getBody();
        String key = event.getHeaders().get("key");
        this.rowKey = key != null ? Bytes.toBytes(HbaseUtils.rowKeyGen(key, rowkeyHeaderSize)) : null;
    }

    @Override
    public void configure(ComponentConfiguration conf) {
        // TODO Auto-generated method stub
    }

}

