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
import static org.tquadrat.foundation.i18n.I18nUtil.resolveText;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.i18n.Text;
import org.tquadrat.foundation.i18n.Translation;
import org.tquadrat.foundation.util.RangeMap;

/**
 *  The different categories for blood pressure values.
 *
 *  @version $Id: Diagnosis.java 123 2022-02-13 19:33:21Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: Diagnosis.java 123 2022-02-13 19:33:21Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public enum Diagnosis
{
        /*------------------*\
    ====** Enum Definitions **=================================================
        \*------------------*/
    /**
     *  The blood pressure is low.
     */
    @Text
    (
        description = "Low blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Niedrig" ),
            @Translation( language = "en", text = "Low" )
        }
    )
    LOW,

    /**
     *  The blood pressure is optimal.
     */
    @Text
    (
        description = "Optimal blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Optimal" ),
            @Translation( language = "en", text = "Optimal" )
        }
    )
    OPTIMAL,

    /**
     *  The blood pressure is normal.
     */
    @Text
    (
        description = "Normal blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Normal" ),
            @Translation( language = "en", text = "Normal" )
        }
    )
    NORMAL,

    /**
     *  The blood pressure is high normal.
     */
    @SuppressWarnings( "SpellCheckingInspection" )
    @Text
    (
        description = "Higher than normal blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Hochnormal" ),
            @Translation( language = "en", text = "High-Normal" )
        }
    )
    NORMAL_HIGH,

    /**
     *  The blood pressure is hypertonic, level 1.
     */
    @Text
    (
        description = "Level 1 high blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Hypertonie Grad 1" ),
            @Translation( language = "en", text = "Hypertonie Level 1" )
        }
    )
    HIGH1,

    /**
     *  The blood pressure is hypertonic, level 2.
     */
    @Text
    (
        description = "Level 2 high blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Hypertonie Grad 2" ),
            @Translation( language = "en", text = "Hypertonie Level 2" )
        }
    )
    HIGH2,

    /**
     *  The blood pressure is hypertonic, level 2.
     */
    @Text
    (
        description = "Level 3 high blood pressure",
        translations =
        {
            @Translation( language = "de", text = "Hypertonie Grad 3" ),
            @Translation( language = "en", text = "Hypertonie Level 3" )
        }
    )
    HIGH3;

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/
    /**
     *  The assessment table for the diastolic blood pressure values.
     */
    private static final RangeMap<Diagnosis> m_DiastolicTable;

    /**
     *  The assessment table for the systolic blood pressure values.
     */
    private static final RangeMap<Diagnosis> m_SystolicTable;

    static
    {
        //---* The table for the systolic values *-----------------------------
        m_SystolicTable = RangeMap.of( HIGH3, false )
            .addRange( 105.0, LOW )
            .addRange( 120.0, OPTIMAL )
            .addRange( 130.0, NORMAL )
            .addRange( 140.0, NORMAL_HIGH )
            .addRange( 160.0, HIGH1 )
            .addRange( 180.0, HIGH2 );

        //---* The table for the diastolic values *----------------------------
        m_DiastolicTable = RangeMap.of( HIGH3, false )
            .addRange( 65.0, LOW )
            .addRange( 80.0, OPTIMAL )
            .addRange( 85.0, NORMAL )
            .addRange( 90.0, NORMAL_HIGH )
            .addRange( 100.0, HIGH1 )
            .addRange( 110.0, HIGH2 );

    }

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The configuration used by this program.
     */
    private final Configuration m_Configuration;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code Diagnosis}.
     */
    private Diagnosis()
    {
        m_Configuration = Configuration.getInstance();
    }   //  Diagnosis()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Assess the diastolic pressure.
     *
     *  @param  value   The diastolic pressure.
     *  @return The diagnosis.
     */
    public static final Diagnosis assessDiastolicPressure( final int value )
    {
        final var retValue = m_DiastolicTable.get( value );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  assessDiastolicPressure()

    /**
     *  Assess the systolic pressure.
     *
     *  @param  value   The systolic pressure.
     *  @return The diagnosis.
     */
    public static final Diagnosis assessSystolicPressure( final int value )
    {
        final var retValue = m_SystolicTable.get( value );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  assessSystolicPressure()

    /**
     *  Combines the given diagnosis.
     *
     *  @param  systolic    The diagnosis for the systolic pressure.
     *  @param  diastolic   The diagnosis for the diastolic pressure.
     */
    public static final Diagnosis combineDiagnosis( final Diagnosis systolic, final Diagnosis diastolic )
    {
        final var retValue = systolic.compareTo( diastolic ) > 0 ? systolic : diastolic;

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  combineDiagnosis()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final String toString()
    {
        final var retValue = resolveText( m_Configuration.getResourceBundle(), this );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  toString()
}
//  enum Diagnosis

/*
 *  End of File
 */