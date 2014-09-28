package net.yazilimsal.tutorials;


public class ShoppingCartModule implements Module {
    @Override
    public String getModuleName() {
        return "Shopping Cart Module";
    }

    @Override
    public void loadModule() {
        System.out.println("Loading " + getModuleName());
    }
}
