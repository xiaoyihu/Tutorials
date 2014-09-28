package net.yazilimsal.tutorials;

import java.util.ServiceLoader;


public class ModuleLoader {

    public void load() {
        ServiceLoader<Module> modules = ServiceLoader.load(Module.class);

        for (Module module : modules) {
            System.out.println("MODULE: " + module.getModuleName());
            module.loadModule();
        }
    }

}
