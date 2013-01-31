package org.roila;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * org.roila Copyright 2013
 * User: Joshua
 * Date: 30/01/13
 * Time: 2:27 PM
 */
public class test
{
    public static void main(String [ ] args)
        {
            Sphinx mySphinx = new Sphinx();
            Festival myFestival = new Festival();

            System.out.println("Welcome to the ROILA test program.\n\n");
            System.out.println("Say 'Fosit Koloke'");

            String said = "";

            int count = 0;
            while (!said.toLowerCase().equals("fosit koloke") && count++ < 5)
            {
                try
                {
                    said = mySphinx.Listen();
                }
                catch (Exception e)
                {
                    System.out.println("An error occurred whilst trying to listen. Can you check a microphone is plugged in?");
                    e.printStackTrace();
                    break;
                }

                System.out.println("What I heard: " + said);
            }

            Boolean festival_continue = true;
            if (!myFestival.IsAvailable())
            {
                System.out.println("Festival doesn't seem to be installed on your system. Please make sure it is installed. Festival will only work on Linux.");
                festival_continue = false;
            }

            try
            {
                if (festival_continue && myFestival.CheckVoice("roila_diphone") == null)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                System.out.println("The roila_diphone voice cannot be found. Please enusre this is installed properly.");
                festival_continue = false;
            }

            if (festival_continue)
            {
                System.out.println("Please ensure that the following phrases sound correct. Please rate on a scale of 1 to 5 to how well it sounds, with 1 being cannot understand and 5 being perfect. Type 0 if you do not hear anything.");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                String[] phrases = {"fosit koloke", "fosit kipupi", "fosit jimeja", "bobuja", "bama buse fosit", "fosit nole", "fosit webufo", "fosit besati"};
                ArrayList<Integer> rankings = new ArrayList<Integer>();

                try
                {
                    for (int i = 0; i < phrases.length; i ++)
                    {
                        System.out.println(phrases[i] + ": ");
                        myFestival.Say(phrases[i]);

                        Integer ranking = null;
                        do
                        {
                            ranking = Integer.parseInt(br.readLine());
                            if (ranking == null || ranking < 0 || ranking > 5) System.out.println("not a valid ranking! Please try again.");
                        }
                        while (ranking == null || ranking < 0 || ranking > 5);

                        rankings.set(i, ranking);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }



            System.out.println("\n\nEnd. Thank you for taking part!\n");
            System.out.println("If you could please copy and paste the output from this and email it to 17019428@student.uws.edu.au");
            System.out.println("That would be a great help.");

        }
}
