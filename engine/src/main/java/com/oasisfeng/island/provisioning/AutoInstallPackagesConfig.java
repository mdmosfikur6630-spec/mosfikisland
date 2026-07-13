package com.oasisfeng.island.provisioning;

/**
 * Configuration for auto-installing packages into a newly created Island profile.
 *
 * Edit the {@link #PACKAGES} array to change which apps are auto-installed.
 * This is a separate config file so the list can be updated independently
 * from any provisioning logic.
 */
public class AutoInstallPackagesConfig {

    /** Packages to auto-install into the new Work Profile after provisioning completes. */
    public static final String[] PACKAGES = {
            "ridmik.keyboard",
            "ch.protonvpn.android",
            "com.bitkeep.wallet",
            "com.xunijun.app.gp",
    };

    private AutoInstallPackagesConfig() {}
}
