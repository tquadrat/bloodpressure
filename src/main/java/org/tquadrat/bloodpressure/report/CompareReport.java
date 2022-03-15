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

import static java.lang.Math.abs;
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
 *  that provides a report comparing the last two full quarters.
 *
 *  @version $Id: CompareReport.java 151 2022-03-15 20:39:31Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: CompareReport.java 151 2022-03-15 20:39:31Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public final class CompareReport extends ReportBase
{
        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code CompareReport}.
     */
    public CompareReport()
    {
        super( "COMPARE", HTML, HTML_EMBEDDED );
    }   //  CompareReport()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Collects the data for one quarter.
     *
     *  @param  contents    The target for the collected data.
     *  @param  quarter The number of the quarter to process. 0 is the current
     *      quarter, -1 is the last quarter before the current, and so on.
     *  @throws IOException A problem occurred when creating the chart.
     *  @throws SQLException    A problem was encountered when retrieving the
     *      data from the database.
     */
    private final void collectQuarterData( final Map<String,String> contents, final int quarter ) throws IOException, SQLException
    {
        //---* Calculate the start and end date *------------------------------
        final var today = LocalDate.now( getConfiguration().getClock() );
        final var currentYear = today.getYear();
        final var currentQuarterBegin = LocalDate.of( currentYear, (today.getMonthValue() - 1) / 3 * 3 + 1, 1 );
        final var start = currentQuarterBegin.plusMonths( quarter * 3L );
        final var end = start.plusMonths( 3L ).minusDays( 1L );

        //---* Get the data *--------------------------------------------------
        final SortedMap<LocalDateTime,DataNode> totalData;
        try( final var connection = getConfiguration().retrieveConnection() )
        {
            totalData = loadData( connection, getConfiguration().getTimezone(), start, end );
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
        final var width = 400;
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
        final var diastolicSeries = chart.addSeries( retrieveText( TXT, "Diastolic" ), categories, diastolicValues );
        diastolicSeries.setSmooth( true );
        final var systolicSeries = chart.addSeries( retrieveText( TXT, "Systolic" ), categories, systolicValues );
        systolicSeries.setSmooth( true );
        final var outputStream = new ByteArrayOutputStream();
        saveVectorGraphic( chart, outputStream, SVG );

        //---* Gather the contents *-------------------------------------------
        final var q = abs( quarter );
        contents.put( format( "Q%d_FirstDay", q ), firstDay.toString() );
        contents.put( format( "Q%d_LastDay", q ), lastDay.toString() );
        contents.put( format( "Q%d_TotalDays", q ), format( "% 4d", totalDays ) );
        contents.put( format( "Q%d_DayCount", q ), format( "% 4d", dayCount ) );
        contents.put( format( "Q%d_TotalCount", q ), format( "% 4d", numberOfMeasuring ) );
        contents.put( format( "Q%d_AverageSystolic", q ), format( "% 3d", average.systolic() ) );
        contents.put( format( "Q%d_AverageDiastolic", q ), format( "% 3d", average.diastolic() ) );
        contents.put( format( "Q%d_AveragePulsePressure", q ), format( "% 3d", average.pulsePressure() ) );
        contents.put( format( "Q%d_AverageAssessment", q ), assessData( average ).toString() );
        contents.put( format( "Q%d_WeightedAverageSystolic", q ), format( "% 3d", weightedAverage.systolic() ) );
        contents.put( format( "Q%d_WeightedAverageDiastolic", q ), format( "% 3d", weightedAverage.diastolic() ) );
        contents.put( format( "Q%d_WeightedAveragePulsePressure", q ), format( "% 3d", weightedAverage.pulsePressure() ) );
        contents.put( format( "Q%d_WeightedAverageAssessment", q ), assessData( weightedAverage ).toString() );
        contents.put( format( "Q%d_MedianSystolic", q ), format( "% 3d", median.systolic() ) );
        contents.put( format( "Q%d_MedianDiastolic", q ), format( "% 3d", median.diastolic() ) );
        contents.put( format( "Q%d_MedianPulsePressure", q ), format( "% 3d", median.pulsePressure() ) );
        contents.put( format( "Q%d_MedianAssessment", q ), assessData( median ).toString() );
        contents.put( format( "Q%d_Chart", q ), outputStream.toString( UTF8 ) );

        for( final var diag : List.of( LOW, OPTIMAL, NORMAL, NORMAL_HIGH, HIGH1, HIGH2, HIGH3 ) )
        {
            final var node = distribution.computeIfAbsent( diag, DistributionNode::new );
            contents.put( format( "Q%d_Diag_%s_SystolicPercent", q, diag.name() ), format( "% 5.1f%%", node.getSystolicPercentage() ) );
            contents.put( format( "Q%d_Diag_%s_SystolicCount", q, diag.name() ), format( "% 4d", node.getSystolicCount() ) );
            contents.put( format( "Q%d_Diag_%s_DiastolicPercent", q, diag.name() ), format( "% 5.1f%%", node.getDiastolicPercentage() ) );
            contents.put( format( "Q%d_Diag_%s_DiastolicCount", q, diag.name() ), format( "% 4d", node.getDiastolicCount() ) );
        }
    }   //  collectQuarterData()

    /**
     *  {@inheritDoc}
     */
    @Override
    public final void generateReport( final File destination, final ReportFormat format ) throws IOException, SQLException
    {
        //---* Get the data *--------------------------------------------------
        final Map<String,String> contents = new HashMap<>();
        for( var i = 0; i < 2; ++i ) collectQuarterData( contents, -i );

        //---* Gather the contents *-------------------------------------------
        contents.put( "Language", getConfiguration().getLocale().getLanguage() );
        contents.put( "Headline", retrieveText( TXT, "Headline" ) );
        contents.put( "Name", getConfiguration().getName() );
        contents.put( "Birthdate", getConfiguration().getBirthdate().toString() );
        contents.put( "NameCaption", retrieveText( CAPTION, "Name" ) );
        contents.put( "CreationDate", LocalDate.now( getConfiguration().getClock() ).toString() );
        contents.put( "CreationCaption", retrieveText( CAPTION, "Created" ) );
        contents.put( "Q1", retrieveText( TXT, "PreviousQuarter" ) );
        contents.put( "Q0", retrieveText( TXT, "CurrentQuarter" ) );
        contents.put( "FirstDayCaption", retrieveText( CAPTION, "FirstDay" ) );
        contents.put( "LastDayCaption", retrieveText( CAPTION, "LastDay" ) );
        contents.put( "TotalDaysCaption", retrieveText( CAPTION, "TotalDays" ) );
        contents.put( "DayCountCaption", retrieveText( CAPTION, "DayCount" ) );
        contents.put( "TotalCountCaption", retrieveText( CAPTION, "TotalCount" ) );
        contents.put( "TableHeader", retrieveText( TXT, "TableHeader" ) );
        contents.put( "TableHeader1", retrieveText( TXT, "TableHeader1" ) );
        contents.put( "TableHeader2", retrieveText( TXT, "TableHeader2" ) );
        contents.put( "TableHeader3", retrieveText( TXT, "TableHeader3" ) );
        contents.put( "TableHeader4", retrieveText( TXT, "TableHeader4" ) );
        contents.put( "AverageCaption", retrieveText( CAPTION, "Average" ) );
        contents.put( "WeightedAverageCaption", retrieveText( CAPTION, "WeightedAverage" ) );
        contents.put( "MedianCaption", retrieveText( CAPTION, "Median" ) );
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
            contents.put( format( "Diag_%s", diag.name() ), format( diagFormat, diag.toString() ) );
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
                  <table border = "1">
                    <thead>
                      <tr>
                        <th width="30%"/>
                        <th width="35%"
                            align="center"
                            colspan="4">${Q1}</th>
                        <th width="35%"
                            align="center"
                            colspan="4">${Q0}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <th align="right">${FirstDayCaption}</th>
                        <td align="center"
                            colspan="4">${Q1_FirstDay}</td>
                        <td align="center"
                            colspan="4">${Q0_FirstDay}</td>
                      </tr>
                      <tr>
                        <th align="right">${LastDayCaption}</th>
                        <td align="center"
                            colspan="4">${Q1_LastDay}</td>
                        <td align="center"
                            colspan="4">${Q0_LastDay}</td>
                      </tr>
                      <tr><td colspan="9">&nbsp;</tr>
                      <tr>
                        <th align="right">${TotalCountCaption}</th>
                        <td align="right"
                            colspan="4">${Q1_TotalCount}</td>
                        <td align="right"
                            colspan="4">${Q0_TotalCount}</td>
                      </tr>
                      <tr>
                        <th align="right">${TotalDaysCaption}</th>
                        <td align="right"
                            colspan="4">${Q1_TotalDays}</td>
                        <td align="right"
                            colspan="4">${Q0_TotalDays}</td>
                      </tr>
                      <tr>
                        <th align="right">${DayCountCaption}</th>
                        <td align="right"
                            colspan="4">${Q1_DayCount}</td>
                        <td align="right"
                            colspan="4">${Q0_DayCount}</td>
                      </tr>
                      <tr><td colspan="9">&nbsp;</tr>
                      <tr>
                        <th/>
                        <th>${TableHeader1}</th>
                        <th>${TableHeader2}</th>
                        <th>${TableHeader3}</th>
                        <th>${TableHeader4}</th>
                        <th>${TableHeader1}</th>
                        <th>${TableHeader2}</th>
                        <th>${TableHeader3}</th>
                        <th>${TableHeader4}</th>
                      </tr>
                      <tr>
                        <th align="right">${AverageCaption}</th>
                        <td align="right">${Q1_AverageSystolic}</td>
                        <td align="right">${Q1_AverageDiastolic}</td>
                        <td align="right">${Q1_AveragePulsePressure}</td>
                        <td>${Q1_AverageAssessment}</td>
                        <td align="right">${Q0_AverageSystolic}</td>
                        <td align="right">${Q0_AverageDiastolic}</td>
                        <td align="right">${Q0_AveragePulsePressure}</td>
                        <td>${Q0_AverageAssessment}</td>
                      </tr>
                      <tr>
                        <th align="right">${WeightedAverageCaption}</th>
                        <td align="right">${Q1_WeightedAverageSystolic}</td>
                        <td align="right">${Q1_WeightedAverageDiastolic}</td>
                        <td align="right">${Q1_WeightedAveragePulsePressure}</td>
                        <td>${Q1_WeightedAverageAssessment}</td>
                        <td align="right">${Q0_WeightedAverageSystolic}</td>
                        <td align="right">${Q0_WeightedAverageDiastolic}</td>
                        <td align="right">${Q0_WeightedAveragePulsePressure}</td>
                        <td>${Q0_WeightedAverageAssessment}</td>
                      </tr>
                      <tr>
                        <th align="right">${MedianCaption}</th>
                        <td align="right">${Q1_MedianSystolic}</td>
                        <td align="right">${Q1_MedianDiastolic}</td>
                        <td align="right">${Q1_MedianPulsePressure}</td>
                        <td>${Q1_MedianAssessment}</td>
                        <td align="right">${Q0_MedianSystolic}</td>
                        <td align="right">${Q0_MedianDiastolic}</td>
                        <td align="right">${Q0_MedianPulsePressure}</td>
                        <td>${Q0_MedianAssessment}</td>
                      </tr>
                      <tr>
                        <td/>
                        <td colspan="4">
                          <div class="svg">${Q1_Chart}</div>
                        </td>
                        <td colspan="4">
                          <div class="svg">${Q0_Chart}</div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                  <br>
                  <h2>${DistributionCaption}</h2>
                  <table border = "1">
                    <thead>
                      <tr>
                        <th width="20%"
                            rowspan="2"/>
                        <th width="40%"
                            align="center"
                            colspan="6">${Q1}</th>
                        <th width="40%"
                            align="center"
                            colspan="6">${Q0}</th>
                      </tr>
                      <tr>
                        <th colspan="3">${DistributionColumn1Header}</th>
                        <th colspan="3">${DistributionColumn2Header}</th>
                        <th colspan="3">${DistributionColumn1Header}</th>
                        <th colspan="3">${DistributionColumn2Header}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <th>${Diag_LOW}</th>
                        <td align="right">(&lt; 105)</td>
                        <td align="right">${Q1_Diag_LOW_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_LOW_SystolicCount}</td>
                        <td align="right">(&lt; 65)</td>
                        <td align="right">${Q1_Diag_LOW_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_LOW_DiastolicCount}</td>
                        <td align="right">(&lt; 105)</td>
                        <td align="right">${Q0_Diag_LOW_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_LOW_SystolicCount}</td>
                        <td align="right">(&lt; 65)</td>
                        <td align="right">${Q0_Diag_LOW_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_LOW_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_OPTIMAL}</th>
                        <td align="right">(105-119)</td>
                        <td align="right">${Q1_Diag_OPTIMAL_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_OPTIMAL_SystolicCount}</td>
                        <td align="right">(65-79)</td>
                        <td align="right">${Q1_Diag_OPTIMAL_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_OPTIMAL_DiastolicCount}</td>
                        <td align="right">(105-119)</td>
                        <td align="right">${Q0_Diag_OPTIMAL_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_OPTIMAL_SystolicCount}</td>
                        <td align="right">(65-79)</td>
                        <td align="right">${Q0_Diag_OPTIMAL_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_OPTIMAL_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_NORMAL}</th>
                        <td align="right">(120-129)</td>
                        <td align="right">${Q1_Diag_NORMAL_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_NORMAL_SystolicCount}</td>
                        <td align="right">(80-84)</td>
                        <td align="right">${Q1_Diag_NORMAL_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_NORMAL_DiastolicCount}</td>
                        <td align="right">(120-129)</td>
                        <td align="right">${Q0_Diag_NORMAL_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_NORMAL_SystolicCount}</td>
                        <td align="right">(80-84)</td>
                        <td align="right">${Q0_Diag_NORMAL_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_NORMAL_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_NORMAL_HIGH}</th>
                        <td align="right">(130-139)</td>
                        <td align="right">${Q1_Diag_NORMAL_HIGH_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_NORMAL_HIGH_SystolicCount}</td>
                        <td align="right">(85-89)</td>
                        <td align="right">${Q1_Diag_NORMAL_HIGH_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_NORMAL_HIGH_DiastolicCount}</td>
                        <td align="right">(130-139)</td>
                        <td align="right">${Q0_Diag_NORMAL_HIGH_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_NORMAL_HIGH_SystolicCount}</td>
                        <td align="right">(85-89)</td>
                        <td align="right">${Q0_Diag_NORMAL_HIGH_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_NORMAL_HIGH_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH1}</th>
                        <td align="right">(140-159)</td>
                        <td align="right">${Q1_Diag_HIGH1_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH1_SystolicCount}</td>
                        <td align="right">(90-99)</td>
                        <td align="right">${Q1_Diag_HIGH1_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH1_DiastolicCount}</td>
                        <td align="right">(140-159)</td>
                        <td align="right">${Q0_Diag_HIGH1_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH1_SystolicCount}</td>
                        <td align="right">(90-99)</td>
                        <td align="right">${Q0_Diag_HIGH1_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH1_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH2}</th>
                        <td align="right">(160-179)</td>
                        <td align="right">${Q1_Diag_HIGH2_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH2_SystolicCount}</td>
                        <td align="right">(100-109)</td>
                        <td align="right">${Q1_Diag_HIGH2_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH2_DiastolicCount}</td>
                        <td align="right">(160-179)</td>
                        <td align="right">${Q0_Diag_HIGH2_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH2_SystolicCount}</td>
                        <td align="right">(100-109)</td>
                        <td align="right">${Q0_Diag_HIGH2_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH2_DiastolicCount}</td>
                      </tr>
                      <tr>
                        <th>${Diag_HIGH3}</th>
                        <td align="right">(&gt;= 180)</td>
                        <td align="right">${Q1_Diag_HIGH3_SystolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH3_SystolicCount}</td>
                        <td align="right">(&gt;= 110)</td>
                        <td align="right">${Q1_Diag_HIGH3_DiastolicPercent}</td>
                        <td align="right">${Q1_Diag_HIGH3_DiastolicCount}</td>
                        <td align="right">(&gt;= 180)</td>
                        <td align="right">${Q0_Diag_HIGH3_SystolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH3_SystolicCount}</td>
                        <td align="right">(&gt;= 110)</td>
                        <td align="right">${Q0_Diag_HIGH3_DiastolicPercent}</td>
                        <td align="right">${Q0_Diag_HIGH3_DiastolicCount}</td>
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
}
//  class CompareReport

/*
 *  End of File
 */