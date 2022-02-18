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

package org.tquadrat.bloodpressure.spi;

import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.foundation.lang.Objects.nonNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.lang.Objects.requireNotEmptyArgument;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.DataImporter;
import org.tquadrat.bloodpressure.DataRecord;
import org.tquadrat.bloodpressure.InputFormat;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The abstract base class for the data import components.
 *
 *  @version $Id: DataImporterBase.java 120 2022-02-10 18:58:05Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: DataImporterBase.java 120 2022-02-10 18:58:05Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public non-sealed abstract class DataImporterBase implements DataImporter
{
        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The identifier for the data origin.
     */
    private final String m_DataOrigin;

    /**
     *  The input format that is handled by this implementation of
     *  {@link DataImporter}.
     */
    private final InputFormat m_InputFormat;

    /**
     *  The registry for the data importer.
     */
    private static final Map<Key,DataImporter> m_Registry = new HashMap<>();

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code DataImporterBase}.
     *
     *  @param  inputFormat The input format that is handled by this
     *      implementation of
     *      {@link DataImporter}.
     *  @param  dataOrigin  The identifier for the data origin.
     */
    protected DataImporterBase( final InputFormat inputFormat, final String dataOrigin )
    {
        m_DataOrigin = requireNotEmptyArgument( dataOrigin, "dataOrigin" );
        m_InputFormat = requireNonNullArgument( inputFormat, "inputFormat" );
    }   //  DataImporterBase

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  {@inheritDoc}
     */
    @Override
    public final Key getKey() { return new Key( m_InputFormat, m_DataOrigin ); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public abstract Collection<DataRecord> parse( final URI source, final ZoneId timezone, final Charset encoding ) throws IOException;

    /**
     *  Registers a
     *  {@link DataImporter}
     *  instance.
     *
     *  @param  importer    The instance to register.
     */
    public static final void registerImporter( final DataImporter importer )
    {
        if( nonNull( importer ) ) m_Registry.put( importer.getKey(), importer );
    }   //  registerImport()

    /**
     *  Returns the data importer that is identified by the given input format
     *  and data origin.
     *
     *  @param  inputFormat The input format.
     *  @param  dataOrigin  The identifier for the data origin.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the retrieved data importer.
     */
    public static final Optional<DataImporter> retrieveDataImporter( final InputFormat inputFormat, final String dataOrigin )
    {
        final var retValue = Optional.ofNullable( m_Registry.get( new Key( inputFormat, dataOrigin ) ) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveDataImporter()
}
//  class DataImporterBase

/*
 *  End of File
 */