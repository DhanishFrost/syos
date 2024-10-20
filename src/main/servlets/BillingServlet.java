package main.servlets;

import main.app.ApplicationContext;
import main.app.UserInteraction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class BillingServlet extends HttpServlet {
    private UserInteraction userInteraction;

    @Override
    public void init() throws ServletException {
        System.out.println("Initializing BillingServlet...");
        // Retrieve the ApplicationContext from the ServletContext
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("appContext");
        if (context == null) {
            System.err.println("ApplicationContext is null in BillingServlet.init()");
        } else {
            // Initialize UserInteraction with the ApplicationContext
            this.userInteraction = new UserInteraction(context);
            System.out.println("BillingServlet initialized successfully.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Generate a unique billing session token if not present
        String billingToken = (String) req.getSession().getAttribute("billingToken");
        if (billingToken == null) {
            billingToken = UUID.randomUUID().toString();
            req.getSession().setAttribute("billingToken", billingToken);
        }

        clearSessionMessages(req);
        req.getRequestDispatcher("/WEB-INF/jspFiles/billing.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        clearSessionMessages(req);

        // Get billing token from session
        String billingToken = (String) req.getSession().getAttribute("billingToken");
        if (billingToken == null) {
            billingToken = UUID.randomUUID().toString(); // Generate a new token if missing (fallback)
            req.getSession().setAttribute("billingToken", billingToken);
        }

        // Delegate to UserInteraction for processing the request
        userInteraction.processRequest(req.getParameterMap(), req, billingToken);

        // Check for errors and handle redirection/forwarding
        if (req.getSession().getAttribute("errorMessage") != null) {
            req.getRequestDispatcher("/WEB-INF/jspFiles/billing.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/jspFiles/billing.jsp").forward(req, resp);
        }
    }

    private void clearSessionMessages(HttpServletRequest req) {
        req.getSession().removeAttribute("successMessage");
        req.getSession().removeAttribute("errorMessage");
    }
}
