package nl.isaac.dotcms.util.osgi;

import com.dotcms.filters.interceptor.Result;
import com.dotcms.filters.interceptor.WebInterceptor;
import com.dotmarketing.util.Logger;
import org.osgi.framework.BundleContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This web interceptor adds the access control allow origin in addition to overrides the request and response
 * @author jsanca
 */
public class ServletWebInterceptor implements WebInterceptor {
    private final Servlet servlet;
    private final String path;
    private final BundleContext context;

    public ServletWebInterceptor(BundleContext context, Servlet servlet, String path) {
        this.context = context;
        this.servlet = servlet;
        this.path = path;
    }

    @Override
    public String getName() {
        return context.getBundle().getSymbolicName() + "_" + servlet.getClass().getName();
    }

    @Override
    public String[] getFilters() {
        return new String[] {"/app" + path};
    }

    @Override
    public Result intercept(final HttpServletRequest request,
                            final HttpServletResponse response) throws IOException {

        Result result = Result.SKIP_NO_CHAIN;

        try {
            servlet.service(request, response);
        } catch (ServletException e) {
            Logger.warn(this, "Exception in servlet " + path, e);
            throw new IOException(e);
        }

        return result;
    }
}