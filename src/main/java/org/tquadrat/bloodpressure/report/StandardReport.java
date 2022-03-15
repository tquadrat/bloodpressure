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

package org.tquadrat.bloodpressure.report;

import static java.nio.file.Files.writeString;
import static org.apiguardian.api.API.Status.STABLE;
import static org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat.SVG;
import static org.knowm.xchart.VectorGraphicsEncoder.saveVectorGraphic;
import static org.tquadrat.bloodpressure.Diagnosis.HIGH1;
import static org.tquadrat.bloodpressure.Diagnosis.HIGH2;
import static org.tquadrat.bloodpressure.Diagnosis.HIGH3;
import static org.tquadrat.bloodpressure.Diagnosis.LOW;
import static org.tquadrat.bloodpressure.Diagnosis.NORMAL;
import static org.tquadrat.bloodpressure.Diagnosis.NORMAL_HIGH;
import static org.tquadrat.bloodpressure.Diagnosis.OPTIMAL;
import static org.tquadrat.bloodpressure.ReportFormat.HTML;
import static org.tquadrat.bloodpressure.ReportFormat.HTML_EMBEDDED;
import static org.tquadrat.bloodpressure.spi.ReportBase.DestinationType.DEST_FILE;
import static org.tquadrat.foundation.i18n.TextUse.CAPTION;
import static org.tquadrat.foundation.i18n.TextUse.TXT;
import static org.tquadrat.foundation.lang.CommonConstants.UTF8;
import static org.tquadrat.foundation.util.StringUtils.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.DataNode;
import org.tquadrat.bloodpressure.Diagnosis;
import org.tquadrat.bloodpressure.ReportFormat;
import org.tquadrat.bloodpressure.spi.ChartTheme;
import org.tquadrat.bloodpressure.spi.ReportBase;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.exception.UnsupportedEnumError;
import org.tquadrat.foundation.util.Template;

