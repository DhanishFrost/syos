package main.scheduler;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EndOfDayShelfReplenisher {
    private final ReplenishShelves replenishShelves;
    private final ScheduledExecutorService scheduler;

    public EndOfDayShelfReplenisher(ReplenishShelves replenishShelves, ScheduledExecutorService scheduler) {
        this.replenishShelves = replenishShelves;
        this.scheduler = scheduler;
    }

    public void scheduleEndOfDayReplenishment(int hour, int minute) {
        Runnable endOfDayTask = replenishShelves::endOfDayReplenishment;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(endOfDayTask, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
