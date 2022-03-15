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

import static java.lang.System.err;
import static java.lang.System.out;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newOutputStream;
import static java.util.Locale.ROOT;
import static java.util.Objects.nonNull;
import static org.apiguardian.api.API.Status.STABLE;
import static org.tquadrat.bloodpressure.Configuration.COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY;
import static org.tquadrat.bloodpressure.Configuration.TABLE_BLOODPRESSURE_DATA;
import static org.tquadrat.bloodpressure.DataImporter.retrieveDataImporter;
import static org.tquadrat.bloodpressure.Report.retrieveReport;
import static org.tquadrat.bloodpressure.spi.DataImporterBase.registerImporter;
import static org.tquadrat.bloodpressure.spi.ReportBase.registerReport;
import static org.tquadrat.foundation.lang.CommonConstants.UTF8;
import static org.tquadrat.foundation.lang.CommonConstants.ZONE_UTC;
import static org.tquadrat.foundation.lang.DebugOutput.isDebug;
import static org.tquadrat.foundation.lang.DebugOutput.isTest;
import static org.tquadrat.foundation.lang.Objects.isNull;
import static org.tquadrat.foundation.lang.Objects.requireNonNullArgument;
import static org.tquadrat.foundation.util.StringUtils.format;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.importer.BlutdruckdatenCSVImporter;
import org.tquadrat.bloodpressure.report.CompareReport;
import org.tquadrat.bloodpressure.report.SimpleReport;
import org.tquadrat.bloodpressure.report.StandardReport;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.annotation.ProgramClass;
import org.tquadrat.foundation.exception.ApplicationError;
import org.tquadrat.foundation.i18n.Message;
import org.tquadrat.foundation.i18n.Translation;

/**
 *  The main entry point into the Blood Pressure Statistics Application.
 *
 * @version $Id: Application.java 151 2022-03-15 20:39:31Z tquadrat $
 * @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 * @UMLGraph.link
 * @since 0.0.1
 */
