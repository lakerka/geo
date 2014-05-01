package handlers;

import java.awt.Color;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.main.GeometryContainer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class StyleHandler {

    private static final Color LINE_COLOUR = Color.BLACK;
    private static final Color FILL_COLOUR = Color.WHITE;
    private static final Color SELECTED_FILL_COLOUR = Color.BLUE;
    private static final float OPACITY = 0.2f;
    private static final float LINE_WIDTH = 1.0f;
    private static final Color SELECTED_LINE_COLOUR = Color.RED;
    private static final float SELECTED_LINE_WIDTH = 1.9f;
    private static final float POINT_SIZE = 10.0f;

    /**
     * Create a default Style for feature display
     */
    public static Style createDefaultStyle(GeometryContainer geometryContainer) {
        
        
        Style style = null;
        
        try {
            
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

            Rule rule = createRule(LINE_COLOUR, FILL_COLOUR, LINE_WIDTH,
                    geometryContainer);
            
            FeatureTypeStyle featureTypeStyle = styleFactory
                    .createFeatureTypeStyle();
            featureTypeStyle.rules().add(rule);
            
            style = styleFactory.createStyle();
            style.featureTypeStyles().add(featureTypeStyle);
           
            return style;
            
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a Style where features with given IDs are painted with selected
     * color, while others are painted with the default colors.
     */
    public static Style createSelectedStyle(Set<FeatureId> IDs,
            GeometryContainer geometryContainer) {

        FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2();
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

        Rule selectedRule = createRule(SELECTED_LINE_COLOUR,
                SELECTED_FILL_COLOUR, SELECTED_LINE_WIDTH, geometryContainer);

        selectedRule.setFilter(filterFactory2.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR, LINE_WIDTH,
                geometryContainer);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing a
     * Symbolizer tailored to the geometry type of the features that we are
     * displaying.
     */
    private static Rule createRule(Color outlineColor, Color fillColor,
            float lineWidth, GeometryContainer geometryContainer) {

        FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2();
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();

        Symbolizer symbolizer = null;
        Fill fill = null;

        // define width of line
        Stroke stroke = styleFactory.createStroke(
                filterFactory2.literal(outlineColor),
                filterFactory2.literal(lineWidth));

        String geometryDescriptorLocalName = geometryContainer
                .getGeometryDescriptor().getLocalName();

        switch (geometryContainer.getGeometryType()) {

        case POLYGON:
            // set polygon fill opacity
            fill = styleFactory.createFill(filterFactory2.literal(fillColor),
                    filterFactory2.literal(OPACITY));
            // set polygon border
            symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill,
                    geometryDescriptorLocalName);
            break;

        case LINE:
            // set line style
            symbolizer = styleFactory.createLineSymbolizer(stroke,
                    geometryDescriptorLocalName);
            break;

        case POINT:
            // set point opacity
            fill = styleFactory.createFill(filterFactory2.literal(fillColor),
                    filterFactory2.literal(OPACITY));

            Mark mark = styleFactory.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);

            Graphic graphic = styleFactory.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(filterFactory2.literal(POINT_SIZE));

            symbolizer = styleFactory.createPointSymbolizer(graphic,
                    geometryDescriptorLocalName);
        }

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(symbolizer);

        return rule;
    }
}
