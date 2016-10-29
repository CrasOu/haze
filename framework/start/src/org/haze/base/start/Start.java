package org.haze.base.start;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Start - Haze Container(s) Startup Class
 *
 */
public class Start {

    private enum Control {
        SHUTDOWN {
            void processRequest(Start start, PrintWriter writer) {
                if (start.serverState.get() == ServerState.STOPPING) {
                    writer.println("IN-PROGRESS");
                } else {
                    writer.println("OK");
                    writer.flush();
                    start.stopServer();
                }
            }
        }, STATUS {
            void processRequest(Start start, PrintWriter writer) {
                writer.println(start.serverState.get());
            }
        }, FAIL {
            void processRequest(Start start, PrintWriter writer) {
                writer.println("FAIL");
            }
        };

        abstract void processRequest(Start start, PrintWriter writer);
    }

    private static void help(PrintStream out) {
        out.println("");
        out.println("Usage: java -jar haze.jar [command] [arguments]");
        out.println("-both    -----> Run simultaneously the POS (Point of Sales) application and Haze standard");
        out.println("-help, -? ----> This screen");
        out.println("-install -----> Run install (create tables/load data)");
        out.println("-pos     -----> Run the POS (Point of Sales) application");
        out.println("-setup -------> Run external application server setup");
        out.println("-start -------> Start the server");
        out.println("-status ------> Status of the server");
        out.println("-shutdown ----> Shutdown the server");
        out.println("-test --------> Run the JUnit test script");
        out.println("[no config] --> Use default config");
        out.println("[no command] -> Start the server w/ default config");
    }

    private enum Command {
        HELP, HELP_ERROR, STATUS, SHUTDOWN, COMMAND
    }

    private static Command checkCommand(Command command, Command wanted) {
        if (wanted == Command.HELP || wanted.equals(command)) {
            return wanted;
        } else if (command == null) {
            return wanted;
        } else {
            System.err.println("Duplicate command detected(was " + command + ", wanted " + wanted);
            return Command.HELP_ERROR;
        }
    }

