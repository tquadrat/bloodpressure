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
import static org.tquadrat.bloodpressure.Diagnosis.HIGH1;
import static org.tquadrat.bloodpressure.Diagnosis.HIGH2;
import static org.tquadrat.bloodpressure.Diagnosis.HIGH3;
import static org.tquadrat.bloodpressure.Diagnosis.LOW;
import static org.tquadrat.bloodpressure.Diagnosis.NORMAL;
import static org.tquadrat.bloodpressure.Diagnosis.NORMAL_HIGH;
import static org.tquadrat.bloodpressure.Diagnosis.OPTIMAL;
import static org.tquadrat.bloodpressure.ReportFormat.HTML;
import static org.tquadrat.bloodpressure.ReportFormat.HTML_EMBEDDED;
import static org.tquadrat.bloodpressure.ReportFormat.TEXT;
import static org.tquadrat.bloodpressure.spi.ReportBase.DestinationType.DEST_FILE;
import static org.tquadrat.foundation.i18n.TextUse.CAPTION;
import static org.tquadrat.foundation.i18n.TextUse.TXT;
import static org.tquadrat.foundation.lang.CommonConstants.UTF8;
import static org.tquadrat.foundation.util.StringUtils.format;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.DataNode;
import org.tquadrat.bloodpressure.Diagnosis;
import org.tquadrat.bloodpressure.ReportFormat;
import org.tquadrat.bloodpressure.spi.ReportBase;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.exception.UnsupportedEnumError;
import org.tquadrat.foundation.util.Template;

/**
 *  An implementation of
 *  {@link org.tquadrat.bloodpressure.Report}
 *  that provides a very basic report.
 *
 *  @version $Id: SimpleReport.java 126 2022-02-19 21:13:35Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: SimpleReport.java 126 2022-02-19 21:13:35Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public final class SimpleReport extends ReportBase
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
     *  Creates a new instance of {@code SimpleReport}.
     */
    public SimpleReport()
    {
        super( "SIMPLE", TEXT, HTML, HTML_EMBEDDED );
    }   //  SimpleReport()

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
        final int numberOfMeasuring;
        final SortedMap<LocalDate, DataNode> data;
        final Map<Diagnosis,DistributionNode> distribution;
        try( final var connection = getConfiguration().retrieveConnection() )
        {
            final var totalData = loadData( connection, getConfiguration().getTimezone(), getConfiguration().getStartDate(), getConfiguration().getEndDate() );
            numberOfMeasuring = totalData.size();
            data = compressToDay( totalData );
            distribution = distributeData( totalData );
        }

        final var firstDay = data.firstKey();
        final var lastDay = data.lastKey();
        final var duration = Duration.between( firstDay.atStartOfDay(), lastDay.plusDays( 1 ).atStartOfDay() );
        final var totalDays = duration.toDays();
        final var dayCount = data.size();

        final var average = calculateAverage( data );
        final var weightedAverage = calculateEndWeightedAverage( data );
        final var median = calculateMedian( data );

        //---* Gather the contents *-------------------------------------------
        final Map<String,String> contents = new HashMap<>();
        contents.put( "Language", getConfiguration().getLocale().getLanguage() );
        contents.put( "Headline", retrieveText( TXT, "Headline" ) );
        contents.put( "Name", getConfiguration().getName() );
        contents.put( "Birthdate", getConfiguration().getBirthdate().toString() );
        contents.put( "NameCaption", retrieveText( CAPTION, "Name" ) );
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
            case TEXT -> generateTextReport( destination, contents );
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
                  <p>${Disclaimer}</p>
                  </body>
                </html>
                """
        );
        writeString( targetFile, template.replaceVariable( contents ), UTF8 );
    }   //  generateHTMLReport()

    /**
     *  Generates the report in
     *  {@link ReportFormat#TEXT}
     *  format.
     *
     *  @param destination  The output file.
     *  @param contents The map with the report data.
     *  @throws IOException An error occurred while writing to the destination.
     */
    private final void generateTextReport( final File destination, final Map<String,String> contents ) throws IOException
    {
        final var targetFile = checkDestination( destination, DEST_FILE );
        final var template = new Template(
            """
                ${Headline}
                
                ${NameCaption} ${Name} (${Birthdate})
                
                -------------------------------------------------------------------------------
                            
                ${FirstDayCaption}: ${FirstDay}
                ${LastDayCaption}: ${LastDay}
                            
                ${TotalCountCaption}: ${TotalCount}
                ${TotalDaysCaption}: ${TotalDays}
                ${DayCountCaption}: ${DayCount}
                            
                                               ${TableHeader}
                ${AverageCaption}:   ${AverageSystolic}         ${AverageDiastolic}          ${AveragePulsePressure}          ${AverageAssessment}
                ${WeightedAverageCaption}:   ${WeightedAverageSystolic}         ${WeightedAverageDiastolic}          ${WeightedAveragePulsePressure}          ${WeightedAverageAssessment}
                ${MedianCaption}:   ${MedianSystolic}         ${MedianDiastolic}          ${MedianPulsePressure}          ${MedianAssessment}
                
                ${DistributionCaption}
                ${Diag_Filler}  ${DistributionColumn1Header} ${DistributionColumn2Header}
                ${Diag_LOW}  (< 105)    ${Diag_LOW_SystolicPercent}   (< 65)    ${Diag_LOW_DiastolicPercent}
                ${Diag_OPTIMAL}  (105-119)  ${Diag_OPTIMAL_SystolicPercent}   (65-79)   ${Diag_OPTIMAL_DiastolicPercent}
                ${Diag_NORMAL}  (120-129)  ${Diag_NORMAL_SystolicPercent}   (80-84)   ${Diag_NORMAL_DiastolicPercent}
                ${Diag_NORMAL_HIGH}  (130-139)  ${Diag_NORMAL_HIGH_SystolicPercent}   (85-89)   ${Diag_NORMAL_HIGH_DiastolicPercent}
                ${Diag_HIGH1}  (140-159)  ${Diag_HIGH1_SystolicPercent}   (90-99)   ${Diag_HIGH1_DiastolicPercent}
                ${Diag_HIGH2}  (160-179)  ${Diag_HIGH2_SystolicPercent}   (100-109) ${Diag_HIGH2_DiastolicPercent}
                ${Diag_HIGH3}  (>= 180)   ${Diag_HIGH3_SystolicPercent}   (>= 110)  ${Diag_HIGH3_DiastolicPercent}
                
                ${Disclaimer}
                """
        );
        writeString( targetFile, template.replaceVariable( contents ), UTF8 );
    }   //  generateTextReport()
}
//  class SimpleReport

/*
 *  End of File
 */