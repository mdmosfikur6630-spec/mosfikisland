package com.oasisfeng.island.provisioning;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.oasisfeng.island.util.DevicePolicies;
import com.oasisfeng.island.util.Users;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;

/**
 * Automatically installs predefined apps from the main (personal) profile
 * into the newly created Island work profile.
 *
 * This runs asynchronously after Island's own provisioning flow completes.
 * It is fully additive — if it fails entirely, the rest of Island behaves
 * exactly as before.
 */
public class AutoInstallPackages implements Runnable {

    private static final String TAG = "Island.AutoInstall";

    private final Context mContext;
    private final DevicePolicies mPolicies;

    public AutoInstallPackages(final Context context) {
        mContext = context;
        mPolicies = new DevicePolicies(context);
    }

    @Override public void run() {
        if (SDK_INT < O) {
            Log.w(TAG, "installExistingPackage requires API 26+");
            return;
        }
        installPackagesFromPrimaryProfile();
    }

    private void installPackagesFromPrimaryProfile() {
        final LauncherApps launcherApps = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (launcherApps == null) {
            Log.w(TAG, "LauncherApps service not available");
            return;
        }

        int successCount = 0;
        final String[] packages = AutoInstallPackagesConfig.PACKAGES;

        for (final String pkg : packages) {
            // Check if the package is installed on the main (primary/personal) profile
            if (! isPackageInstalledOnPrimaryProfile(launcherApps, pkg)) {
                Log.i(TAG, "Package not found on primary profile, skipping: " + pkg);
                continue;
            }

            // Install the existing package from the primary profile into this managed profile
            try {
                final boolean installed = mPolicies.invoke(
                        DevicePolicyManager::installExistingPackage, pkg);
                if (installed) {
                    successCount++;
                    Log.i(TAG, "Successfully auto-installed: " + pkg);
                } else {
                    Log.w(TAG, "installExistingPackage returned false for: " + pkg);
                }
            } catch (final Exception e) {
                Log.e(TAG, "Failed to auto-install package: " + pkg, e);
            }
        }

        if (successCount > 0) {
            final String message = successCount + " app" + (successCount != 1 ? "s" : "") + " installed to Island";
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);
        }
    }

    private static boolean isPackageInstalledOnPrimaryProfile(final LauncherApps launcherApps,
                                                              final String packageName) {
        try {
            launcherApps.getApplicationInfo(packageName, 0, Users.owner);
            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
