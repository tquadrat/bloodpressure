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

import static java.lang.System.err;
import static java.lang.System.out;
import static java.nio.file.Files.lines;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.bloodpressure.importer.BlutdruckdatenCSVImporter.PATTERN;
import static org.tquadrat.foundation.lang.Objects.nonNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.exception.ApplicationError;

/**
 *  Checks the validity of the regex pattern that filters the valid records.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 */
@ClassVersion( sourceVersion = "$Id: PatternTester.java 123 2022-02-13 19:33:21Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public class PatternTester
{
        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  The program entry point.
     *
     *  @param  args    The command line arguments.
     */
    public static final void main( final String... args )
    {
        try
        {
            final var inputFile = new File( "data/export_2022-01-23T1215.csv" ).getAbsoluteFile();
            if( !inputFile.exists() )
            {
                throw new ApplicationError( "File does not exist" );
            }
            lines( inputFile.toPath(), StandardCharsets.ISO_8859_1 )
                .filter( l -> !Pattern.matches( PATTERN, l ) )
                .forEach( out::println );
        }
        catch( final ApplicationError e )
        {
            err.println( e.getLocalizedMessage() );
            final var cause = e.getCause();
            if( nonNull( cause ) )
            {
                e.printStackTrace( err );
            }
        }
        catch( final Throwable t )
        {
            t.printStackTrace( err );
        }
    }   //  main()
}
//  class PatternTester

/*
 *  End of File
 */