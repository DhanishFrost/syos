package main.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ApplicationContext appContext = new ApplicationContext();
        ApplicationInitializer initializer = new ApplicationInitializer(appContext);
        initializer.initialize();

        servletContextEvent.getServletContext().setAttribute("appContext", appContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ApplicationContext appContext = (ApplicationContext) servletContextEvent.getServletContext().getAttribute("appContext");
        if (appContext != null) {
            appContext.shutdown();
        }
    }
}
