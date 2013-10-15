/*******************************************************************************
 * DARPA XDATA licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with 
 * the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 * 
 * Copyright 2013 Raytheon BBN Technologies Corp. All Rights Reserved.
 ******************************************************************************/
/* =============================================================================
 *
 *                  COPYRIGHT 2010 BBN Technologies Corp.
 *                  1300 North 17th Street, Suite 600
 *                       Arlington, VA  22209
 *                          (703) 284-1200
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * ==============================================================================
 */
package com.bbn.c2s2.pint.exception;

/**
 * Exception that can be thrown for any invalid Process objects, including
 * incomplete processes, processes with 1-length paths, etc.
 * 
 * @author tself
 * 
 */
public class InvalidProcessException extends PintException {
	private static final long serialVersionUID = -2492700627064787164L;

	public InvalidProcessException(String msg) {
		super(msg);
	}

	public InvalidProcessException(Throwable cause) {
		super(cause);
	}

	public InvalidProcessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
