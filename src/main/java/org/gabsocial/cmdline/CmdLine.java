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
 * Steps to use parser.
 *
 * 1. Define your command definitions.
 *
 * CmdLine.defineCommand("-l, --load, !fileName, #Load a files into the system")
 * .defineCommand("-s, --save, #Save the application");
 * .defineCommand("-q, --quit, #Quit the application");
 *
 * 2. parse the command line arguments and assign a listener for command
 * definitions
 *
 * CmdLine.parse( args, listener );
 *
 * 3. clear parser to release resources.
 *
 * CmdLine.clear();
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
    /*
     * The maximum length allowed for any size - String, tokens, etc.
     */
    private static final int                      MAX_LENGTH    = 256;

    /*
     * Regex to split the define command method
     */
    private static final String                   REGEX_PATTERN = "\\s*,\\s*";

    /*
     * The application name.
     */
    private static String                         s_applicationName;

    /*
     * The command line tokenizer
     */
    private static CommandLineTokenizer           s_commandLineTokenizer;

    /*
     * The listener that will handle commands as they are processed, if it is
     * set. May be 0 to 1.
     */
    private static CommandListener                s_commandListener;

    /*
     * A map that holds the key of a command string and a value of a command
     * definition.
     */
    private static Map<String, CommandDefinition> s_commandMap;

    /*
     * The tokenizer that handles the defineCommand(xxxx) method.
     */
    private static DefinedCommandTokenizer        s_definedCommandTokenizer;

    /*
     * Support method chaining.
     */
    private static CmdLine                        s_instance;

    /*
     * Holds the variable names assigned to commands. Variable names are unique
     * across commands. One a variable is used by a command, another command
     * *may not use* that same variable name.
     */
    private static Set<String>                    s_variableNameSet;

    /*
     * The application version.
     */
    private static String                         s_version;

    /**
     * The CmdLine constructor.
     */
    static
    {
        CmdLine.s_commandMap = new HashMap<String, CommandDefinition>();
        CmdLine.s_variableNameSet = new HashSet<String>();
        CmdLine.s_definedCommandTokenizer = new DefinedCommandTokenizer();
        CmdLine.s_commandLineTokenizer = new CommandLineTokenizer();
        CmdLine.s_instance = new CmdLine();
    }

    /*
     * Adds variable name to existing set. If the name already exists, then the
     * DuplicateException is thrown.
     */
    private static void addVariableName(final String name)
    {
        assert ((name != null) && (name.length() > 0)) : "The parameter 'name' must not be null or empty";
        assert (name.length() <= CmdLine.MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        if (!CmdLine.s_variableNameSet.add(name)) { throw (new DuplicateException(
                "Error: The variable '"
                        + name
                        + "' has already been defined.  Define a new variable name.")); }
    }

    /**
     * Clears the CmdLine and releases resources.
     *
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine clear()
    {

        CmdLine.s_commandMap.clear();
        CmdLine.s_variableNameSet.clear();
        CmdLine.s_commandListener = null;

        return (CmdLine.s_instance);
    }

    /*
     * Creates the Command if a CommandDefinition exists.
     */
    private static Command createCommand(final String commandName,
            final List<String> tokens)
    {

        assert ((commandName != null) && (commandName.length() > 0)) : "The parameter 'commandName' must not be null or empty";
        assert (commandName.length() <= CmdLine.MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        assert (tokens != null) : "The parameter 'tokens' must not be null";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        final Command command = new Command(commandName);
        if (!tokens.isEmpty())
        {
            final CommandDefinition commandDefinition = CmdLine.s_commandMap
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
                CmdLine.processVariable(pattern, tokens, names, command, true);
            }

            if (commandDefinition.hasRequiredVariableLists())
            {
                final String name = commandDefinition
                        .getRequiredVariableListName();
                CmdLine.processVariableList(pattern, tokens, name, command,
                        true);
            }

            if (commandDefinition.hasOptionalVariables())
            {
                final List<String> names = commandDefinition
                        .getOptionalVariableNames();
                CmdLine.processVariable(pattern, tokens, names, command, false);
            }

            if (commandDefinition.hasOptionalVariableLists())
            {
                final String name = commandDefinition
                        .getOptionalVariableListName();
                CmdLine.processVariableList(pattern, tokens, name, command,
                        false);
            }
        }
        return (command);
    }

    /*
     * Creates a CommandDefinition.
     */
    private static CommandDefinition createCommandDefinition(
            final List<Token> tokens)
    {

        assert ((tokens != null) && (tokens.size() > 0)) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : "The parameter 'name' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        final CommandDefinition command = new CommandDefinition();

        // a list counter. Only one list can exist.
        int xorListCount = 0;

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
                                        + "' contains spaces which is not supported.  "
                                        + "The definition may need a comma."));
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
                                "Error: An optional variable has already been defined before this required variable.  "
                                + "Required variables must be defined before optional variables.'"));
                    }
                    else
                    {
                        CmdLine.addVariableName(name);
                        command.addRequiredVariable(name);
                    }
                    break;
                }
                case REQUIRED_LIST_VALUE:
                {
                    if (isOptionalVarDefined)
                    {
                        throw (new UnsupportedException(
                                "Error: An optional variable has already been defined before this required variable.  "
                                + "Required variables must be defined before optional variables.'"));
                    }
                    else if (xorListCount > 0)
                    {
                        throw (new UnsupportedException(
                                "Error: A List has already been defined for '"
                                        + name
                                        + "'.  A command can only have one list defined. "));
                    }
                    else
                    {
                        ++xorListCount;
                        CmdLine.addVariableName(name);
                        command.setRequiredVariableList(name);
                    }
                    break;
                }
                case OPTIONAL_VALUE:
                {
                    CmdLine.addVariableName(name);
                    command.addOptionalVariable(name);
                    isOptionalVarDefined = true;
                    break;
                }
                case OPTIONAL_LIST_VALUE:
                {
                    if (xorListCount > 0)
                    {
                        throw (new UnsupportedException(
                                "Error: A List has already been defined for '"
                                        + name
                                        + "'.  A command can only have one list defined. "));
                    }
                    else
                    {
                        ++xorListCount;
                        CmdLine.addVariableName(name);
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
    public static CmdLine defineCommand(final String... nameArgs)
    {
        Validate.defineBoolean(
                (nameArgs != null) && (nameArgs.length > 0)
                && (nameArgs.length <= CmdLine.MAX_LENGTH)).testTrue()
                .throwExceptionOnFailedValidation().validate();

        final List<Token> tokens = CmdLine.s_definedCommandTokenizer
                .tokenize(nameArgs);

        final CommandDefinition command = CmdLine
                .createCommandDefinition(tokens);
        final List<String> names = command.getNames();

        for (final String name : names)
        {
            final CommandDefinition existingCommand = CmdLine.s_commandMap.put(
                    name, command);
            if (existingCommand != null) { throw (new DuplicateException(
                    "Error: The command '"
                            + name
                            + "' has already been defined.  Define a new command name.")); }
        }

        // System.out.println(command);
        return (CmdLine.s_instance);
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
    public static CmdLine defineCommand(final String nameArgs)
    {
        Validate.defineString(nameArgs).testNotNullEmpty()
        .testMaxLength(CmdLine.MAX_LENGTH)
                .throwExceptionOnFailedValidation().validate();

        final String[] nameArgTokens = nameArgs.split(CmdLine.REGEX_PATTERN);
        CmdLine.defineCommand(nameArgTokens);
        return (CmdLine.s_instance);
    }

    /**
     * Gets the application name that was defined.
     *
     * @return A String. May be null or empty if the application name was not
     *         defined.
     */
    public static String getApplicationName()
    {
        return (CmdLine.s_applicationName);
    }

    /**
     * Gets the version String that was defined.
     *
     * @return A String. May be null or empty if the version was not defined.
     */
    public static String getVersion()
    {
        return (CmdLine.s_version);
    }

    /**
     * Parse the command line arguments.
     *
     * @param args
     *            The arguments from the command line.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine parse(final String[] args)
    {
        Validate.defineBoolean(
                (args != null) && (args.length > 0)
                        && (args.length <= CmdLine.MAX_LENGTH)).testTrue()
                .throwExceptionOnFailedValidation().validate();

        final List<String> tokens = CmdLine.tokenizeCmdLineArgs(args);
        CmdLine.processCmdLineTokens(tokens);
        return (CmdLine.s_instance);
    }

    /**
     * Parse the command line arguments.
     *
     * @param args
     *            The arguments from the command line.
     * @param commandListener
     *            A listener that will handle the callbacks.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine parse(final String[] args,
            final CommandListener commandListener)
    {
        CmdLine.setCommandListener(commandListener);
        return (CmdLine.parse(args));
    }

    /*
     * Processes the String tokens and creates Command.
     */
    private static void processCmdLineTokens(final List<String> tokens)
    {

        assert ((tokens != null) && (tokens.size() > 0)) : "The parameter 'tokens' must not be null or empty";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        String tokenValue = tokens.remove(0);

        // System.out.println( "Processing token: " + tokenValue );

        // check to see that a command definition exists for the current token.
        if (CmdLine.s_commandMap.containsKey(tokenValue))
        {
            // if defined, then create a command.
            final Command command = CmdLine.createCommand(tokenValue, tokens);

            // if the listener was set, then notify the listener of the created
            // command.
            if (CmdLine.s_commandListener != null)
            {
                CmdLine.s_commandListener.handle(command);
            }

            // Have all tokens been consumed?
            if (tokens.size() > 0)
            {
                // Reclusive call and process the remaining
                // tokens.
                CmdLine.processCmdLineTokens(tokens);
            }

        }
        else
        {
            // Process -D<property>=<value> if it exists.
            while (CmdLine.processSystemProperty(tokenValue, tokens))
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
                if (!CmdLine.s_commandMap.containsKey(tokenValue))
                {
                    throw (new UnsupportedException("Error: The command name '"
                            + tokenValue + "' is not defined."));
                }
                else
                {
                    // if the token is supported, recursive call and process the
                    // remaining tokens.

                    // add the token that was previously removed back to the
                    // token list since it was not processed.
                    tokens.add(0, tokenValue);
                    CmdLine.processCmdLineTokens(tokens);
                }
            }
        }
    }

    /*
     * Processes the -D<property>=<value> and adds it to the System property.
     */
    private static boolean processSystemProperty(final String valueString,
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
                        CmdLine.class,
                        "parseSystemProperty",
                        "Setting System Property: " + systemPropertyKey
                        + "=" + systemPropertyValue);

                isSystemPropertyProcessed = true;
                System.setProperty(systemPropertyKey, systemPropertyValue);

                final Command command = new Command(valueString);
                command.addVariable(systemPropertyKey, systemPropertyValue);

                if (CmdLine.s_commandListener != null)
                {
                    CmdLine.s_commandListener.handle(command);
                }
            }
        }
        return (isSystemPropertyProcessed);
    }

    /*
     * Process the required and optional variables that are associated with a
     * command.
     */
    private static void processVariable(final Pattern pattern,
            final List<String> tokens, final List<String> definedVariableNames,
            final Command command, final boolean required)
    {

        // pattern can be null.

        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        assert (definedVariableNames != null) : "The parameter 'definedVariableNames' must not be null.";
        assert (definedVariableNames.size() <= CmdLine.MAX_LENGTH) : "The parameter 'definedVariableNames' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

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

                if (CmdLine.s_variableNameSet.contains(varName))
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
    private static void processVariableList(final Pattern pattern,
            final List<String> tokens, final String varName,
            final Command command, final boolean required)
    {
        // pattern can be null.

        assert (tokens != null) : "The parameter 'tokens' must not be null.";
        assert (tokens.size() <= CmdLine.MAX_LENGTH) : "The parameter 'tokens' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        assert ((varName != null) && (varName.length() > 0)) : "The parameter 'varName' must not be null or empty.";
        assert (varName.length() <= CmdLine.MAX_LENGTH) : "The parameter 'varName' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

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
                    && !CmdLine.s_commandMap.containsKey(tokens.get(0)))
            {

                final String argToken = tokens.remove(0);

                final boolean processedSystemProperty = CmdLine
                        .processSystemProperty(argToken, tokens);

                if (!processedSystemProperty
                        && CmdLine.s_variableNameSet.contains(varName))
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
     * Sets the application name in the cmdline. To be used in the help menu -
     * (future release).
     *
     * @param name
     *            The name of the application.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine setApplicationName(final String name)
    {
        Validate.defineString(name).testNotNullEmpty()
        .testMaxLength(CmdLine.MAX_LENGTH)
                .throwExceptionOnFailedValidation().validate();

        CmdLine.s_applicationName = name;
        return (CmdLine.s_instance);
    }

    /**
     * Sets the listener that will handle the Commands that are created by the
     * parser.
     *
     * @param commandListener
     *            A listener that will handle the callbacks.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine setCommandListener(
            final CommandListener commandListener)
    {
        Validate.defineObject(commandListener).testNotNull()
        .throwExceptionOnFailedValidation().validate();

        CmdLine.s_commandListener = commandListener;
        return (CmdLine.s_instance);
    }

    /**
     * The version number of the application using the cmdline. To be used in
     * the help menu - (future release).
     *
     * @param version
     *            A String value. Must not be null or empty.
     * @return The CmdLine instance. Used for chaining calls.
     */
    public static CmdLine setVersion(final String version)
    {
        Validate.defineString(version).testNotNullEmpty()
        .throwExceptionOnFailedValidation().validate();

        CmdLine.s_version = version;
        return (CmdLine.s_instance);
    }

    /*
     * Converts the command line args.into String tokens.
     */
    private static List<String> tokenizeCmdLineArgs(final String[] args)
    {
        assert ((args != null) && (args.length > 0)) : "The parameter 'args' must not be null or empty";
        assert (args.length <= CmdLine.MAX_LENGTH) : "The parameter 'args' must be less than or equal to "
        + CmdLine.MAX_LENGTH;

        final List<String> tokens = CmdLine.s_commandLineTokenizer
                .tokenize(args);
        return (tokens);
    }

    private CmdLine()
    {
        // block instance.
    }
}
