package main.servlets;

import main.app.ApplicationContext;
import main.app.UserInteraction;
import main.domain.model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class CustomerServlet extends HttpServlet {
    private UserInteraction userInteraction;

    @Override
    public void init() throws ServletException {
        System.out.println("Initializing CustomerServlet...");
        // Retrieve the ApplicationContext from the ServletContext
        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("appContext");
        if (context == null) {
            System.err.println("ApplicationContext is null in CustomerServlet.init()");
        } else {
            this.userInteraction = new UserInteraction(context);
            System.out.println("CustomerServlet initialized successfully.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        clearSessionMessages(req); // Clear messages before processing request

        ApplicationContext context = (ApplicationContext) getServletContext().getAttribute("appContext");

        String action = req.getParameter("action");
        List<Customer> customers = null;

        if ("search".equals(action)) {
            String phoneNumber = req.getParameter("customerPhone");
            Customer customer = context.customerManagementFacade.findCustomerByPhoneNumber(phoneNumber);
            customers = customer != null ? List.of(customer) : List.of();
        } else {
            customers = context.customerManagementFacade.getAllCustomers();
        }

        req.setAttribute("customers", customers);

        // Retrieve messages from session and then clear them
        HttpSession session = req.getSession();
        req.setAttribute("errorMessage", session.getAttribute("errorMessage"));
        req.setAttribute("successMessage", session.getAttribute("successMessage"));
        session.removeAttribute("errorMessage");
        session.removeAttribute("successMessage");

        // Forward the request
        req.getRequestDispatcher("/WEB-INF/jspFiles/customer.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        clearSessionMessages(req); // Clear messages before processing request

        // Handle the customer management logic
        userInteraction.processRequest(req.getParameterMap(), req, null);

        // Retrieve messages from request scope
        String errorMessage = (String) req.getAttribute("errorMessage");
        String successMessage = (String) req.getAttribute("successMessage");

        // Optionally, store messages in session (if needed across multiple requests)
        HttpSession session = req.getSession();
        if (errorMessage != null) {
            session.setAttribute("errorMessage", errorMessage);
        }
        if (successMessage != null) {
            session.setAttribute("successMessage", successMessage);
        }

        // Forward the request back to the customer management JSP page
        req.getRequestDispatcher("/WEB-INF/jspFiles/customer.jsp").forward(req, resp);
    }

    private void clearSessionMessages(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute("successMessage");
        session.removeAttribute("errorMessage");
    }
}
