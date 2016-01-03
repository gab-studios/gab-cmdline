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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gabsocial.cmdline.Token.Type;
import org.gabsocial.gablogging.LogProvider;
import org.gabsocial.gabvalidate.Validate;


/**
 * This class is the main command line parser.
 *
 * Steps to use parser. 1. Instantiate the parser.
 * 
 * CmdLine cmdline = new CmdLine();
 * 
 * 2. set you command listener.
 * 
 * cmdline.setListener( myListener );
 * 
 * 3. define your command definitions.
 * 
 * cmdline.defineCommand("-l, --load, !fileName, #Load a files into the system");
 *        .defineCommand("-s, --save, #Save the application");
 *        .defineCommand("-q, --quit, #Quit the application");
 * 
 * 4. parse the command line arguments.
 * 
 * cmdline.parse( args );
 * 
 * 5. close parser to release resources.
 * 
 * cmdline.close();
 * 
 * @see setCommandListener
 * @see defineCommand
 * @see parse
 *
 * @author Gregory Brown (sysdevone)
 *
 */
public class CmdLine
{
    private static final int               MAX_LENGTH    = 256;
    
    private static final String            REGEX_PATTERN = "\\s*,\\s*";
    private CommandLineTokenizer           _commandLineTokenizer;
    private CommandListener                _commandListener;
    private Map<String, CommandDefinition> _commandMap;
    private DefinedCommandTokenizer        _definedCommandTokenizer;
    private Set<String>                    _variableNameSet;
    private String                         _applicationName;
    private String                         _version;
    
    /**
     * The CmdLine constructor.
     */
    public CmdLine()
    {
        this._commandMap = new HashMap<String, CommandDefinition>();
        this._variableNameSet = new HashSet<String>();
        this._definedCommandTokenizer = new DefinedCommandTokenizer();
        this._commandLineTokenizer = new CommandLineTokenizer();
    }
    