@ProgramClass
@ClassVersion( sourceVersion = "$Id: Application.java 151 2022-03-15 20:39:31Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public final class Application
{
        /*---------------*\
    ====** Inner Classes **====================================================
        \*---------------*/

        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/
    /**
     *  Message: An attempt to create a folder failed.
     */
    @Message
    (
        description = "The error message about a failed folder creation.",
        translations =
        {
            @Translation( language = "de", text = "Beim Erstellen des Verzeichnisses '%1$s' ist ein Fehler aufgetreten" ),
            @Translation( language = "en", text = "An error occurred on creating the folder '%1$s" )
        }
    )
    public static final int MSG_CreateFolderFailed = 7;

    /**
     *  Message: An attempt to perform a database operation failed.
     */
    @Message
    (
        description = "The error message about a failed database operation.",
        translations =
        {
            @Translation( language = "de", text = "Beim Zugriff auf die Datenbank ist ein Fehler aufgetreten" ),
            @Translation( language = "en", text = "An error occurred while accessing the database" )
        }
    )
    public static final int MSG_DatabaseAccessFailed = 4;

    /**
     *  Message: Import file is missing.
     */
    @Message
    (
        description = "The error message about a missing import file.",
        translations =
        {
            @Translation( language = "de", text = "Die Import-Datei '%1$s' existiert nicht" ),
            @Translation( language = "en", text = "Import file '%1$s' is missing" )
        }
    )
    public static final int MSG_FileIsMissing = 1;

    /**
     *  Message: File is invalid.
     */
    @Message
    (
        description = "The error message about an invalid output destination.",
        translations =
        {
            @Translation( language = "de", text = "Die Ausgabedatei '%1$s' ist unzulässig" ),
            @Translation( language = "en", text = "The output file '%1$s' is invalid" )
        }
    )
    public static final int MSG_InvalidFile = 5;

    /**
     *  Message: Folder is invalid.
     */
    @Message
    (
        description = "The error message about an invalid output destination.",
        translations =
        {
            @Translation( language = "de", text = "Der Ausgabeordner '%1$s' ist unzulässig" ),
            @Translation( language = "en", text = "The output folder '%1$s' is invalid" )
        }
    )
    public static final int MSG_InvalidFolder = 10;

    /**
     *  Message: Birthdate is missing.
     */
    @Message
    (
        description = "The error message about a missing birthdate on the input.",
        translations =
        {
            @Translation( language = "de", text = "Das Geburtsdatum fehlt" ),
            @Translation( language = "en", text = "No birthdate" )
        }
    )
    public static final int MSG_NoBirthdate = 11;

    /**
     *  Message: Importer is missing.
     */
    @Message
    (
        description = "The error message about a missing importer implementation.",
        translations =
        {
            @Translation( language = "de", text = "Ein Import-Modul für eine Datei mit der Herkunft '%1$s' und dem Format '%2$s' konnte nicht gefunden werden" ),
            @Translation( language = "en", text = "Could not find an import module for a file with the origin '%1$s' and the format '%2$s'" )
        }
    )
    public static final int MSG_NoImporter = 2;

    /**
     *  Message: Name is missing.
     */
    @Message
        (
            description = "The error message about a missing name on the input.",
            translations =
                {
                    @Translation( language = "de", text = "Der Name fehlt" ),
                    @Translation( language = "en", text = "No name" )
                }
        )
    public static final int MSG_NoName = 12;

    /**
     *  Message: No report.
     */
    @Message
    (
        description = "The error message indicating that there is no report of the given style supporting the given output format.",
        translations =
        {
            @Translation( language = "de", text = "Es gibt keinen Report '%1$s', der das Ausgabeformat '%2$s' unterstützt" ),
            @Translation( language = "en", text = "There is no report '%1$s' supporting the output format '%2$s'" )
        }
    )
    public static final int MSG_NoReport = 8;

    /**
     *  Message: Reading failed.
     */
    @Message
    (
        description = "The error message about a failure when reading an import file.",
        translations =
        {
            @Translation( language = "de", text = "Die Import-Datei '%1$s' konnte nicht gelesen werden" ),
            @Translation( language = "en", text = "Could not read the import file '%1$s'" )
        }
    )
    public static final int MSG_ReadingFailed = 3;

    /**
     *  Message: Report generation failed.
     */
    @Message
    (
        description = "The error message about a failure on generating a report.",
        translations =
        {
            @Translation( language = "de", text = "Report konnte nicht erstellt werden" ),
            @Translation( language = "en", text = "Cannot generate report" )
        }
    )
    public static final int MSG_ReportGenerationFailed = 9;

    /**
     *  Message: Writing to file failed.
     */
    @Message
        (
            description = "The error message about a failure when writing to a file.",
            translations =
                {
                    @Translation( language = "de", text = "Beim Schreiben in die Datei '%1$s' ist ein Fehler aufgetreten" ),
                    @Translation( language = "en", text = "Writing to the file '%1$s' failed" )
                }
        )
    public static final int MSG_WriteToFileFailed = 6;

        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The configuration for this application.
     */
    private final Configuration m_Configuration;

        /*------------------------*\
    ====** Static Initialisations **===========================================
        \*------------------------*/
    static
    {
        //---* Creates the importer and adds them to the registry *------------
        registerImporter( new BlutdruckdatenCSVImporter() );

        //---* Creates the reports and adds them to the registry *-------------
        registerReport( new SimpleReport() );
        registerReport( new StandardReport() );
        registerReport( new CompareReport() );
    }

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance for {@code Application}.
     */
    private Application()
    {
        //---* Retrieve the configuration bean *-------------------------------
        m_Configuration = Configuration.getInstance();
    }   //  Application()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Dumps the data from the database to the given file.
     *
     *  @param  dumpFile    The destination for the data.
     */
    private final void dumpData( final File dumpFile )
    {
        final var file = requireNonNullArgument( dumpFile, "dumpFile" ).toPath();
        if( exists( file ) && !isRegularFile( file ) ) throw new ApplicationError( m_Configuration.getMessage( MSG_InvalidFile, dumpFile.getAbsolutePath() ) );

        final var folder = file.getParent();
        if( !exists( folder ) )
        {
            try
            {
                createDirectories( folder );
            }
            catch( IOException e )
            {
                throw new ApplicationError( m_Configuration.getMessage( MSG_CreateFolderFailed, dumpFile.getParentFile().getAbsolutePath() ), e );
            }
        }

        final var timestampFormatter = new DateTimeFormatterBuilder()
            .appendPattern( "yyyy-MM-dd HH:mm:ssX" )
            .toFormatter( ROOT );

        try( final var writer = new PrintWriter( new OutputStreamWriter( newOutputStream( file ), UTF8 ) ) )
        {
            writer.printf(
                """
                MERGE INTO %1$s
                KEY( %2$s )
                VALUES
                """, TABLE_BLOODPRESSURE_DATA, COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY
            );
            try( final var connection = m_Configuration.retrieveConnection() )
            {
                try( final var statement = connection.createStatement() )
                {
                    final var sql = format(
                        """
                        SELECT * FROM %1$s
                        """, TABLE_BLOODPRESSURE_DATA );
                    try( final var resultSet = statement.executeQuery( sql ) )
                    {
                        var isFirst = true;
                        while( resultSet.next() )
                        {
                            if( isFirst )
                            {
                                isFirst = false;
                            }
                            else
                            {
                                writer.println( "," );
                            }
                            final var timestamp = resultSet.getObject( 1, ZonedDateTime.class ).withZoneSameInstant( ZONE_UTC );
                            final var systolic = resultSet.getInt( 2 );
                            final var diastolic = resultSet.getInt( 3 );
                            final var isIgnored = resultSet.getBoolean( 4 );

                            writer.printf( "('%s', %d, %d, %B )", timestamp.format( timestampFormatter ), systolic, diastolic, isIgnored );
                        }
                        writer.println();
                    }
                }
            }
        }
        catch( final SQLException e )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_DatabaseAccessFailed ), e );
        }
        catch( final IOException e )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_WriteToFileFailed, dumpFile.getAbsolutePath() ), e );
        }
    }   //  dumpData()
    /**
     *  Does the programs work.
     *
     *  @throws IOException An I/O issue occurred.
     */
    @SuppressWarnings( "RedundantThrows" )
    private final void execute() throws IOException
    {
        //---* Import more data *----------------------------------------------
        m_Configuration.getImportFile().ifPresent( this::importData );

        //---* Dump the existing data *----------------------------------------
        m_Configuration.getDumpFile().ifPresent( this::dumpData );

        //---* Create a report *-----------------------------------------------
        m_Configuration.getReportFile().ifPresent( this::generateReport );
    }   //  execute()

    /**
     *  <p>{@summary Generates a report and writes it to the given
     *  destination.} Whether this destination denotes a folder or a file
     *  depends on the
     *  {@link ReportFormat}.</p>
     *  <p>Not all formats are defined for all
     *  {@linkplain Configuration#getReportStyle() report styles}.</p>
     *
     *  @param  reportDestination   The destination for the generated report.
     *
     *  @see Configuration#getReportFormat()
     */
    private final void generateReport( final File reportDestination )
    {
        final var style = m_Configuration.getReportStyle();
        final var format = m_Configuration.getReportFormat();
        final var report = retrieveReport( style, format )
            .orElseThrow( () -> new ApplicationError( m_Configuration.getMessage( MSG_NoReport, style, format ) ) );
        try
        {
            report.generateReport( reportDestination, format );
        }
        catch( final IOException | SQLException e )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_ReportGenerationFailed ), e );
        }
    }   //  generateReport()

    /**
     *  Imports the contents of the given file.
     *
     *  @param  importFile  The file to import.
     */
    private final void importData( final File importFile )
    {
        if( !importFile.exists() )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_FileIsMissing, importFile.getAbsolutePath() ) );
        }

        //---* Retrieve the import implementation *----------------------------
        final var importer = retrieveDataImporter( m_Configuration.getImportSource(), m_Configuration.getImportFormat() )
            .orElseThrow( () -> new ApplicationError( m_Configuration.getMessage( MSG_NoImporter, m_Configuration.getImportSource(), m_Configuration.getImportFormat() ) ) );

        //---* Get the data *--------------------------------------------------
        try( final var connection = m_Configuration.retrieveConnection() )
        {
            //---* Get the data from the import file *-------------------------
            final var data = importer.parse( importFile.toURI(), m_Configuration.getImportTimeZone().orElseGet( m_Configuration::getTimezone ), m_Configuration.getImportCharset().orElse( null ) );

            //---* Prepare the insert statement *------------------------------
            final var sql = format(
                """
                MERGE INTO %1$s
                KEY( %2$s )
                VALUES( ?, ?, ?, ? )\
                """, TABLE_BLOODPRESSURE_DATA, COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY );
            try( final var statement = connection.prepareStatement( sql ) )
            {
                //---* Insert the data *---------------------------------------
                for( final var r : data )
                {
                    statement.setObject( 1, r.timestamp() );
                    statement.setInt( 2, r.getSystolicPressure() );
                    statement.setInt( 3, r.getDiastolicPressure() );
                    statement.setBoolean( 4, r.isIgnored() );

                    statement.execute();
                }
            }
        }
        catch( final IOException e )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_ReadingFailed, importFile.getAbsolutePath() ), e );
        }
        catch( SQLException e )
        {
            throw new ApplicationError( m_Configuration.getMessage( MSG_DatabaseAccessFailed ), e );
        }
    }   //  import()

    /**
     *  Initialises the application.
     *
     *  @param  args    The command line arguments.
     *  @return {@code true} if the initialisation was successful and the
     *      application may proceed, {@code false} if it failed and the
     *      application needs to terminate.
     *  @throws IOException An I/O issue occurred.
     *  @throws java.sql.SQLException   A problem occurred when initialising
     *      the database.
     */
    private final boolean initialize( final String [] args ) throws IOException, SQLException
    {
        //---* Read the INIFile *----------------------------------------------
        m_Configuration.loadINIFile();

        //---* Parse the command line *----------------------------------------
        var retValue = m_Configuration.parseCommandLine( args );
        if( !retValue )
        {
            err.printf( "Error: %s%n", m_Configuration.retrieveParseErrorMessage().orElse( "??" ) );
            m_Configuration.printUsage( err, "bloodpressure" );
        }
        else
        {
            //---* Check the database *----------------------------------------
            try( final var connection = m_Configuration.retrieveConnection() )
            {
                var sql = format(
                    """
                    SELECT * FROM information_schema.tables
                      WHERE TABLE_NAME = '%s'
                        AND TABLE_SCHEMA = 'PUBLIC'\
                    """, TABLE_BLOODPRESSURE_DATA );

                @SuppressWarnings( "UnusedAssignment" )
                boolean databaseIsInitialized = false;
                try( final var sqlStatement = connection.createStatement() )
                {
                    try( final var resultSet = sqlStatement.executeQuery( sql ) )
                    {
                        databaseIsInitialized = resultSet.next(); // The table exists …
                    }
                }

                if( isDebug() )
                {
                    sql = format(
                        """
                        SELECT table_schema, table_name FROM information_schema.tables\
                        """, TABLE_BLOODPRESSURE_DATA );

                    try( final var sqlStatement = connection.createStatement() )
                    {
                        try( final var resultSet = sqlStatement.executeQuery( sql ) )
                        {
                            while( resultSet.next() )
                            {
                                out.printf( "Schema: %s – Name: %s%n", resultSet.getString( 1 ), resultSet.getString( 2 ) );
                            }
                        }
                    }
                }

                if( !databaseIsInitialized ) m_Configuration.initializeDatabase( connection );
            }

            //---* Check the name and birthdate *------------------------------
            if( isNull( m_Configuration.getName() )  ) throw new ApplicationError( m_Configuration.getMessage( MSG_NoName ) );
            if( isNull( m_Configuration.getBirthdate() ) ) throw new ApplicationError( m_Configuration.getMessage( MSG_NoBirthdate ) );

            //---* Save the settings *-----------------------------------------
            m_Configuration.updateINIFile();
        }

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  initialize()

    /**
     *  The program entry point.
     *
     *  @param  args    The command line arguments.
     */
    public static final void main( final String... args )
    {
        try
        {
            final var application = new Application();
            final var fakeArgs = new String []
                {
                    "--import", "data/export_2022-03-13T1657.csv",
                    "--dump", "data/dump.sql",
                    "--report", "data/compare.html",
                    "--reportStyle", "compare",
                    "--reportFormat", "HTML",
                    "--reportStart", "2021-10-01",
                    "--name", "Thrien, Thomas",
                    "--birthdate", "1963-06-26"
                };
            if( application.initialize( fakeArgs ) )
            {
                application.execute();
            }
        }
        catch( final ApplicationError e )
        {
            if( isDebug() || isTest() )
            {
                e.printStackTrace( err );
            }
            else
            {
                err.printf( "Error: %s", e.getLocalizedMessage() );
                final var cause = e.getCause();
                if( nonNull( cause ) ) err.printf( "Reason: %s", cause.getLocalizedMessage() );
            }
        }
        catch( final Throwable t )
        {
            t.printStackTrace( err );
        }
    }   //  main()
}
//  class Application

/*
 *  End of File
 */