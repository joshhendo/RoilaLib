package org.roila;

import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.frontend.util.Microphone;

import java.util.concurrent.*;

public class Sphinx
{
    private Listen listener;
    private String CONFIG_LOCATION = "roila.config.xml";

    /**
     * Default constructor. Uses default configuration location of "roila.config.xml" in the current directory.
     */
    public Sphinx()
    {
        listener = new Listen(CONFIG_LOCATION);
    }

    /**
     * Constructor allowing you to specify a location for "roila.config.xml" as either a relative or absolute path.
     * @param location Specify the location of roila.config.xml as a relative or absolute path. Absolute path is not recommended in most situations.
     */
    public Sphinx(String location)
    {
        CONFIG_LOCATION = location;
        listener = new Listen(CONFIG_LOCATION);
    }

    /**
     * Allows you to change the location of roila.config.xml after this has been constructed.
     * @param location Specfiy the location of roila.config.xml as a relative or absolute path. Absolute path is not recommended in most situations.
     */
    public void SetLocation(String location)
    {
        CONFIG_LOCATION = location;
    }

    /**
     * This will listen until it understands a phrase or sentence. There is no timeout.
     * @return Text recognised.
     * @throws Exception
     */
    public String Listen() throws Exception {
        return Listen(-1, false);
    }

    /**
     * This will listen until it understands a phrase or sentence, or until it times out.
     * @param milliseconds Timeout in milliseconds. 1 second = 1000 milliseconds.
     * @return
     * @throws Exception
     */
    public String Listen(int milliseconds) throws Exception {
        return Listen(milliseconds, false);
    }

    /**
     * Initialise the Sphinx listening library. You may want to call this manually, but if you don't it will be called automatically when needed.
     * This is because it can take a few seconds to do so, and this gives you greater control over Sphinx (e.g. don't prompt to start speaking
     * until after initialisation.)
     * @return true if successful, otherwise false.
     * @throws Exception
     */
    public Boolean init() throws Exception
    {
        return listener.init();
    }

    // This will listen until it understands a sentence or until the timer runs out.

    /**
     * This will listen until it understands a phrase or sentence, or until it times out. Also allows you to output debug information.
     * @param milliseconds Timeout in milliseconds. 1 second = 1000 milliseconds.
     * @param debug Will output debug info to stdout. Not recommended for final product. Suggest having a constant called DEBUG that you can change to false.
     * @return
     * @throws Exception
     */
    public String Listen(int milliseconds, Boolean debug) throws Exception
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        listener.SetMilliseconds(milliseconds);
        listener.SetDebug(debug);
        Future<String> future = executor.submit(listener);

        String outputValue = null;

        try
        {
            if (milliseconds >= 0)
            {
                outputValue = future.get(milliseconds, TimeUnit.MILLISECONDS);
            }
            else
            {
                outputValue = future.get();
            }
        }
        catch (TimeoutException e)
        {
            if (debug) System.out.println("Timed out without reaching a value");
        }

        return outputValue;
    }
}

// see http://stackoverflow.com/questions/2275443/how-to-timeout-a-thread
class Listen implements Callable<String>
{
    String CONFIG_LOCATION;
    int milliseconds = -1;
    Boolean debug = false;

    Boolean init = false;
    Recognizer recognizer = null;


    public Listen(String location)
    {
        this.CONFIG_LOCATION = location;
    }

    public Listen(String location, int milliseconds, Boolean debug)
    {
        this.CONFIG_LOCATION = location;
        this.milliseconds = milliseconds;
        this.debug = debug;
    }

    public void SetMilliseconds(int milliseconds)
    {
        this.milliseconds = milliseconds;
    }

    public void SetDebug(Boolean debug)
    {
        this.debug = debug;
    }

    public Boolean IsReady()
    {
        return init;
    }

    public Boolean init() throws Exception
    {
        // Open the roila.config.xml file. The default is the directory that it is being run from,
        // however this can be overwritten using SetLocation(String);
        ConfigurationManager cm;
        cm = new ConfigurationManager(CONFIG_LOCATION);
        if (debug) System.out.println("Loading Recognizer...");
        recognizer = (Recognizer) cm.lookup("recognizer");
        recognizer.allocate();

        // Start the microphone or return null if it fails.
        Microphone microphone = (Microphone) cm.lookup("microphone");
        if (!microphone.startRecording())
        {
            if (debug) System.out.println("Cannot start microphone");
            recognizer.deallocate();
            throw new Exception("Cannot start Microphone");
        }

        init = true;

        return init;
    }

    @Override
    public String call() throws Exception
    {
        if (!init) init();

        while (true)
        {
            if (debug) System.out.println("Start speaking!\n");
            Result result = recognizer.recognize();
            if (result != null)
            {
                return result.getBestResultNoFiller();
            }
            else
            {
                continue;
            }
        }
    }
}
