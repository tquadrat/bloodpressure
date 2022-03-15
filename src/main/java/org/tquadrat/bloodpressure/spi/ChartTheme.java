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

package org.tquadrat.bloodpressure.spi;

import static org.apiguardian.api.API.Status.STABLE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.apiguardian.api.API;
import org.knowm.xchart.style.PieStyler.LabelType;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.Styler.ToolTipType;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.theme.Theme;
import org.knowm.xchart.style.theme.XChartTheme;
import org.tquadrat.foundation.annotation.ClassVersion;

/* Inner Classes */
/**
 *  An implementation of
 *  {@link org.knowm.xchart.style.theme.Theme}
 *  for the blood pressure charts.
 *
 *  @version $Id: ChartTheme.java 151 2022-03-15 20:39:31Z tquadrat $
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @UMLGraph.link
 *  @since 0.1.0
 */
@ClassVersion( sourceVersion = "$Id: ChartTheme.java 151 2022-03-15 20:39:31Z tquadrat $" )
@API( status = STABLE, since = "0.1.0" )
public final class ChartTheme implements Theme
{
        /*------------*\
    ====** Attributes **=======================================================
        \*------------*/
    /**
     *  The base theme.
     */
    private final XChartTheme m_BaseTheme;

        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  Creates a new instance of {@code CharTheme}.
     */
    public ChartTheme()
    {
        m_BaseTheme = new XChartTheme();
    }   //  ChartTheme()

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAnnotationLineColor() { return m_BaseTheme.getAnnotationLineColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final BasicStroke getAnnotationLineStroke() { return m_BaseTheme.getAnnotationLineStroke(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getAnnotationTextFont() { return m_BaseTheme.getAnnotationTextFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAnnotationTextFontColor() { return m_BaseTheme.getAnnotationTextFontColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAnnotationTextPanelBackgroundColor() { return m_BaseTheme.getAnnotationTextPanelBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAnnotationTextPanelBorderColor() { return m_BaseTheme.getAnnotationTextPanelBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getAnnotationTextPanelFont() { return m_BaseTheme.getAnnotationTextPanelFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAnnotationTextPanelFontColor() { return m_BaseTheme.getAnnotationTextPanelFontColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getAnnotationTextPanelPadding() { return m_BaseTheme.getAnnotationTextPanelPadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final double getAvailableSpaceFill() { return m_BaseTheme.getAvailableSpaceFill(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAxisTickLabelsColor() { return m_BaseTheme.getAxisTickLabelsColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getAxisTickLabelsFont() { return m_BaseTheme.getAxisTickLabelsFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getAxisTickMarkLength() { return m_BaseTheme.getAxisTickMarkLength(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getAxisTickMarksColor() { return m_BaseTheme.getAxisTickMarksColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final BasicStroke getAxisTickMarksStroke() { return m_BaseTheme.getAxisTickMarksStroke(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getAxisTickPadding() { return m_BaseTheme.getAxisTickPadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getAxisTitleFont() { return m_BaseTheme.getAxisTitleFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getAxisTitlePadding() { return m_BaseTheme.getAxisTitlePadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getBaseFont() { return m_BaseTheme.getBaseFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartBackgroundColor() { return m_BaseTheme.getChartBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartButtonBackgroundColor() { return m_BaseTheme.getChartButtonBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartButtonBorderColor() { return m_BaseTheme.getChartButtonBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getChartButtonFont() { return m_BaseTheme.getChartButtonFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartButtonFontColor() { return m_BaseTheme.getChartButtonFontColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartButtonHoverColor() { return m_BaseTheme.getChartButtonHoverColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getChartButtonMargin() { return m_BaseTheme.getChartButtonMargin(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartFontColor() { return m_BaseTheme.getChartFontColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getChartPadding() { return m_BaseTheme.getChartPadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartTitleBoxBackgroundColor() { return m_BaseTheme.getChartTitleBoxBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getChartTitleBoxBorderColor() { return m_BaseTheme.getChartTitleBoxBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getChartTitleFont() { return m_BaseTheme.getChartTitleFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getChartTitlePadding() { return m_BaseTheme.getChartTitlePadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getCursorBackgroundColor() { return m_BaseTheme.getCursorBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getCursorColor() { return m_BaseTheme.getCursorColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getCursorFont() { return m_BaseTheme.getCursorFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getCursorFontColor() { return m_BaseTheme.getCursorFontColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final float getCursorSize() { return m_BaseTheme.getCursorSize(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final double getDonutThickness() { return m_BaseTheme.getDonutThickness(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getErrorBarsColor() { return m_BaseTheme.getErrorBarsColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final LabelType getLabelType() { return m_BaseTheme.getLabelType(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final double getLabelsDistance() { return m_BaseTheme.getLabelsDistance(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getLabelsFontColorAutomaticDark() { return m_BaseTheme.getLabelsFontColorAutomaticDark(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getLabelsFontColorAutomaticLight() { return m_BaseTheme.getLabelsFontColorAutomaticLight(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getLegendBackgroundColor() { return m_BaseTheme.getLegendBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getLegendBorderColor() { return m_BaseTheme.getLegendBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getLegendFont() { return m_BaseTheme.getLegendFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getLegendPadding() { return m_BaseTheme.getLegendPadding(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final LegendPosition getLegendPosition() { return m_BaseTheme.getLegendPosition(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getLegendSeriesLineLength() { return m_BaseTheme.getLegendSeriesLineLength(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getMarkerSize() { return 0; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getPieFont() { return m_BaseTheme.getPieFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getPlotBackgroundColor() { return m_BaseTheme.getPlotBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getPlotBorderColor() { return m_BaseTheme.getPlotBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final double getPlotContentSize() { return m_BaseTheme.getPlotContentSize(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getPlotGridLinesColor() { return m_BaseTheme.getPlotGridLinesColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final BasicStroke getPlotGridLinesStroke() { return m_BaseTheme.getPlotGridLinesStroke(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getPlotMargin() { return m_BaseTheme.getPlotMargin(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color[] getSeriesColors() { return m_BaseTheme.getSeriesColors(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final BasicStroke[] getSeriesLines() { return m_BaseTheme.getSeriesLines(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Marker[] getSeriesMarkers() { return m_BaseTheme.getSeriesMarkers(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final double getStartAngleInDegrees() { return m_BaseTheme.getStartAngleInDegrees(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getSumFont() { return m_BaseTheme.getSumFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getToolTipBackgroundColor() { return m_BaseTheme.getToolTipBackgroundColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getToolTipBorderColor() { return m_BaseTheme.getToolTipBorderColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Font getToolTipFont() { return m_BaseTheme.getToolTipFont(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final Color getToolTipHighlightColor() { return m_BaseTheme.getToolTipHighlightColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final ToolTipType getToolTipType() { return m_BaseTheme.getToolTipType(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getXAxisTickMarkSpacingHint() { return m_BaseTheme.getXAxisTickMarkSpacingHint(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final int getYAxisTickMarkSpacingHint() { return m_BaseTheme.getYAxisTickMarkSpacingHint(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isAxisTicksLineVisible() { return m_BaseTheme.isAxisTicksLineVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isAxisTicksMarksVisible() { return m_BaseTheme.isAxisTicksMarksVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isChartTitleBoxVisible() { return m_BaseTheme.isChartTitleBoxVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isChartTitleVisible() { return m_BaseTheme.isChartTitleVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isCircular() { return m_BaseTheme.isCircular(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isCursorEnabled() { return m_BaseTheme.isCursorEnabled(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isErrorBarsColorSeriesColor() { return m_BaseTheme.isErrorBarsColorSeriesColor(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isLabelsFontColorAutomaticEnabled() { return m_BaseTheme.isLabelsFontColorAutomaticEnabled(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isLegendVisible() { return false; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isOverlapped() { return m_BaseTheme.isOverlapped(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isPlotBorderVisible() { return m_BaseTheme.isPlotBorderVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isPlotGridHorizontalLinesVisible() { return m_BaseTheme.isPlotGridHorizontalLinesVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isPlotGridLinesVisible() { return m_BaseTheme.isPlotGridLinesVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isPlotGridVerticalLinesVisible() { return m_BaseTheme.isPlotGridVerticalLinesVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isPlotTicksMarksVisible() { return m_BaseTheme.isPlotTicksMarksVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isSumVisible() { return m_BaseTheme.isSumVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isToolTipsEnabled() { return m_BaseTheme.isToolTipsEnabled(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isXAxisTicksVisible() { return m_BaseTheme.isXAxisTicksVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isXAxisTitleVisible() { return m_BaseTheme.isXAxisTitleVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isYAxisTicksVisible() { return m_BaseTheme.isYAxisTicksVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isYAxisTitleVisible() { return m_BaseTheme.isYAxisTitleVisible(); }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean isZoomEnabled() { return true; }

    /**
     *  {@inheritDoc}
     */
    @Override
    public final boolean setForceAllLabelsVisible() { return m_BaseTheme.setForceAllLabelsVisible(); }
}
//  class ChartTheme

/*
 *  End of File
 */