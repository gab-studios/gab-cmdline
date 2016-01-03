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

package org.gabsocial.cmdline;

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
public class DefinedCommandTokenizerTest
{
    
    DefinedCommandTokenizer _tokenizer;
    
    @Before
    public void setUp()
    {
        this._tokenizer = new DefinedCommandTokenizer();
    }
    
    @After
    public void tearDown()
    {
        this._tokenizer = null;
    }
    
    @Test
    public void testTokenizer()
    { 
        final String inputString = "file , !fileName1,:file\\d.txt, #Load a files into the system";
        final String[] inputTokens = inputString.split("\\s*,\\s*");
        
        List<Token> tokens = this._tokenizer.tokenize(inputTokens);
        
        Assert.assertTrue(tokens.size() == 4);
        Assert.assertTrue(tokens.get(0).getValue().equals("file"));
        Assert.assertTrue(tokens.get(1).getValue().equals("fileName1"));
        Assert.assertTrue(tokens.get(2).getValue().equals("file\\d.txt"));
        Assert.assertTrue(tokens.get(3).getValue().equals("Load a files into the system"));
        
    }
}
