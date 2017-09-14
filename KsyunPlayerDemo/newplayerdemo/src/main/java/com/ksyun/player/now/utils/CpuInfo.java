package com.ksyun.player.now.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CpuInfo {
    static final String USER_CPU_REGEX = "((?i)(use?r\\s+[0-9]{1,2}(\\.[0-9]{1,2})?\\%))|([0-9]{1,2}\\.[0-9]{1,2}\\%\\s+(?i)(use?r))";
    static final String SYSTEM_CPU_REGEX = "((?i)(sys(tem)?\\s+[0-9]{1,2}(\\.[0-9]{1,2})?\\%))|([0-9]{1,2}\\.[0-9]{1,2}\\%\\s+(?i)(sys(tem)?))";

    private String mPackageName;
    private String m_sTopResults;

    /**
     * Store the user CPU usage
     */
    private	float			m_fUserCpuUsage;
    /**
     * Store the system CPU usage
     */
    private float			m_fSystemCpuUsage;
    /**
     * Store the idle CPU
     */
    private float			m_fIdleCpuUsage;

    private String m_sProcessCpuUsage;

    public CpuInfo() {
        m_sTopResults = null;
        m_fIdleCpuUsage = 0f;
        m_fSystemCpuUsage = 0f;
        m_fUserCpuUsage = 0f;
    }

    private void getCPUInfo()
    {
        BufferedReader ifp = null;

        //* empty the raw results buffer
        this.m_sTopResults = "";

        try
        {
            //* execute the top command
            Process process = null;
            process = Runtime.getRuntime().exec("top -n 1 -d 1");
            //* we only want to retrieve the top few lines

            //* read the output from the command
            String sLine = new String();
            ifp = new BufferedReader(new InputStreamReader(process.getInputStream()));

            //* Read all the available output and store it in the class member
            while ( (sLine = ifp.readLine()) != null)
            {
                if(sLine.indexOf(mPackageName) >= 0) {
                    this.m_sTopResults = sLine;
                    break;
                }
            }
        } catch (IOException exp) {

        }
        finally
        {
            //* we need to close the stream once everything is done.
            try {
                if (ifp != null)
                    ifp.close();
            }
            catch (IOException exp)
            {

            }
        }
    }

    public void parseTopResults(String name)
    {
        String sUserCpuUsage		= null;
        String sSystemCpuUsage		= null;

        Pattern xRegexSearchPattern 	= null;
        Matcher xSearch  				= null;

        mPackageName = name;

        getCPUInfo();

        //* Check to make sure we have some data to parse
        if (this.m_sTopResults == null)
        {
            return;
        }

        String[] result = m_sTopResults.split(" ");
        if(result != null && result.length > 0) {
            for(String idx : result) {
                if(idx.indexOf("%") > 0)
                {
                    m_sProcessCpuUsage = idx;
                    break;
                }
            }
        }
    }

    public String summaryString() {
        String sSummary = "";

        sSummary += "CPU Information: \n";
        sSummary += "User CPU utilized: " + this.m_fUserCpuUsage + "%\n";
        sSummary += "System CPU utilized: " + this.m_fSystemCpuUsage + "%\n";
        sSummary += "Idle CPU: " + this.m_fIdleCpuUsage + "%\n";

        return sSummary;
    }

    /**
     * Return the percent of the CPU utilized by the system processes.
     * @return The percentage of CPU utilized by system processes.
     */
    public float getSystemUsage()
    {
        return this.m_fSystemCpuUsage;
    }


    /**
     * Return the percent of the CPU that is being utilized by user processes.
     * @return The percentage of CPU utilized by user processes.
     */
    public float getUserUsage()
    {
        return this.m_fUserCpuUsage;
    }

    /**
     * Return the percentage of the CPU that is idle
     * @return The percentage of idle CPU
     */
    public float getIdle()
    {
        return this.m_fIdleCpuUsage;
    }

    public String getProcessCpuUsage() { return this.m_sProcessCpuUsage; }
}
