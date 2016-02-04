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


/*
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
public class CommandLineTokenizer
{
    
    /*
     * Constructor.
     */
    protected CommandLineTokenizer()
    {
        // void do nothing.
    }
    
    /*
     * Tokenize the command line arguments.
     * 
     * @param args An array of String
     * 
     * @return A new List<String> instance.
     */
    protected List<String> tokenize(final String[] args)
    {
        assert( args != null && args.length > 0 ) : "The parameter 'args' must not be null or empty";
        
        //TODO - add assert for max length.
        
        final LogService logService = LogProvider.getProvider().getService();
        
        // process spaces - taken care of by command line.
        final LinkedList<String> tokenList = new LinkedList<String>();
        
        //
        // Loop through all of the args.
        // ---------------------------
        for( String argString : args )
        {
            
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
            for( String argAfterEqual :argsAfterEquals)
            {
                
                logService.logDetail(this.getClass(), "tokenize",
                        "Before comma split: " + argAfterEqual);
                
                //
                // Tokenize the values using the COMMA as a delimiter and process.
                // ---------------------------
                final String[] argsAfterCommas = argAfterEqual.split(",");
                
                logService.logDetail(this.getClass(), "tokenize",
                        "After comma split length: " + argsAfterCommas.length);
                
                //
                // Process tokens after using COMMA as a delimiter.
                // ---------------------------
                for( String argAfterComma : argsAfterCommas)
                {
                    // Add tokens within the commas.
                    // values
                    if (argAfterComma.length() > 0)
                    {
                        tokenList.add(argAfterComma.trim());
                    }
                }
                
                //
                // After processing args for commas, add the previous token if it is not a comma. 
                // ---------------------------
                if (argsAfterCommas.length == 0)
                {
                    if (argAfterEqual.equals(","))
                    {
                        // ignore.
                    }
                    else if (argAfterEqual.length() > 0)
                    {
                        tokenList.add(argAfterEqual.trim());
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
                    // values if no equals found.
                    // is the value a java system property key: -DsetSomthing?
                    //
                    tokenList.add(argString.trim());
                    
                }
            }
            
        }
        
        return (tokenList);
    }
}
