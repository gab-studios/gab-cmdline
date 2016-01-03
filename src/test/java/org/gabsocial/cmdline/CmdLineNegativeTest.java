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
import java.util.Map;

import org.gabsocial.gabvalidate.ValidateException;
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
public class CmdLineNegativeTest
{
    
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
        
        try
        {
            
            this._cmdline
                    .defineCommand("file, !fileName, ?fileName, :file\\d.txt, #Load files into the system");
            
            Assert.fail();
        }
        catch (DuplicateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand2()
    {
        
        try
        {
            
            this._cmdline
                    .defineCommand("file, file, !fileName, :file\\d.txt, #Load files into the system");
            
            Assert.fail();
        }
        catch (DuplicateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand3()
    {
        
        try
        {
            
            this._cmdline
                    .defineCommand("file, !fileName, :file\\d.txt, :file\\d.txt, #Load files into the system");
            
            Assert.fail();
        }
        catch (DuplicateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand4()
    {
        
        try
        {
            
            this._cmdline
                    .defineCommand("file, !fileName, :file\\d.txt, #Load files into the system, #Load files into the system");
            
            Assert.fail();
        }
        catch (DuplicateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand5()
    {
        
        try
        {
            
            this._cmdline
                    .defineCommand("file !fileName :file\\d.txt #Load files into the system");
            
            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand6()
    {
        
        try
        {
            
            this._cmdline.defineCommand("file, !fileNames..., ?fileNames...");
            
            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
        
    }
    
    @Test
    public void testDefineCommand7()
    {
        
        try
        {
            
            this._cmdline.defineCommand("file, ?fileNames..., !fileNames2...");
            
            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand8()
    {

        this._cmdline
        .defineCommand("file, !file1, !file2");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try
        {
            this._cmdline.parse(args);

            Assert.fail();
        }
        catch (MissingException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand9()
    {

        this._cmdline
        .defineCommand("file, !file, !files...");

        final String[] args = new String[3];
        args[0] = "file";
        args[1] = "=";
        args[2] = "file1.txt";

        try
        {
            this._cmdline.parse(args);

            Assert.fail();
        }
        catch (MissingException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand10()
    {

        this._cmdline
        .defineCommand("file, !file");

        final String[] args = new String[3];
        args[0] = "install";
        args[1] = "=";
        args[2] = "file1.txt";

        try
        {
            this._cmdline.parse(args);

            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand11()
    {
        
        try
        {
            
            this._cmdline.defineCommand("file, ?fileName1, !fileNames2");
            
            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand12()
    {
        
        try
        {
            
            this._cmdline.defineCommand("");
            
            Assert.fail();
        }
        catch (ValidateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand13()
    {
        
        try
        {
            
            this._cmdline.defineCommand();
            
            Assert.fail();
        }
        catch (ValidateException e)
        {
            Assert.assertTrue(true);
        }
    }
    
    @Test
    public void testDefineCommand14()
    {
        
        try
        {
            
            this._cmdline.defineCommand("    ");
            
            Assert.fail();
        }
        catch (UnsupportedException e)
        {
            Assert.assertTrue(true);
        }
    }
    
}
