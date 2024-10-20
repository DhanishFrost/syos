package main.facade;

import java.util.Map;

public interface ReportFacade {
    String handleReportGeneration(Map<String, String[]> parameters);
}