    public static void main(String[] args) throws StartupException {
        Command command = null;
        List<String> loaderArgs = new ArrayList<String>(args.length);
        for (String arg: args) {
            if (arg.equals("-help") || arg.equals("-?")) {
                command = checkCommand(command, Command.HELP);
            } else if (arg.equals("-status")) {
                command = checkCommand(command, Command.STATUS);
            } else if (arg.equals("-shutdown")) {
                command = checkCommand(command, Command.SHUTDOWN);
            } else if (arg.startsWith("-")) {
                command = checkCommand(command, Command.COMMAND);
                loaderArgs.add(arg.substring(1));
            } else {
                command = checkCommand(command, Command.COMMAND);
                if (command == Command.COMMAND) {
                    loaderArgs.add(arg);
                } else {
                    command = Command.HELP_ERROR;
                }
            }
        }
        if (command == null) {
            command = Command.COMMAND;
            loaderArgs.add("start");
        }
        if (command == Command.HELP) {
            help(System.out);
            return;
        } else if (command == Command.HELP_ERROR) {
            help(System.err);
            System.exit(1);
        }
        Start start = new Start();
        start.init(args, command == Command.COMMAND);
        try {
            if (command == Command.STATUS) {
                System.out.println("Current Status : " + start.status());
            } else if (command == Command.SHUTDOWN) {
                System.out.println("Shutting down server : " + start.shutdown());
            } else {
                // general start
                start.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(99);
        }
    }

    private enum ServerState {
        STARTING, RUNNING, STOPPING;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    private Config config = null;
    private List<String> loaderArgs = new ArrayList<String>();
    private final ArrayList<StartupLoader> loaders = new ArrayList<StartupLoader>();
    private AtomicReference<ServerState> serverState = new AtomicReference<ServerState>(ServerState.STARTING);
    private Thread adminPortThread = null;

    private void createListenerThread() throws StartupException {
        if (config.adminPort > 0) {
            this.adminPortThread = new AdminPortThread();
            this.adminPortThread.start();
        } else {
            System.out.println("Admin socket not configured; set to port 0");
        }
    }

    private void createLogDirectory() {
        File logDir = new File(config.logDir);
        if (!logDir.exists()) {
            if (logDir.mkdir()) {
                System.out.println("Created Haze log dir [" + logDir.getAbsolutePath() + "]");
            }
        }
    }

    public void init(String[] args) throws StartupException {
        init(args, true);
    }

    public void init(String[] args, boolean fullInit) throws StartupException {
        String globalSystemPropsFileName = System.getProperty("haze.system.props");
        if (globalSystemPropsFileName != null) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(globalSystemPropsFileName);
                System.getProperties().load(stream);
            } catch (IOException e) {
                throw (StartupException) new StartupException("Couldn't load global system props").initCause(e);
            } finally {
                if (stream != null){
                    try {
                        stream.close();
                    } catch (IOException e) {
                        throw (StartupException) new StartupException("Couldn't close stream").initCause(e);
                    }
                }
            }
        }
        try {
            this.config = Config.getInstance(args);
        } catch (IOException e) {
            throw (StartupException) new StartupException("Couldn't not fetch config instance").initCause(e);
        }
        // parse the startup arguments
        if (args.length > 1) {
            this.loaderArgs.addAll(Arrays.asList(args).subList(1, args.length));
        }

        if (!fullInit) {
            return;
        }
        // initialize the classpath
        initClasspath();

        // create the log directory
        createLogDirectory();

        // create the listener thread
        createListenerThread();

        // set the shutdown hook
        if (config.useShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { shutdownServer(); } });
        } else {
            System.out.println("Shutdown hook disabled");
        }

        // initialize the startup loaders
        initStartLoaders();
    }

    private void initClasspath() throws StartupException {
        Classpath classPath = new Classpath(System.getProperty("java.class.path"));
        try {
            this.config.initClasspath(classPath);
        } catch (IOException e) {
            throw (StartupException) new StartupException("Couldn't initialized classpath").initCause(e);
        }
        // Set the classpath/classloader
        System.setProperty("java.class.path", classPath.toString());
        ClassLoader classloader = classPath.getClassLoader();
        Thread.currentThread().setContextClassLoader(classloader);
        if (System.getProperty("DEBUG") != null) {
            System.out.println("Startup Classloader: " + classloader.toString());
            System.out.println("Startup Classpath: " + classPath.toString());
        }
    }

    private void initStartLoaders() throws StartupException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        synchronized (this.loaders) {
            // initialize the loaders
            for (String loaderClassName: config.loaders) {
                if (this.serverState.get() == ServerState.STOPPING) {
                    return;
                }
                try {
                    Class<?> loaderClass = classloader.loadClass(loaderClassName);
                    StartupLoader loader = (StartupLoader) loaderClass.newInstance();
                    loader.load(config, loaderArgs.toArray(new String[loaderArgs.size()]));
                    loaders.add(loader);
                } catch (ClassNotFoundException e) {
                    throw (StartupException) new StartupException(e.getMessage()).initCause(e);
                } catch (InstantiationException e) {
                    throw (StartupException) new StartupException(e.getMessage()).initCause(e);
                } catch (IllegalAccessException e) {
                    throw (StartupException) new StartupException(e.getMessage()).initCause(e);
                }
            }
            this.loaders.trimToSize();
        }
        return;
    }

    private String sendSocketCommand(Control control) throws IOException, ConnectException {
        String response = "Haze is Down";

        try {
        Socket socket = new Socket(config.adminAddress, config.adminPort);

        // send the command
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(config.adminKey + ":" + control);
        writer.flush();

        // read the reply
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = reader.readLine();

        reader.close();

        // close the socket
        writer.close();
        socket.close();

        } catch (ConnectException e) {
            System.out.println("Could not connect to " + config.adminAddress + ":" + config.adminPort);
        }
        return response;
    }

    public String shutdown() throws IOException {
        return sendSocketCommand(Control.SHUTDOWN);
    }

    private void shutdownServer() {
        ServerState currentState;
        do {
            currentState = this.serverState.get();
            if (currentState == ServerState.STOPPING) {
                return;
            }
        } while (!this.serverState.compareAndSet(currentState, ServerState.STOPPING));
        // The current thread was the one that successfully changed the state;
        // continue with further processing.
        synchronized (this.loaders) {
            // Unload in reverse order
            for (int i = this.loaders.size(); i > 0; i--) {
                StartupLoader loader = this.loaders.get(i - 1);
                try {
                    loader.unload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.adminPortThread != null && this.adminPortThread.isAlive()) {
            this.adminPortThread.interrupt();
        }
    }

    // org.apache.commons.daemon.Daemon.start()
    public void start() throws Exception {
        if (!startStartLoaders()) {
            if (this.serverState.get() == ServerState.STOPPING) {
                return;
            } else {
                throw new Exception("Error during start.");
            }
        }
        if (config.shutdownAfterLoad) {
            stopServer();
        }
    }

    /**
     * Returns <code>true</code> if all loaders were started.
     * 
     * @return <code>true</code> if all loaders were started.
     */
    private boolean startStartLoaders() {
        synchronized (this.loaders) {
            // start the loaders
            for (StartupLoader loader: this.loaders) {
                if (this.serverState.get() == ServerState.STOPPING) {
                    return false;
                }
                try {
                    loader.start();
                } catch (StartupException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return this.serverState.compareAndSet(ServerState.STARTING, ServerState.RUNNING);
    }

    public String status() throws IOException {
        try {
            return sendSocketCommand(Control.STATUS);
        } catch (ConnectException e) {
            return "Not Running";
        } catch (IOException e) {
            throw e;
        }
    }

    public void stopServer() {
        shutdownServer();
        System.exit(0);
    }

    // org.apache.commons.daemon.Daemon.destroy()
    public void destroy() {
        // FIXME: undo init() calls.
    }

    // org.apache.commons.daemon.Daemon.stop()
    public void stop() {
        shutdownServer();
    }

    private class AdminPortThread extends Thread {
        private ServerSocket serverSocket = null;

        AdminPortThread() throws StartupException {
            super("AdminPortThread");
            try {
                this.serverSocket = new ServerSocket(config.adminPort, 1, config.adminAddress);
            } catch (IOException e) {
                throw (StartupException) new StartupException("Couldn't create server socket(" + config.adminAddress + ":" + config.adminPort + ")").initCause(e);
            }
            setDaemon(false);
        }

        private void processClientRequest(Socket client) throws IOException {
            BufferedReader reader = null;
            PrintWriter writer = null;
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String request = reader.readLine();
                writer = new PrintWriter(client.getOutputStream(), true);
                Control control;
                if (request != null && !request.isEmpty() && request.contains(":")) {
                    String key = request.substring(0, request.indexOf(':'));
                    if (key.equals(config.adminKey)) {
                        control = Control.valueOf(request.substring(request.indexOf(':') + 1));
                        if (control == null) {
                            control = Control.FAIL;
                        }
                    } else {
                        control = Control.FAIL;
                    }
                } else {
                    control = Control.FAIL;
                }
                control.processRequest(Start.this, writer);
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
        }

        @Override
        public void run() {
            System.out.println("Admin socket configured on - " + config.adminAddress + ":" + config.adminPort);
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Received connection from - " + clientSocket.getInetAddress() + " : " + clientSocket.getPort());
                    processClientRequest(clientSocket);
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
