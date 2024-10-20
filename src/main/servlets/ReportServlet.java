package main.servlets;

import main.app.ApplicationContext;
import main.app.UserInteraction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ReportServlet extends HttpServlet {
    private UserInteraction userInteraction;

    @Override
    public void init() throws ServletException {
        System.out.println("Initializing ReportServlet...");
        // Retrieve the ApplicationContext from the ServletContext
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("appContext");
        if (context == null) {
            System.err.println("ApplicationContext is null in ReportServlet.init()");
        } else {
            this.userInteraction = new UserInteraction(context);
            System.out.println("ReportServlet initialized successfully.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Clear any session messages before rendering the report page
        clearSessionMessages(req);

        req.getRequestDispatcher("/WEB-INF/jspFiles/report.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Clear any session messages before processing the report request
        clearSessionMessages(req);
        
        userInteraction.processRequest(req.getParameterMap(), req, null);

        // Forward to the JSP after processing
        req.getRequestDispatcher("/WEB-INF/jspFiles/report.jsp").forward(req, resp);
    }

    private void clearSessionMessages(HttpServletRequest req) {
        req.getSession().removeAttribute("successMessage");
        req.getSession().removeAttribute("errorMessage");
    }
}
