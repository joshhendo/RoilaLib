package org.roila;

import java.io.*;
import java.util.ArrayList;

public class Festival
{
    String current_voice = null;

    // http://stackoverflow.com/questions/2439984/how-to-check-if-a-program-is-installed-on-system

    /**
     * Checks to see if "festival" is installed.
     * @return true if festival is installed, otherwise false.
     */
    private Boolean CheckInstall() {
        try
        {
            Runtime.getRuntime().exec("festival");
        }
        catch (IOException e)
        {
            //throw new Exception("Festival not installed");
            return false;
        }

        return true;
    }

    /**
     * Generates an ArrayList<String> of locations where voices could be instaled.
     * @return ArrayList<String> of voices. If no voices are found this will be null.
     * @throws Exception if it has an issue running the "find" command on the Linux system.
     */
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

    /**
     * Checks a voice is installed in an appropriate directory. The paramater is case sensitive.
     * @param name The name of the folder of the voice to check. This is case sensitive.
     * @return The first identified path of the voice, otherwise null.
     * @throws Exception if it has an issue running VoiceLocations()
     */
    public String CheckVoice(String name) throws Exception
    {
        ArrayList<String> locations = VoiceLocations();

        for (String location : locations)
        {
            String path = location + "/" + name;
            if (new File(path).exists()) return path;
        }

        return null;
    }

    /**
     * Sets the current voice to a particular voice name.
     * @param voice name of the folder that the voice is contained in.
     * @return true if the voice is successfully changed, otherwise false.
     */
    private boolean SetVoice(String voice)
    {
        String voice_location = null;
        try
        {
            voice_location = CheckVoice(voice);
        }
        catch (Exception e)
        {
            return false;
        }

        if (voice_location == null) return false;

        File file = new File(voice_location);

        if (file.exists())
        {
            current_voice = voice;
            return true;
        }

        return false;
    }

    /**
     * Public method using SetVoice to change to the male voice.
     */
    public void SetMale()
    {
        SetVoice("roila_diphone");
    }

    /**
     * Public method using SetVoice to change to the female voice.
     */
    public void SetFemale()
    {
        SetVoice("roila_female");
    }

    /**
     * Public mehtod to clear any set voice and use the default.
     */
    public void ClearVoice()
    {
        current_voice = null;
    }

    /**
     * Say something using festival. Will use the most recently set voice.
     * @param input Sentence or series of words to say.
     * @throws Exception
     */
    public void Say(String input) throws Exception
    {
        CheckInstall();
        Process p = Runtime.getRuntime().exec("festival");
        Writer w = new OutputStreamWriter(p.getOutputStream());
        if (current_voice != null)
        {
            w.append("(voice_" + current_voice + ")");
            System.out.println("(voice_" + current_voice + ")");
            //w.flush();
        }
        w.append("(SayText \"" + input + "\")");
        System.out.println("(SayText \"" + input + "\")");
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

    /**
     * Public version of CheckInstall.
     * @return true if festival is available on the system, otherwise false;
     */
    public Boolean IsAvailable()
    {
        return CheckInstall();
    }
}
