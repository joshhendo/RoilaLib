package org.roila;

import java.io.*;
import java.util.ArrayList;

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

    private ArrayList<String> VoiceLocations() throws Exception
    {
        ArrayList<String> locations = new ArrayList<String>();

        try
        {
            ProcessBuilder pb = new ProcessBuilder("find", "/", "-path", "*festival/voices/english");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null)
            {
                if (!line.endsWith("Permission denied"))
                {
                    locations.add(line);
                }
            }
        }
        catch (IOException e)
        {
            throw new Exception("Issue running find command");
        }

        return locations;
    }

    private String CheckVoice(String name) throws Exception
    {
        ArrayList<String> locations = VoiceLocations();

        for (String location : locations)
        {
            String path = location + "/" + name;
            if (new File(path).exists()) return path;
        }

        return null;
    }

    public void Say(String input) throws Exception
    {
        CheckInstall();
        Process p = Runtime.getRuntime().exec("festival");
        Writer w = new OutputStreamWriter(p.getOutputStream());
        w.append("(SayText \"" + input + "\")");
        w.flush();
    }

    public void SayAlt(String input) throws Exception
    {
        CheckInstall();
        ProcessBuilder pb = new ProcessBuilder("text2wave");
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
