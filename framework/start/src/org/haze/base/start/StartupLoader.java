package org.haze.base.start;

/**
 * An object that loads server startup classes.
 * <p>
 * When Haze starts, the main thread will create the <code>StartupLoader</code> instance and
 * then call the loader's <code>load</code> method. If the method returns without
 * throwing an exception the loader will be added to a list of initialized loaders.
 * After all instances have been created and initialized, the main thread will call the
 * <code>start</code> method of each loader in the list. When Haze shuts down, a
 * separate shutdown thread will call the <code>unload</code> method of each loader.
 * Implementations should anticipate asynchronous calls to the methods by different
 * threads.
 * </p>
 * 
 */
public interface StartupLoader {

    /**
     * Load a startup class.
     *
     * @param config Startup config.
     * @param args Command-line arguments.
     * @throws StartupException If an error was encountered. Throwing this exception
     * will halt loader loading, so it should be thrown only when Haze can't
     * operate without it.
     */
    public void load(Config config, String args[]) throws StartupException;

    /**
     * Start the startup class. This method must not block - implementations
     * that require thread blocking must create a separate thread and then return.
     * 
     * @throws StartupException If an error was encountered.
     */
    public void start() throws StartupException;

    /**
     * Stop the startup class. This method must not block.
     *
     * @throws StartupException If an error was encountered.
     */
    public void unload() throws StartupException;
}
