package net.yazilimsal.ldapserver;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.apache.directory.server.constants.ApacheSchemaConstants;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.entry.ClonedServerEntry;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.directory.server.protocol.shared.store.LdifLoadFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache Directory Server top level.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifLoader {
    private static final Logger LOG = LoggerFactory.getLogger(LdifLoader.class.getName());

    public LdifLoader() {

    }

    // ----------------------------------------------------------------------
    // From CoreContextFactory: presently in intermediate step but these
    // methods will be moved to the appropriate protocol service eventually.
    // This is here simply to start to remove the JNDI dependency then further
    // refactoring will be needed to place these where they belong.
    // ----------------------------------------------------------------------

    /**
     * Check that the entry where are stored the loaded Ldif files is created.
     * 
     * If not, create it.
     * 
     * The files are stored in ou=loadedLdifFiles,ou=configuration,ou=system
     */
    private void ensureLdifFileBase(DirectoryService directoryService) throws Exception {
        Dn dn = new Dn(ServerDNConstants.LDIF_FILES_DN);
        Entry entry = null;

        try {
            entry = directoryService.getAdminSession().lookup(dn);
        } catch (Exception e) {
            LOG.info("{} does not exists. The entry will be created now.", ServerDNConstants.LDIF_FILES_DN);
        }

        if (entry == null) {
            entry = directoryService.newEntry(new Dn(ServerDNConstants.LDIF_FILES_DN));
            entry.add(SchemaConstants.OU_AT, "loadedLdifFiles");
            entry.add(SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.TOP_OC, SchemaConstants.ORGANIZATIONAL_UNIT_OC);

            directoryService.getAdminSession().add(entry);
        }
    }

    /**
     * Create a string containing a hex dump of the loaded ldif file name.
     * 
     * It is associated with the attributeType wrt to the underlying system.
     */
    private Dn buildProtectedFileEntryDn(File ldif) throws Exception {
        String fileSep = File.separatorChar == '\\' ? ApacheSchemaConstants.WINDOWS_FILE_AT
                : ApacheSchemaConstants.UNIX_FILE_AT;

        return new Dn(fileSep + "=" + Strings.dumpHexPairs(Strings.getBytesUtf8(getCanonical(ldif))) + ","
                + ServerDNConstants.LDIF_FILES_DN);
    }

    private void addFileEntry(DirectoryService directoryService, File ldif) throws Exception {
        String rdnAttr = File.separatorChar == '\\' ? ApacheSchemaConstants.WINDOWS_FILE_AT
                : ApacheSchemaConstants.UNIX_FILE_AT;
        String oc = File.separatorChar == '\\' ? ApacheSchemaConstants.WINDOWS_FILE_OC
                : ApacheSchemaConstants.UNIX_FILE_OC;

        Entry entry = directoryService.newEntry(buildProtectedFileEntryDn(ldif));
        entry.add(rdnAttr, getCanonical(ldif));
        entry.add(SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.TOP_OC, oc);
        directoryService.getAdminSession().add(entry);
    }

    private String getCanonical(File file) {
        String canonical;

        try {
            canonical = file.getCanonicalPath();
        } catch (IOException e) {
            LOG.error(I18n.err(I18n.ERR_179), e);
            return null;
        }

        return StringUtils.replace(canonical, "\\", "\\\\");
    }

    /**
     * Load a ldif into the directory.
     * 
     * @param root
     *            The context in which we will inject the entries
     * @param ldifFile
     *            The ldif file to read
     * @throws NamingException
     *             If something went wrong while loading the entries
     */
    private void loadLdif(DirectoryService directoryService, File ldifFile) throws Exception {
        Entry fileEntry = null;

        try {
            fileEntry = directoryService.getAdminSession().lookup(buildProtectedFileEntryDn(ldifFile));
        } catch (Exception e) {
            // if does not exist
        }

        if (fileEntry != null) {
            String time = ((ClonedServerEntry) fileEntry).getOriginalEntry().get(SchemaConstants.CREATE_TIMESTAMP_AT)
                    .getString();
            LOG.info("Load of LDIF file '" + getCanonical(ldifFile) + "' skipped.  It has already been loaded on "
                    + time + "");
        } else {
            LdifFileLoader loader = new LdifFileLoader(directoryService.getAdminSession(), ldifFile,
                    new ArrayList<LdifLoadFilter>());
            int count = loader.execute();
            LOG.info("Loaded " + count + " entries from LDIF file '" + getCanonical(ldifFile) + "'");
            addFileEntry(directoryService, ldifFile);
        }
    }

    /**
     * Load the existing LDIF files in alphabetic order
     */
    public void loadLdifs(DirectoryService directoryService, File ldifDirectory) throws Exception {

        // LOG and bail if property not set
        if (ldifDirectory == null) {
            LOG.info("LDIF load directory not specified.  No LDIF files will be loaded.");
            return;
        }

        // LOG and bail if LDIF directory does not exists
        if (!ldifDirectory.exists()) {
            LOG.warn("LDIF load directory '{}' does not exist.  No LDIF files will be loaded.",
                    getCanonical(ldifDirectory));
            return;
        }

        ensureLdifFileBase(directoryService);

        // if ldif directory is a file try to load it
        if (ldifDirectory.isFile()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("LDIF load directory '{}' is a file. Will attempt to load as LDIF.",
                        getCanonical(ldifDirectory));
            }

            try {
                loadLdif(directoryService, ldifDirectory);
            } catch (Exception ne) {
                // If the file can't be read, log the error, and stop
                // loading LDIFs.
                LOG.error(I18n.err(I18n.ERR_180, ldifDirectory.getAbsolutePath(), ne.getLocalizedMessage()));
                throw ne;
            }
        } else {
            // get all the ldif files within the directory
            File[] ldifFiles = ldifDirectory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    boolean isLdif = Strings.toLowerCase(pathname.getName()).endsWith(".ldif");
                    return pathname.isFile() && pathname.canRead() && isLdif;
                }
            });

            // LOG and bail if we could not find any LDIF files
            if ((ldifFiles == null) || (ldifFiles.length == 0)) {
                LOG.warn("LDIF load directory '{}' does not contain any LDIF files. No LDIF files will be loaded.",
                        getCanonical(ldifDirectory));
                return;
            }

            // Sort ldifFiles in alphabetic order
            Arrays.sort(ldifFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return f1.getName().compareTo(f2.getName());
                }
            });

            // load all the ldif files and load each one that is loaded
            for (File ldifFile : ldifFiles) {
                try {
                    LOG.info("Loading LDIF file '{}'", ldifFile.getName());
                    loadLdif(directoryService, ldifFile);
                } catch (Exception ne) {
                    // If the file can't be read, log the error, and stop
                    // loading LDIFs.
                    LOG.error(I18n.err(I18n.ERR_180, ldifFile.getAbsolutePath(), ne.getLocalizedMessage()));
                    throw ne;
                }
            }
        }
    }
}
