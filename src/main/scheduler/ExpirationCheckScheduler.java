package main.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpirationCheckScheduler {
    private final ScheduledExecutorService scheduler;

    public ExpirationCheckScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleDailyExpirationCheck(Runnable task, int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
