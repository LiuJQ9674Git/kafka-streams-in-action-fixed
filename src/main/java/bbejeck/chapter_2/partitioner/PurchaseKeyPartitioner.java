package bbejeck.chapter_2.partitioner;

import bbejeck.model.PurchaseKey;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;

/**
 * 按照客户ID分片
 */
public class PurchaseKeyPartitioner extends DefaultPartitioner {


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        Object newKey = null;
        if (key != null) {
            PurchaseKey purchaseKey = (PurchaseKey) key;
            newKey = purchaseKey.getCustomerId();
            keyBytes = ((String) newKey).getBytes();
        }
        return super.partition(topic, newKey, keyBytes, value, valueBytes, cluster);
    }
}
