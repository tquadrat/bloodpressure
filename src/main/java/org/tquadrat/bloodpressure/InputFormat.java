/*
 * ============================================================================
 * Copyright © 2002-2022 by Thomas Thrien.
 * All Rights Reserved.
 * ============================================================================
 * Licensed to the public under the agreements of the GNU Lesser General Public
 * License, version 3.0 (the "License"). You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.tquadrat.bloodpressure;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The support formats for import files.
 *
 *  @version $Id: InputFormat.java 116 2022-01-26 16:36:56Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.1.0
 */
@ClassVersion( sourceVersion = "$Id: InputFormat.java 116 2022-01-26 16:36:56Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public enum InputFormat
{
        /*------------------*\
    ====** Enum Definitions **=================================================
        \*------------------*/
    /**
     *  Input format CSV (comma-separated values).
     */
    CSV,

    /**
     *  Import format JSON.
     */
    JSON,

    /**
     *  Input format XML.
     */
    XML
}
//  enum InputFormat

/*
 *  End of File
 */