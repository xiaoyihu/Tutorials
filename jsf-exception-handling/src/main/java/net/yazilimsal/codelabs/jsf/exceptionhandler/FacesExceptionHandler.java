package net.yazilimsal.codelabs.jsf.exceptionhandler;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;

public class FacesExceptionHandler extends ExceptionHandlerWrapper {

    private MyExceptionHandler myExceptionHandler = new MyExceptionHandler();

    private ExceptionHandler wrapped;

    public FacesExceptionHandler(ExceptionHandler exceptionHandler) {
        this.wrapped = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

        while (iterator.hasNext()) {
            Throwable throwable = getThrowable(iterator);
            boolean isHandled = false;

            try {
                isHandled = myExceptionHandler.handleException(throwable);
            } finally {
                if (isHandled) iterator.remove();
            }
        }

        getWrapped().handle();
    }

    private Throwable getThrowable(Iterator<ExceptionQueuedEvent> iterator) {
        ExceptionQueuedEvent event = iterator.next();
        ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
        Throwable throwable = context.getException();

        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        return throwable;
    }
}
