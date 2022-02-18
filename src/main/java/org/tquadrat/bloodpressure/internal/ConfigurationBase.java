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

package org.tquadrat.bloodpressure.internal;

import static java.lang.System.getProperty;
import static java.sql.DriverManager.getConnection;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.bloodpressure.InputFormat.CSV;
import static org.tquadrat.bloodpressure.ReportFormat.TEXT;
import static org.tquadrat.foundation.lang.CommonConstants.PROPERTY_USER_HOME;
import static org.tquadrat.foundation.util.StringUtils.format;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.Configuration;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.util.stringconverter.PathStringConverter;

/**
 *  The base class for the configuration bean.
 *
 *  @version $Id: ConfigurationBase.java 122 2022-02-12 20:09:13Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: ConfigurationBase.java 122 2022-02-12 20:09:13Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public abstract class ConfigurationBase implements Configuration
{
        /*---------------*\
    ====** Inner Classes **====================================================
        \*---------------*/

        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code ConfigurationBase}.
     */
    protected ConfigurationBase() { /* Just exists */ }

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  {@inheritDoc}
     */
    @Override
    public final Map<String,Object> initData()
    {
        final Map<String,Object> buffer = new HashMap<>();

        buffer.put( "databasePassword", "4%dFE8§4$gB" );
        buffer.put( "databaseName", "bloodpressure" );
        buffer.put( "dataFolder", Path.of( getProperty( PROPERTY_USER_HOME ), ".bloodpressure" ) );
        buffer.put( "endDate", LocalDate.MAX );
        buffer.put( "importFormat", CSV );
        buffer.put( "importSource", "blutdruckdaten.de" );
        buffer.put( "reportFormat", TEXT );
        buffer.put( "reportStyle", "SIMPLE" );
        buffer.put( "startDate", LocalDate.MIN );

        final var retValue = Map.copyOf( buffer );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  initData()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void initializeDatabase( final Connection connection ) throws SQLException
    {
        final var ddl = format(
            """
            CREATE TABLE %1$s
            (
              %2$s TIMESTAMP WITH TIME ZONE PRIMARY KEY,
              systolic_pressure SMALLINT NOT NULL,
              diastolic_pressure SMALLINT NOT NULL,
              ignored BOOLEAN NOT NULL
            )
            """, TABLE_BLOODPRESSURE_DATA, COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY );

        try( final var statement = connection.createStatement() )
        {
            statement.execute( ddl );
        }
    }   //  initializeDatabase()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Connection retrieveConnection() throws SQLException
    {
        final var jdbcURL = format( "jdbc:h2:%1s/%2$s", PathStringConverter.INSTANCE.toString( getDataFolder() ), getDatabaseName() );
        final var retValue = getConnection( jdbcURL, getUsername(), getDatabasePassword() );

        //---* We want to have autocommit *------------------------------------
        retValue.setAutoCommit( true );

        //---* No warnings … *-------------------------------------------------
        retValue.clearWarnings();

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveConnection()
}
//  class ConfigurationBase

/*
 *  End of File
 */