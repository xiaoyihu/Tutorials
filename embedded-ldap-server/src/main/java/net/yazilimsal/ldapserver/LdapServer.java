package net.yazilimsal.ldapserver;

import org.apache.directory.server.ApacheDsService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

public class LdapServer {

    private static final Logger log = LoggerFactory.getLogger(LdapServer.class);

    private ApacheDsService apacheDsService;

    private boolean isStarted = false;

    public static void main(String[] args) {
        final LdapServer server = new LdapServer();

        server.start();

        Thread shutdownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.stop();
            }
        });

        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    public void start() {
        if (isStarted)
            return;

        log.info("Starting...");

        try {
            InstanceLayout instanceLayout = new InstanceLayout(getLdapPath());

            checkConfigFile(instanceLayout);

            apacheDsService = new ApacheDsService();
            apacheDsService.start(instanceLayout);

            isStarted = true;

            initLdifFiles(apacheDsService.getDirectoryService());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Server is started.");
    }

    public void stop() {
        if (!isStarted)
            return;

        try {
            apacheDsService.stop();

            log.info("Server is stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getLdapPath() {
        String path = System.getProperty("ldap.path");

        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("-Dldap.path is required!");
        }

        return path;
    }

    private void checkConfigFile(InstanceLayout instanceLayout) {
        File configLdif = new File(instanceLayout.getConfDirectory(), "config.ldif");
        if (!configLdif.exists()) {
            log.warn("config.ldif not found in " + instanceLayout.getConfDirectory().getAbsolutePath() + "!");
        }
    }

    private void initLdifFiles(DirectoryService directoryService) {
        File ldifDirectory = new File(directoryService.getInstanceLayout().getInstanceDirectory(), "ldif");

        if (!ldifDirectory.isDirectory())
            return;

        LdifLoader ldifLoader = new LdifLoader();

        File[] ldifFiles = ldifDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ldif");
            }
        });

        for (File ldifFile : ldifFiles) {
            try {
                ldifLoader.loadLdifs(directoryService, ldifFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
