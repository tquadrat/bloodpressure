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

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;

/**
 *  The output formats for a report.
 *
 *  @version $Id: ReportFormat.java 125 2022-02-18 22:24:52Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: ReportFormat.java 125 2022-02-18 22:24:52Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public enum ReportFormat
{
        /*------------------*\
    ====** Enum Declaration **=================================================
        \*------------------*/
    /**
     *  Plain text. It will not contain any graphs or charts.
     */
    TEXT,

    /**
     *  <p>{@summary Plain HTML in a single file.}</p>
     *  <p>Some reports may also use it as a synonym for
     *  {@link #HTML_EMBEDDED}
     *  or
     *  {@link #HTML_FOLDER}.</p>
     */
    HTML,

    /**
     *  <p>{@summary A single HTML file with embedded images and CSS.}</p>
     *  <p>This is meant to be more complex than the output for
     *  {@link #HTML},
     *  but some reports may use it as a synonym.</p>
     */
    HTML_EMBEDDED,

    /**
     *  A folder containing HTML files and images.
     */
    HTML_FOLDER,

    /**
     *  The zipped variant of
     *  {@link #HTML_FOLDER}.
     */
    HTML_ZIPPED,

    /**
     *  A PDF document.
     */
    PDF,

    /**
     *  An OpenText document.
     */
    OPEN_TEXT
}
//  enum ReportFormat

/*
 *  End of File
 */