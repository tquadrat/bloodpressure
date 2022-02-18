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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.util.Arrays.sort;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;
import static java.util.Locale.ROOT;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.bloodpressure.Application.MSG_InvalidFile;
import static org.tquadrat.bloodpressure.Application.MSG_InvalidFolder;
import static org.tquadrat.bloodpressure.Configuration.COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY;
import static org.tquadrat.bloodpressure.Configuration.TABLE_BLOODPRESSURE_DATA;
import static org.tquadrat.bloodpressure.Diagnosis.assessDiastolicPressure;
import static org.tquadrat.bloodpressure.Diagnosis.assessSystolicPressure;
import static org.tquadrat.bloodpressure.Diagnosis.combineDiagnosis;
import static org.tquadrat.foundation.lang.Objects.nonNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.lang.Objects.requireNotEmptyArgument;
import static org.tquadrat.foundation.util.StringUtils.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.Configuration;
import org.tquadrat.bloodpressure.DataNode;
import org.tquadrat.bloodpressure.Diagnosis;
import org.tquadrat.bloodpressure.Report;
import org.tquadrat.bloodpressure.ReportFormat;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.exception.ImpossibleExceptionError;
import org.tquadrat.foundation.i18n.TextUse;

/**
 *  The abstract base class for all reports.
 *
 *  @version $Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public non-sealed abstract class ReportBase implements Report
{
        /*---------------*\
    ====** Inner Classes **====================================================
        \*---------------*/
    /**
     *  The type of the destination for the report.
     *
     *  @version $Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $
     *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
     *  @UMLGraph.link
     *  @since 0.0.1
     */
    @SuppressWarnings( "SpellCheckingInspection" )
    @ClassVersion( sourceVersion = "$Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $" )
    @API( status = STABLE, since = "0.0.1" )
    public static enum DestinationType
    {
            /*------------------*\
        ====** Enum Declaration **=================================================
            \*------------------*/
        /**
         *  The destination is a file.
         */
        DEST_FILE,

        /**
         *  The destination is a folder.
         */
        DEST_FOLDER
    }
    //  enum DestinationType

    /**
     *  The record for the distribution data.
     *
     *  @version $Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $
     *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
     *  @UMLGraph.link
     *  @since 0.0.1
     */
    @ClassVersion( sourceVersion = "$Id: ReportBase.java 123 2022-02-13 19:33:21Z tquadrat $" )
    @API( status = STABLE, since = "0.0.1" )
    public static final class DistributionNode
    {
            /*------------*\
        ====** Attributes **===================================================
            \*------------*/
        /**
         *  The diagnosis for this node.
         */
        private final Diagnosis m_Diagnosis;

        /**
         *  The count for the diastolic values for this diagnosis.
         */
        private int m_DiastolicCount;

        /**
         *  The percentage of diastolic values for this diagnosis.
         */
        private double m_DiastolicPercent;

        /**
         *  The count for the systolic values for this diagnosis.
         */
        private int m_SystolicCount;

        /**
         *  The percentage of systolic values for this diagnosis.
         */
        private double m_SystolicPercent;

            /*--------------*\
        ====** Constructors **=================================================
            \*--------------*/
        /**
         *  Creates a new instance of {@code DistributionName}.
         *
         *  @param  diagnosis   The diagnosis for this node.
         */
        public DistributionNode( final Diagnosis diagnosis )
        {
            m_Diagnosis = requireNonNullArgument( diagnosis, "diagnosis" );
            m_SystolicCount = 0;
            m_DiastolicCount = 0;
            m_SystolicPercent = 0.0;
            m_DiastolicPercent = 0.0;
        }

            /*---------*\
        ====** Methods **======================================================
            \*---------*/
        /**
         *  Calculates the diastolic percentage.
         *
         *  @param  totalCount  The total amount of values.
         *  @return The percentage.
         */
        public final double calcDiastolicPercentage( final int totalCount )
        {
            m_DiastolicPercent = (double) m_DiastolicCount * 100.0 / (double) totalCount;

            //---* Done *----------------------------------------------------------
            return m_DiastolicPercent;
        }   //  calcDiastolicPercentage()

        /**
         *  Calculates the systolic percentage.
         *
         *  @param  totalCount  The total amount of values.
         *  @return The percentage.
         */
        public final double calcSystolicPercentage( final int totalCount )
        {
            m_SystolicPercent = (double) m_SystolicCount * 100.0 / (double) totalCount;

            //---* Done *----------------------------------------------------------
            return m_SystolicPercent;
        }   //  calcSystolicPercentage()

        /**
         *  Returns the diagnosis.
         *
         *  @return The diagnosis.
         */
        public final Diagnosis getDiagnosis() { return m_Diagnosis; }

        /**
         *  Returns the amount of diastolic values for this diagnosis.
         *
         *  @return The value count.
         */
        public final int getDiastolicCount() { return m_DiastolicCount; }

        /**
         *  Returns the percentage of diastolic values for this diagnosis.
         *
         *  @return The value percentage.
         */
        public final double getDiastolicPercentage() { return m_DiastolicPercent; }

        /**
         *  Returns the amount of systolic values for this diagnosis.
         *
         *  @return The value count.
         */
        public final int getSystolicCount() { return m_SystolicCount; }

        /**
         *  Returns the percentage of systolic values for this diagnosis.
         *
         *  @return The value percentage.
         */
        public final double getSystolicPercentage() { return m_SystolicPercent; }

        /**
         *  Increments the counter for the diastolic values.
         */
        public final void incrementDiastolic() { ++m_DiastolicCount; }

        /**
         *  Increments the counter for the systolic values.
         */
        public final void incrementSystolic() { ++m_SystolicCount; }
    }   //  class DistributionNode

        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The reference to the configuration for this application.
     */
    private final Configuration m_Configuration;

    /**
     *  The registry for the report generators.
     */
    private static final Map<String,Report> m_Registry = new HashMap<>();

    /**
     *  <p>{@summary The report style.} Basically, this is the name of the
     *  report generator.
     */
    private final String m_Style;

    /**
     *  The output formats that are supported by this report generator.
     */
    private final Set<ReportFormat> m_SupportedFormats = EnumSet.noneOf( ReportFormat.class );

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code ReportBase}.
     *
     *  @param  style   The report style.
     *  @param  supportedFormats    The supported report output formats.
     */
    protected ReportBase( final String style, final ReportFormat... supportedFormats )
    {
        m_Configuration = Configuration.getInstance();
        m_Style = requireNotEmptyArgument( style, "style" );
        addAll( m_SupportedFormats, requireNotEmptyArgument( supportedFormats, "supportedFormats" ) );
    }   //  ReportBase()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Makes an assessment of the given
     *  {@link DataNode}.
     *
     *  @param  data    The data to assess.
     *  @return The assessment.
     */
    protected Diagnosis assessData( final DataNode data )
    {
        final var systolicDiagnosis = assessSystolicPressure( data.systolic() );
        final var diastolicDiagnosis = assessDiastolicPressure( data.diastolic() );
        final var retValue = combineDiagnosis( systolicDiagnosis, diastolicDiagnosis );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  assessData()

    /**
     *  Calculates the plain average for the given values.
     *
     *  @param  data    The input data.
     *  @return The average.
     */
    protected final DataNode calculateAverage( final Map<? extends TemporalAccessor,DataNode> data )
    {
        final var retValue = calculateAverage( requireNonNullArgument( data, "data" ).values().toArray( DataNode []::new ) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateAverage()

    /**
     *  Calculates the plain average for the given values.
     *
     *  @param  data    The input data.
     *  @return The average.
     */
    protected final DataNode calculateAverage( final DataNode [] data )
    {
        final var size = requireNonNullArgument( data, "data" ).length;
        var systolicSum = 0L;
        var diastolicSum = 0L;
        var pulsePressureSum = 0L;

        for( var i = 0; i < size; ++i )
        {
            systolicSum += data [i].systolic();
            diastolicSum += data [i].diastolic();
            pulsePressureSum += data [i].pulsePressure();
        }

        final var retValue = new DataNode( (int) (systolicSum / size), (int) (diastolicSum / size), (int) (pulsePressureSum / size) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateAverage()

    /**
     *  Calculates an average for the given values that is weighted to the end
     *  of the list.
     *
     *  @param  data    The input data.
     *  @return The average.
     */
    protected final DataNode calculateEndWeightedAverage( final SortedMap<? extends TemporalAccessor,DataNode> data )
    {
        final var retValue = calculateEndWeightedAverage( data.values().toArray( DataNode []::new ) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateEndWeightedAverage()

    /**
     *  Calculates an average for the given values that is weighted to the end
     *  of the list.
     *
     *  @param  data    The input data.
     *  @return The average.
     */
    protected final DataNode calculateEndWeightedAverage( final DataNode[] data )
    {
        final var size = requireNonNullArgument( data, "data" ).length;
        var systolicSum = 0L;
        var diastolicSum = 0L;
        var pulsePressureSum = 0L;

        for( var i = 0; i < size; ++i )
        {
            systolicSum += (long) data [i].systolic() * (i + 1);
            diastolicSum += (long) data [i].diastolic() * (i + 1);
            pulsePressureSum += (long) data [i].pulsePressure() * (i + 1);
        }

        final var divisor = (size + 1) * (size / 2);

        final var retValue = new DataNode( (int) (systolicSum / divisor), (int) (diastolicSum / divisor), (int) (pulsePressureSum / divisor) );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateEndWeightedAverage()

    /**
     *  Calculates a median for the given values.
     *
     *  @param  data    The input data.
     *  @return The median.
     */
    protected final int calculateMedian( final int[] data )
    {
        final var copy = requireNonNullArgument( data, "data" ).clone();
        sort( copy );
        final var size = copy.length;
        final var index = size / 2;

        final var retValue = switch( size % 2 )
            {
                case 0 -> copy [index];
                case 1 -> (copy [index] + copy [index + 1]) / 2;
                default -> throw new ImpossibleExceptionError( new Exception( "Impossible value" ) );
            };

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateMedian()

    /**
     *  Calculates a median for the given values.
     *
     *  @param  data    The input data.
     *  @return The median.
     */
    protected final DataNode calculateMedian( final Map<? extends TemporalAccessor,DataNode> data )
    {
        final var size = requireNonNullArgument( data, "data" ).size();
        final var systolicArray = new int [size];
        final var diastolicArray = new int [size];
        final var pulsePressureArray = new int [size];

        var index = 0;
        for( final var node : data.values() )
        {
            systolicArray [index] = node.systolic();
            diastolicArray [index] = node.diastolic();
            pulsePressureArray [index] = node.pulsePressure();
            ++index;
        }

        final var systolic = calculateMedian( systolicArray );
        final var diastolic = calculateMedian(diastolicArray );
        final var pulsePressure = calculateMedian( pulsePressureArray );

        final var retValue =  new DataNode( systolic, diastolic, pulsePressure );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  calculateMedian()

    /**
     *  Checks whether the given destination is valid according to the given
     *      type and returns the path for an existing file or folder.
     *
     * @param   destination The destination.
     * @param   type    The type of the destination.
     * @return  The path for the destination.
     * @throws IOException  The destination is invalid.
     */
    protected final Path checkDestination( final File destination, final DestinationType type ) throws IOException
    {
        Path retValue = requireNonNullArgument( destination, "destination" ).toPath();
        switch( requireNonNullArgument( type, "type" ) )
        {
            case DEST_FILE ->
            {
                if( exists( retValue) )
                {
                    if( !isRegularFile( retValue ) ) throw new IOException( m_Configuration.getMessage( MSG_InvalidFile, destination.getAbsolutePath() ) );
                }
                else
                {
                    final var parent = retValue.getParent();
                    if( !exists( parent ) ) createDirectories( parent );
                    createFile( retValue );
                }
            }
            case DEST_FOLDER ->
            {
                if( exists( retValue ) )
                {
                    if( !isDirectory( retValue ) ) throw new IOException( m_Configuration.getMessage( MSG_InvalidFolder, destination.getAbsolutePath() ) );
                }
                else
                {
                    createDirectories( retValue );
                }
            }
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  checkDestination()

    /**
     *  Distributes the data based on the diagnosis.
     *
     *  @param  data    The input data.
     *  @return The distribution.
     */
    protected final Map<Diagnosis,DistributionNode> distributeData( final Map<LocalDateTime,DataNode> data )
    {
        final Map<Diagnosis,DistributionNode> retValue = new EnumMap<>( Diagnosis.class );
        for( final var node : data.values() )
        {
            var diagnosis = assessSystolicPressure( node.systolic() );
            retValue.computeIfAbsent( diagnosis, DistributionNode::new ).incrementSystolic();
            diagnosis = assessDiastolicPressure( node.diastolic() );
            retValue.computeIfAbsent( diagnosis, DistributionNode::new ).incrementDiastolic();
        }

        final var totalCount = data.size();
        for( final var node : retValue.values() )
        {
            node.calcSystolicPercentage( totalCount );
            node.calcDiastolicPercentage( totalCount );
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  distributeData()

    /**
     *  Compresses the values so that there is only one entry per day.
     *
     *  @param  data    The input data.
     *  @return The compressed data.
     */
    protected final SortedMap<LocalDate,DataNode> compressToDay( final SortedMap<LocalDateTime,DataNode> data )
    {
        final var retValue = new TreeMap<LocalDate,DataNode>();

        LocalDate currentDay = null;
        final var currentDayData = new ArrayList<DataNode>();

        for( final var entry : requireNonNullArgument( data, "data" ).entrySet() )
        {
            final var day = entry.getKey().toLocalDate();
            if( !day.equals( currentDay ) && !currentDayData.isEmpty() )
            {
                retValue.put( currentDay, calculateAverage( currentDayData.toArray( DataNode []::new ) ) );
                currentDayData.clear();
            }
            currentDay = day;
            currentDayData.add( entry.getValue() );
        }
        if( nonNull( currentDay ) )
        {
            retValue.put( currentDay, calculateAverage( currentDayData.toArray( DataNode []::new ) ) );
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  compressToDay()

    /**
     *  {@inheritDoc}
     */
    @Override
    public abstract void generateReport( final File destination, final ReportFormat format ) throws IOException, SQLException;

    /**
     *  Returns a reference to the configuration for this application run.
     *
     *  @return The configuration.
     */
    protected final Configuration getConfiguration() { return m_Configuration; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getStyle() { return m_Style; }

    /**
     *  Loads the data from the database.
     *
     *  @param  connection  The database connection.
     *  @param  timezone    The time zone for the report.
     *  @param  start   The start date.
     *  @param  end The end date.
     *  @return The data.
     *  @throws SQLException    Problems when accessing the database.
     */
    protected final SortedMap<LocalDateTime,DataNode> loadData( final Connection connection, final ZoneId timezone, final LocalDate start, final LocalDate end ) throws SQLException
    {
        final var startTime = requireNonNullArgument( start, "start" )
            .atTime( 0, 0 )
            .atZone( timezone );
        final var endTime = (requireNonNullArgument( end, "end" ).equals( LocalDate.MAX ) ? end : end.plusDays( 1 ))
            .atTime( 0,0 )
            .atZone( timezone );

        final var sql = format(
            """
            SELECT * FROM  %1$s
              WHERE %2$s > ?
                AND %2$s < ?\
            """, TABLE_BLOODPRESSURE_DATA, COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY );
        final SortedMap<LocalDateTime,DataNode> retValue = new TreeMap<>();
        try( final var statement = requireNonNullArgument( connection, "connection" ).prepareStatement( sql ) )
        {
            statement.setObject( 1, startTime );
            statement.setObject( 2, endTime );
            try( final var resultSet = statement.executeQuery() )
            {
                while( resultSet.next() )
                {
                    final var isIgnored = resultSet.getBoolean( 4 );
                    if( !isIgnored )
                    {
                        final var timestamp = resultSet.getObject( 1, LocalDateTime.class );
                        final var systolic = resultSet.getInt( 2 );
                        final var diastolic = resultSet.getInt( 3 );
                        final var dataNode = new DataNode( systolic, diastolic );
                        retValue.put( timestamp, dataNode );
                    }
                }
            }
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  loadData()

    /**
     *  Registers a report generator.
     *
     *  @param  report  The report generator to register.
     */
    public static final void registerReport( final Report report )
    {
        final var style = requireNonNullArgument( report, "report" ).getStyle().toUpperCase( ROOT );
        m_Registry.put( style, report );
    }   //  registerReport()

    /**
     *  Retrieves the report generator for the given style and format.
     *
     *  @param  style   The report style.
     *  @param  format  The output format for the report.
     *  @return An instance of
     *      {@link Optional}
     *      that holds the report generator. It will be
     *      {@linkplain Optional#empty()}
     *      if there is no registered report for the given style, or when the
     *      style does not support the requested format.
     */
    public static final Optional<Report> retrieveReport( final String style, final ReportFormat format )
    {
        Optional<Report> retValue = Optional.empty();
        final var report = m_Registry.get( requireNotEmptyArgument( style, "style" ).toUpperCase( ROOT ) );
        if( nonNull( report ) && report.supportedOutputFormats().contains( requireNonNullArgument( format, "format" ) ) )
        {
            retValue = Optional.of( report );
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveReport()

    /**
     *  Retrieves the text identified by the given usage and id.
     *
     *  @param  usage   The text usage.
     *  @param  id  The id for the text.
     *  @param  args    The optional components for the text.
     */
    protected final String retrieveText( final TextUse usage, final String id, final Object... args )
    {
        final var retValue = m_Configuration.getText( getClass(), usage, id, args );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  retrieveText()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Set<ReportFormat> supportedOutputFormats()
    {
        return unmodifiableSet( m_SupportedFormats );
    }   //  supportedOutputFormats()
}
//  class ReportBase

/*
 *  End of File
 */