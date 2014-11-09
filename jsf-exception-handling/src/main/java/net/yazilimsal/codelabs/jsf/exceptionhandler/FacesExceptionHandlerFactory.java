package net.yazilimsal.codelabs.jsf.exceptionhandler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class FacesExceptionHandlerFactory extends ExceptionHandlerFactory {

    private ExceptionHandlerFactory factory;

    public FacesExceptionHandlerFactory(ExceptionHandlerFactory factory) {
        this.factory = factory;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new FacesExceptionHandler(factory.getExceptionHandler());
    }
}
