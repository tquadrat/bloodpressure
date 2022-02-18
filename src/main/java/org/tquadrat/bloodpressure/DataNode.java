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
 *  <p>{@summary An instance of
 *  {@code DataNode}
 *  holds only the systolic and diastolic pressures as integer values, based on
 *  mmHG, but not the timestamp.}</p>
 *  <p>It is used for the inputs to the reports.</p>
 *
 *  @param  systolic    The systolic pressure in mmHG.
 *  @param  diastolic   The diastolic pressure in mmHG.
 *  @param  pulsePressure   This is the difference between the systolic and the
 *      diastolic pressure.
 *
 *  @version $Id: DataNode.java 119 2022-02-10 18:55:45Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: DataNode.java 119 2022-02-10 18:55:45Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public record DataNode( int systolic, int diastolic, int pulsePressure )
{
        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code DataNode}. The {@code pulsePressure}
     *  will be calculated.
     *
     *  @param  systolic    The systolic pressure in mmHG.
     *  @param  diastolic   The diastolic pressure in mmHG.
     */
    public DataNode( final int systolic, final int diastolic )
    {
        this( systolic, diastolic, systolic - diastolic );
    }   //  DataNode()
}
//  record DataNode

/*
 *  End of File
 */