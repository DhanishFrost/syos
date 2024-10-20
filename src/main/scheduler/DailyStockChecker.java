package main.scheduler;

import main.domain.dao.StockDAO;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.Duration;

public class DailyStockChecker {
    private final StockDAO stockDAO;
    private final int threshold;
    private final ScheduledExecutorService scheduler;

    public DailyStockChecker(StockDAO stockDAO, int threshold, ScheduledExecutorService scheduler) {
        this.stockDAO = stockDAO;
        this.threshold = threshold;
        this.scheduler = scheduler;
    }

    public void startDailyCheck(int hour, int minute) {
        Runnable dailyStockCheckTask = new DailyStockCheckTask(stockDAO, threshold);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(dailyStockCheckTask, initialDelay, period, TimeUnit.MILLISECONDS);
    }
}