/**
 *  An implementation of
 *  {@link org.tquadrat.bloodpressure.Report}
 *  that provides a standard report.
 *
 *  @version $Id: StandardReport.java 151 2022-03-15 20:39:31Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: StandardReport.java 151 2022-03-15 20:39:31Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public final class StandardReport extends ReportBase
{
        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code StandardReport}.
     */
    public StandardReport()
    {
        super( "STANDARD", HTML, HTML_EMBEDDED );
    }   //  StandardReport()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  {@inheritDoc}
     */
    @Override
    public final void generateReport( final File destination, final ReportFormat format ) throws IOException, SQLException
    {
        //---* Get the data *--------------------------------------------------
        final SortedMap<LocalDateTime,DataNode> totalData;
        try( final var connection = getConfiguration().retrieveConnection() )
        {
            totalData = loadData( connection, getConfiguration().getTimezone(), getConfiguration().getStartDate(), getConfiguration().getEndDate() );
        }
        final var numberOfMeasuring = totalData.size();
        final SortedMap<LocalDate,DataNode> data = compressToDay( totalData );
        final Map<Diagnosis,DistributionNode> distribution = distributeData( totalData );

        final var firstDay = data.firstKey();
        final var lastDay = data.lastKey();
        final var duration = Duration.between( firstDay.atStartOfDay(), lastDay.plusDays( 1 ).atStartOfDay() );
        final var totalDays = duration.toDays();
        final var dayCount = data.size();

        final var average = calculateAverage( data );
        final var weightedAverage = calculateEndWeightedAverage( data );
        final var median = calculateMedian( data );

        //---* Create the chart *----------------------------------------------
        final var width = 800;
        final var height = width / 5 * 3;
        final var theme = new ChartTheme();
        final var chart = new org.knowm.xchart.XYChart( width, height, theme );
        final var categories = new ArrayList<Date>();
        final var systolicValues = new ArrayList<Integer>();
        final var diastolicValues = new ArrayList<Integer>();
        for( final var entry : totalData.entrySet() )
        {
            categories.add( new Date( entry.getKey().atZone( getConfiguration().getTimezone() ).toInstant().toEpochMilli() ) );
            final var node = entry.getValue();
            systolicValues.add( Integer.valueOf( node.systolic() ) );
            diastolicValues.add( Integer.valueOf( node.diastolic() ) );
        }
        chart.addSeries( retrieveText( TXT, "Diastolic" ), categories, diastolicValues );
        chart.addSeries( retrieveText( TXT, "Systolic" ), categories, systolicValues );
        final var outputStream = new ByteArrayOutputStream();
        saveVectorGraphic( chart, outputStream, SVG );

        //---* Gather the contents *-------------------------------------------
        final Map<String,String> contents = new HashMap<>();
        contents.put( "Language", getConfiguration().getLocale().getLanguage() );
        contents.put( "Headline", retrieveText( TXT, "Headline" ) );
        contents.put( "Name", getConfiguration().getName() );
        contents.put( "Birthdate", getConfiguration().getBirthdate().toString() );
        contents.put( "NameCaption", retrieveText( CAPTION, "Name" ) );
        contents.put( "CreationDate", LocalDate.now( getConfiguration().getClock() ).toString() );
        contents.put( "CreationCaption", retrieveText( CAPTION, "Created" ) );
        contents.put( "FirstDay", firstDay.toString() );
        contents.put( "FirstDayCaption", retrieveText( CAPTION, "FirstDay" ) );
        contents.put( "LastDay", lastDay.toString() );
        contents.put( "LastDayCaption", retrieveText( CAPTION, "LastDay" ) );
        contents.put( "TotalDays", format( "% 4d", totalDays ) );
        contents.put( "TotalDaysCaption", retrieveText( CAPTION, "TotalDays" ) );
        contents.put( "DayCount", format( "% 4d", dayCount ) );
        contents.put( "DayCountCaption", retrieveText( CAPTION, "DayCount" ) );
        contents.put( "TotalCount", format( "% 4d", numberOfMeasuring ) );
        contents.put( "TotalCountCaption", retrieveText( CAPTION, "TotalCount" ) );
        contents.put( "TableHeader", retrieveText( TXT, "TableHeader" ) );
        contents.put( "TableHeader1", retrieveText( TXT, "TableHeader1" ) );
        contents.put( "TableHeader2", retrieveText( TXT, "TableHeader2" ) );
        contents.put( "TableHeader3", retrieveText( TXT, "TableHeader3" ) );
        contents.put( "TableHeader4", retrieveText( TXT, "TableHeader4" ) );
        contents.put( "AverageCaption", retrieveText( CAPTION, "Average" ) );
        contents.put( "AverageSystolic", format( "% 3d", average.systolic() ) );
        contents.put( "AverageDiastolic", format( "% 3d", average.diastolic() ) );
        contents.put( "AveragePulsePressure", format( "% 3d", average.pulsePressure() ) );
        contents.put( "AverageAssessment", assessData( average ).toString() );
        contents.put( "WeightedAverageCaption", retrieveText( CAPTION, "WeightedAverage" ) );
        contents.put( "WeightedAverageSystolic", format( "% 3d", weightedAverage.systolic() ) );
        contents.put( "WeightedAverageDiastolic", format( "% 3d", weightedAverage.diastolic() ) );
        contents.put( "WeightedAveragePulsePressure", format( "% 3d", weightedAverage.pulsePressure() ) );
        contents.put( "WeightedAverageAssessment", assessData( weightedAverage ).toString() );
        contents.put( "MedianCaption", retrieveText( CAPTION, "Median" ) );
        contents.put( "MedianSystolic", format( "% 3d", median.systolic() ) );
        contents.put( "MedianDiastolic", format( "% 3d", median.diastolic() ) );
        contents.put( "MedianPulsePressure", format( "% 3d", median.pulsePressure() ) );
        contents.put( "MedianAssessment", assessData( median ).toString() );
        contents.put( "DistributionCaption", retrieveText( CAPTION, "Distribution" ) );
        contents.put( "DistributionColumn1Header", retrieveText( CAPTION, "DistributionColumn1" ) );
        contents.put( "DistributionColumn2Header", retrieveText( CAPTION, "DistributionColumn2" ) );
        contents.put( "Disclaimer", retrieveText( TXT, "Disclaimer" ) );
        contents.put( "Chart", outputStream.toString( UTF8 ) );

        final var maxLenDiagnosisText = Stream.of( LOW, OPTIMAL, NORMAL, NORMAL_HIGH, HIGH1, HIGH2, HIGH3 )
            .map( Diagnosis::toString )
            .mapToInt( String::length )
            .max()
            .orElse( 0 );
        final var diagFormat = format( "%%%ds", maxLenDiagnosisText );

        contents.put( "Diag_Filler", " ".repeat( maxLenDiagnosisText ) );

        for( final var diag : List.of( LOW, OPTIMAL, NORMAL, NORMAL_HIGH, HIGH1, HIGH2, HIGH3 ) )
        {
            final var node = distribution.computeIfAbsent( diag, DistributionNode::new );
            contents.put( format( "Diag_%s", diag.name() ), format( diagFormat, diag.toString() ) );
            contents.put( format( "Diag_%s_SystolicPercent", diag.name() ), format( "% 5.1f%%", node.getSystolicPercentage() ) );
            contents.put( format( "Diag_%s_SystolicCount", diag.name() ), format( "% 4d", node.getSystolicCount() ) );
            contents.put( format( "Diag_%s_DiastolicPercent", diag.name() ), format( "% 5.1f%%", node.getDiastolicPercentage() ) );
            contents.put( format( "Diag_%s_DiastolicCount", diag.name() ), format( "% 4d", node.getDiastolicCount() ) );
        }

        //---* Create the output *---------------------------------------------
        switch( format )
        {
            case HTML, HTML_EMBEDDED -> generateHTMLReport( destination, contents );
            default -> throw new UnsupportedEnumError( format );
        }
    }   //  generateReport()

    /**
     *  Generates the report in
     *  {@link ReportFormat#HTML}
     *  or
     *  {@link ReportFormat#HTML_EMBEDDED}
     *  format.
     *
     *  @param destination  The output file.
     *  @param contents The map with the report data.
     *  @throws IOException An error occurred while writing to the destination.
     */
    private final void generateHTMLReport( final File destination, final Map<String,String> contents ) throws IOException
    {
        final var targetFile = checkDestination( destination, DEST_FILE );
        final var template = new Template(
            """
                <!DOCTYPE html>
                <html lang="${Language}">
                  <head>
                    <meta charset="utf-8">
                    <title>${Headline} ${NameCaption} ${Name} (${Birthdate})</title>
                  </head>
                  <body>
                  <h1>${Headline}</h1>
                  <p>${NameCaption} <b>${Name}</b> (${Birthdate})</p>
                  <p>${CreationCaption} <b>${CreationDate}</b></p>
                  <hr>
                  <table>
                    <tr>
                      <th>${FirstDayCaption}:</th><td>${FirstDay}</td>
                    </tr>
                    <tr>
                      <th>${LastDayCaption}:</th><td>${LastDay}</td>
                    </tr>
                  </table>
                  <br>
                  <table>
                    <tr>
                      <th>${TotalCountCaption}:</th><td align="right">${TotalCount}</td>
                    </tr>
                    <tr>
                      <th>${TotalDaysCaption}:</th><td align="right">${TotalDays}</td>
                    </tr>
                    <tr>
                      <th>${DayCountCaption}:</th><td align="right">${DayCount}</td>
                    </tr>
                  </table>
                  <br>
                  <table border="1">
                    <thead>
                      <tr>
                        <th/>
                        <th>${TableHeader1}</th>
                        <th>${TableHeader2}</th>
                        <th>${TableHeader3}</th>
                        <th>${TableHeader4}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <th>${AverageCaption}</th>
                        <td align="right">${AverageSystolic}</td>
                        <td align="right">${AverageDiastolic}</td>
                        <td align="right">${AveragePulsePressure}</td>
                        <td>${AverageAssessment}</td>
                      </tr>
                      <tr>
                        <th>${WeightedAverageCaption}</th>
                        <td align="right">${WeightedAverageSystolic}</td>
                        <td align="right">${WeightedAverageDiastolic}</td>
                        <td align="right">${WeightedAveragePulsePressure}</td>
                        <td>${WeightedAverageAssessment}</td>
                      </tr>
                      <tr>
                        <th>${MedianCaption}</th>
                        <td align="right">${MedianSystolic}</td>
                        <td align="right">${MedianDiastolic}</td>
                        <td align="right">${MedianPulsePressure}</td>
                        <td>${MedianAssessment}</td>
                      </tr>
                    </tbody>
                  </table>
                  <br>
                  <h2>${DistributionCaption}</h2>
                  <table border="1">
                    <thead>
                      <tr>
                        <th/>
                        <th colspan="3">${DistributionColumn1Header}</th>
                        <th colspan="3">${DistributionColumn2Header}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <th>${Diag_LOW}</th>
                        <td align="right">(&lt; 105)</td>
                        <td align="right">${Diag_LOW_SystolicPercent}</td>
                        <td align="right">${Diag_LOW_SystolicCount}</td>
                        <td align="right">(&lt; 65)</td>
                        <td align="right">${Diag_LOW_DiastolicPercent}</td>
                        <td align="right">${Diag_LOW_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_OPTIMAL}</th>
                        <td align="right">(105-119)</td>
                        <td align="right">${Diag_OPTIMAL_SystolicPercent}</td>
                        <td align="right">${Diag_OPTIMAL_SystolicCount}</td>
                        <td align="right">(65-79)</td>
                        <td align="right">${Diag_OPTIMAL_DiastolicPercent}</td>
                        <td align="right">${Diag_OPTIMAL_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_NORMAL}</th>
                        <td align="right">(120-129)</td>
                        <td align="right">${Diag_NORMAL_SystolicPercent}</td>
                        <td align="right">${Diag_NORMAL_SystolicCount}</td>
                        <td align="right">(80-84)</td>
                        <td align="right">${Diag_NORMAL_DiastolicPercent}</td>
                        <td align="right">${Diag_NORMAL_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_NORMAL_HIGH}</th>
                        <td align="right">(130-139)</td>
                        <td align="right">${Diag_NORMAL_HIGH_SystolicPercent}</td>
                        <td align="right">${Diag_NORMAL_HIGH_SystolicCount}</td>
                        <td align="right">(85-89)</td>
                        <td align="right">${Diag_NORMAL_HIGH_DiastolicPercent}</td>
                        <td align="right">${Diag_NORMAL_HIGH_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH1}</th>
                        <td align="right">(140-159)</td>
                        <td align="right">${Diag_HIGH1_SystolicPercent}</td>
                        <td align="right">${Diag_HIGH1_SystolicCount}</td>
                        <td align="right">(90-99)</td>
                        <td align="right">${Diag_HIGH1_DiastolicPercent}</td>
                        <td align="right">${Diag_HIGH1_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH2}</th>
                        <td align="right">(160-179)</td>
                        <td align="right">${Diag_HIGH2_SystolicPercent}</td>
                        <td align="right">${Diag_HIGH2_SystolicCount}</td>
                        <td align="right">(100-109)</td>
                        <td align="right">${Diag_HIGH2_DiastolicPercent}</td>
                        <td align="right">${Diag_HIGH2_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH3}</th>
                        <td align="right">(&gt;= 180)</td>
                        <td align="right">${Diag_HIGH3_SystolicPercent}</td>
                        <td align="right">${Diag_HIGH3_SystolicCount}</td>
                        <td align="right">(&gt;= 110)</td>
                        <td align="right">${Diag_HIGH3_DiastolicPercent}</td>
                        <td align="right">${Diag_HIGH3_DiastolicCount}</td>
                      </tr>
                    </tbody>
                  </table>
                  <br>
                  <div class="svg">
                    ${Chart}
                  </div>
                  <p>${Disclaimer}</p>
                  </body>
                </html>
                """
        );
        writeString( targetFile, template.replaceVariable( contents ), UTF8 );
    }   //  generateHTMLReport()
}
//  class StandardReport

/*
 *  End of File
 */