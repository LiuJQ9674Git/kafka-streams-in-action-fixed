package bbejeck.util.datagen;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 客户化日期
 */
public class CustomDateGenerator {

    /**
     * 时间戳
     */
    private Instant instant = Instant.now();

    /**
     * Duration 是在 Java 8中加入的，主要是用来计算日期，差值之类的。
     *
     * Duration 被声明final（immutable），并且线程安全。
     */
    private Duration increaseDuration;

    private CustomDateGenerator(Duration increaseDuration) {
        this.increaseDuration = increaseDuration;
    }

    public static CustomDateGenerator withTimestampsIncreasingBy(Duration increaseDuration) {
           return new CustomDateGenerator(increaseDuration);
    }

    public Date get() {
          Date date =  Date.from(instant);
          instant = instant.plus(increaseDuration);
          return date;
    }
}
