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

package org.tquadrat.bloodpressure.importer;

import static java.util.regex.Pattern.compile;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.bloodpressure.InputFormat.CSV;
import static org.tquadrat.foundation.lang.CommonConstants.ISO8859_1;
import static org.tquadrat.foundation.lang.Objects.nonNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.util.StringUtils.format;
import static org.tquadrat.foundation.value.Pressure.MILLIMETER_OF_MERCURY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.DataRecord;
import org.tquadrat.bloodpressure.spi.DataImporterBase;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.value.PressureValue;

/**
 *  The implementation of
 *  {@link org.tquadrat.bloodpressure.DataImporter}
 *  for CSV files from the site &quot;blutdruckdaten.de&quot;-
 *
 *  @version $Id: BlutdruckdatenCSVImporter.java 120 2022-02-10 18:58:05Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: BlutdruckdatenCSVImporter.java 120 2022-02-10 18:58:05Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public final class BlutdruckdatenCSVImporter extends DataImporterBase
{
        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/
    /**
     *  The default encoding.
     */
    public static final Charset DEFAULT_ENCODING = ISO8859_1;

    /**
     *  The pattern that is used to parse a data line: {@value}.
     */
    public static final String PATTERN =
        """
        "([0-9]{2}.[0-9]{2}.[0-9]{4})","([0-9]{2}:[0-9]{2})","([0-9]{1,3})","([0-9]{1,3})","[0-9]{0,3}",".*",".*",".*",".*",".*",".*","([xX]?)",.*\
        """;

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/
    /**
     *  The instance of
     *  {@link java.util.regex.Pattern}
     *  that hold the compiled
     *  {@linkplain #PATTERN pattern}
     *  for parsing a data line.
     */
    private static final Pattern m_Pattern;

    static
    {
        try
        {
            m_Pattern = compile( PATTERN );
        }
        catch( final PatternSyntaxException e )
        {
            throw new ExceptionInInitializerError( e );
        }
    }

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     * Creates a new instance of {@code BlutdruckdatenCSVImporter}.
     */
    public BlutdruckdatenCSVImporter()
    {
        super( CSV, "blutdruckdaten.de" );
    }   //  BlutdruckdatenCSVImporter()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "OverlyBroadThrowsClause" )
    @Override
    public final Collection<DataRecord> parse( final URI source, final ZoneId timezone, final Charset encoding ) throws IOException
    {
        requireNonNullArgument( timezone, "timezone" );
        final var url = requireNonNullArgument( source, "source" ).toURL();
        final var effectiveEncoding = nonNull( encoding ) ? encoding : DEFAULT_ENCODING;

        final var dateParser = new DateTimeFormatterBuilder().appendPattern( "dd.MM.yyyy HH:mm" ).toFormatter();
        final Collection<DataRecord> retValue;

        try( final var inputStream = url.openStream();
            final var reader = new BufferedReader( new InputStreamReader( inputStream, effectiveEncoding ) ) )
        {
            retValue = reader.lines()
                .map( l -> parseLine( l, dateParser, timezone ) )
                .filter( Objects::nonNull )
                .toList();
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  parse()

    /**
     *  Parses the given line.
     *
     *  @param  line    The line to parse.
     *  @param  dateParser  The
     *      {@link DateTimeFormatter}
     *      that is used to parse the timestamp.
     *  @param  timezone    The time zone for the timestamp on the source data.
     *  @return The resulting data record.
     */
    private final DataRecord parseLine( final String line, final DateTimeFormatter dateParser, final ZoneId timezone )
    {
        DataRecord retValue = null;
        final var matcher = m_Pattern.matcher( line );
        if( matcher.matches() )
        {
            final var timestamp = LocalDateTime.parse( format( "%1$s %2$s", matcher.group( 1 ), matcher.group( 2 ) ), dateParser ).atZone( timezone );
            final var systolic = new PressureValue( MILLIMETER_OF_MERCURY, matcher.group( 3 ) );
            final var diastolic = new PressureValue( MILLIMETER_OF_MERCURY, matcher.group( 4 ) );
            final var isIgnored = !matcher.group( 5 ).isBlank();
            retValue = new DataRecord( timestamp, systolic, diastolic, isIgnored );
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  parseLine()
}
//  class BlutdruckdatenCSVImporter

/*
 *  End of File
 */