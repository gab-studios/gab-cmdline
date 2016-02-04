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

import java.util.LinkedList;
import java.util.List;

import org.gabsocial.gablogging.LogProvider;
import org.gabsocial.gablogging.LogService;


/**
 * This class is used to tokenize the values from the command line.
 * 
 * This parser is able to handle equals, commas and a value.
 * 
 * The order of priority is: equals, commas and value.
 * 
 * It will produce a List of String values.
 *
 * @author Gregory Brown (sysdevone)
 *
 */
public class DefinedCommandTokenizer
{
    /*
     * Constructor.
     */
    protected DefinedCommandTokenizer()
    {
        // void - do nothing.
    }
    
    /*
     * Creates a token based on the first char.
     * 
     * A String must use one of these symbols in order for it to be recognized
     * as that type:
     * 
     * # = The description of the command. There may be zero to one defined.
     * 
     * ! = A required value for the command name. There can be zero to many
     * defined.
     * 
     * ? = An optional value for the command name. There can be zero to many
     * defined.
     * 
     * : = The regex value to match on for any values that are defined. There
     * can be zero to one defined.
     * 
     * 
     * @param inputString
     * @param tokenList
     */
    private static void createToken(final String inputString,
            final List<Token> tokenList)
    {
        
        assert( inputString != null && inputString.length() > 0 ) : "The parameter 'inputString' must not be null or empty";
        assert( tokenList != null) : "The parameter 'tokenList' must not be null";
        //TODO - add assert for max length.
        
        if (inputString.startsWith("#"))
        {
            tokenList.add(new Token(Token.Type.DESCRIPTION, inputString
                    .substring(1)));
        }
        else if (inputString.startsWith("!") || inputString.startsWith("?"))
        {
            DefinedCommandTokenizer.handleListValue(inputString, tokenList);
        }
        else if (inputString.startsWith(":"))
        {
            tokenList.add(new Token(Token.Type.REGEX_VALUE, inputString
                    .substring(1)));
        }
        else
        {
            tokenList.add(new Token(Token.Type.COMMAND, inputString));
        }
    }
    
    /*
     * Creates a token based on the first char.
     * 
     * A String must use one of these symbols in order for it to be recognized
     * as that type:
     * 
     * ! = A required value for the command name. There can be zero to many
     * defined.
     * 
     * ? = An optional value for the command name. There can be zero to many
     * defined.
     * 
     * ... = A value ends with ... and is a list for the command name. There can
     * be zero to one defined. This can be used with the ! and ? symbols
     * 
     * 
     * @param inputString
     * @param tokenList
     */
    
    private static void handleListValue(final String inputString,
            final List<Token> tokenList)
    {
        
        assert( inputString != null && inputString.length() > 0 ) : "The parameter 'inputString' must not be null or empty";
        assert( tokenList != null) : "The parameter 'tokenList' must not be null";
        //TODO - add assert for max length.
        
        if (inputString.startsWith("!") && inputString.endsWith("..."))
        {
            int length = inputString.length() - 3;
            tokenList.add(new Token(Token.Type.REQUIRED_LIST_VALUE, inputString
                    .substring(1, length)));
        }
        else if (inputString.startsWith("!"))
        {
            tokenList.add(new Token(Token.Type.REQUIRED_VALUE, inputString
                    .substring(1)));
        }
        else if (inputString.startsWith("?") && inputString.endsWith("..."))
        {
            int length = inputString.length() - 3;
            tokenList.add(new Token(Token.Type.OPTIONAL_LIST_VALUE, inputString
                    .substring(1, length)));
        }
        else if (inputString.startsWith("?"))
        {
            tokenList.add(new Token(Token.Type.OPTIONAL_VALUE, inputString
                    .substring(1)));
        }
    }
    
    /*
     * Tokenize the values that were defined.
     * 
     * @param args
     *            An array of String containing one to many values.
     * @return A List<Token> instance.  May return an empty List if input is empty or nothing is processed.
     */
    protected List<Token> tokenize(final String[] args)
    {
        //assert( args != null && args.length > 0 ) : "The parameter 'args' must not be null or empty";
        //TODO - add assert for max length.
        
        // process spaces - taken care of by command line.
        final LinkedList<Token> tokenList = new LinkedList<Token>();
        
        if (args != null && args.length > 0)
        {
            final LogService logService = LogProvider.getProvider()
                    .getService();
            
            //
            // Tokenize the values using the EQUALS as a delimiter and process.
            // ---------------------------
            for (String argString : args)
            {
                // equals
                logService.logDetail(this.getClass(), "tokenize",
                        "Before equals split: " + argString);
                
                //
                // Tokenize the values using the EQUALS as a delimiter and process.
                // ---------------------------
                final String[] argsAfterEquals = argString.split("=");
                
                logService.logDetail(this.getClass(), "tokenize",
                        "After equals split length: " + argsAfterEquals.length);
                
                //
                // Process tokens after using EQUALS as a delimiter.
                // ---------------------------
                for (String argAfterEquals : argsAfterEquals)
                {
                    
                    // commas
                    
                    logService.logDetail(this.getClass(), "tokenize",
                            "Before comma split: " + argAfterEquals);
                    
                    //
                    // Tokenize the values using the COMMA as a delimiter and process.
                    // ---------------------------
                    final String[] argsAfterCommas = argAfterEquals.split(",");
                    
                    logService.logDetail(this.getClass(), "tokenize",
                            "After comma split length: "
                                    + argsAfterCommas.length);
                    //
                    // Process tokens after using COMMA as a delimiter.
                    // ---------------------------
                    for (String argAfterComma : argsAfterCommas)
                    {
                        
                        // values
                        if (argAfterComma.length() > 0)
                        {
                            DefinedCommandTokenizer.createToken(argAfterComma,
                                    tokenList);
                        }
                    }
                    //
                    // After processing args for commas, add the previous token if it is not a comma. 
                    // ---------------------------
                    if (argsAfterCommas.length == 0)
                    {
                        if (argAfterEquals.equals(","))
                        {
                            // ignore.
                        }
                        else if (argAfterEquals.length() > 0)
                        {
                            DefinedCommandTokenizer.createToken(argAfterEquals,
                                    tokenList);
                        }
                    }
                    
                }
                
                //
                // After processing args for equals, add the previous token if it is not a equal. 
                // ---------------------------
                if (argsAfterEquals.length == 0)
                {
                    if (argString.equals("="))
                    {
                        // ignore.
                    }
                    else if (argString.length() > 0)
                    {
                        tokenList.add(new Token(Token.Type.COMMAND, argString));
                    }
                }
                
            }
        }
        return (tokenList);
    }
}
