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
import static org.tquadrat.foundation.config.ConfigUtil.getConfiguration;
import static org.tquadrat.foundation.config.SpecialPropertyType.CONFIG_PROPERTY_CLOCK;
import static org.tquadrat.foundation.i18n.TextUse.USAGE;
import static org.tquadrat.foundation.lang.CommonConstants.PROPERTY_USER_NAME;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;
import org.tquadrat.bloodpressure.internal.ConfigurationBase;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.config.CLIBeanSpec;
import org.tquadrat.foundation.config.ConfigBeanSpec;
import org.tquadrat.foundation.config.ConfigurationBeanSpecification;
import org.tquadrat.foundation.config.ExemptFromToString;
import org.tquadrat.foundation.config.I18nSupport;
import org.tquadrat.foundation.config.INIBeanSpec;
import org.tquadrat.foundation.config.INIFileConfig;
import org.tquadrat.foundation.config.INIValue;
import org.tquadrat.foundation.config.Option;
import org.tquadrat.foundation.config.SpecialProperty;
import org.tquadrat.foundation.config.SystemProperty;
import org.tquadrat.foundation.i18n.BaseBundleName;
import org.tquadrat.foundation.i18n.MessagePrefix;
import org.tquadrat.foundation.i18n.Text;
import org.tquadrat.foundation.i18n.Translation;

/**
 *  The configuration bean specification for the Blood Pressure Statistics
 *  application.
 *
 *  @version $Id: Configuration.java 151 2022-03-15 20:39:31Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.0.1
 */
@SuppressWarnings( "ClassWithTooManyMethods" )
@ConfigurationBeanSpecification( baseClass = ConfigurationBase.class, synchronizeAccess = false )
@INIFileConfig( comment = "Settings for the Blood Pressure Statistics application", mustExist = false, path = "${dataFolder}bloodpressure.ini" )
@ClassVersion( sourceVersion = "$Id: Configuration.java 151 2022-03-15 20:39:31Z tquadrat $" )
@API( status = STABLE, since = "0.0.1" )
public interface Configuration extends CLIBeanSpec, ConfigBeanSpec, I18nSupport, INIBeanSpec
{
        /*-----------*\
    ====** Constants **========================================================
        \*-----------*/
    /**
     *  The name of the resource bundle for the texts and messages: {@value}.
     */
    @SuppressWarnings( "unused" )
    @BaseBundleName( defaultLanguage = "de" )
    public static final String BASE_BUNDLE_NAME = "org.tquadrat.bloodpressure.TextsAndMessages";

    /**
     *  The name for the key column of the blood pressure data table: {@value}.
     *
     *  @see #TABLE_BLOODPRESSURE_DATA
     */
    public static final String COLUMN_BLOODPRESSURE_DATA_PRIMARY_KEY = "MEASURING_DATETIME";

    /**
     *  The message prefix for messages from this application: {@value}.
     */
    @SuppressWarnings( "unused" )
    @MessagePrefix
    public static final String MESSAGE_PREFIX = "BPS";

