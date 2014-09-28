package net.yazilimsal.tutorials;


public class ProductModule implements Module {
    @Override
    public String getModuleName() {
        return "Product Module";
    }

    @Override
    public void loadModule() {
        System.out.println("Loading " + getModuleName());
    }
}
