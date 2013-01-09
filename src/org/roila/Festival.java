package org.roila;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Festival
{
    // http://stackoverflow.com/questions/2439984/how-to-check-if-a-program-is-installed-on-system
    private Boolean CheckInstall() throws Exception {
        try
        {
            Runtime.getRuntime().exec("festival");
        }
        catch (IOException e)
        {
            throw new Exception("Festival not installed");
        }

        return true;
    }

    public void Say(String input) throws Exception
    {
        CheckInstall();
        Process p = Runtime.getRuntime().exec("festival");
        Writer w = new OutputStreamWriter(p.getOutputStream());
        w.append("(SayText \"" + input + "\")");
        w.flush();
    }

    public void Save(String input) throws Exception
    {
        CheckInstall();

    }

    public Boolean IsAvailable()
    {
        try
        {
            CheckInstall();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
