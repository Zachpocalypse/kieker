/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.test.monitoring.aspectJ.loadTimeWeaving.bookstore;


/**
 * A simple test and demonstration scenario for Kieker's 
 * monitoring component.
 *
 * @author Matthias Rohr
 * History:
 * 2008/01/09: Refactoring for the first release of
 *             Kieker and publication under an open source licence
 * 2007-04-18: Initial version
 *
 */


public class Catalog {
    
    public static void getBook(boolean complexQuery){
    	if (complexQuery) {
//		System.out.println("  complex query "+Thread.currentThread().getName());
    	//See ticket http://samoa.informatik.uni-kiel.de/kieker/trac/ticket/239
		Bookstore.waitabit(20);
	}
	else 	{			
//		System.out.println("  simple query "+Thread.currentThread().getName());
		Bookstore.waitabit(2);	
	}

    }
   
}
