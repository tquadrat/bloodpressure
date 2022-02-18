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

import static java.math.MathContext.DECIMAL128;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.value.Pressure.MILLIMETER_OF_MERCURY;

import java.time.ZonedDateTime;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.value.PressureValue;

/**
 *  A data record as it is produced by a
 *  {@link DataImporter}.
 *
 *  @version $Id: DataRecord.java 117 2022-02-10 13:13:07Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 *
 *  @param  timestamp   The timestamp for the measuring.
 *  @param  systolic    The systolic pressure value.
 *  @param  diastolic   The diastolic pressure value.
 *  @param  isIgnored   {@code true} if the record should be ignored by any
 *      statistics.
 */
@ClassVersion( sourceVersion = "$Id: DataRecord.java 117 2022-02-10 13:13:07Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public record DataRecord( ZonedDateTime timestamp, PressureValue systolic, PressureValue diastolic, boolean isIgnored )
{
        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance for {@code DataRecord}.
     *
     *  @param  timestamp   The timestamp for the measuring.
     *  @param  systolic    The systolic pressure value.
     *  @param  diastolic   The diastolic pressure value.
     *  @param  isIgnored   {@code true} if the record should be ignored by any
     *      statistics.
     */
    public DataRecord( final ZonedDateTime timestamp, final PressureValue systolic, final PressureValue diastolic, final boolean isIgnored )
    {
        this.timestamp = requireNonNullArgument( timestamp, "timestamp" );
        this.systolic = requireNonNullArgument( systolic, "systolic" );
        this.diastolic = requireNonNullArgument( diastolic, "diastolic" );
        this.isIgnored = isIgnored;
    }   //  DataRecord()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Returns the diastolic pressure as an integer value with the unit mmHG.
     *
     * @return  The diastolic pressure.
     */
    public final int getDiastolicPressure() { return diastolic.convert( MILLIMETER_OF_MERCURY ).round( DECIMAL128 ).intValue(); }

    /**
     *  Returns the systolic pressure as an integer value with the unit mmHG.
     *
     * @return  The systolic pressure.
     */
    public final int getSystolicPressure() { return systolic.convert( MILLIMETER_OF_MERCURY ).round( DECIMAL128 ).intValue(); }
}
//  record DataRecord

/*
 *  End of File
 */