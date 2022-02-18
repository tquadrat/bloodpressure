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

import static java.lang.Integer.signum;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.Objects.hash;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.lang.Objects.requireNotEmptyArgument;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.spi.DataImporterBase;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The definition for a component that imports data from a specified source.
 *
 *  @version $Id: DataImporter.java 119 2022-02-10 18:55:45Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: DataImporter.java 119 2022-02-10 18:55:45Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public sealed interface DataImporter
    permits DataImporterBase
{
        /*---------------*\
    ====** Inner Classes **====================================================
        \*---------------*/
    /**
     *  The abstract base class for the data import components.
     *
     *  @version $Id: DataImporter.java 119 2022-02-10 18:55:45Z tquadrat $
     *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
     *  @UMLGraph.link
     *  @since 0.0.1
     */
    @ClassVersion( sourceVersion = "$Id: DataImporter.java 119 2022-02-10 18:55:45Z tquadrat $" )
    @API( status = INTERNAL, since = "0.0.1" )
    public static class Key implements Comparable<Key>
    {
            /*------------*\
        ====** Attributes **===================================================
            \*------------*/
        /**
         *  The identifier for the data origin.
         */
        private final String m_DataOrigin;

        /**
         *  The input format.
         */
        private final InputFormat m_InputFormat;

            /*--------------*\
        ====** Constructors **=================================================
            \*--------------*/
        /**
         *  Creates a new instance of {@code Key}.
         *
         *  @param  inputFormat The input format.
         *  @param  dataOrigin  The identifier for the data origin.
         */
        public Key( final InputFormat inputFormat, final String dataOrigin )
        {
            m_DataOrigin = requireNotEmptyArgument( dataOrigin, "dataOrigin" );
            m_InputFormat = requireNonNullArgument( inputFormat, "inputFormat" );
        }   //  Key()

            /*---------*\
        ====** Methods **======================================================
            \*---------*/
        /**
         *  {@inheritDoc}
         */
        @Override
        public final int compareTo( final Key other )
        {
            var retValue = 0;
            if( this != other )
            {
                retValue = signum( m_DataOrigin.compareTo( other.m_DataOrigin ) );
                if( retValue == 0 ) retValue = signum( m_InputFormat.compareTo( other.m_InputFormat ) );
            }

            //---* Done *------------------------------------------------------
            return retValue;
        }   //  compareTo()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final boolean equals( final Object o )
        {
            var retValue = this == o;
            if( !retValue && o instanceof Key other )
            {
                retValue = (m_InputFormat == other.m_InputFormat) && (m_DataOrigin.equals( other.m_DataOrigin ) );
            }

            //---* Done *----------------------------------------------------------
            return retValue;
        }   //  equals()

        /**
         *  {@inheritDoc}
         */
        @Override
        public final int hashCode() { return hash( m_InputFormat, m_DataOrigin ); }
    }
    //  class Key

        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Returns the registry key for this data importer.
     *
     *  @return The key.
     */
    public Key getKey();

    /**
     *  Parses the data on the given source location and returns a collection
     *  of
     *  {@link DataRecord}
     *  instance.
     *
     *  @param  source  The location for the source data.
     *  @param  timezone    The time zone for the timestamp on the source data.
     *  @param  encoding    The encoding of the source data.
     *  @return The data records.
     *  @throws IOException Something went wrong while pulling the data from
     *      the source.
     */
    public Collection<DataRecord> parse( final URI source, final ZoneId timezone, final Charset encoding ) throws IOException;

    /**
     *  Returns the data importer that is identified by the given data origin
     *  and input format.
     *
     *  @param  dataOrigin  The identifier for the data origin.
     *  @param  inputFormat The input format.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the retrieved data importer.
     */
    public static Optional<DataImporter> retrieveDataImporter( final String dataOrigin, final InputFormat inputFormat )
    {
        final var retValue = DataImporterBase.retrieveDataImporter( inputFormat, dataOrigin );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveDataImporter()
}
//  interface DataImporter

/*
 *  End of File
 */