    /**
     *  The table name for the blood pressure data: {@value}. The schema is the
     *  default schema.
     */
    public static final String TABLE_BLOODPRESSURE_DATA = "BLOODPRESSURE_DATA";

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Returns the birthdate of the person whose blood pressure data is
     *  processed.
     *
     *  @return The birthdate.
     */
    @Text(
        description = "The usage text for the --birthdate option",
        use = USAGE,
        id = "Birthdate",
        translations =
            {
                @Translation( language = "de", text = "Das Geburtsdatum der Person, deren Blutdruckdaten verarbeitet werden" ),
                @Translation( language = "en", text = "The birthdate of the person whose blood pressure data is processed" )
            }
    )
    @Option( name = "--birthdate", aliases = { "--birthday"}, metaVar = "DATA", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_Birthdate" )
    @INIValue( group = "Owner", key = "birthdate", comment = "The birthdate of the person whose blood pressure data is processed" )
    public LocalDate getBirthdate();

    /**
     *  <p>{@summary Returns the clock that is used by this program.}</p>
     *  <p>It can be adjusted for testing purposes.</p>
     *
     *  @return The clock.
     */
    @SpecialProperty( CONFIG_PROPERTY_CLOCK )
    public Clock getClock();

    /**
     *  Returns the name of the database.
     *
     *  @return The name of the database that is used by this application.
     */
    public String getDatabaseName();

    /**
     *  Returns the password for the access to the database.
     *
     *  @return The database password.
     */
    @ExemptFromToString
    public String getDatabasePassword();

    /**
     *  Returns the folder that holds the data for the program.
     *
     *  @return The data folder.
     */
    public Path getDataFolder();

    /**
     *  Returns the destination file for a dump from the database. An existing
     *  file will be overwritten.
     *
     *  @return An instance of
     *      {@link Optional}
     *      that holds the dump file.
     */
    @Text(
        description = "The usage text for the --dump option",
        use = USAGE,
        id = "Dump",
        translations =
        {
            @Translation( language = "de", text = "Ausgabedatei für einen Datenbank-Auszug; eine bereits bestehende Datei wird überschrieben" ),
            @Translation( language = "en", text = "Output file for a database dump; an already existing file will be overwritten" )
        }
    )
    @Option( name = "--dump", metaVar = "FILE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_Dump" )
    public Optional<File> getDumpFile();

    /**
     *  Returns the end date for a report.
     *
     *  @return The end date.
     */
    @Text(
        description = "The usage text for the --reportEnd option",
        use = USAGE,
        id = "ReportEnd",
        translations =
            {
                @Translation( language = "de", text = "Das Ende-Datum für die Report-Periode" ),
                @Translation( language = "en", text = "The end date for the report period" )
            }
    )
    @Option( name = "--reportEnd", metaVar = "DATE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ReportEnd" )
    public LocalDate getEndDate();

    /**
     *  Returns the file to import.
     *
     *  @return An instance of
     *      {@link Optional}
     *      that holds the file to import.
     */
    @Text(
        description = "The usage text for the --import option",
        use = USAGE,
        id = "Import",
        translations =
        {
            @Translation( language = "de", text = "Datei mit Blutdruckdaten für den Import" ),
            @Translation( language = "en", text = "File with Blood Pressure Data to be imported" )
        }
    )
    @Option( name = "--import", metaVar = "FILE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_Import" )
    public Optional<File> getImportFile();

    /**
     *  Returns the encoding for the file to import.
     *
     *  @return An instance of
     *      {@link Optional}
     *      that holds the import encoding.
     */
    @Text(
        description = "The usage text for the --importEncoding option",
        use = USAGE,
        id = "ImportEncoding",
        translations =
        {
            @Translation( language = "de", text = "Das Encoding der Importdatei" ),
            @Translation( language = "en", text = "The encoding of the import file" )
        }
    )
    @Option( name = "--importEncoding", metaVar = "CHARSET", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ImportEncoding" )
    public Optional<Charset> getImportCharset();

    /**
     *  Returns the format for the file to import.
     *
     *  @return The import format.
     */
    @Text(
        description = "The usage text for the --importFormat option",
        use = USAGE,
        id = "ImportFormat",
        translations =
        {
            @Translation( language = "de", text = "Das Format der Importdatei" ),
            @Translation( language = "en", text = "The format of the import file" )
        }
    )
    @Option( name = "--importFormat", metaVar = "FORMAT", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ImportFormat" )
    public InputFormat getImportFormat();

    /**
     *  Returns the source of the file to import.
     *
     *  @return The import source.
     */
    @Text(
        description = "The usage text for the --importSource option",
        use = USAGE,
        id = "ImportSource",
        translations =
        {
            @Translation( language = "de", text = "Die Herkunft der Importdatei" ),
            @Translation( language = "en", text = "The origin of the import file" )
        }
    )

    @Option( name = "--importSource", metaVar = "ORIGIN", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ImportSource" )
    public String getImportSource();

    /**
     *  Returns the time zone for the record timestamps in the file to import.
     *
     *  @return The import time zone.
     */
    @Text(
        description = "The usage text for the --importTimeZone option",
        use = USAGE,
        id = "ImportTimeZone",
        translations =
        {
            @Translation( language = "de", text = "Die Zeitzone für die Zeitstempel in der Importdatei; wenn nicht angegeben, wird die aktuelle Zeitzone benutzt" ),
            @Translation( language = "en", text = "The time zone for the record timestamps in the import file; if not set, the current time zone is used" )
        }
    )
    @Option( name = "--importTimeZone", metaVar = "TIMEZONE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ImportTimeZone" )
    public Optional<ZoneId> getImportTimeZone();

    /**
     *  Returns the instance of the configuration bean.
     *
     *  @return The configuration bean instance.
     */
    public static Configuration getInstance()
    {
        final var retValue = getConfiguration( Configuration.class, c -> c.getConstructor().newInstance() );

        //---* Done *----------------------------------------------------------
        return retValue;
    }   //  getInstance()

    /**
     *  Returns the name of the person whose blood pressure data is processed.
     *
     *  @return The name.
     */
    @Text(
        description = "The usage text for the --name option",
        use = USAGE,
        id = "Name",
        translations =
            {
                @Translation( language = "de", text = "Der Name der Person, deren Blutdruckdaten verarbeitet werden" ),
                @Translation( language = "en", text = "The name of the person whose blood pressure data is processed" )
            }
    )
    @Option( name = "--name", metaVar = "NAME", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_Name" )
    @INIValue( group = "Owner", key = "name", comment = "The name of the person whose blood pressure data is processed" )
    public String getName();

    /**
     *  Returns the destination for a report. Depending on the selected
     *  {@link ReportFormat},
     *  this is either a file or a folder. Any existing files will be
     *  overwritten.
     *
     *  @return An instance of
     *      {@link Optional}
     *      that holds the destination file or folder.
     *
     *  @see    #getReportFormat()
     */
    @Text(
        description = "The usage text for the --report option",
        use = USAGE,
        id = "Report",
        translations =
        {
            @Translation( language = "de", text = "Ziel für den Report, entweder eine Datei oder ein Ordner; existierende Dateien werden überschrieben" ),
            @Translation( language = "en", text = "The destination for a report, either a file or a folder; any already existing files will be overwritten" )
        }
    )
    @Option( name = "--report", metaVar = "FILE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_Report" )
    public Optional<File> getReportFile();

    /**
     *  Returns the report format.
     *
     *  @return The report format.
     */
    @Text(
        description = "The usage text for the --reportFormat option",
        use = USAGE,
        id = "ReportFormat",
        translations =
        {
            @Translation( language = "de", text = "Das Ausgabeformat des Reports" ),
            @Translation( language = "en", text = "The report output format" )
        }
    )
    @Option( name = "--reportFormat", metaVar = "FORMAT", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ReportFormat" )
    public ReportFormat getReportFormat();

    /**
     *  Returns the report style.
     *
     *  @return The report style.
     */
    @Text(
        description = "The usage text for the --reportStyle option",
        use = USAGE,
        id = "ReportStyle",
        translations =
        {
            @Translation( language = "de", text = "Die Art des Reports" ),
            @Translation( language = "en", text = "The report style" )
        }
    )
    @Option( name = "--reportStyle", metaVar = "STYLE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ReportStyle" )
    public String getReportStyle();

    /**
     *  Returns the start date for a report.
     *
     *  @return The start date.
     */
    @Text(
        description = "The usage text for the --reportStart option",
        use = USAGE,
        id = "ReportStart",
        translations =
            {
                @Translation( language = "de", text = "Das Start-Datum für die Report-Periode" ),
                @Translation( language = "en", text = "The start date for the report period" )
            }
    )
    @Option( name = "--reportStart", metaVar = "DATE", usageKey = "org.tquadrat.bloodpressure.Configuration.USAGE_ReportStart" )
    public LocalDate getStartDate();

    /**
     *  Returns the name of the current user.
     *
     *  @return The current user's name.
     */
    @SystemProperty( value = PROPERTY_USER_NAME )
    public String getUsername();

    /**
     *  Initialises the configuration bean.
     *
     *  @return The initialisation data.
     */
    public Map<String,Object> initData();

    /**
     *  In case a new database was created, a call to this method will
     *  initialise it.
     *
     *  @param  connection  The database connection.
     *  @throws SQLException    The initialisation of the database failed.
     */
    public void initializeDatabase( Connection connection ) throws SQLException;

    /**
     *  Retrieves the connection to the database.
     *
     *  @return The connection to the database.
     *  @throws SQLException    There is a problem with retrieving the database
     *      connection.
     */
    public Connection retrieveConnection() throws SQLException;

    /**
     *  <p>{@summary Sets the clock that should be used for this program.}</p>
     *  <p>This is used mainly for testing purposes.</p>
     *
     *  @param  clock   The clock.
     */
    @SuppressWarnings( "unused" )
    public void setClock( final Clock clock );
}
//  interface Configuration

/*
 *  End of File
 */