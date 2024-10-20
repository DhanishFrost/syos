package main.facade;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface BillingFacade {
    void handleBilling(String operation, Map<String, String[]> parameters, HttpServletRequest req, String billingToken);
}
