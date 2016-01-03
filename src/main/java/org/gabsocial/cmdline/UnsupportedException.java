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

/**
 * An exception that is used if an action is not supported.
 * 
 * 
 * @author Gregory Brown (sysdevone)
 * 
 */
public class UnsupportedException extends RuntimeException
{
    
    /**
     * Serialized version number.
     */
    private static final long serialVersionUID = 2473729829921263263L;
    
    /**
     * Constructor that takes a message.
     * 
     * @param message
     *            A <code>String</code> message.
     */
    protected UnsupportedException(final String message)
    {
        super(message);
    }
}
