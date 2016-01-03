/*****************************************************************************************
 *
 * Copyright 2015 Gregory Brown. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *****************************************************************************************
 */

package org.gabsocial.cmdline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 *
 *
 * @author Gregory Brown (sysdevone)
 *
 */
public class CmdLineTest
{    
    private class CmdLineListener implements CommandListener
    {
        
        private final Map<String, Command> _commandMap = new HashMap<String, Command>();
        
        public Command getCommand(final String name)
        {
            return (this._commandMap.get(name));
        }
        
        public int getCount()
        {
            return (this._commandMap.size());
        }
        
        @Override
        public void handle(final Command command)
        {
            this._commandMap.put(command.getName(), command);
            
            // System.out.println( command );
        }
    }
    
    CmdLine _cmdline;
    
    public CmdLine getCmdLine()
    {
        return (this._cmdline);
    }
    
    @Before
    public void setUp()
    {
        this._cmdline = new CmdLine();
    }
    
    @After
    public void tearDown()
    {
        this._cmdline.close();
    }
    
    @Test
    public void testDefineCommand()
    {
        
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("file , !fileName1,:file\\d.txt,       #Load files into the system");
        
        final String[] args = new String[1];
        args[0] = "file=file1.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("file");
            Assert.assertTrue(command != null);
            
            final List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() > 0);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
        
    }
    
    @Test
    public void testDefineCommand1()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("file, !fileName1, :file\\d.txt, #Load a files into the system");
        
        final String[] args = new String[1];
        args[0] = "file = file1.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("file");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() > 0);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
        
    }
    
    @Test
    public void testDefineCommand1a()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system");
        
        final String[] args = new String[1];
        args[0] = "-f=file1.txt, file2.txt, file3.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            values = command.getValues("fileName2");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file2.txt"));
            
            values = command.getValues("fileName3");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file3.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1b()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system");
        
        final String[] args = new String[3];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() > 0);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1c()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("-f, --file, !fileName1, ?fileName2, ?fileName3, #Load a files into the system");
        
        final String[] args = new String[3];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1d()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("-f, --file, !fileName1, ?fileNames..., #Load a files into the system");
        
        final String[] args = new String[7];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";
        args[5] = ",";
        args[6] = "file3.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            values = command.getValues("fileNames");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 2);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file2.txt"));
            
            value = values.get(1);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file3.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1e()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline
                .defineCommand("-f, --file, !fileName1, ?fileNames..., #Load a files into the system, file");
        
        final String[] args = new String[7];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";
        args[5] = ",";
        args[6] = "file3.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("file");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("fileName1");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            values = command.getValues("fileNames");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 2);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file2.txt"));
            
            value = values.get(1);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file3.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1f()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.defineCommand("file, !file");
        
        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("file");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("file");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineCommand1g()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.defineCommand("file, !file, !files...");
        
        final String[] args = new String[5];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = ",";
        args[4] = "file2.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 1);
            
            final Command command = listener.getCommand("file");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("file");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineMultipleCommand1()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.setVersion("1.1.0");
        this._cmdline.defineCommand("-f", "--file", "!fileNames...",
                ":file\\d.txt", "#Load files into the system");
        this._cmdline.defineCommand("-l", "--list",
                "#List the files loaded into the system");
        this._cmdline.defineCommand("-q", "--quit", "#Quit the application");
        
        final String[] args = new String[4];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "--list";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 2);
            
            Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command.getValues("fileNames");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            command = listener.getCommand("--list");
            Assert.assertTrue(command != null);
            Assert.assertTrue(!command.hasVariables());
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testDefineMultipleCommand2()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener)
            .setVersion("1.1.0")
            .defineCommand("-f", "--file", "!fileNames...", ":file\\d.txt", "#Load files into the system")
            .defineCommand("-l", "--list", "#List the files loaded into the system")
            .defineCommand("-q", "--quit", "#Quit the application");
        
        final String[] args = new String[5];
        args[0] = "-f";
        args[1] = "=";
        args[2] = "file1.txt";
        args[3] = "-Dcom.gabsocial.cmdline.debug=true";
        args[4] = "--list";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 3);
            
            Command command = listener.getCommand("-f");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command.getValues("fileNames");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            command = listener.getCommand("-Dcom.gabsocial.cmdline.debug");
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            values = command.getValues("com.gabsocial.cmdline.debug");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            command = listener.getCommand("--list");
            Assert.assertTrue(command != null);
            Assert.assertTrue(!command.hasVariables());
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testSystemPropertyCommand1()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.setVersion("1.1.0");
        
        final String[] args = new String[1];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        
        try
        {
            this._cmdline.parse(args);
            
            final Command command = listener
                    .getCommand("-Dcom.gabsocial.cmdline.debug");
            
            Assert.assertTrue(listener.getCount() == 1);
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command
                    .getValues("com.gabsocial.cmdline.debug");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testSystemPropertyCommand2()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.setVersion("1.1.0");
        
        final String[] args = new String[4];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "-Dcom.gabsocial.cmdline.screen=true";
        args[2] = "-Dcom.gabsocial.cmdline.gfx=true";
        args[3] = "-Dcom.gabsocial.cmdline.load=true";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 4);
            
            final Command command = listener
                    .getCommand("-Dcom.gabsocial.cmdline.debug");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            final List<String> values = command
                    .getValues("com.gabsocial.cmdline.debug");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            final String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    };
    
    @Test
    public void testSystemPropertyCommand3()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.defineCommand("file, !file, !files..., :file\\d.txt");
        
        this._cmdline.setVersion("1.1.0");
        
        final String[] args = new String[4];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "file";
        args[2] = "file1.txt";
        args[3] = "file2.txt";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 2);
            
            Command command = listener
                    .getCommand("-Dcom.gabsocial.cmdline.debug");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command
                    .getValues("com.gabsocial.cmdline.debug");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            command = listener.getCommand("file");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            values = command.getValues("file");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            values = command.getValues("files");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file2.txt"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testSystemPropertyCommand4()
    {
        final CmdLineListener listener = new CmdLineListener();
        this._cmdline.setCommandListener(listener);
        
        this._cmdline.defineCommand("file, !file, !files..., :file\\d.txt");
        
        this._cmdline.setVersion("1.1.0");
        
        final String[] args = new String[5];
        args[0] = "-Dcom.gabsocial.cmdline.debug=true";
        args[1] = "file";
        args[2] = "file1.txt";
        args[3] = "file2.txt";
        args[4] = "-Dcom.gabsocial.cmdline.load=true";
        
        try
        {
            this._cmdline.parse(args);
            
            Assert.assertTrue(listener.getCount() == 3);
            
            Command command = listener
                    .getCommand("-Dcom.gabsocial.cmdline.debug");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            List<String> values = command
                    .getValues("com.gabsocial.cmdline.debug");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            String value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            command = listener.getCommand("file");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            values = command.getValues("file");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file1.txt"));
            
            values = command.getValues("files");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("file2.txt"));
            
            command = listener
                    .getCommand("-Dcom.gabsocial.cmdline.load");
            
            Assert.assertTrue(command != null);
            Assert.assertTrue(command.hasVariables());
            
            values = command.getValues("com.gabsocial.cmdline.load");
            Assert.assertTrue(values != null);
            Assert.assertTrue(values.size() == 1);
            
            value = values.get(0);
            Assert.assertTrue(value != null);
            Assert.assertTrue(value.length() > 0);
            Assert.assertTrue(value.equals("true"));
            
            Assert.assertTrue(true);
        }
        catch (final Exception e)
        {
            Assert.fail(e.toString());
        }
    }
}
