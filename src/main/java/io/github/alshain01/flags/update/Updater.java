package io.github.alshain01.flags.update;

/*
* Updater for Bukkit.
*
* This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
*/
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

final class Updater {
    private static final String PLUGIN_NAME = "Flags";
    private static final String[] NO_UPDATE_TAG = { "-ALPHA", "-BETA", "-SNAPSHOT" }; // If the version number contains one of these, don't update.

    private final Logger log;
    private final String version;
    private String versionName;

    private URL url; // Connecting to RSS
    private final Thread thread; // Updater thread
    private String apiKey = null; // BukkitDev ServerMods API key
    private UpdateResult result = UpdateResult.NO_UPDATE; // Used for determining the outcome of the update process

    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION,
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY,
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE
    }

    public Updater(String version, Logger log, String key) {
        this.version = version;
        this.log = log;

        if (key == null || key.equalsIgnoreCase("null") || key.equals("")) { key = null; }
        this.apiKey = key;

        try {
            this.url = new URL("https://api.curseforge.com/servermods/files?projectIds=65024");
        } catch (final MalformedURLException e) {
            log.severe("An error occured generating the update URL.");
        }

        this.thread = new Thread(new UpdateRunnable());
        this.thread.start();
    }

    /**
     * Get the result of the update process.
     */
    public Updater.UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    private void waitForThread() {
        if (this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be updated
     */
    private boolean versionCheck(String title) {
        if (title.split(" v").length == 2) {
            final String remoteVersion = title.split(" v")[1].split(" ")[0]; // Get the newest file's version number
            int remVer, curVer = 0;
            try {
                remVer = this.calVer(remoteVersion);
                curVer = this.calVer(version);
            } catch (final NumberFormatException nfe) {
                remVer = -1;
            }
            if (this.hasTag(version) || version.equalsIgnoreCase(remoteVersion) || (curVer >= remVer)) {
                // We already have the latest version, or this build is tagged for no-update
                this.result = Updater.UpdateResult.NO_UPDATE;
                return false;
            }
        } else {
            // The file's name did not contain the string 'vVersion'
            this.log.warning("The updater found a malformed file version. Please notify the author of this error.");
            this.result = Updater.UpdateResult.FAIL_NOVERSION;
        }
        return false;
    }

    /**
     * Used to calculate the version string as an Integer
     */
    private Integer calVer(String s) throws NumberFormatException {
        if (s.contains(".")) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                final Character c = s.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    sb.append(c);
                }
            }
            return Integer.parseInt(sb.toString());
        }
        return Integer.parseInt(s);
    }

    /**
     * Evaluate whether the version number is marked showing that it should not be updated by this program
     */
    private boolean hasTag(String version) {
        for (final String string : Updater.NO_UPDATE_TAG) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean read() {
        try {
            final URLConnection conn = this.url.openConnection();
            conn.setConnectTimeout(5000);

            if (this.apiKey != null) {
                conn.addRequestProperty("X-API-Key", this.apiKey);
            }
            conn.addRequestProperty("User-Agent", PLUGIN_NAME + " Updater");

            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONArray array = (JSONArray) JSONValue.parse(response);

            this.versionName = (String) ((JSONObject) array.get(array.size() - 1)).get("name");
            return true;
        } catch (final IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                this.log.warning("dev.bukkit.org rejected the API key provided in plugins/" + PLUGIN_NAME + "/config.yml");
                this.log.warning("Please double-check your configuration to ensure it is correct.");
                this.result = UpdateResult.FAIL_APIKEY;
            } else {
                this.log.warning("The updater could not contact dev.bukkit.org for updating.");
                this.log.warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                this.result = UpdateResult.FAIL_DBO;
            }
            //e.printStackTrace();
            return false;
        }
    }

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
            if (Updater.this.url != null && Updater.this.read() && Updater.this.versionCheck(Updater.this.versionName)) {
                Updater.this.result = UpdateResult.UPDATE_AVAILABLE;
            }
        }
    }
}