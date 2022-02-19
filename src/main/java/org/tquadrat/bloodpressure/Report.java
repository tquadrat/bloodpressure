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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.spi.ReportBase;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The definition of a report generator.
 *
 *  @version $Id: Report.java 126 2022-02-19 21:13:35Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: Report.java 126 2022-02-19 21:13:35Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public sealed interface Report
    permits ReportBase
{
        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Generates the report and writes it to the destination from the
     *  configuration.
     *
     *  @param  destination The destination for the generated report.
     *  @param  format  The output format for the report.
     *  @throws IOException A problem was encountered when writing the report
     *      to the destination.
     *  @throws SQLException    A problem was encountered when retrieving the
     *      data from the database.
     */
    public void generateReport( final File destination, final ReportFormat format ) throws IOException, SQLException;

    /**
     *  Returns the report style.
     *
     * @return  The report style.
     */
    public String getStyle();

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
    public static Optional<Report> retrieveReport( final String style, final ReportFormat format )
    {
        return ReportBase.retrieveReport( style, format );
    }   //  retrieveReport()

    /**
     *  Returns the supported output formats.
     *
     *  @return The supported output formats.
     */
    public Set<ReportFormat> supportedOutputFormats();
}
//  interface Report

/*
 *  End of File
 */