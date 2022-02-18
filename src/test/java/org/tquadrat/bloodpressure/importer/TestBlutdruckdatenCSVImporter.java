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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tquadrat.bloodpressure.DataImporter.retrieveDataImporter;
import static org.tquadrat.bloodpressure.InputFormat.CSV;
import static org.tquadrat.bloodpressure.spi.DataImporterBase.registerImporter;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tquadrat.bloodpressure.DataImporter;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.testutil.TestBaseClass;

/**
 *  Some tests for the class
 *  {@link BlutdruckdatenCSVImporter}.
 *
 *  @author Thomas Thrien - thomas.thrien@tquadrat.org
 *  @since 0.0.1
 */
@ClassVersion( sourceVersion = "$Id: TestBlutdruckdatenCSVImporter.java 120 2022-02-10 18:58:05Z tquadrat $" )
@DisplayName( "org.tquadrat.bloodpressure.importer.TestBlutdruckdatenCSVImporter" )
public class TestBlutdruckdatenCSVImporter extends TestBaseClass
{
        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  Initialises the test.
     */
    @BeforeAll
    static final void init()
    {
        final var candidate = new BlutdruckdatenCSVImporter();
        assertNotNull( candidate );
        registerImporter( candidate );
        final var importer = retrieveDataImporter( "blutdruckdaten.de", CSV );
        assertNotNull( importer );
        assertTrue( importer.isPresent() );
        assertSame( candidate, importer.get() );
    }   //  init()

    /**
     *  Some tests for the method
     *  {@link DataImporter#parse(URI, ZoneId, Charset)}
     *  as implemented by
     *  {@link BlutdruckdatenCSVImporter}.
     *
     *  @throws Exception   Something went unexpectedly wrong.
     */
    @Test
    final void testParse() throws Exception
    {
        skipThreadTest();

        final var resource = getClass().getResource( "/test.csv" );
        assertNotNull( resource );
    }   //  testParse()
}
//  class TestBlutdruckdatenCSVImporter

/*
 *  End of File
 */