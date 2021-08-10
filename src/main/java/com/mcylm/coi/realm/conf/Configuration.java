package com.mcylm.coi.realm.conf;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class Configuration {

    /**
     * Create a config from the given inputstream and file. Creates file if it
     * does not exist
     *
     * @param stream
     *            InputStream to write to file if it does not exist
     * @param file
     *            File object for configuration
     * @return
     */
    public static FileConfiguration createConfig(InputStream stream, File file) {

        if (!file.exists()) {

            file.getParentFile().mkdirs();
            copy(stream, file);

        }
        return YamlConfiguration.loadConfiguration(file);

    }

    /**
     * Create a config based on a resource file contained in the plugin
     *
     * @param plugin
     *            Main class of plugin possessing the resource file
     * @param resource
     *            String filename of resource inside the plugin
     * @param file
     *            File object for configuration
     * @return
     */
    public static FileConfiguration createConfig(JavaPlugin plugin, String resource,
                                                 File file) {

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            copy(plugin.getResource(resource), file);
        }
        return YamlConfiguration.loadConfiguration(file);

    }

    /**
     * Create a config based on a resource file contained in the plugin
     *
     * @param plugin
     *            Main class of plugin possessing the resource file
     * @param resource
     *            String filename of resource inside the plugin
     * @param filename
     *            Name of file to create
     * @return
     */
    public static FileConfiguration createConfig(JavaPlugin plugin, String resource,
                                                 String filename) {

        File file = new File(filename);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            copy(plugin.getResource(resource), file);
        }
        return YamlConfiguration.loadConfiguration(file);

    }

    /**
     * Create FileConfiguration from the given file
     *
     * @param file
     *            File to load from config
     * @return
     * @throws FileNotFoundException
     *             If file does not exist
     * @throws InvalidConfigurationException
     *             If there is an invalid configuration
     */
    public static FileConfiguration createConfig(File file){
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Create a folder in the given location
     * @param location Parent location
     * @param name Folder/Directory name
     * @return
     */
    public static File mkdirs(File location, String name) {
        File dir = new File(location, name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static void copy(InputStream in, File file) {

        try {

            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);

            }
            out.close();
            in.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}