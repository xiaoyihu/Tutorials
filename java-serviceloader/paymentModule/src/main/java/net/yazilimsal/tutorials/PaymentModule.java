package net.yazilimsal.tutorials;


public class PaymentModule implements Module {

    @Override
    public String getModuleName() {
        return "Payment Module";
    }

    @Override
    public void loadModule() {
        System.out.println("Loading " + getModuleName());
    }
}
