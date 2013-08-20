package com.diabol.pipefitter;

import hudson.Plugin;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PluginImpl extends Plugin
{
    @Override
    public void start() throws Exception
    {
        System.out.println("Hello, World");
    }
}
