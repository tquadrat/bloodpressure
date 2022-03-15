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

/**
 *  The Blood Pressure Statistics Application.
 */

module org.tquadrat.bloodpressure
{
    requires java.prefs;
    requires java.sql;
    requires java.desktop;

    //---* The Foundation stuff *----------------------------------------------
    requires org.tquadrat.foundation.base;
    requires org.tquadrat.foundation.util;
    requires org.tquadrat.foundation.i18n;
    requires org.tquadrat.foundation.inifile;
    requires org.tquadrat.foundation.config;
    requires org.tquadrat.foundation.fx;
    requires org.tquadrat.foundation.value;

    //---* The JavaFX stuff *--------------------------------------------------
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.media;

    opens org.tquadrat.bloodpressure to javafx.fxml, javafx.graphics;
    opens org.tquadrat.bloodpressure.internal to javafx.fxml, javafx.graphics;
    opens org.tquadrat.bloodpressure.spi to javafx.fxml, javafx.graphics;

    //---* The XChart stuff *--------------------------------------------------
    requires org.knowm.xchart;
}   //  module

/*
 *  End of File
 */