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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class is the command that is created when the command line is parsed. It
 * is sent to the CommandListener.handle(Command command) method.
 * 
 * It holds the name and variables of the command.
 *
 * @author Gregory Brown (sysdevone)
 *
 */
public class Command
{
    /*
     * The name of the command
     */
    protected String            _name;
    
    /*
     * The variables associated with the command. A variable has a name and
     * value. The value is held in a <code>List</code> instance.
     */
    protected Map<String, List> _variables;
    
    /**
     * A Command POJO. Associates a name and creates the data structure that
     * holds the variables.F
     * 
     * @param name
     *            The name of the command.
     */
    protected Command(String name)
    {
        assert (name != null && name.length() > 0) : "The parameter 'name' must not be null or empty";
        
        this._name = name;
        this._variables = new HashMap<String, List>();
    }
    
    /**
     * Gets the name
     * 
     * @return A String name.
     */
    public String getName()
    {
        return (this._name);
    }
    
    /**
     * A test to see if the Command has any variables associated with it.
     * 
     * @return A boolean value. True if the Command has variables, otherwise it
     *         is false.F
     */
    public boolean hasVariables()
    {
        return (this._variables.size() > 0);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.format("Command [_name=%s, _variables=%s]", this._name,
                this._variables);
    }
    
    /**
     * Adds a variable to the Command. The value is added to a <code>List</code>
     * .
     * 
     * @param name
     *            The name of the Variable. Must be unique.
     * @param value
     *            The value associated with the name.
     */
    public void addVariable(final String name, final String value)
    {
        assert (name != null && name.length() > 0) : "The parameter 'name' must not be null or empty";
        assert (value != null && value.length() > 0) : "The parameter 'value' must not be null or empty";
        
        List<String> variables;
        if (!this._variables.containsKey(name))
        {
            // create list
            variables = new LinkedList<String>();
            this._variables.put(name, variables);
            
        }
        else
        {
            variables = this._variables.get(name);
        }
        variables.add(value);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this._name == null) ? 0 : this._name.hashCode());
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Command other = (Command) obj;
        if (this._name == null)
        {
            if (other._name != null) return false;
        }
        else if (!this._name.equals(other._name)) return false;
        return true;
    }
    
    /**
     * Gets the values associated with the variable name.
     * 
     * @param name
     *            The name of the variable to get the values for.
     * @return A new List instance holding one to many Strings.
     */
    public List<String> getValues(String name)
    {
        List<String> values = new ArrayList<String>(this._variables.get(name));
        return (values);
    }
    
}
