package net.yazilimsal.codelabs.jsf.exceptionhandler;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class MyExceptionHandler {

    public boolean handleException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            String message = throwable.getMessage();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));

            return true;
        }

        return false;
    }

}