    /*
     * Adds variable name to existing set. If the name already exists, then the
     * DuplicateException is thrown.
     */
    private void addVariableName(final String name)
    {
        assert (name != null && name.length() > 0) : "The parameter 'name' must not be null or empty";
        assert (name.length() <= MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
                + MAX_LENGTH;
        
        if (!this._variableNameSet.add(name)) { throw (new DuplicateException(
                "Error: The variable '"
                        + name
                        + "' has already been defined.  Define a new variable name.")); }
    }
    
    /**
     * Sets the application name in the cmdline. To be used in the help menu
     * - (future release).
     * 
     * @param name
     *            The name of the application.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine setApplicationName(final String name)
    {
        Validate.defineString(name).testNotNullEmpty()
                .testMaxLength(MAX_LENGTH).throwExceptionOnFailedValidation()
                .validate();
        
        this._applicationName = name;
        return (this);
    }
    
    /**
     * Closes the CmdLine and releases resources.
     */
    public void close()
    {
        // FIXME - add a close flag. if it is closed then public method should
        // lock down.
        
        this._commandMap.clear();
        this._commandMap = null;
        
        this._variableNameSet.clear();
        this._variableNameSet = null;
        
        this._definedCommandTokenizer = null;
        this._commandLineTokenizer = null;
    }
    
    /*
     * Creates the Command if a CommandDefinition exists.
     */
    private Command createCommand(final String commandName,
            final List<String> tokens)
    {
        
        assert (commandName != null && commandName.length() > 0) : "The parameter 'commandName' must not be null or empty";
        assert (commandName.length() <= MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
                + MAX_LENGTH;
        
        assert (tokens != null) : "The parameter 'tokens' must not be null";
        assert (tokens.size() <= MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
                + MAX_LENGTH;
        
        final Command command = new Command(commandName);
        if (!tokens.isEmpty())
        {
            final CommandDefinition commandDefinition = this._commandMap
                    .get(commandName);
            
            final String regex = commandDefinition.getRegexValue();
            Pattern pattern = null;
            if ((regex != null) && (regex.length() > 0))
            {
                pattern = Pattern.compile(regex);
            }
            
            if (commandDefinition.hasRequiredVariables())
            {
                final List<String> names = commandDefinition
                        .getRequiredVariableNames();
                this.processVariable(pattern, tokens, names, command, true);
            }
            
            if (commandDefinition.hasRequiredVariableLists())
            {
                final String name = commandDefinition
                        .getRequiredVariableListName();
                this.processVariableList(pattern, tokens, name, command, true);
            }
            
            if (commandDefinition.hasOptionalVariables())
            {
                final List<String> names = commandDefinition
                        .getOptionalVariableNames();
                this.processVariable(pattern, tokens, names, command, false);
            }
            
            if (commandDefinition.hasOptionalVariableLists())
            {
                final String name = commandDefinition
                        .getOptionalVariableListName();
                this.processVariableList(pattern, tokens, name, command, false);
            }
        }
        return (command);
    }
    
    /*
     * Creates a CommandDefinition.
     */
    private CommandDefinition createCommandDefinition(final List<Token> tokens)
    {
        
        assert (tokens != null && tokens.size() > 0) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
                + MAX_LENGTH;
        
        final CommandDefinition command = new CommandDefinition();
        
        // a list counter. Only one list can exist.
        int varListCount = 0;
        
        // a flag to mark if an optional var was created. If this is true and an
        // attempt to create a required var is made, then an exception will be
        // thrown.
        boolean isOptionalVarDefined = false;
        for (final Token token : tokens)
        {
            
            final Type type = token.getType();
            final String name = token.getValue();
            switch (type)
            {
                case COMMAND:
                {
                    if (name.contains(" "))
                    {
                        throw (new UnsupportedException(
                                "Error: The command name '"
                                        + name
                                        + "' contains spaces which is not supported.  The definition may need a comma."));
                    }
                    else
                    {
                        command.addName(name);
                    }
                    break;
                }
                case DESCRIPTION:
                {
                    
                    final String description = command.getDescription();
                    if ((description != null) && (description.length() > 0))
                    {
                        throw (new DuplicateException(
                                "Error: The description '" + name
                                        + "' has already been defined."));
                    }
                    else
                    {
                        command.setDescription(name);
                    }
                    
                    break;
                }
                case REGEX_VALUE:
                {
                    final String existingRegex = command.getRegexValue();
                    if ((existingRegex != null) && (existingRegex.length() > 0))
                    {
                        throw (new DuplicateException("Error: The regex '"
                                + name + "' has already been defined."));
                    }
                    else
                    {
                        command.setRegexValue(name);
                    }
                    break;
                }
                case REQUIRED_VALUE:
                {
                    if (isOptionalVarDefined)
                    {
                        throw (new UnsupportedException(
                                "Error: An optional variable has already been defined before this required variable.  Required variables must be defined before optional variables.'"));
                    }
                    else
                    {
                        this.addVariableName(name);
                        command.addRequiredVariable(name);
                    }
                    break;
                }
                case REQUIRED_LIST_VALUE:
                {
                    if (isOptionalVarDefined)
                    {
                        throw (new UnsupportedException(
                                "Error: An optional variable has already been defined before this required variable.  Required variables must be defined before optional variables.'"));
                    }
                    else if (varListCount > 0)
                    {
                        throw (new UnsupportedException(
                                "Error: A List has already been defined for '"
                                        + name
                                        + "'.  A command can only have one list defined. "));
                    }
                    else
                    {
                        ++varListCount;
                        this.addVariableName(name);
                        command.setRequiredVariableList(name);
                    }
                    break;
                }
                case OPTIONAL_VALUE:
                {
                    this.addVariableName(name);
                    command.addOptionalVariable(name);
                    isOptionalVarDefined = true;
                    break;
                }
                case OPTIONAL_LIST_VALUE:
                {
                    if (varListCount > 0)
                    {
                        throw (new UnsupportedException(
                                "Error: A List has already been defined for '"
                                        + name
                                        + "'.  A command can only have one list defined. "));
                    }
                    else
                    {
                        ++varListCount;
                        this.addVariableName(name);
                        command.setOptionalVariableList(name);
                        isOptionalVarDefined = true;
                    }
                    break;
                }
                default:
                {
                    throw (new UnsupportedException("Error:  Unknown token '"
                            + name + "' is an unknown type ='" + type.name()
                            + "')."));
                }
            }
        }
        
        return (command);
    }
    
    /**
     * This method defines the command definitions expected in the parser. Call
     * this method for each command that will be defined.
     *
     * A token must use one of these symbols in order for it to be recognized as
     * that type:
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
     * ... = A value ends with ... and is a list for the command name. There can
     * be zero to one defined. This can be used with the ! and ? symbols
     *
     * If a token does not start with one of these tokens, then it is considered
     * a command name.
     *
     * Exmaples:
     *
     * "file, !fileName1, :file\\d.txt, #Load a files into the system"
     * "-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system"
     * "-f, --file, !fileName1, ?fileNames..., #Load a files into the system"
     *
     * @param nameArgs
     *            An array of String containing values.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine defineCommand(final String... nameArgs)
    {
        Validate.defineBoolean(
                nameArgs != null && nameArgs.length > 0
                        && nameArgs.length <= MAX_LENGTH).testTrue()
                .throwExceptionOnFailedValidation().validate();
        
        final List<Token> tokens = this._definedCommandTokenizer
                .tokenize(nameArgs);
        
        final CommandDefinition command = this.createCommandDefinition(tokens);
        final List<String> names = command.getNames();
        
        for (final String name : names)
        {
            final CommandDefinition existingCommand = this._commandMap.put(
                    name, command);
            if (existingCommand != null) { throw (new DuplicateException(
                    "Error: The command '"
                            + name
                            + "' has already been defined.  Define a new command name.")); }
        }
        
        // System.out.println(command);
        return (this);
    }
    
    /**
     * This method defines the command definitions expected in the parser. Call
     * this method for each command that will be defined.
     *
     * A token must use one of these symbols in order for it to be recognized as
     * that type:
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
     * ... = A value ends with ... and is a list for the command name. There can
     * be zero to one defined. This can be used with the ! and ? symbols
     *
     * If a token does not start with one of these tokens, then it is considered
     * a command name.
     *
     * Exmaples:
     *
     * "file, !fileName1, :file\\d.txt, #Load a files into the system"
     * "-f, --file, !fileName1, ?fileName2, ?fileName3, :file\\d.txt, #Load a files into the system"
     * "-f, --file, !fileName1, ?fileNames..., #Load a files into the system"
     *
     * @param nameArgs
     *            A comma delimited String containing values.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine defineCommand(final String nameArgs)
    {
        Validate.defineString(nameArgs).testNotNullEmpty()
                .testMaxLength(MAX_LENGTH).throwExceptionOnFailedValidation()
                .validate();
        
        final String[] nameArgTokens = nameArgs.split(CmdLine.REGEX_PATTERN);
        this.defineCommand(nameArgTokens);
        return (this);
    }
    
    /**
     * Gets the version String that was defined.
     * 
     * @return A String. May be null or empty if the version was not defined.
     */
    public String getVersion()
    {
        return (this._version);
    }
    
    /**
     * Gets the application name that was defined.
     * 
     * @return A String. May be null or empty if the application name was not
     *         defined.
     */
    public String getApplicationName()
    {
        return (this._applicationName);
    }
    
    /**
     * Parse the command line arguments.
     *
     * @param args
     *            The arguments from the command line.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine parse(final String[] args)
    {
        Validate.defineBoolean(
                args != null && args.length > 0 && args.length <= MAX_LENGTH)
                .testTrue().throwExceptionOnFailedValidation().validate();
        
        final List<String> tokens = this.tokenizeCmdLineArgs(args);
        this.processCmdLineTokens(tokens);
        return (this);
    }
    
    /*
     * Processes the String tokens and creates Command.
     */
    private void processCmdLineTokens(final List<String> tokens)
    {
        
        assert (tokens != null && tokens.size() > 0) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
                + MAX_LENGTH;
        
        String tokenValue = tokens.remove(0);
        
        // System.out.println( "Processing token: " + tokenValue );
        
        // check to see that a command defintion exists for the current token.
        if (this._commandMap.containsKey(tokenValue))
        {
            // if defined, then create a command.
            final Command command = this.createCommand(tokenValue, tokens);
            
            // if the listener was set, then notify the listener of the created
            // command.
            if (this._commandListener != null)
            {
                this._commandListener.handle(command);
            }
            
            // Have all tokens been consumed?
            if (tokens.size() > 0)
            {
                // Reclusive call and process the remaining
                // tokens.
                this.processCmdLineTokens(tokens);
            }
            
        }
        else
        {
            // Process -D<property>=<value> if it exists.
            while (this.processSystemProperty(tokenValue, tokens))
            {
                if (tokens.size() == 0)
                {
                    break;
                }
                else
                {
                    tokenValue = tokens.remove(0);
                }
            }
            
            // Have all tokens been consumed?
            if (tokens.size() > 0)
            {
                // Check to see if token is a defined command.
                // if not defined, then the token is not supported.
                if (!this._commandMap.containsKey(tokenValue))
                {
                    throw (new UnsupportedException("Error: The command name '"
                            + tokenValue + "' is not defined."));
                }
                else
                {
                    // if the token is supported, reclusive call and process the
                    // remaining
                    // tokens.
                    
                    // add the token that was previously removed back to the
                    // token list since it was not processed.
                    tokens.add(0, tokenValue);
                    this.processCmdLineTokens(tokens);
                }
            }
        }
    }
    
    /*
     * Processes the -D<property>=<value> and adds it to the System property.
     */
    private boolean processSystemProperty(final String valueString,
            final List<String> tokens)
    {
        
        boolean isSystemPropertyProcessed = false;
        if ((valueString != null) && (tokens != null) && (tokens.size() > 0))
        {
            final int indexOfSystemProperty = valueString.indexOf("-D");
            
            if (indexOfSystemProperty > -1)
            {
                final String systemPropertyKey = valueString
                        .substring(indexOfSystemProperty + 2);
                
                final String systemPropertyValue = tokens.remove(0);
                
                LogProvider
                        .getProvider()
                        .getService()
                        .logDetail(
                                this.getClass(),
                                "parseSystemProperty",
                                "Setting System Property: " + systemPropertyKey
                                        + "=" + systemPropertyValue);
                
                isSystemPropertyProcessed = true;
                System.setProperty(systemPropertyKey, systemPropertyValue);
                
                final Command command = new Command(valueString);
                command.addVariable(systemPropertyKey, systemPropertyValue);
                
                if (this._commandListener != null)
                {
                    this._commandListener.handle(command);
                }
            }
        }
        return (isSystemPropertyProcessed);
    }
    
    /*
     * Process the required and optional variables that are associated with a
     * command.
     */
    private void processVariable(final Pattern pattern,
            final List<String> tokens, final List<String> definedVariableNames,
            final Command command, final boolean required)
    {
        
        // pattern can be null.
        
        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
                + MAX_LENGTH;
        
        assert (definedVariableNames != null) : "The parameter 'definedVariableNames' must not be null.";
        assert (definedVariableNames.size() <= MAX_LENGTH) : "The parameter 'definedVariableNames' must be less than or equal to "
                + MAX_LENGTH;
        
        assert (command != null) : "The parameter 'command' must not be null.";
        
        for (final String varName : definedVariableNames)
        {
            // A varName must not start with a space, otherwise an exception is
            // thrown.
            if (varName.contains(" "))
            {
                throw (new UnsupportedException(
                        "Error: The variable name '"
                                + varName
                                + "' contains spaces which is not supported.  The definition may need a comma."));
            }
            else if ((tokens.size() == 0) && !required)
            {
                // if there isnt any info from the command line and this
                // variable is not required then break and exit.
                break;
            }
            else if ((tokens.size() == 0) && required)
            {
                // if there isnt any info from the command line but this
                // variable is required then throw exception.
                throw (new MissingException(
                        "Error:  The value for the required variable '"
                                + varName + "' is missing."));
            }
            else
            {
                
                final String argToken = tokens.remove(0);
                
                // System.out.println("processVariable: " + name + " : "
                // + argToken + " = " + _variableNameSet);
                
                boolean isMatch = true;
                if (pattern != null)
                {
                    final Matcher matcher = pattern.matcher(argToken);
                    isMatch = matcher.matches();
                    if (!isMatch) { throw (new MatchException(
                            "Error:  The value '" + argToken
                                    + "' does not match the expected pattern '"
                                    + pattern.toString() + "'.")); }
                }
                
                if (this._variableNameSet.contains(varName))
                {
                    command.addVariable(varName, argToken);
                }
            }
        }
    }
    
    /*
     * Process the required and optional variable lists that are associated with
     * a command.
     */
    private void processVariableList(final Pattern pattern,
            final List<String> tokens, final String varName,
            final Command command, final boolean required)
    {
        // pattern can be null.
        
        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
                + MAX_LENGTH;
        
        assert (varName != null && varName.length() > 0) : "The parameter 'varName' must not be null or empty.";
        assert (varName.length() <= MAX_LENGTH) : "The parameter 'varName' must be less than or equal to "
                + MAX_LENGTH;
        
        assert (command != null) : "The parameter 'command' must not be null.";
        
        if (varName.contains(" "))
        {
            throw (new UnsupportedException(
                    "Error: The variable name '"
                            + varName
                            + "' contains spaces which is not supported.  The definition may need a comma."));
        }
        else if ((tokens.size() == 0) && required)
        {
            // if there isnt any info from the command line but this
            // variable is required then throw exception.
            throw (new MissingException(
                    "Error:  The value for the required variable '" + varName
                            + "' is missing."));
        }
        else
        {
            while (!tokens.isEmpty()
                    && !this._commandMap.containsKey(tokens.get(0)))
            {
                
                final String argToken = tokens.remove(0);
                
                final boolean processedSystemProperty = this
                        .processSystemProperty(argToken, tokens);
                
                if (!processedSystemProperty
                        && this._variableNameSet.contains(varName))
                {
                    
                    boolean isMatch = true;
                    if (pattern != null)
                    {
                        final Matcher matcher = pattern.matcher(argToken);
                        isMatch = matcher.matches();
                        if (!isMatch) { throw (new MatchException(
                                "Error:  The value '"
                                        + argToken
                                        + "' does not match the expected pattern '"
                                        + pattern.toString() + "'.")); }
                    }
                    
                    command.addVariable(varName, argToken);
                    
                }
            }
        }
    }
    
    /**
     * Sets the listener that will handle the Commands that are created by the
     * parser.
     * 
     * @param commandListener
     *            A listener that will handle the callbacks.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine setCommandListener(final CommandListener commandListener)
    {
        Validate.defineObject(commandListener).testNotNull()
                .throwExceptionOnFailedValidation().validate();
        
        this._commandListener = commandListener;
        return( this );
    }
    
    /**
     * The version number of the application using the cmdline. To be used in the help menu
     * - (future release).
     * 
     * @param version
     *            A String value. Must not be null or empty.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public CmdLine setVersion(final String version)
    {
        Validate.defineString(version).testNotNullEmpty()
                .throwExceptionOnFailedValidation().validate();
        
        this._version = version;
        return (this);
    }
    
    /*
     * Converts the command line args.into String tokens.
     */
    private List<String> tokenizeCmdLineArgs(final String[] args)
    {
        assert (args != null && args.length > 0) : "The parameter 'args' must not be null or empty";
        assert (args.length <= MAX_LENGTH) : "The parameter 'args' must be less than or equal to "
                + MAX_LENGTH;
        
        final List<String> tokens = this._commandLineTokenizer.tokenize(args);
        return (tokens);
    }
}
