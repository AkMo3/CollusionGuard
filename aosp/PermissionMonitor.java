package android.security;

import android.content.Context;
import android.content.Intent;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.SystemProperties;
import android.Manifest;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Objects;

public class PermissionMonitor {
    private static PermissionMonitor instance;
    private static Context context = null;
    private PackageManager packageManager;

    // REGEX - PERMISSION
    private final HashMap<Pattern, String> dataPermissionMap; 


    private final List<Pattern> ALL_REGEX_PATTERNS;
    private final static int REQUESTED_PERMISSION_GRANTED = PackageInfo.REQUESTED_PERMISSION_GRANTED;
    private static final String TAG = "PermissionMonitor";
    private static final String TRANSACTION_INTENT_ACTION = "com.akmo.collusionguard.action.RECORD_ACTIVITY";

    private PermissionMonitor(PackageManager packageManager) {
        this.packageManager = packageManager; 
        dataPermissionMap = new HashMap<Pattern, String>();
        ALL_REGEX_PATTERNS = new ArrayList<>();
        for (PermissionMap pm: PermissionMap.values()) {
            Pattern pattern = Pattern.compile(pm.getPermissionRegex());
            ALL_REGEX_PATTERNS.add(pattern);
            dataPermissionMap.put(pattern, pm.manifestPermission);
        }
    }

    /**
     * @hide
     * @param packageManager Instance of package manager to be used for permission validation.
     * @return Instance of class PermissionMonitor.
     */
    public static @NonNull PermissionMonitor getInstance(@NonNull PackageManager packageManager) {
        if (instance == null) instance = new PermissionMonitor(packageManager);
        return instance;
    }

    /**
     * Returns instance of this.
     * @hide
     */
    public static @Nullable PermissionMonitor getInstance() {
        return instance;
    }

    /**
     * @hide
     * @param data Data to be validated against a permission.
     * @param callingPackage Package sending the data.
     * @param receivingPackage Packages receiving the data.
     * @return true, if of unathorised transaction takes place.
     */
    public boolean validateTransaction(@NonNull String data, @NonNull String callingPackage, @NonNull String receivingPackage) {
        Log.d("SAFEMODE", "Permission Monitor, Sender: " + callingPackage + ", Data: " + data + ", Receiver: " + receivingPackage);
        if (SystemProperties.getBoolean("sys.boot_completed", false) && context != null) {
            if (callingPackage != "android") sendBroadcastToApp(callingPackage, data, receivingPackage);
        }

        checkPermissionForPackages("akmo");
        Pattern patternThatMatches = null;
        for (Pattern pattern: ALL_REGEX_PATTERNS) {
            Matcher matcher = pattern.matcher(data);
            if (matcher.matches()) patternThatMatches = pattern;
        }

        if (Objects.isNull(patternThatMatches)) return false;
        String permission = dataPermissionMap.get(patternThatMatches);
        int hasRequiredPermission = packageManager.checkPermission(permission, receivingPackage);
        boolean result = hasRequiredPermission == PackageManager.PERMISSION_DENIED;
        
        if (result) {
            Log.d("BTP", "Unathorised Sensitive Permission transacted by " + callingPackage  + " To " + receivingPackage + 
                " For Data " + data + " That matches with permission " + permission);
        }
        return result;
    }

    private void checkPermissionForPackages(String packageName) {
        for (final PackageInfo pi: packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (!pi.packageName.contains(packageName)) continue;
            // Log.d(TAG, "For package: " + pi.packageName);
            final String[] requestedPermissions = pi.requestedPermissions;
            if (requestedPermissions == null) {
                // No permissions defined in <manifest>
                Log.d(TAG, "No permission defined for " + pi.packageName);
            }
            int len = requestedPermissions.length;
            // Loop each <uses-permission> tag to retrieve the permission flag
            for (int i = 0; i < len; i++) {
                final String requestedPerm = requestedPermissions[i];
                // Retrieve the protection level for each requested permission
                int protLevel;
                try {
                    protLevel = packageManager.getPermissionInfo(requestedPerm, 0).protectionLevel;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Unknown permission: " + requestedPerm, e);
                    continue;
                }
                // final boolean system = protLevel == PROTECTION_SIGNATURE;
                // final boolean dangerous = protLevel == PROTECTION_DANGERO>US;
                final boolean granted = (pi.requestedPermissionsFlags[i] & REQUESTED_PERMISSION_GRANTED) != 0;
                Log.d(TAG, "Permission Status: " + requestedPerm + " granted: " + granted);
            }
        }
    }

    /**
     * @hide
     * @param data Data to be validated against a permission.
     * @param callingPackage Package sending the data.
     * @param receivingPackages List of Packages receiving the data.
     * @return true, if of unathorised transaction takes place.
     */
    public boolean validateTransaction(@NonNull Context context, @NonNull String data, @NonNull String callingPackage,@NonNull List<String> receivingPackages) {
        if (this.context == null) this.context = context;

        if (SystemProperties.getBoolean("sys.boot_completed", false) && (callingPackage != "android")) {
            for (String receivingPackage: receivingPackages) {
                sendBroadcastToApp(callingPackage, data, receivingPackage);
            }
        }

        Pattern patternThatMatches = null;
        for (Pattern pattern: ALL_REGEX_PATTERNS) {
            Matcher matcher = pattern.matcher(data);
            if (matcher.matches()) patternThatMatches = pattern;
            if (callingPackage.contains("akmo")) {
                Log.d("BTP", "For data " + data + "match result for " + pattern.pattern() + 
                " is " + matcher.matches());
            }
        }


        if (Objects.isNull(patternThatMatches)) return false;
        String permission = dataPermissionMap.get(patternThatMatches);
        boolean result = false;
        for (String receivingPackage: receivingPackages) {
            int hasRequiredPermission = packageManager.checkPermission(permission, receivingPackage);
            result = hasRequiredPermission == PackageManager.PERMISSION_DENIED;
            if (result) {
                Log.d("BTP", "Unathorised Sensitive Permission transacted by " + callingPackage);
                break;
            }
        }
        
        return result;
    }

    private void sendBroadcastToApp(String callingPackage, String data, String receivingPackage) {
        Log.d("SAFEMODE", "Permission Monitor, Sender: " + callingPackage + ", Data: " + data + ", Receiver: " + receivingPackage);
        Intent transactionDataIntent = new Intent();
        transactionDataIntent.setAction(TRANSACTION_INTENT_ACTION);
        transactionDataIntent.putExtra("com.akmo.collusionguard.extra.SENDER", callingPackage);
        transactionDataIntent.putExtra("com.akmo.collusionguard.extra.RECEIVER", receivingPackage);
        transactionDataIntent.putExtra("com.akmo.collusionguard.extra.DATA", data);
        this.context.sendBroadcast(transactionDataIntent);
    }

    
    private enum PermissionMap {
        LOCATION_PATTERN_1("^\\S*\\d{1,2}\\.\\d{1,15},\\s?-?\\d{1,3}\\.\\d{1,15}\\S*$",
                Manifest.permission.ACCESS_BACKGROUND_LOCATION),
        LOCATION_PATTERN_2("^Latitude:(-?\\d+(\\.\\d+)?),\\s?Longitude:(-?\\d+(\\.\\d+)?)$",
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);


        public final String permissionRegex;
        public final String manifestPermission;

        private PermissionMap(String regex, String permission) {
            this.permissionRegex = regex;
            this.manifestPermission = permission;
        }

        public String getManifestPermission() {
            return this.manifestPermission;
        }

        public String getPermissionRegex() {
            return this.permissionRegex;
        }
    }
    
}
