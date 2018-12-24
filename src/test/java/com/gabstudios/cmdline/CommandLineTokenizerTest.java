/*****************************************************************************************
 *
 * Copyright 2016 Gregory Brown. All Rights Reserved.
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

package com.gabstudios.cmdline;

import java.util.List;

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
public class CommandLineTokenizerTest
{

    CommandLineTokenizer _tokenizer;

    @Before
    public void setUp()
    {
        this._tokenizer = new CommandLineTokenizer();
    }

    @After
    public void tearDown()
    {
        this._tokenizer = null;
    }

    @Test
    public void testTokenizer()
    {
        // file=file1.txt
        final String[] inputTokens =
            { "file=file1.txt" };

        final List<String> tokens = this._tokenizer.tokenize(inputTokens);

        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).equals("file"));
        Assert.assertTrue(tokens.get(1).equals("file1.txt"));

    }

    @Test
    public void testTokenizer2()
    {
        // -file=file1,txt, file2.txt
        final String[] inputTokens =
            { "-file=file1.txt,", "file2.txt" };

        final List<String> tokens = this._tokenizer.tokenize(inputTokens);

        Assert.assertTrue(tokens.size() == 3);
        Assert.assertTrue(tokens.get(0).equals("-file"));
        Assert.assertTrue(tokens.get(1).equals("file1.txt"));
        Assert.assertTrue(tokens.get(2).equals("file2.txt"));

    }

    @Test
    public void testTokenizer3()
    {
        // -file=file1,txt, file2.txt -Dorg.gabsocial.cmdline.debug=true
        final String[] inputTokens =
            { "-file", "file1.txt", "file2.txt",
            "-Dorg.gabsocial.cmdline.debug=true" };

        final List<String> tokens = this._tokenizer.tokenize(inputTokens);

        Assert.assertTrue(tokens.size() == 5);
        Assert.assertTrue(tokens.get(0).equals("-file"));
        Assert.assertTrue(tokens.get(1).equals("file1.txt"));
        Assert.assertTrue(tokens.get(2).equals("file2.txt"));
        Assert.assertTrue(tokens.get(3).equals("-Dorg.gabsocial.cmdline.debug"));
        Assert.assertTrue(tokens.get(4).equals("true"));
    }
}
