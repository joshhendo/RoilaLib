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

    public Sphinx()
    {
        listener = new Listen(CONFIG_LOCATION);
    }

    public Sphinx(String location)
    {
        CONFIG_LOCATION = location;
        listener = new Listen(CONFIG_LOCATION);
    }


    public void SetLocation(String location)
    {
        CONFIG_LOCATION = location;
    }

    // This will listen until it understands a sentence with no time limit
    public String Listen() throws Exception {
        return Listen(-1, false);
    }

    public String Listen(int milliseconds) throws Exception {
        return Listen(milliseconds, false);
    }

    public Boolean init() throws Exception
    {
        return listener.init();
    }

    // This will listen until it understands a sentence or until the timer runs out.
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
